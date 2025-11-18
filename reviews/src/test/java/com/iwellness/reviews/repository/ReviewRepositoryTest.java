package com.iwellness.reviews.repository;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.iwellness.reviews.entity.Review;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReviewRepositoryTest {

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
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        
        // Enable Flyway for test schema
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        
        // Validate against Hibernate
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    @DisplayName("Should save and retrieve review with PostgreSQL")
    void shouldSaveAndRetrieveReview() {
        Review review = Review.builder()
                .entityType("SERVICE")
                .entityId(1L)
                .userId(100L)
                .rating(5)
                .comment("Great service!")
                .build();

        Review saved = reviewRepository.save(review);
        Review found = reviewRepository.findById(saved.getId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getRating()).isEqualTo(5);
        assertThat(found.getComment()).isEqualTo("Great service!");
    }

    @Test
    @DisplayName("Should find reviews by entity with pagination")
    void shouldFindReviewsByEntityWithPagination() {
        // Given
        reviewRepository.save(Review.builder().entityType("SERVICE").entityId(1L).userId(1L).rating(5).build());
        reviewRepository.save(Review.builder().entityType("SERVICE").entityId(1L).userId(2L).rating(4).build());
        reviewRepository.save(Review.builder().entityType("PROVIDER").entityId(10L).userId(3L).rating(5).build());

        // When
        Page<Review> page = reviewRepository.findByEntityTypeAndEntityId("SERVICE", 1L, PageRequest.of(0, 10));

        // Then
        assertThat(page).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should prevent duplicate reviews via unique constraint")
    void shouldPreventDuplicateReviews() {
        Review review1 = Review.builder()
                .entityType("SERVICE")
                .entityId(1L)
                .userId(100L)
                .rating(5)
                .build();

        Review review2 = Review.builder()
                .entityType("SERVICE")
                .entityId(1L)
                .userId(100L)  // Same user, same entity
                .rating(3)
                .build();

        reviewRepository.save(review1);

        assertThatThrownBy(() -> reviewRepository.save(review2))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("constraint");
    }
}
