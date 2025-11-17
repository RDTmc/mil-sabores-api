# API Mil Sabores – Backend

API REST desarrollada con **Spring Boot (Java)** para el proyecto **Mil Sabores**, una tienda de pastelería que expone su catálogo de productos para ser consumido por una aplicación frontend (por ejemplo, la app Android en Kotlin + Jetpack Compose o la versión React).

Esta API permite obtener el listado de productos, así como el detalle individual, entregando la información en formato **JSON** para su uso en aplicaciones cliente.

---

## 1. Tecnologías utilizadas

- **Java 17** (o versión equivalente configurada en el proyecto)
- **Spring Boot** (API REST)
- **Maven** como gestor de dependencias
- **Spring Web**
- (Opcional) **Spring Data JPA** + base de datos relacional (MySQL/H2 u otra, según configuración)
- **JSON** como formato de respuesta

---

## 2. Requisitos previos

Para ejecutar esta API necesitas:

- **Java JDK 21** instalado (o la versión que use el proyecto)
- **Maven 3.x** instalado
- Un IDE Java recomendado:
    - IntelliJ IDEA / Spring Tools Suite / Eclipse
- Acceso a una base de datos compatible (si el proyecto la utiliza)
- Puerto libre **9090** (o el que esté configurado en `application.properties`)

---

## 3. Cómo clonar y ejecutar la API

### 3.1. Clonar el proyecto

```bash
git clone https://github.com/RDTmc/mil-sabores-api.git
cd mil-sabores-api
