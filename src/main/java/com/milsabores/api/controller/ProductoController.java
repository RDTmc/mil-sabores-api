package com.milsabores.api.controller;

import com.milsabores.api.service.ProductosService;
import com.milsabores.api.service.ProductosService.PagedProducts;
import com.milsabores.api.service.ProductosService.Product;
import com.milsabores.api.service.ProductosService.Category;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

/**
 * Controlador REST de catálogo (solo lectura por ahora).
 * Rutas:
 *  GET /api/products
 *  GET /api/products/{id}
 *  GET /api/categories
 *  GET /api/featured
 */
@RestController
@RequestMapping("/api")
public class ProductoController {

    private final ProductosService service;
    public ProductoController(ProductosService service){ this.service = service; }

    @GetMapping("/products")
    public ResponseEntity<PagedProducts> listProducts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "categoryId", required = false) Integer categoryId,
            @RequestParam(name = "sort", required = false) String sort
    ){
        PagedProducts result = service.listProducts(q, categoryId, sort, page, size);
        return ResponseEntity.ok(result);

    }
    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProduct(@PathVariable("id") String id) {
        var p = service.getProduct(id);
        if (p == null) return ResponseEntity.status(404).body(new ErrorBody("Producto no encontrado"));
        return ResponseEntity.ok(p);
    }
    @GetMapping("/_debug/db")
    public ResponseEntity<java.util.Map<String,Object>> debugDb() {
        return ResponseEntity.ok(service.debug());
    }
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> listCategories() {
        return ResponseEntity.ok(service.listCategories());
    }

    @GetMapping("/featured")
    public ResponseEntity<List<Product>> listFeatured() {
        return ResponseEntity.ok(service.listFeatured());
    }

    /* ======== ErrorBody simple para 404/errores genéricos en esta iteración ======== */
    public static class ErrorBody {
        public String message;
        public ErrorBody() { }
        public ErrorBody(String message) { this.message = message; }
    }
}
