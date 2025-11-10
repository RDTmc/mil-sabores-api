package com.milsabores.api.service;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrdersService {

    private final JdbcTemplate jdbc;

    public OrdersService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record ProductRow(String id, Integer price) {}

    private Map<String, ProductRow> loadProductsByIds(Set<String> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyMap();
        String inSql = ids.stream().map(s -> "?").collect(Collectors.joining(","));
        String sql = "select id, price from public.products where id in (" + inSql + ")";
        List<Object> args = new ArrayList<>(ids);
        return jdbc.query(sql, args.toArray(), (rs) ->
        {
            Map<String, ProductRow> map = new HashMap<>();
            while (rs.next()) {
                map.put(rs.getString("id"), new ProductRow(rs.getString("id"), rs.getInt("price")));
            }
            return map;
        });
    }

    public static class CreateOrderItem {
        public String productId;
        public Integer qty;
        public String size;          // opcional
        public Integer priceEach;    // opcional: el cliente puede sugerirlo, pero el servidor lo recalcula
    }

    public static class CreateOrderRequest {
        public String userId;        // opcional por ahora (null si anónimo)
        public List<CreateOrderItem> items;
        public String note;          // opcional: comentarios del cliente
    }

    public static class OrderItemDTO {
        public String productId;
        public Integer qty;
        public Integer priceEach;
        public String size;
    }

    public static class OrderResponse {
        public UUID orderId;
        public String status;
        public Integer total;
        public Integer itemsCount;
        public List<OrderItemDTO> items;
        public String message;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest req) {
        if (req == null || req.items == null || req.items.isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío.");
        }
        // Validación básica
        for (CreateOrderItem it : req.items) {
            if (it == null || it.productId == null || it.productId.isBlank()) {
                throw new IllegalArgumentException("Ítem sin productId.");
            }
            if (it.qty == null || it.qty < 1) {
                throw new IllegalArgumentException("Cantidad inválida en " + it.productId);
            }
        }

        // Cargar precios reales desde BD (no confiamos en el cliente)
        Set<String> ids = req.items.stream().map(i -> i.productId).collect(Collectors.toSet());
        Map<String, ProductRow> catalog = loadProductsByIds(ids);
        if (catalog.size() != ids.size()) {
            // detectar faltantes
            Set<String> missing = new HashSet<>(ids);
            missing.removeAll(catalog.keySet());
            throw new IllegalArgumentException("Productos inexistentes: " + String.join(",", missing));
        }

        // Calcular total
        int total = 0;
        for (CreateOrderItem it : req.items) {
            int price = Optional.ofNullable(catalog.get(it.productId)).map(p -> p.price).orElseThrow();
            total += price * it.qty;
        }

        // Insertar orden (status 'pending' por defecto)
        UUID orderId = insertOrder(req.userId, total);

        // Insertar items (batch)
        batchInsertItems(orderId, req.items, catalog);

        // Construir respuesta
        OrderResponse out = new OrderResponse();
        out.orderId = orderId;
        out.status  = "pending";
        out.total   = total;
        out.itemsCount = req.items.stream().mapToInt(i -> i.qty).sum();
        out.items = req.items.stream().map(i -> {
            OrderItemDTO d = new OrderItemDTO();
            d.productId = i.productId;
            d.qty = i.qty;
            d.size = i.size;
            d.priceEach = catalog.get(i.productId).price;
            return d;
        }).collect(Collectors.toList());
        out.message = "Orden creada correctamente";
        return out;
    }

    private UUID insertOrder(String userId, int total) {
        // user_id puede ser null (anónimo). En tu schema, status default 'pending'
        String sql = "insert into public.orders(user_id, total) values (?, ?) returning id";
        return jdbc.query(sql, ps -> {
            if (userId == null || userId.isBlank()) ps.setObject(1, null);
            else ps.setObject(1, UUID.fromString(userId)); // si más adelante autenticas via JWT -> vendrá el uuid
            ps.setInt(2, total);
        }, rs -> {
            if (rs.next()) return (UUID) rs.getObject("id");
            throw new EmptyResultDataAccessException("No se pudo crear la orden", 1);
        });
    }

    private void batchInsertItems(UUID orderId, List<CreateOrderItem> items, Map<String, ProductRow> catalog) {
        String sql = "insert into public.order_items(order_id, product_id, qty, price_each, size) values (?,?,?,?,?)";
        jdbc.batchUpdate(sql, items, items.size(), (PreparedStatement ps, CreateOrderItem it) -> {
            ps.setObject(1, orderId);
            ps.setString(2, it.productId);
            ps.setInt(3, it.qty);
            ps.setInt(4, catalog.get(it.productId).price);
            if (it.size == null || it.size.isBlank()) ps.setObject(5, null);
            else ps.setString(5, it.size);
        });
    }
}
