-- 001-schema.sql
-- Esquema Mil Sabores para Postgres local (alineado con Supabase)

-- Limpieza previa (solo afecta la PRIMERA vez si ya existen tablas)
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS cart_items CASCADE;
DROP TABLE IF EXISTS carts CASCADE;
DROP TABLE IF EXISTS featured_products CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS users CASCADE;

DROP SEQUENCE IF EXISTS categories_id_seq;

-- =========================
--  SEQUENCE PARA CATEGORIES
-- =========================
CREATE SEQUENCE categories_id_seq START 1;

-- ==========
--  USERS
-- ==========
CREATE TABLE users (
  id                character varying NOT NULL,
  created_at        timestamp without time zone NOT NULL,
  email             character varying NOT NULL UNIQUE,
  full_name         character varying NOT NULL,
  password_hash     character varying NOT NULL,
  phone             character varying,
  role              character varying NOT NULL,
  birth_date        date,
  registration_code character varying,
  CONSTRAINT users_pkey PRIMARY KEY (id)
);

-- =============
--  CATEGORIES
-- =============
CREATE TABLE categories (
  id   integer NOT NULL DEFAULT nextval('categories_id_seq'::regclass),
  name text    NOT NULL UNIQUE,
  CONSTRAINT categories_pkey PRIMARY KEY (id)
);

-- ==========
--  PRODUCTS
-- ==========
CREATE TABLE products (
  id          text NOT NULL,
  category_id integer,
  name        text NOT NULL,
  price       integer NOT NULL,
  image_path  text,
  description text,
  tags        text[],   -- ARRAY en Supabase → text[] aquí
  sizes       text[],   -- ARRAY en Supabase → text[] aquí
  CONSTRAINT products_pkey PRIMARY KEY (id),
  CONSTRAINT products_category_id_fkey FOREIGN KEY (category_id)
    REFERENCES categories(id)
);

-- =====================
--  FEATURED_PRODUCTS
-- =====================
CREATE TABLE featured_products (
  product_id text NOT NULL,
  position   integer DEFAULT 0,
  CONSTRAINT featured_products_pkey PRIMARY KEY (product_id),
  CONSTRAINT featured_products_product_id_fkey FOREIGN KEY (product_id)
    REFERENCES products(id)
);

-- ======
--  CARTS
-- ======
CREATE TABLE carts (
  id          character varying NOT NULL,
  created_at  timestamp without time zone NOT NULL,
  status      character varying NOT NULL,
  updated_at  timestamp without time zone NOT NULL,
  user_id     character varying NOT NULL,
  CONSTRAINT carts_pkey PRIMARY KEY (id)
);

-- ============
--  CART_ITEMS
-- ============
CREATE TABLE cart_items (
  id           character varying NOT NULL,
  flavor       character varying,
  image        character varying,
  product_id   character varying NOT NULL,
  product_name character varying NOT NULL,
  quantity     integer NOT NULL,
  size         character varying,
  unit_price   integer NOT NULL,
  cart_id      character varying NOT NULL,
  CONSTRAINT cart_items_pkey PRIMARY KEY (id),
  CONSTRAINT fkpcttvuq4mxppo8sxggjtn5i2c FOREIGN KEY (cart_id)
    REFERENCES carts(id)
);

-- =======
-- ORDERS
-- =======
CREATE TABLE orders (
  id                   character varying NOT NULL,
  created_at           timestamp without time zone NOT NULL,
  payment_method       character varying NOT NULL,
  shipping_address     character varying NOT NULL,
  status               character varying NOT NULL,
  total_amount         integer NOT NULL,
  updated_at           timestamp without time zone NOT NULL,
  user_id              character varying NOT NULL,
  subtotal_amount      integer NOT NULL DEFAULT 0,
  discount_amount      integer NOT NULL DEFAULT 0,
  discount_code        character varying,
  discount_description character varying,
  CONSTRAINT orders_pkey PRIMARY KEY (id)
);

-- ============
-- ORDER_ITEMS
-- ============
CREATE TABLE order_items (
  id           character varying NOT NULL,
  flavor       character varying,
  image        character varying,
  product_id   character varying NOT NULL,
  product_name character varying NOT NULL,
  quantity     integer NOT NULL,
  size         character varying,
  unit_price   integer NOT NULL,
  order_id     character varying NOT NULL,
  CONSTRAINT order_items_pkey PRIMARY KEY (id),
  CONSTRAINT fkbioxgbv59vetrxe0ejfubep1w FOREIGN KEY (order_id)
    REFERENCES orders(id)
);
