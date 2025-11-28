package com.milsabores.api.controller;

import com.milsabores.api.service.ProductosService;
import com.milsabores.api.service.ProductosService.PagedProducts;
import com.milsabores.api.service.ProductosService.Product;
import com.milsabores.api.service.ProductosService.Category;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping
@Tag(name = "Products", description = "Catálogo público de productos")
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

    /* ========== Endpoints PÚBLICOS ========== */

    @Operation(
            summary = "Listar productos",
            description = "Devuelve una página de productos con búsqueda, filtro por categoría y ordenación."
    )
    @ApiResponse(responseCode = "200", description = "Página de productos devuelta correctamente")
    @ApiResponse(responseCode = "400", description = "Parámetros inválidos")
    @ApiResponse(responseCode = "500", description = "Error interno")
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

    @Operation(
            summary = "Detalle de producto",
            description = "Obtiene el detalle de un producto por su ID (ej. TC001)."
    )
    @ApiResponse(responseCode = "200", description = "Producto encontrado")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    @ApiResponse(responseCode = "500", description = "Error interno")
    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProduct(
            @Parameter(description = "ID del producto (ej. TC001)", example = "TC001")
            @PathVariable("id") String id
    ) {
        Product p = service.getProduct(id);
        if (p == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorBody("Producto no encontrado"));
        }
        return ResponseEntity.ok(p);
    }

    @Operation(
            summary = "Listar categorías",
            description = "Devuelve el listado de categorías disponibles."
    )
    @ApiResponse(responseCode = "200", description = "Listado de categorías devuelto correctamente")
    @ApiResponse(responseCode = "500", description = "Error interno")
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> listCategories() {
        return ResponseEntity.ok(service.listCategories());
    }

    @Operation(
            summary = "Productos destacados",
            description = "Devuelve los productos marcados como destacados, ya ordenados por posición."
    )
    @ApiResponse(responseCode = "200", description = "Listado de productos destacados devuelto correctamente")
    @ApiResponse(responseCode = "500", description = "Error interno")
    @GetMapping("/featured")
    public ResponseEntity<List<Product>> listFeatured() {
        return ResponseEntity.ok(service.listFeatured());
    }

    @Operation(
            summary = "Debug de conexión a BD",
            description = "Endpoint de depuración para verificar conexión a la base de datos."
    )
    @ApiResponse(responseCode = "200", description = "Información de debug devuelta correctamente")
    @GetMapping("/_debug/db")
    public ResponseEntity<java.util.Map<String, Object>> debugDb() {
        var map = service.debug();
        return ResponseEntity.ok(map);
    }

    /* ========== Endpoints ADMIN (crear / editar / borrar productos) ========== */

    @Operation(
            summary = "Crear producto (ADMIN)",
            description = "Crea un nuevo producto en el catálogo. Requiere rol ADMIN."
    )
    @ApiResponse(responseCode = "201", description = "Producto creado correctamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "500", description = "Error interno")
    @PostMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> createProduct(@RequestBody Product input) {
        try {
            Product created = service.createProduct(input);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al crear producto");
        }
    }

    @Operation(
            summary = "Actualizar producto (ADMIN)",
            description = "Actualiza un producto existente. Requiere rol ADMIN."
    )
    @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    @ApiResponse(responseCode = "500", description = "Error interno")
    @PutMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(
            @PathVariable("id") String id,
            @RequestBody Product input
    ) {
        try {
            Product updated = service.updateProduct(id, input);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Error de validación";
            if (msg.toLowerCase().contains("no encontrado")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al actualizar producto");
        }
    }

    @Operation(
            summary = "Eliminar producto (ADMIN)",
            description = "Elimina un producto por ID. Requiere rol ADMIN."
    )
    @ApiResponse(responseCode = "204", description = "Producto eliminado correctamente")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    @ApiResponse(responseCode = "500", description = "Error interno")
    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") String id) {
        try {
            service.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Error de validación";
            if (msg.toLowerCase().contains("no encontrado")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al eliminar producto");
        }
    }

    /* ======== ErrorBody simple para 404/errores puntuales ======== */
    public static class ErrorBody {
        public String message;

        public ErrorBody() {
        }

        public ErrorBody(String message) {
            this.message = message;
        }
    }
}
