# Tests de Integración con Testcontainers

## Configuración Completada ✅

Los tests de integración están configurados para usar **Testcontainers** con PostgreSQL 15.

### Archivos Creados:

1. **`IntegrationTestConfig.java`** - Configuración de mocks para servicios externos
   - Mockea `UserApiClient`
   - Mockea `ServicioApiClient`  
   - Mockea `RabbitTemplate`

2. **`ReviewIntegrationTest.java`** - Suite de tests de integración
   - ✅ `testCompleteReviewFlow()` - Ciclo completo CRUD
   - ✅ `testDuplicateReviewPrevention()` - Prevención de duplicados
   - ✅ `testUnauthorizedUpdate()` - Control de acceso

### Configuración de Testcontainers:

```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("test_reviews")
        .withUsername("test")
        .withPassword("test");

@DynamicPropertySource
static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.flyway.enabled", () -> "true");
    registry.add("spring.flyway.clean-disabled", () -> "false");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
}
```

## Requisitos Previos

1. **Docker debe estar corriendo** en el sistema
2. Docker daemon debe ser accesible por el usuario actual
3. Permisos para descargar imágenes Docker

## Ejecución de Tests

### Tests Unitarios (Rápidos - No requieren Docker)
```bash
mvn test -Dtest="ReviewServiceTest,ReviewControllerTest"
```

**Resultado:** ✅ 9/9 tests pasando
- ReviewServiceTest: 4/4 ✅
- ReviewControllerTest: 5/5 ✅

### Tests de Integración (Requieren Docker)
```bash
# Todos los tests de integración
mvn test -Dtest="ReviewIntegrationTest"

# Solo un test específico
mvn test -Dtest="ReviewIntegrationTest#testCompleteReviewFlow"
```

### Todos los Tests
```bash
mvn verify
```

## Solución de Problemas

### Error: "Cannot connect to Docker daemon"
```bash
# Verificar que Docker está corriendo
sudo systemctl status docker

# Iniciar Docker si está detenido
sudo systemctl start docker

# Agregar usuario al grupo docker (requiere logout/login)
sudo usermod -aG docker $USER
```

### Error: "Port already in use"
Los tests de integración usan puertos aleatorios (`RANDOM_PORT`), no debería haber conflictos.

### Error: "Testcontainers timeout"
Testcontainers necesita descargar la imagen de PostgreSQL la primera vez:
```bash
# Pre-descargar la imagen
docker pull postgres:15-alpine
```

### Tests muy lentos
La primera ejecución es lenta porque descarga la imagen Docker. Ejecuciones posteriores son más rápidas.

## Arquitectura de Tests

```
tests/
├── unit/                          # Tests Unitarios (Rápidos)
│   ├── ReviewServiceTest          # Lógica de negocio
│   └── ReviewControllerTest       # Endpoints REST
│
├── integration/                   # Tests de Integración (Testcontainers)
│   ├── IntegrationTestConfig      # Mocks compartidos
│   └── ReviewIntegrationTest      # Tests E2E con BD real
│
└── repository/                    # Tests de Repositorio (BD en memoria)
    └── ReviewRepositoryTest       # Queries JPA
```

## Configuraciones de Profile

### Profile: `test` (usado por Testcontainers)
```yaml
spring:
  config:
    activate:
      on-profile: test
  datasource:
    # Sobrescrito dinámicamente por Testcontainers
    url: jdbc:postgresql://localhost:5432/test_db
  jpa:
    hibernate:
      ddl-auto: none  # Flyway maneja el esquema
  flyway:
    enabled: true
    clean-disabled: false
```

## Cobertura de Tests

**Tests Unitarios:**
- ✅ Creación de reviews
- ✅ Actualización de reviews  
- ✅ Eliminación de reviews
- ✅ Validación de duplicados
- ✅ Control de autorización
- ✅ Obtención de ratings
- ✅ Manejo de errores (headers faltantes, etc.)

**Tests de Integración:**
- ✅ Flujo completo CRUD con BD real
- ✅ Prevención de reviews duplicadas
- ✅ Control de acceso (usuarios no autorizados)
- ✅ Integración con Flyway migrations
- ✅ Serialización/deserialización JSON
- ✅ Validaciones de negocio end-to-end

## Próximos Pasos

1. ✅ Tests unitarios configurados y funcionando
2. ✅ Tests de integración configurados con Testcontainers
3. ⏳ Ejecutar tests de integración (requiere Docker activo)
4. ⏳ Agregar tests de repository si es necesario
5. ⏳ Configurar CI/CD para ejecutar tests automáticamente
