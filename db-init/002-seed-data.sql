-- 002-seed-data.sql
-- Seed inicial de datos basado en export de Supabase

BEGIN;

-- ==========================
--  CATEGORIES
-- ==========================
INSERT INTO categories (id, name) VALUES
  (1, 'Tortas Cuadradas'),
  (2, 'Tortas Circulares'),
  (3, 'Postres Individuales'),
  (4, 'Sin Azúcar'),
  (5, 'Sin Gluten'),
  (6, 'Vegano'),
  (7, 'Tortas Especiales')
ON CONFLICT (id) DO NOTHING;

-- Ajustamos la secuencia al máximo ID actual
SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));

-- ==========================
--  PRODUCTS
-- ==========================
INSERT INTO products (id, category_id, name, price, image_path, description, tags, sizes) VALUES
  ('PG001', 5, 'Brownie Sin Gluten', 4000, 'img/pg_brownie.png',
   'Rico y denso, este brownie es perfecto para quienes necesitan evitar el gluten sin sacrificar el sabor.',
   '{"sin gluten"}', NULL),

  ('PG002', 5, 'Pan Sin Gluten', 3500, 'img/pg_pan.png',
   'Suave y esponjoso, ideal para sándwiches o para acompañar cualquier comida.',
   '{"sin gluten"}', NULL),

  ('PI001', 3, 'Mousse de Chocolate', 5000, 'img/pi_mousse.png',
   'Postre individual cremoso y suave, hecho con chocolate de alta calidad, ideal para los amantes del chocolate.',
   '{"individual"}', NULL),

  ('PI002', 3, 'Tiramisú Clásico', 5500, 'img/pi_tiramisu.png',
   'Un postre italiano individual con capas de café, mascarpone y cacao, perfecto para finalizar cualquier comida.',
   '{"individual"}', NULL),

  ('PSA001', 4, 'Torta Sin Azúcar de Naranja', 48000, 'img/psa_naranja.png',
   'Torta ligera y deliciosa, endulzada naturalmente, ideal para quienes buscan opciones más saludables.',
   '{"sin azúcar"}', NULL),

  ('PSA002', 4, 'Cheesecake Sin Azúcar', 47000, 'img/psa_cheesecake.png',
   'Suave y cremoso, este cheesecake es una opción perfecta para disfrutar sin culpa.',
   '{"sin azúcar"}', NULL),

  ('PV001', 6, 'Torta Vegana de Chocolate', 50000, 'img/pv_chocolate.png',
   'Torta de chocolate húmeda y deliciosa, hecha sin productos de origen animal, perfecta para veganos.',
   '{"vegano"}', NULL),

  ('PV002', 6, 'Galletas Veganas de Avena', 4500, 'img/pv_avena.png',
   'Crujientes y sabrosas, estas galletas son una excelente opción para un snack saludable y vegano.',
   '{"vegano"}', NULL),

  ('TC001', 1, 'Torta Cuadrada de Chocolate', 45000, 'img/tc_chocolate.png',
   'Deliciosa torta de chocolate con capas de ganache y un toque de avellanas. Personalizable con mensajes especiales.',
   '{"tradicional"}', '{"8 porciones","10 porciones","12 porciones"}'),

  ('TC002', 1, 'Torta Cuadrada de Frutas', 50000, 'img/tc_frutas.png',
   'Una mezcla de frutas frescas y crema chantilly sobre un suave bizcocho de vainilla, ideal para celebraciones.',
   '{"tradicional"}', NULL),

  ('TE001', 7, 'Torta Especial de Cumpleaños', 55000, 'img/te_cumple.png',
   'Diseñada especialmente para celebraciones, personalizable con decoraciones y mensajes únicos.',
   '{"especial"}', NULL),

  ('TE002', 7, 'Torta Especial de Boda', 60000, 'img/te_boda.png',
   'Elegante y deliciosa, esta torta está diseñada para ser el centro de atención en cualquier boda.',
   '{"especial"}', NULL),

  ('TT001', 2, 'Torta Circular de Vainilla', 40000, 'img/tt_vainilla.png',
   'Bizcocho de vainilla clásico relleno con crema pastelera y cubierto con un glaseado dulce, perfecto para cualquier ocasión.',
   '{"tradicional"}', NULL),

  ('TT002', 2, 'Torta Circular de Manjar', 42000, 'img/tt_manjar.png',
   'Torta tradicional chilena con manjar y nueces, un deleite para los amantes de los sabores dulces y clásicos.',
   '{"tradicional"}', NULL)
ON CONFLICT (id) DO NOTHING;

-- ==========================
--  FEATURED_PRODUCTS
-- ==========================
INSERT INTO featured_products (product_id, position) VALUES
  ('TC001', 0),
  ('TT001', 1),
  ('PI001', 2),
  ('PSA001', 3)
ON CONFLICT (product_id) DO NOTHING;

-- ==========================
--  USERS
-- ==========================
INSERT INTO users (id, created_at, email, full_name, password_hash, phone, role, birth_date, registration_code) VALUES
  ('08af7cf9-3eb1-4095-b77a-163e69fb9f41',
   '2025-11-22 19:16:39.389056',
   'cliente@demo.cl',
   'Cliente Demo',
   '$2a$10$.meW/vQnzh3FA/eX3jcRDOz000o9/7kDQbjQSDvJ50VRiYXhiY/ba',
   '+56912345678',
   'CUSTOMER',
   NULL,
   NULL),

  ('28529e4b-ae6c-4078-a7af-19b5ccf23aa1',
   '2025-11-23 21:00:22.051771',
   'testcliente1@demo.cl',
   'Luis Fuentes',
   '$2a$10$3n7nckMbuUpB4kRlUcKqluCN9yRUXFimjB3sgTtL8EbdtFC.zGiUO',
   NULL,
   'CUSTOMER',
   NULL,
   NULL)
ON CONFLICT (id) DO NOTHING;

-- ==========================
--  CARTS
-- ==========================
INSERT INTO carts (id, created_at, status, updated_at, user_id) VALUES
  ('289e7dea-c822-485f-9f3a-9b4fa8c216da',
   '2025-11-23 21:59:47.038004',
   'ACTIVE',
   '2025-11-23 21:59:47.038004',
   '28529e4b-ae6c-4078-a7af-19b5ccf23aa1')
ON CONFLICT (id) DO NOTHING;

-- ==========================
--  CART_ITEMS
-- ==========================
-- Supabase reporta esta tabla vacía, por ahora no insertamos nada aquí.

-- ==========================
--  ORDERS
-- ==========================
INSERT INTO orders (
  id,
  created_at,
  payment_method,
  shipping_address,
  status,
  total_amount,
  updated_at,
  user_id,
  subtotal_amount,
  discount_amount,
  discount_code,
  discount_description
) VALUES
  (
    '146e017d-0916-42e4-bf79-f040d742c35f',
    '2025-11-24 12:25:09.457167',
    'webpay',
    'Ines de Suarez 1518 | Providencia | Tel: +569 99887766 | Fecha entrega: 2025-11-28',
    'CREATED',
    10000,
    '2025-11-24 12:25:09.457167',
    '28529e4b-ae6c-4078-a7af-19b5ccf23aa1',
    0,
    0,
    NULL,
    NULL
  ),
  (
    'test-order-1',
    '2025-11-24 14:41:29.622212',
    'CARD',
    'Direccion de prueba',
    'CREATED',
    1000,
    '2025-11-24 14:41:29.622212',
    'test-user-1',
    0,
    0,
    NULL,
    NULL
  )
ON CONFLICT (id) DO NOTHING;

-- ==========================
--  ORDER_ITEMS
-- ==========================
INSERT INTO order_items (
  id,
  flavor,
  image,
  product_id,
  product_name,
  quantity,
  size,
  unit_price,
  order_id
) VALUES
  (
    '676d80f7-2e38-4eae-b411-b41ce10e01d8',
    NULL,
    NULL,
    'PI001',
    'Mousse de Chocolate',
    2,
    NULL,
    5000,
    '146e017d-0916-42e4-bf79-f040d742c35f'
  )
ON CONFLICT (id) DO NOTHING;

COMMIT;
