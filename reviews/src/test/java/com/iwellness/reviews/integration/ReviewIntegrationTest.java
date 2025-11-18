package com.iwellness.reviews.integration;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.iwellness.reviews.client.ServicioApiClient;
import com.iwellness.reviews.client.UserApiClient;
import com.iwellness.reviews.dto.ServicioDTO;
import com.iwellness.reviews.dto.UsuarioDTO;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@Import(IntegrationTestConfig.class)
class ReviewIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserApiClient userApiClient;

    @Autowired
    private ServicioApiClient servicioApiClient;

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

    @BeforeAll
    static void runMigrations() {
        // Ejecutar Flyway manualmente para asegurar que las tablas se crean
        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .cleanDisabled(false)  // Permitir limpiar en tests
                .load();
        
        flyway.clean();  // Limpiar base de datos antes de migrar
        flyway.migrate(); // Ejecutar migraciones
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/reviews";
        
        // Configure mocks for each test
        setupUserApiClientMocks();
        setupServicioApiClientMocks();
    }

    private void setupUserApiClientMocks() {
        UsuarioDTO mockUser = new UsuarioDTO();
        mockUser.setId(100L);
        mockUser.setNombre("Test");
        mockUser.setApellido("User");
        mockUser.setFoto("https://example.com/photo.jpg");

        UsuarioDTO mockUser101 = new UsuarioDTO();
        mockUser101.setId(101L);
        mockUser101.setNombre("Test");
        mockUser101.setApellido("User101");
        mockUser101.setFoto("https://example.com/photo101.jpg");

        UsuarioDTO mockUser102 = new UsuarioDTO();
        mockUser102.setId(102L);
        mockUser102.setNombre("Test");
        mockUser102.setApellido("User102");
        mockUser102.setFoto("https://example.com/photo102.jpg");

        UsuarioDTO mockUser103 = new UsuarioDTO();
        mockUser103.setId(103L);
        mockUser103.setNombre("Test");
        mockUser103.setApellido("User103");
        mockUser103.setFoto("https://example.com/photo103.jpg");

        when(userApiClient.findById(100L)).thenReturn(mockUser);
        when(userApiClient.findById(101L)).thenReturn(mockUser101);
        when(userApiClient.findById(102L)).thenReturn(mockUser102);
        when(userApiClient.findById(103L)).thenReturn(mockUser103);
    }

    private void setupServicioApiClientMocks() {
        ServicioDTO mockService1 = new ServicioDTO();
        mockService1.setIdServicio(1L);
        mockService1.setIdProveedor(1L);
        mockService1.setNombre("Test Service 1");
        mockService1.setDescripcion("Integration Test Service");
        mockService1.setPrecio(100.0);
        mockService1.setEstado(true);

        ServicioDTO mockService2 = new ServicioDTO();
        mockService2.setIdServicio(2L);
        mockService2.setIdProveedor(1L);
        mockService2.setNombre("Test Service 2");
        mockService2.setDescripcion("Integration Test Service 2");
        mockService2.setPrecio(150.0);
        mockService2.setEstado(true);

        ServicioDTO mockService3 = new ServicioDTO();
        mockService3.setIdServicio(3L);
        mockService3.setIdProveedor(1L);
        mockService3.setNombre("Test Service 3");
        mockService3.setDescripcion("Integration Test Service 3");
        mockService3.setPrecio(200.0);
        mockService3.setEstado(true);

        when(servicioApiClient.getServicioById(1L)).thenReturn(mockService1);
        when(servicioApiClient.getServicioById(2L)).thenReturn(mockService2);
        when(servicioApiClient.getServicioById(3L)).thenReturn(mockService3);
    }

    @Test
    @DisplayName("Integration Test: Create and Retrieve Review Flow")
    void testCompleteReviewFlow() {
        String requestBody = """
                {
                    "entityType": "SERVICE",
                    "entityId": 1,
                    "rating": 5,
                    "comment": "Excellent service from integration test!"
                }
                """;

        Integer reviewId = given()
                .contentType(ContentType.JSON)
                .header("X-User-Id", "100")
                .body(requestBody)
            .when()
                .post()
            .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("rating", equalTo(5))
                .body("comment", containsString("integration test"))
                .body("id", notNullValue())
                .extract()
                .path("id");

        given()
            .param("page", 0)
            .param("size", 10)
        .when()
            .get("/entity/SERVICE/1")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("content", hasSize(greaterThanOrEqualTo(1)))
            .body("content[0].id", equalTo(reviewId));

        given()
        .when()
            .get("/entity/SERVICE/1/rating")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("averageRating", equalTo(5.0f))
            .body("totalReviews", greaterThanOrEqualTo(1));

        String updateBody = """
                {
                    "entityType": "SERVICE",
                    "entityId": 1,
                    "rating": 4,
                    "comment": "Updated comment"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .header("X-User-Id", "100")
                .body(updateBody)
        .when()
            .put("/" + reviewId)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("rating", equalTo(4))
            .body("comment", equalTo("Updated comment"));

        given()
                .header("X-User-Id", "100")
        .when()
            .delete("/" + reviewId)
        .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        given()
        .when()
            .get("/" + reviewId)
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Integration Test: Duplicate Review Prevention")
    void testDuplicateReviewPrevention() {
        String requestBody = """
                {
                    "entityType": "SERVICE",
                    "entityId": 2,
                    "rating": 5,
                    "comment": "First review"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .header("X-User-Id", "101")
                .body(requestBody)
        .when()
            .post()
        .then()
            .statusCode(HttpStatus.CREATED.value());

        given()
                .contentType(ContentType.JSON)
                .header("X-User-Id", "101")
                .body(requestBody)
        .when()
            .post()
        .then()
            .statusCode(HttpStatus.CONFLICT.value())
            .body("message", containsString("already has a review"));
    }

    @Test
    @DisplayName("Integration Test: Unauthorized Update")
    void testUnauthorizedUpdate() {
        String requestBody = """
                {
                    "entityType": "SERVICE",
                    "entityId": 3,
                    "rating": 5,
                    "comment": "My review"
                }
                """;

        Integer reviewId = given()
                .contentType(ContentType.JSON)
                .header("X-User-Id", "102")
                .body(requestBody)
        .when()
            .post()
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .path("id");

        String updateBody = """
                {
                    "entityType": "SERVICE",
                    "entityId": 3,
                    "rating": 1,
                    "comment": "Hacked!"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .header("X-User-Id", "103")
                .body(updateBody)
        .when()
            .put("/" + reviewId)
        .then()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }
}
