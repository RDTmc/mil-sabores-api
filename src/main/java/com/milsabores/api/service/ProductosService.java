package com.milsabores.api.service;

import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Servicio ahora conectado a Supabase mediante ProductosDao.
 * Mantiene los mismos DTOs públicos usados por el Controller.
 */
@Service
public class ProductosService {

    /* ======== DTOs públicos (sin cambios para el Controller) ======== */
    public static class Product {
        public String id;
        public Integer categoryId; // nullable
        public String name;
        public Integer price;      // CLP entero
        public String imagePath;
        public String description;
        public List<String> tags;  // nullable
        public List<String> sizes; // nullable

        public Product() {
        }

        public Product(String id, Integer categoryId, String name, Integer price,
                       String imagePath, String description, List<String> tags, List<String> sizes) {
            this.id = id;
            this.categoryId = categoryId;
            this.name = name;
            this.price = price;
            this.imagePath = imagePath;
            this.description = description;
            this.tags = tags;
            this.sizes = sizes;
        }
    }

    public static class Category {
        public Integer id;
        public String name;

        public Category() {
        }

        public Category(Integer id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static class PagedProducts {
        public List<Product> items;
        public int page;
        public int size;
        public long totalItems;
        public int totalPages;
        public boolean hasNext;

        public PagedProducts() {
        }

        public PagedProducts(List<Product> items, int page, int size, long totalItems) {
            this.items = items;
            this.page = page;
            this.size = size;
            this.totalItems = totalItems;
            this.totalPages = (int) Math.ceil((double) totalItems / Math.max(size, 1));
            this.hasNext = (page + 1) < this.totalPages;
        }
    }

    /* ======== Dependencia DAO ======== */
    private final ProductosDao dao;

    public ProductosService(ProductosDao dao) {
        this.dao = dao;
    }

    /* ======== Adaptadores (DAO → DTOs públicos) ======== */
    private static Product toDto(ProductosDao.ProductRow r) {
        if (r == null) return null;
        return new Product(
                r.id,
                r.categoryId,
                r.name,
                r.price,
                r.imagePath,
                r.description,
                r.tags,
                r.sizes
        );
    }

    private static Category toDto(ProductosDao.CategoryRow r) {
        if (r == null) return null;
        return new Category(r.id, r.name);
    }

    /* ======== API del servicio ======== */
    public PagedProducts listProducts(String q, Integer categoryId, String sort, int page, int size) throws RuntimeException {
        try {
            int safeSize = Math.max(size, 1);
            int safePage = Math.max(page, 0);

            long total = dao.countProducts(q, categoryId);
            List<Product> items = new ArrayList<>();
            for (ProductosDao.ProductRow r : dao.findProducts(q, categoryId, sort, safePage, safeSize)) {
                items.add(toDto(r));
            }
            return new PagedProducts(items, safePage, safeSize, total);
        } catch (Exception e) {
            // Propaga como RuntimeException para que lo capture @ControllerAdvice → 500 + {"message": "..."}
            throw new RuntimeException("DB error on listProducts", e);
        }
    }

    public Product getProduct(String id) {
        try {
            return toDto(dao.findProductById(id));
        } catch (Exception e) {
            throw new RuntimeException("DB error on getProduct", e);
        }
    }

    public List<Category> listCategories() {
        try {
            List<Category> out = new ArrayList<>();
            for (ProductosDao.CategoryRow r : dao.findCategories()) out.add(toDto(r));
            return out;
        } catch (Exception e) {
            throw new RuntimeException("DB error on listCategories", e);
        }
    }

    public List<Product> listFeatured() {
        try {
            List<Product> out = new ArrayList<>();
            for (ProductosDao.ProductRow r : dao.findFeatured()) out.add(toDto(r));
            return out;
        } catch (Exception e) {
            throw new RuntimeException("DB error on listFeatured", e);
        }
    }

    /**
     * Debug snapshot para diagnóstico rápido
     */
    public java.util.Map<String, Object> debug() {
        try {
            long total = dao.countProducts(null, null);
            boolean tc001 = dao.findProductById("TC001") != null;
            int cats = dao.findCategories().size();
            int feat = dao.findFeatured().size();
            return java.util.Map.of(
                    "db", "ok",
                    "productsCount", total,
                    "hasTC001", tc001,
                    "categoriesCount", cats,
                    "featuredCount", feat
            );
        } catch (Exception e) {
            return java.util.Map.of("db", "error", "message", e.getMessage());
        }
    }
}