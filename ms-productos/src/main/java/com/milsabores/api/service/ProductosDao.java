package com.milsabores.api.service;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * DAO de lectura/escritura contra Postgres (Docker) usando JDBC básico.
 * Usa el DataSource autoconfigurado por Spring Boot (ms-productos-pool).
 */
@Repository
public class ProductosDao {

    private final DataSource dataSource;

    public ProductosDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /* ============= MODELOS INTERNOS (uso del Service) ============= */
    public static class ProductRow {
        public String id;
        public Integer categoryId;
        public String name;
        public Integer price;
        public String imagePath;
        public String description;
        public List<String> tags;
        public List<String> sizes;
    }

    public static class CategoryRow {
        public Integer id;
        public String name;
    }

    /* ===================== HELPERS ===================== */
    private static List<String> toStringList(Array array) throws SQLException {
        if (array == null) return null;
        Object[] vals = (Object[]) array.getArray();
        List<String> out = new ArrayList<>();
        for (Object v : vals) out.add(v != null ? v.toString() : null);
        return out;
    }

    private static ProductRow mapProduct(ResultSet rs) throws SQLException {
        ProductRow p = new ProductRow();
        p.id = rs.getString("id");
        int cat = rs.getInt("category_id");
        p.categoryId = rs.wasNull() ? null : cat;
        p.name = rs.getString("name");
        int price = rs.getInt("price");
        p.price = rs.wasNull() ? null : price;
        p.imagePath = rs.getString("image_path");
        p.description = rs.getString("description");
        p.tags = toStringList(rs.getArray("tags"));
        p.sizes = toStringList(rs.getArray("sizes"));
        return p;
    }

    private static CategoryRow mapCategory(ResultSet rs) throws SQLException {
        CategoryRow c = new CategoryRow();
        c.id = rs.getInt("id");
        c.name = rs.getString("name");
        return c;
    }

    private static PreparedStatement prepare(Connection conn, String sql, List<Object> params) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql);
        int i = 1;
        for (Object p : params) ps.setObject(i++, p);
        return ps;
    }

    /* ===================== QUERIES LECTURA ===================== */

    public long countProducts(String q, Integer categoryId) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            select count(*) as cnt
            from public.products p
            where 1=1
        """);
        List<Object> params = new ArrayList<>();

        if (q != null && !q.isBlank()) {
            sql.append(" and (p.name ilike ? or p.description ilike ?) ");
            String like = "%" + q.trim() + "%";
            params.add(like);
            params.add(like);
        }
        if (categoryId != null) {
            sql.append(" and p.category_id = ? ");
            params.add(categoryId);
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = prepare(conn, sql.toString(), params);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong("cnt");
        }
    }

    public List<ProductRow> findProducts(String q, Integer categoryId, String sort, int page, int size) throws SQLException {
        // whitelist de campos de ordenamiento
        String orderBy = "p.name asc";
        if (sort != null && !sort.isBlank()) {
            boolean desc = sort.startsWith("-");
            String field = desc ? sort.substring(1) : sort;
            switch (field) {
                case "price" -> orderBy = "p.price " + (desc ? "desc" : "asc");
                case "name"  -> orderBy = "p.name "  + (desc ? "desc" : "asc");
                case "createdAt" -> orderBy = "p.id " + (desc ? "desc" : "asc"); // placeholder
                default -> { /* mantener por defecto */ }
            }
        }

        StringBuilder sql = new StringBuilder("""
            select p.id, p.category_id, p.name, p.price, p.image_path, p.description, p.tags, p.sizes
            from public.products p
            where 1=1
        """);
        List<Object> params = new ArrayList<>();

        if (q != null && !q.isBlank()) {
            sql.append(" and (p.name ilike ? or p.description ilike ?) ");
            String like = "%" + q.trim() + "%";
            params.add(like);
            params.add(like);
        }
        if (categoryId != null) {
            sql.append(" and p.category_id = ? ");
            params.add(categoryId);
        }

        sql.append(" order by ").append(orderBy)
                .append(" limit ? offset ? ");
        int safeSize = Math.max(size, 1);
        int offset = Math.max(page, 0) * safeSize;
        params.add(safeSize);
        params.add(offset);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = prepare(conn, sql.toString(), params);
             ResultSet rs = ps.executeQuery()) {
            List<ProductRow> out = new ArrayList<>();
            while (rs.next()) out.add(mapProduct(rs));
            return out;
        }
    }

    public ProductRow findProductById(String id) throws SQLException {
        String sql = """
            select p.id, p.category_id, p.name, p.price, p.image_path, p.description, p.tags, p.sizes
            from public.products p
            where p.id = ?
            limit 1
        """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = prepare(conn, sql, List.of(id));
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return mapProduct(rs);
            return null;
        }
    }

    public List<CategoryRow> findCategories() throws SQLException {
        String sql = "select c.id, c.name from public.categories c order by c.name asc";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<CategoryRow> out = new ArrayList<>();
            while (rs.next()) out.add(mapCategory(rs));
            return out;
        }
    }

    public List<ProductRow> findFeatured() throws SQLException {
        String sql = """
            select p.id, p.category_id, p.name, p.price, p.image_path, p.description, p.tags, p.sizes
            from public.featured_products f
            join public.products p on p.id = f.product_id
            order by f.position asc
        """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<ProductRow> out = new ArrayList<>();
            while (rs.next()) out.add(mapProduct(rs));
            return out;
        }
    }

    /* ===================== QUERIES ESCRITURA (ADMIN) ===================== */

    public void insertProduct(ProductRow p) throws SQLException {
        String sql = """
            insert into public.products
            (id, category_id, name, price, image_path, description, tags, sizes)
            values (?,?,?,?,?,?,?,?)
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.id);

            if (p.categoryId != null) ps.setInt(2, p.categoryId);
            else ps.setNull(2, Types.INTEGER);

            if (p.name != null) ps.setString(3, p.name);
            else ps.setNull(3, Types.VARCHAR);

            if (p.price != null) ps.setInt(4, p.price);
            else ps.setNull(4, Types.INTEGER);

            if (p.imagePath != null) ps.setString(5, p.imagePath);
            else ps.setNull(5, Types.VARCHAR);

            if (p.description != null) ps.setString(6, p.description);
            else ps.setNull(6, Types.VARCHAR);

            if (p.tags != null && !p.tags.isEmpty()) {
                Array tagsArray = conn.createArrayOf("text", p.tags.toArray());
                ps.setArray(7, tagsArray);
            } else {
                ps.setNull(7, Types.ARRAY);
            }

            if (p.sizes != null && !p.sizes.isEmpty()) {
                Array sizesArray = conn.createArrayOf("text", p.sizes.toArray());
                ps.setArray(8, sizesArray);
            } else {
                ps.setNull(8, Types.ARRAY);
            }

            ps.executeUpdate();
        }
    }

    public int updateProduct(ProductRow p) throws SQLException {
        String sql = """
            update public.products
               set category_id = ?,
                   name        = ?,
                   price       = ?,
                   image_path  = ?,
                   description = ?,
                   tags        = ?,
                   sizes       = ?
             where id = ?
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (p.categoryId != null) ps.setInt(1, p.categoryId);
            else ps.setNull(1, Types.INTEGER);

            if (p.name != null) ps.setString(2, p.name);
            else ps.setNull(2, Types.VARCHAR);

            if (p.price != null) ps.setInt(3, p.price);
            else ps.setNull(3, Types.INTEGER);

            if (p.imagePath != null) ps.setString(4, p.imagePath);
            else ps.setNull(4, Types.VARCHAR);

            if (p.description != null) ps.setString(5, p.description);
            else ps.setNull(5, Types.VARCHAR);

            if (p.tags != null && !p.tags.isEmpty()) {
                Array tagsArray = conn.createArrayOf("text", p.tags.toArray());
                ps.setArray(6, tagsArray);
            } else {
                ps.setNull(6, Types.ARRAY);
            }

            if (p.sizes != null && !p.sizes.isEmpty()) {
                Array sizesArray = conn.createArrayOf("text", p.sizes.toArray());
                ps.setArray(7, sizesArray);
            } else {
                ps.setNull(7, Types.ARRAY);
            }

            ps.setString(8, p.id);

            return ps.executeUpdate();
        }
    }

    public int deleteProduct(String id) throws SQLException {
        String sql = "delete from public.products where id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate();
        }
    }
}
