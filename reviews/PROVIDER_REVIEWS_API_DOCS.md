# API de Reseñas - Documentación de Endpoints para Proveedores

## 📋 Resumen

El microservicio de reseñas ahora soporta reseñas tanto para **servicios** como para **proveedores**. Esta actualización permite a los usuarios calificar y comentar sobre proveedores de servicios además de los servicios individuales.

## 🔄 Cambios en el Backend

### Modelo de Datos Actualizado

La entidad `Review` ahora incluye:

```java
@Entity
@Table(name = "reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"entity_type", "entity_id", "user_id"})
})
public class Review {
    private Long id;
    private String entityType;  // "servicio" o "proveedor"
    private Long entityId;      // ID del servicio o proveedor
    private Long serviceId;     // Mantenido por compatibilidad
    private Long userId;
    private Integer rating;     // 1-5
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### Migración de Base de Datos

Ejecutar el script SQL en `src/main/resources/db/migration/V2__add_provider_reviews_support.sql`:

```sql
-- Agrega entity_type y entity_id
-- Migra datos existentes a entityType='servicio'
-- Crea índices para rendimiento
-- Agrega constraint único (entity_type, entity_id, user_id)
```

## 🚀 Endpoints de la API

### Base URL: `http://localhost:8084/api/reviews`

---

## 📝 Reseñas de Proveedores

### 1. Crear Reseña de Proveedor

```http
POST /api/reviews/provider
Content-Type: application/json
X-User-Id: {userId}

{
  "providerId": 123,
  "rating": 5,
  "comment": "Excelente proveedor, muy profesional"
}
```

**Respuesta (201 Created):**

```json
{
  "id": 456,
  "serviceId": null,
  "userId": 789,
  "username": "Juan Pérez",
  "userImageUrl": "https://example.com/avatar.jpg",
  "rating": 5,
  "comment": "Excelente proveedor, muy profesional",
  "createdAt": "2025-10-22T10:30:00",
  "updatedAt": "2025-10-22T10:30:00"
}
```

**Códigos de Error:**
- `409 Conflict`: El usuario ya tiene una reseña para este proveedor
- `401 Unauthorized`: Header X-User-Id faltante o inválido
- `400 Bad Request`: Datos de validación incorrectos

---

### 2. Obtener Reseñas de un Proveedor

```http
GET /api/reviews/provider/{providerId}?page=0&size=10&sortBy=createdAt
```

**Parámetros de Query:**
- `page` (default: 0): Número de página
- `size` (default: 10): Elementos por página
- `sortBy` (default: createdAt): Campo de ordenamiento

**Respuesta (200 OK):**

```json
{
  "content": [
    {
      "id": 1,
      "userId": 123,
      "username": "María García",
      "userImageUrl": "https://...",
      "rating": 4,
      "comment": "Muy buen servicio",
      "createdAt": "2025-10-20T15:30:00",
      "updatedAt": "2025-10-20T15:30:00"
    }
  ],
  "totalElements": 25,
  "totalPages": 3,
  "size": 10,
  "number": 0
}
```

---

### 3. Obtener Rating de un Proveedor

```http
GET /api/reviews/provider/{providerId}/rating
```

**Respuesta (200 OK):**

```json
{
  "providerId": 123,
  "averageRating": 4.5,
  "totalReviews": 25,
  "distribution": {
    "fiveStars": 12,
    "fourStars": 8,
    "threeStars": 3,
    "twoStars": 1,
    "oneStar": 1
  }
}
```

---

### 4. Obtener Reseñas Recientes de un Proveedor

```http
GET /api/reviews/provider/{providerId}/recent?limit=5
```

**Parámetros de Query:**
- `limit` (default: 5): Número de reseñas recientes a obtener

**Respuesta (200 OK):**

```json
[
  {
    "id": 1,
    "userId": 123,
    "username": "Ana López",
    "rating": 5,
    "comment": "Increíble experiencia",
    "createdAt": "2025-10-22T09:00:00"
  }
]
```

---

## 🔧 Reseñas de Servicios (Existentes)

Los endpoints para servicios permanecen sin cambios:

### 1. Crear Reseña de Servicio

```http
POST /api/reviews
X-User-Id: {userId}

{
  "serviceId": 123,
  "rating": 5,
  "comment": "Excelente servicio"
}
```

### 2. Actualizar Reseña (Funciona para servicios y proveedores)

```http
PUT /api/reviews/{reviewId}
X-User-Id: {userId}

{
  "serviceId": 123,  // o providerId
  "rating": 4,
  "comment": "Actualizado..."
}
```

### 3. Eliminar Reseña (Funciona para servicios y proveedores)

```http
DELETE /api/reviews/{reviewId}
X-User-Id: {userId}
```

### 4. Obtener Reseñas de un Servicio

```http
GET /api/reviews/service/{serviceId}?page=0&size=10
```

### 5. Obtener Rating de un Servicio

```http
GET /api/reviews/service/{serviceId}/rating
```

### 6. Obtener Reseñas Recientes de un Servicio

```http
GET /api/reviews/service/{serviceId}/recent?limit=5
```

---

## 💻 Frontend - Uso del ReviewsCallService

### Crear Reseña de Proveedor

```typescript
import { ReviewsCallService } from '@shared/services/reviews-call.service';

constructor(private reviewsService: ReviewsCallService) {}

crearReseñaProveedor(providerId: number) {
  const submission: ReviewSubmission = {
    entityType: 'proveedor',
    entityId: providerId,
    rating: 5,
    comment: 'Excelente proveedor'
  };

  this.reviewsService.createReviewForProvider(submission)
    .subscribe({
      next: (review) => {
        console.log('Reseña creada:', review);
        Swal.fire('Éxito', 'Reseña publicada', 'success');
      },
      error: (err) => {
        console.error('Error:', err);
        Swal.fire('Error', err.message, 'error');
      }
    });
}
```

### Obtener Reseñas de un Proveedor

```typescript
cargarReseñasProveedor(providerId: number) {
  this.reviewsService.getAllReviewsForProvider(providerId)
    .subscribe({
      next: (reviews) => {
        this.reviews = reviews;
        console.log(`${reviews.length} reseñas cargadas`);
      },
      error: (err) => {
        console.error('Error al cargar reseñas:', err);
      }
    });
}
```

### Obtener Rating de un Proveedor

```typescript
cargarRatingProveedor(providerId: number) {
  this.reviewsService.getProviderRating(providerId)
    .subscribe({
      next: (rating) => {
        this.averageRating = rating.averageRating;
        this.totalReviews = rating.totalReviews;
        console.log(`Rating: ${rating.averageRating}/5 (${rating.totalReviews} reseñas)`);
      },
      error: (err) => {
        console.error('Error al cargar rating:', err);
      }
    });
}
```

### Obtener Reseñas Recientes

```typescript
cargarReseñasRecientes(providerId: number) {
  this.reviewsService.getRecentProviderReviews(providerId, 5)
    .subscribe({
      next: (reviews) => {
        this.recentReviews = reviews;
      },
      error: (err) => {
        console.error('Error:', err);
      }
    });
}
```

---

## 🧪 Testing con CURL

### Crear Reseña de Proveedor

```bash
curl -X POST http://localhost:8084/api/reviews/provider \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "providerId": 123,
    "rating": 5,
    "comment": "Excelente proveedor"
  }'
```

### Obtener Reseñas de Proveedor

```bash
curl -X GET "http://localhost:8084/api/reviews/provider/123?page=0&size=10"
```

### Obtener Rating de Proveedor

```bash
curl -X GET http://localhost:8084/api/reviews/provider/123/rating
```

### Obtener Reseñas Recientes

```bash
curl -X GET "http://localhost:8084/api/reviews/provider/123/recent?limit=5"
```

---

## ✅ Validaciones

### ProviderReviewRequestDTO

```java
public class ProviderReviewRequestDTO {
    @NotNull(message = "El ID del proveedor es obligatorio")
    private Long providerId;

    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    private Integer rating;

    @Size(max = 1000, message = "El comentario no puede exceder 1000 caracteres")
    private String comment;
}
```

---

## 🔐 Autenticación

Todos los endpoints que modifican datos requieren el header:

```
X-User-Id: {userId}
```

Este header identifica al usuario que está creando/actualizando/eliminando la reseña.

---

## 📊 Eventos Publicados (RabbitMQ)

El servicio publica eventos a través de RabbitMQ cuando:

1. **Se crea una reseña**: `review.created`
2. **Se actualiza una reseña**: `review.updated`
3. **Se elimina una reseña**: `review.deleted`
4. **Cambia el rating**: `rating.changed`

Estos eventos pueden ser consumidos por otros microservicios para actualizar estadísticas, enviar notificaciones, etc.

---

## 🚦 Estados y Códigos HTTP

| Código | Descripción |
|--------|-------------|
| 200 | OK - Operación exitosa |
| 201 | Created - Reseña creada |
| 204 | No Content - Reseña eliminada |
| 400 | Bad Request - Datos inválidos |
| 401 | Unauthorized - Falta X-User-Id |
| 403 | Forbidden - No autorizado a modificar esta reseña |
| 404 | Not Found - Reseña no encontrada |
| 409 | Conflict - Ya existe una reseña para esta entidad |

---

## 📝 Notas Importantes

1. **Unicidad**: Un usuario solo puede tener una reseña por proveedor/servicio
2. **Eliminación**: Solo el propietario puede modificar/eliminar su reseña
3. **Rating**: Escala de 1 a 5 estrellas
4. **Comentario**: Máximo 1000 caracteres
5. **Compatibilidad**: Los endpoints legacy de servicios siguen funcionando

---

## 🔄 Migración de Código Existente

Si tienes código que usa los endpoints antiguos de servicios, **no necesitas cambiarlo**. Los endpoints legacy siguen funcionando. Solo agrega los nuevos endpoints de proveedores cuando los necesites.

---

## 📞 Soporte

Para reportar problemas o solicitar funcionalidades adicionales, contacta al equipo de desarrollo.
