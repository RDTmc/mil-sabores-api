

---

````markdown
# Mil Sabores – Backend (Microservicios)

Conjunto de **microservicios en Spring Boot** para el proyecto **Pastelería Mil Sabores**, que provee:

- Autenticación y roles (clientes y administradores).
- Catálogo de productos de pastelería.
- Carrito de compras por usuario.
- Órdenes de compra con sistema de promociones.
- Endpoints de administración para monitorear usuarios y pedidos.

Este backend es consumido por el **frontend en React** (rama `React+Microservicios`) y por futuras apps móviles.

---

## 1. Arquitectura general

El backend está dividido en 4 microservicios independientes:

- `ms-usuarios`  
  Autenticación (login/registro), gestión de usuarios, roles y datos de perfil.

- `ms-productos`  
  Catálogo de productos, categorías y productos destacados.

- `ms-cart`  
  Carrito de compras por usuario, persistido en base de datos.

- `ms-orders`  
  Órdenes, cálculo de totales y **promociones automáticas** (adulto mayor, FELICES50, Duoc cumpleaños), además de endpoints para panel administrador.

Cada microservicio expone una API REST y su propia documentación **Swagger / OpenAPI**.

---

## 2. Tecnologías utilizadas

**Backend**

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security + JWT
- HikariCP (pool de conexiones)
- Maven

**Base de datos & almacenamiento**

- PostgreSQL (usuarios, productos, carrito, órdenes)
- Supabase Storage (imágenes de productos, consumidas desde el frontend)

**Infraestructura y tooling**

- Git + GitHub
- Swagger / OpenAPI (springdoc)
- Maven Wrapper / Maven 3.x

---

## 3. Estructura del repositorio

```text
mil-sabores-api/
├── pom.xml
├── run-microservices.bat
├── run-microservices-ms-usuarios.bat
├── run-microservices-ms-productos.bat
├── run-microservices-ms-cart.bat
├── run-microservices-ms-orders.bat
└── ms-usuarios/
    ├── src/main/java/com/milsabores/usuarios/...
    └── src/main/resources/application.properties
└── ms-productos/
    ├── src/main/java/com/milsabores/api/...
    └── src/main/resources/application.properties
└── ms-cart/
    ├── src/main/java/com/milsabores/cart/...
    └── src/main/resources/application.properties
└── ms-orders/
    ├── src/main/java/com/milsabores/orders/...
    └── src/main/resources/application.properties
````
---

## 4. Requisitos previos

* **Java JDK 21**
* **Maven 3.x**
* **PostgreSQL** 
* Puerto libres:

  * `ms-usuarios` → `8082`
  * `ms-productos` → `8081`
  * `ms-cart` → `8084`
  * `ms-orders` → `8083`

---

## 5. Configuración y ejecución

### 5.1. Configuración de base de datos

En cada microservicio, configura la conexión en:

```properties
# Ejemplo application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/milsabores_usuarios
spring.datasource.username=postgres
spring.datasource.password=tu_password
spring.jpa.hibernate.ddl-auto=update
```

Cada microservicio puede usar su propia base (recomendado en arquitectura de microservicios).

### 5.2. Ejecutar todos los microservicios

**Opción 1 – Scripts `.bat` (Windows)**

En la raíz del proyecto:

```bash
# Levantar todos los microservicios en paralelo
run-microservices.bat

# O cada uno por separado:
run-microservices-ms-usuarios.bat
run-microservices-ms-productos.bat
run-microservices-ms-cart.bat
run-microservices-ms-orders.bat
```

**Opción 2 – Maven desde cada módulo**

```bash
# Backend productos
cd ms-productos
mvn spring-boot:run

# Backend usuarios
cd ms-usuarios
mvn spring-boot:run

# Backend carrito
cd ms-cart
mvn spring-boot:run

# Backend órdenes
cd ms-orders
mvn spring-boot:run
```

---

## 6. URLs base por microservicio

| Microservicio | Descripción            | URL base REST               |
| ------------- | ---------------------- | --------------------------- |
| ms-productos  | Catálogo de productos  | `http://localhost:8081/api` |
| ms-usuarios   | Auth, usuarios y roles | `http://localhost:8082/api` |
| ms-orders     | Órdenes y promociones  | `http://localhost:8083/api` |
| ms-cart       | Carrito de compras     | `http://localhost:8084/api` |

```env
VITE_API_URL=http://localhost:8081/api
VITE_AUTH_API_URL=http://localhost:8082/api
VITE_ORDERS_API_URL=http://localhost:8083/api
VITE_CART_API_URL=http://localhost:8084/api
```

---

## 7. Documentación Swagger / OpenAPI

Cada microservicio expone su propia documentación Swagger.

Por defecto con **springdoc-openapi**, la URL suele ser:

```text
http://localhost:<PUERTO>/swagger-ui/index.html
```

Por ejemplo:

* **ms-productos**:
  `http://localhost:8081/swagger-ui/index.html`

* **ms-usuarios**:
  `http://localhost:8082/swagger-ui/index.html`

* **ms-orders**:
  `http://localhost:8083/swagger-ui/index.html`

* **ms-cart**:
  `http://localhost:8084/swagger-ui/index.html`

---

## 8. Endpoints principales por microservicio

### 8.1. `ms-usuarios` – Autenticación y administración de usuarios

**Base URL:** `http://localhost:8082/api`

#### Auth

| Método | Ruta             | Descripción                              | Seguridad |
| ------ | ---------------- | ---------------------------------------- | --------- |
| POST   | `/auth/register` | Registro de un nuevo usuario (CUSTOMER). | Público   |
| POST   | `/auth/login`    | Login. Devuelve JWT + datos de usuario.  | Público   |

**Respuesta login típica:**

```json
{
  "token": "jwt-aquí",
  "userId": "UUID-usuario",
  "email": "cliente@milsabores.cl",
  "fullName": "Nombre Apellido"
}
```

#### Admin (requiere rol `ADMIN`)

| Método | Ruta                | Descripción                       |
| ------ | ------------------- | --------------------------------- |
| GET    | `/admin/users`      | Lista todos los usuarios.         |
| PUT    | `/admin/users/{id}` | Actualiza nombre, teléfono y rol. |
| DELETE | `/admin/users/{id}` | Elimina un usuario por ID.        |

> Estos endpoints requieren encabezado `Authorization: Bearer <JWT_ADMIN>`.

---

### 8.2. `ms-productos` – Catálogo

**Base URL:** `http://localhost:8081/api`

#### Productos

| Método | Ruta             | Descripción                                                         |
| ------ | ---------------- | ------------------------------------------------------------------- |
| GET    | `/products`      | Lista paginada de productos. Soporta filtros por `q`, `categoryId`. |
| GET    | `/products/{id}` | Detalle de un producto por ID.                                      |
| GET    | `/categories`    | Lista de categorías disponibles.                                    |
| GET    | `/featured`      | Productos destacados (para home).                                   |

**Ejemplos:**

```http
GET http://localhost:8081/api/products?page=0&size=48&q=brownie

GET http://localhost:8081/api/products/PG001
```

La propiedad `imagePath` / `image_path` apunta a un URL de Supabase o a una ruta relativa consumida por el frontend.

---

### 8.3. `ms-cart` – Carrito de compras

**Base URL:** `http://localhost:8084/api`

Todos los endpoints requieren:

* **Cabecera**: `X-User-Id: <id-del-usuario>`
* **JWT válido** (según configuración de seguridad).

#### Endpoints

| Método | Ruta               | Descripción                                |
| ------ | ------------------ | ------------------------------------------ |
| GET    | `/cart`            | Obtiene el carrito actual del usuario.     |
| POST   | `/cart/items`      | Agrega o actualiza un ítem en el carrito.  |
| PUT    | `/cart/items/{id}` | Modifica la cantidad de un ítem.           |
| DELETE | `/cart/items/{id}` | Elimina un ítem específico del carrito.    |
| DELETE | `/cart`            | Vacía por completo el carrito del usuario. |

**Ejemplo payload `POST /cart/items`:**

```json
{
  "productId": "PG001",
  "productName": "Brownie Chocolate",
  "image": "https://.../pg_brownie.jpg",
  "unitPrice": 12990,
  "quantity": 2,
  "size": "8 porciones",
  "flavor": null
}
```

---

### 8.4. `ms-orders` – Órdenes, promociones y panel administrador

**Base URL:** `http://localhost:8083/api`

#### Cliente autenticado

| Método | Ruta           | Descripción                                 |
| ------ | -------------- | ------------------------------------------- |
| POST   | `/orders`      | Crea una nueva orden a partir del carrito.  |
| GET    | `/orders`      | Lista todas las órdenes del usuario actual. |
| GET    | `/orders/{id}` | Obtiene el detalle de una orden específica. |

**Request `POST /orders`:**

```json
{
  "paymentMethod": "CARD",
  "shippingAddress": "Dirección | Comuna | Teléfono | Fecha entrega | Notas",
  "items": [
    {
      "productId": "PG001",
      "productName": "Brownie Chocolate",
      "image": "https://.../pg_brownie.jpg",
      "unitPrice": 12990,
      "quantity": 1,
      "size": "8 porciones",
      "flavor": null
    }
  ]
}
```

El microservicio:

1. Calcula el **subtotal**.
2. Llama a `PromotionService` para determinar si hay una promo aplicable (ver más abajo).
3. Calcula `discountAmount` y `totalAmount`.
4. Devuelve un `OrderResponse` con todos los detalles.

#### Admin (panel administrador)

| Método | Ruta                   | Descripción                                |
| ------ | ---------------------- | ------------------------------------------ |
| GET    | `/admin/orders/latest` | Últimas N órdenes del sistema (dashboard). |

Parámetros:

* `limit` (opcional): cantidad de órdenes a devolver (por defecto, ej. `5` o `10`).

> Requiere rol `ADMIN` y encabezado `Authorization: Bearer <JWT_ADMIN>`.

---

## 9. Sistema de promociones (`PromotionService` en ms-orders)

La lógica de promociones está encapsulada en `PromotionService`:

* **ADULTO_MAYOR (50%)**

  * Aplica a usuarios con **50+ años** (`birthDate`).
* **FELICES50 (10%)**

  * Aplica si el usuario se registró con código `FELICES50`.
* **DUOC_CUMPLE (25%)**

  * Aplica si el correo termina en `@duocuc.cl` y es el **día de cumpleaños**.

Reglas:

* Las promociones **no se acumulan**.
* Se escoge **la de mayor porcentaje**.
* La respuesta de orden incluye:

```json
{
  "subtotalAmount": 25980,
  "discountAmount": 12990,
  "discountCode": "ADULTO_MAYOR",
  "discountDescription": "Descuento 50% por adulto mayor (50+ años)",
  "totalAmount": 12990
}
```

En la página de confirmación (`/compra` en el frontend) se muestra esta información como resumen de la compra.

---

## 10. Seguridad y JWT

* El login en `ms-usuarios` devuelve un **JWT**.
* El frontend lo guarda en `localStorage` (`ms_auth_state`).
* Las peticiones a endpoints protegidos incluyen:

```http
Authorization: Bearer <JWT>
```

* `ms-cart` además exige `X-User-Id` con el ID del usuario.
* `ms-orders` obtiene el `userId` desde el JWT (claim `sub`) para asociar las órdenes.

Roles:

* `CUSTOMER` → acceso a endpoints de cliente (`/orders`, `/cart`, etc.).
* `ADMIN` → acceso adicional a endpoints `/admin/...` en `ms-usuarios` y `ms-orders`.

---

## 11. Integración con frontend React

El frontend del proyecto se encuentra en el repositorio:

* [`RDTmc/react-mil-sabores`](https://github.com/RDTmc/react-mil-sabores)

Ramas relevantes:

* `master` → versión estable.
* `feature/react-api` → integración inicial API.
* `React+Microservicios` → rama preparada para la defensa con:

  * Autenticación completa.
  * Carrito conectado a `ms-cart`.
  * Checkout y promociones conectadas a `ms-orders`.
  * Panel admin (usuarios + órdenes recientes).

Variables `.env` en el frontend deben apuntar a las URLs base de este backend (ver sección 6).

---

## 12. Cómo probar rápidamente

1. Levantar PostgreSQL y crear las bases para cada microservicio.

2. Configurar `application.properties` en cada módulo.

3. Ejecutar:

   ```bash
   # En cada microservicio
   mvn spring-boot:run
   ```

4. Verificar Swagger:

   * `http://localhost:8081/swagger-ui/index.html` (productos)
   * `http://localhost:8082/swagger-ui/index.html` (usuarios)
   * `http://localhost:8083/swagger-ui/index.html` (orders)
   * `http://localhost:8084/swagger-ui/index.html` (cart)

5. Levantar frontend React y navegar:

   * `http://localhost:5173/` (home)
   * Flujo: **catálogo → producto → carrito → pedido → compra**.

---


