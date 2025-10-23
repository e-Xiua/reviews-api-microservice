# Reviews API - e-Xiua

Microservicio de gestión de reseñas y calificaciones para servicios turísticos. Permite a los usuarios crear, actualizar, eliminar y consultar reseñas, así como calcular estadísticas de calificación.

## 🚀 Características

- **CRUD de Reseñas**: Crear, leer, actualizar y eliminar reseñas
- **Sistema de Calificación**: Rating de 1 a 5 estrellas
- **Estadísticas**: Cálculo de promedio y distribución de calificaciones
- **Validación**: Prevención de reseñas duplicadas por usuario/servicio
- **Autorización**: Control de acceso para modificación/eliminación
- **Paginación**: Consultas paginadas para mejor rendimiento
- **Eventos RabbitMQ**: Publicación de eventos de reseñas
- **Integración Feign**: Comunicación con otros microservicios

## 📋 Requisitos Previos

- Java 21
- Maven 3.9+
- H2 Database (desarrollo) o PostgreSQL (producción)
- RabbitMQ
- Docker (opcional)

## 🛠️ Configuración

### Variables de Entorno

```bash
# Database
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/reviews_db"
export SPRING_DATASOURCE_USERNAME="postgres"
export SPRING_DATASOURCE_PASSWORD="tu-password"

# RabbitMQ
export SPRING_RABBITMQ_HOST="localhost"
export SPRING_RABBITMQ_PORT="5672"
export SPRING_RABBITMQ_USERNAME="guest"
export SPRING_RABBITMQ_PASSWORD="guest"

# JWT
export JWT_SECRET="tu-clave-secreta"
```

### Perfiles de Spring

- **default**: Base de datos H2 en memoria (desarrollo)
- **postgres**: Base de datos PostgreSQL (producción)

## 📊 Modelo de Datos

### Entidad Review

```java
{
  "id": Long,
  "serviceId": Long,
  "userId": Long,
  "rating": Integer (1-5),
  "comment": String (máx 1000 caracteres),
  "createdAt": LocalDateTime,
  "updatedAt": LocalDateTime
}
```

## 🌐 API Endpoints

### Crear Reseña

```http
POST /api/reviews
Headers: X-User-Id: {userId}
Body: {
  "serviceId": 1,
  "rating": 5,
  "comment": "Excelente servicio"
}
```

### Actualizar Reseña

```http
PUT /api/reviews/{reviewId}
Headers: X-User-Id: {userId}
Body: {
  "serviceId": 1,
  "rating": 4,
  "comment": "Muy buen servicio"
}
```

### Eliminar Reseña

```http
DELETE /api/reviews/{reviewId}
Headers: X-User-Id: {userId}
```

### Obtener Reseña

```http
GET /api/reviews/{reviewId}
```

### Listar Reseñas por Servicio

```http
GET /api/reviews/service/{serviceId}?page=0&size=10&sortBy=createdAt
```

### Listar Reseñas por Usuario

```http
GET /api/reviews/user/{userId}?page=0&size=10
```

### Obtener Estadísticas de Calificación

```http
GET /api/reviews/service/{serviceId}/rating
Response: {
  "serviceId": 1,
  "averageRating": 4.5,
  "totalReviews": 100,
  "distribution": {
    "fiveStars": 60,
    "fourStars": 25,
    "threeStars": 10,
    "twoStars": 3,
    "oneStar": 2
  }
}
```

### Obtener Reseñas Recientes

```http
GET /api/reviews/service/{serviceId}/recent?limit=5
```

## 📨 Eventos RabbitMQ

### Eventos Publicados

| Evento | Routing Key | Descripción |
|--------|-------------|-------------|
| ReviewCreatedEvent | `review.created` | Se creó una nueva reseña |
| ReviewUpdatedEvent | `review.updated` | Se actualizó una reseña |
| ReviewDeletedEvent | `review.deleted` | Se eliminó una reseña |
| RatingChangedEvent | `review.rating.changed` | Cambió la calificación de un servicio |

### Estructura de Eventos

**ReviewCreatedEvent**

```json
{
  "reviewId": 123,
  "serviceId": 1,
  "userId": 456,
  "rating": 5,
  "comment": "Excelente",
  "createdAt": "2024-01-15T10:30:00"
}
```

**RatingChangedEvent**

```json
{
  "serviceId": 1,
  "timestamp": "2024-01-15T10:30:00"
}
```

## 🚀 Ejecución

### Desarrollo Local

```bash
# Compilar
mvn clean install

# Ejecutar con H2
mvn spring-boot:run

# Ejecutar con PostgreSQL
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

## 🧪 Testing

```bash
# Ejecutar tests
mvn test

# Con cobertura
mvn test jacoco:report
```

### Ejemplos de Test

```bash
# Crear reseña
curl -X POST http://localhost:8084/api/reviews \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "serviceId": 1,
    "rating": 5,
    "comment": "Excelente experiencia"
  }'

# Obtener estadísticas
curl http://localhost:8084/api/reviews/service/1/rating
```

## 🔐 Seguridad

- **Header X-User-Id**: Inyectado por el API Gateway desde el JWT
- **Validación de Propiedad**: Solo el creador puede modificar/eliminar su reseña
- **Prevención de Duplicados**: Un usuario solo puede crear una reseña por servicio

## 📊 Base de Datos

### H2 Console (Desarrollo)

- **URL**: `http://localhost:8084/h2-console`
- **JDBC URL**: `jdbc:h2:mem:reviewsdb`
- **Username**: `sa`
- **Password**: (vacío)

### PostgreSQL (Producción)

```sql
CREATE DATABASE reviews_db;

CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT unique_user_service UNIQUE (service_id, user_id)
);

CREATE INDEX idx_reviews_service_id ON reviews(service_id);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);
```

## 🐛 Troubleshooting

### Error: "Ya existe una reseña de este usuario para este servicio"

- Un usuario solo puede crear una reseña por servicio
- Usar el endpoint PUT para actualizar la reseña existente

### Error: "No tienes permiso para modificar esta reseña"

- Verifica que el `X-User-Id` coincida con el creador de la reseña

### Error: RabbitMQ Connection

- Asegúrate que RabbitMQ esté ejecutándose en el puerto 5672
- Verifica las credenciales de conexión

## 📦 Dependencias Principales

- Spring Boot 3.2.0
- Spring Data JPA
- Spring AMQP (RabbitMQ)
- Spring Cloud OpenFeign 4.1.0
- PostgreSQL Driver
- H2 Database
- JJWT 0.12.3
- Lombok

## 🤝 Integración con Otros Servicios

### API Gateway

- Todas las solicitudes deben pasar por el gateway en `http://localhost:8765`
- El gateway inyecta el header `X-User-Id` desde el JWT

### Providers API

- Puede consumir eventos de cambio de calificación para actualizar servicios

### Data Services

- Puede consumir todos los eventos de reseñas para analytics

## 📝 Arquitectura

```
┌─────────────────────────────────────┐
│         Reviews API (8084)          │
│  ┌─────────────────────────────┐   │
│  │  ReviewController           │   │
│  │  ReviewService              │   │
│  │  ReviewRepository           │   │
│  │  ReviewEventPublisher       │   │
│  └─────────────────────────────┘   │
└──────┬────────────────┬─────────────┘
       │                │
       ▼                ▼
  ┌─────────┐    ┌──────────────┐
  │   H2 /  │    │   RabbitMQ   │
  │ Postgres│    │   Exchange   │
  └─────────┘    └──────────────┘
```

## 🤝 Contribución

1. Fork el repositorio
2. Crea una rama para tu feature
3. Commit tus cambios
4. Push a la rama
5. Abre un Pull Request

## 📄 Licencia

Proyecto e-Xiua - Todos los derechos reservados
