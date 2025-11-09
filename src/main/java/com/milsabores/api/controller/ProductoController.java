package com.milsabores.api.controller;

import com.milsabores.api.service.ProductosService;
import com.milsabores.api.service.ProductosService.PagedProducts;
import com.milsabores.api.service.ProductosService.Product;
import com.milsabores.api.service.ProductosService.Category;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class ProductoController {

    private final ProductosService service;

    public ProductoController(ProductosService service) {
        this.service = service;
    }

    /* ========== Helpers de validación ========== */
    private static final Set<String> SORT_WHITELIST = Set.of(
            "price", "-price", "name", "-name", "createdAt", "-createdAt"
    );

    private void validateListParams(Integer page, Integer size, Integer categoryId, String sort) {
        if (page != null && page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page debe ser >= 0");
        }
        if (size != null && (size < 1 || size > 100)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size debe estar en el rango [1..100]");
        }
        if (categoryId != null && categoryId < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "categoryId debe ser >= 1");
        }
        if (sort != null && !sort.isBlank() && !SORT_WHITELIST.contains(sort)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "sort inválido. Permitidos: price,-price,name,-name,createdAt,-createdAt");
        }
    }

    /* ========== Endpoints ========== */

    @Tag(name = "Products")
    @Operation(
            summary = "Listar productos",
            description = "Paginación, búsqueda (q en nombre/descripcion), filtro por categoría y orden permitido.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Página de productos",
                            content = @Content(schema = @Schema(implementation = PagedProducts.class))),
                    @ApiResponse(responseCode = "400", description = "Parámetros inválidos",
                            content = @Content(schema = @Schema(example = "{\"message\":\"size debe estar en el rango [1..100]\"}"))),
                    @ApiResponse(responseCode = "500", description = "Error interno",
                            content = @Content(schema = @Schema(example = "{\"message\":\"Error interno\"}")))
            }
    )
    @GetMapping("/products")
    public ResponseEntity<PagedProducts> listProducts(
            @Parameter(description = "Página (0-index)", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página [1..100]", example = "12")
            @RequestParam(name = "size", defaultValue = "12") int size,
            @Parameter(description = "Texto a buscar en nombre/descripcion", example = "chocolate")
            @RequestParam(name = "q", required = false) String q,
            @Parameter(description = "ID de categoría (>=1)", example = "1")
            @RequestParam(name = "categoryId", required = false) Integer categoryId,
            @Parameter(description = "Orden permitido: price,-price,name,-name,createdAt,-createdAt", example = "-price")
            @RequestParam(name = "sort", required = false) String sort
    ) {
        validateListParams(page, size, categoryId, sort);
        PagedProducts result = service.listProducts(q, categoryId, sort, page, size);
        return ResponseEntity.ok(result);
    }

    @Tag(name = "Products")
    @Operation(
            summary = "Detalle de producto",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Producto encontrado",
                            content = @Content(schema = @Schema(implementation = Product.class))),
                    @ApiResponse(responseCode = "404", description = "No encontrado",
                            content = @Content(schema = @Schema(example = "{\"message\":\"Producto no encontrado\"}"))),
                    @ApiResponse(responseCode = "500", description = "Error interno",
                            content = @Content(schema = @Schema(example = "{\"message\":\"Error interno\"}")))
            }
    )
    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProduct(
            @Parameter(description = "ID del producto (ej. TC001)", example = "TC001")
            @PathVariable("id") String id
    ) {
        var p = service.getProduct(id);
        if (p == null) return ResponseEntity.status(404).body(new ErrorBody("Producto no encontrado"));
        return ResponseEntity.ok(p);
    }

    @Tag(name = "Categories")
    @Operation(
            summary = "Listar categorías",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = Category.class))),
                    @ApiResponse(responseCode = "500", description = "Error interno",
                            content = @Content(schema = @Schema(example = "{\"message\":\"Error interno\"}")))
            }
    )
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> listCategories() {
        return ResponseEntity.ok(service.listCategories());
    }

    @Tag(name = "Featured")
    @Operation(
            summary = "Productos destacados",
            description = "Devuelve productos ya ordenados por posición.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = Product.class))),
                    @ApiResponse(responseCode = "500", description = "Error interno",
                            content = @Content(schema = @Schema(example = "{\"message\":\"Error interno\"}")))
            }
    )
    @GetMapping("/featured")
    public ResponseEntity<List<Product>> listFeatured() {
        return ResponseEntity.ok(service.listFeatured());
    }

    /* ======== ErrorBody simple para 404/errores puntuales ======== */
    public static class ErrorBody {
        public String message;
        public ErrorBody() {}
        public ErrorBody(String message) { this.message = message; }
    }

    @GetMapping("/_debug/db")
    public ResponseEntity<java.util.Map<String,Object>> debugDb() {
        var map = service.debug(); // ya lo tenías en el service
        return ResponseEntity.ok(map);
    }

}
