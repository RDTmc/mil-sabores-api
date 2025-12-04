package com.milsabores.api.service;

import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de productos conectado a Postgres (Docker) mediante ProductosDao.
 * Mantiene los DTOs públicos usados por el Controller.
 */
@Service
public class ProductosService {

    /* ======== DTOs públicos (usados por el Controller) ======== */
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

        // Normaliza rutas relativas (img/...) a /img/... ; respeta URLs http/https.
        private static String normalizeImagePath(String path) {
            if (path == null || path.isBlank()) return path;
            String lower = path.toLowerCase();
            if (lower.startsWith("http://") || lower.startsWith("https://")) return path;
            String p = path.startsWith("/") ? path : ("/" + path);
            return p.replaceAll("//+", "/");
        }

        // --- Getters para serialización JSON (camelCase y alias snake_case) ---

        @JsonProperty("imagePath")
        public String getImagePath() {
            return normalizeImagePath(this.imagePath);
        }

        @JsonProperty("image_path")
        public String getImage_path() {
            return normalizeImagePath(this.imagePath);
        }

        @JsonProperty("category_id")
        public Integer getCategory_id() {
            return this.categoryId;
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

    /* ======== REGLAS DE NEGOCIO: MARCA / DISTRIBUIDOR / DESCUENTOS ======== */

    /** Aplica un % de descuento sobre un entero (precio CLP). */
    private static int applyPercentDiscount(int price, int percent) {
        if (percent <= 0) return price;
        double factor = (100.0 - percent) / 100.0;
        return (int) Math.round(price * factor);
    }

    /**
     * Regla de negocio:
     * - Si la "marca" es distribuidor → 20% descuento
     * - Si el distribuidor es "Santa Rosa" → 20% descuento
     *
     * En este ejemplo usamos tags / nombre / descripción para identificarlo,
     * sin tocar la estructura de la BD.
     */
    private static Product applyDiscountRules(Product p) {
        if (p == null || p.price == null) return p;

        boolean isDistribuidor = false;
        boolean isSantaRosa = false;

        // 1) Revisamos tags (si usas tags como ["distribuidor", "santa rosa"])
        if (p.tags != null) {
            for (String tag : p.tags) {
                if (tag == null) continue;
                String normalized = tag.trim().toLowerCase();
                if (normalized.contains("distribuidor")) {
                    isDistribuidor = true;
                }
                if (normalized.contains("santa rosa") || normalized.contains("santarosa")) {
                    isSantaRosa = true;
                }
            }
        }

        // 2) También revisamos nombre y descripción (por si lo manejas como texto)
        String name = p.name != null ? p.name.toLowerCase() : "";
        String desc = p.description != null ? p.description.toLowerCase() : "";

        if (name.contains("distribuidor") || desc.contains("distribuidor")) {
            isDistribuidor = true;
        }
        if (name.contains("santa rosa") || desc.contains("santa rosa")) {
            isSantaRosa = true;
        }

        // 3) Regla final: si cumple alguna de las condiciones → 20% descuento
        if (isDistribuidor || isSantaRosa) {
            p.price = applyPercentDiscount(p.price, 20);
        }

        return p;
    }


    private static Category toDto(ProductosDao.CategoryRow r) {
        if (r == null) return null;
        return new Category(r.id, r.name);
    }

    /* ======== API del servicio (catálogo público) ======== */

    public PagedProducts listProducts(String q, Integer categoryId, String sort, int page, int size) {
        try {
            int safeSize = Math.max(size, 1);
            int safePage = Math.max(page, 0);

            long total = dao.countProducts(q, categoryId);
            List<Product> items = new ArrayList<>();
            for (ProductosDao.ProductRow r : dao.findProducts(q, categoryId, sort, safePage, safeSize)) {
                Product p = toDto(r);
                applyDiscountRules(p);   // aquí aplicamos la lógica
                items.add(p);
            }
            return new PagedProducts(items, safePage, safeSize, total);
        } catch (Exception e) {
            throw new RuntimeException("DB error on listProducts", e);
        }
    }


    public Product getProduct(String id) {
        try {
            Product p = toDto(dao.findProductById(id));
            return applyDiscountRules(p);  // regla también en detalle
        } catch (Exception e) {
            throw new RuntimeException("DB error on getProduct", e);
        }
    }


    @Cacheable(cacheNames = "categories")
    public List<Category> listCategories() {
        try {
            List<Category> out = new ArrayList<>();
            for (ProductosDao.CategoryRow r : dao.findCategories()) out.add(toDto(r));
            return out;
        } catch (Exception e) {
            throw new RuntimeException("DB error on listCategories", e);
        }
    }

    @Cacheable(cacheNames = "featured")
    public List<Product> listFeatured() {
        try {
            List<Product> out = new ArrayList<>();
            for (ProductosDao.ProductRow r : dao.findFeatured()) {
                Product p = toDto(r);
                applyDiscountRules(p);   // destacados también con descuento
                out.add(p);
            }
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

    /* ======== API del servicio (ADMIN: crear / editar / borrar) ======== */

    public Product createProduct(Product input) {
        if (input == null) {
            throw new IllegalArgumentException("Producto requerido");
        }
        if (input.id == null || input.id.isBlank()) {
            throw new IllegalArgumentException("El id del producto es obligatorio (ej: TC001)");
        }
        if (input.name == null || input.name.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (input.price == null || input.price < 0) {
            throw new IllegalArgumentException("El precio debe ser un entero mayor o igual a 0");
        }

        ProductosDao.ProductRow row = new ProductosDao.ProductRow();
        row.id = input.id;
        row.categoryId = input.categoryId;
        row.name = input.name;
        row.price = input.price;
        row.imagePath = input.imagePath;
        row.description = input.description;
        row.tags = input.tags;
        row.sizes = input.sizes;

        try {
            dao.insertProduct(row);
            // devolvemos el producto desde BD (ya normalizado)
            return getProduct(row.id);
        } catch (Exception e) {
            throw new RuntimeException("DB error on createProduct", e);
        }
    }

    public Product updateProduct(String id, Product input) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id del producto es obligatorio");
        }
        if (input == null) {
            throw new IllegalArgumentException("Cuerpo de producto requerido");
        }

        ProductosDao.ProductRow row = new ProductosDao.ProductRow();
        row.id = id; // usamos el id de la URL
        row.categoryId = input.categoryId;
        row.name = input.name;
        row.price = input.price;
        row.imagePath = input.imagePath;
        row.description = input.description;
        row.tags = input.tags;
        row.sizes = input.sizes;

        try {
            int updated = dao.updateProduct(row);
            if (updated == 0) {
                throw new IllegalArgumentException("Producto no encontrado con id=" + id);
            }
            return getProduct(id);
        } catch (RuntimeException e) {
            // re-lanzamos IllegalArgumentException tal cual
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("DB error on updateProduct", e);
        }
    }

    public void deleteProduct(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id del producto es obligatorio");
        }
        try {
            int deleted = dao.deleteProduct(id);
            if (deleted == 0) {
                throw new IllegalArgumentException("Producto no encontrado con id=" + id);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("DB error on deleteProduct", e);
        }
    }
}
