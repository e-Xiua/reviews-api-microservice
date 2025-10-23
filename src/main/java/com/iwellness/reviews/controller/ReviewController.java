package com.iwellness.reviews.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iwellness.reviews.dto.ProviderRatingDTO;
import com.iwellness.reviews.dto.ProviderReviewRequestDTO;
import com.iwellness.reviews.dto.ReviewRequestDTO;
import com.iwellness.reviews.dto.ReviewResponseDTO;
import com.iwellness.reviews.dto.ServiceRatingDTO;
import com.iwellness.reviews.service.ReviewService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Crea una nueva reseña
     */
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(
            @Valid @RequestBody ReviewRequestDTO requestDTO,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /api/reviews - Usuario: {}, Servicio: {}", userId, requestDTO.getServiceId());
        ReviewResponseDTO response = reviewService.createReview(requestDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Actualiza una reseña existente
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequestDTO requestDTO,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("PUT /api/reviews/{} - Usuario: {}", reviewId, userId);
        ReviewResponseDTO response = reviewService.updateReview(reviewId, requestDTO, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina una reseña
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("DELETE /api/reviews/{} - Usuario: {}", reviewId, userId);
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene una reseña por ID
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> getReview(@PathVariable Long reviewId) {
        log.info("GET /api/reviews/{}", reviewId);
        ReviewResponseDTO response = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene todas las reseñas de un servicio
     */
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<Page<ReviewResponseDTO>> getReviewsByService(
            @PathVariable Long serviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        log.info("GET /api/reviews/service/{} - Page: {}, Size: {}", serviceId, page, size);
        Page<ReviewResponseDTO> reviews = reviewService.getReviewsByServiceId(serviceId, page, size, sortBy);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Obtiene todas las reseñas de un usuario
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ReviewResponseDTO>> getReviewsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/reviews/user/{} - Page: {}, Size: {}", userId, page, size);
        Page<ReviewResponseDTO> reviews = reviewService.getReviewsByUserId(userId, page, size);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Obtiene las estadísticas de calificación de un servicio
     */
    @GetMapping("/service/{serviceId}/rating")
    public ResponseEntity<ServiceRatingDTO> getServiceRating(@PathVariable Long serviceId) {
        log.info("GET /api/reviews/service/{}/rating", serviceId);
        ServiceRatingDTO rating = reviewService.getServiceRating(serviceId);
        return ResponseEntity.ok(rating);
    }

    /**
     * Obtiene las reseñas más recientes de un servicio
     */
    @GetMapping("/service/{serviceId}/recent")
    public ResponseEntity<List<ReviewResponseDTO>> getRecentReviews(
            @PathVariable Long serviceId,
            @RequestParam(defaultValue = "5") int limit) {
        log.info("GET /api/reviews/service/{}/recent - Limit: {}", serviceId, limit);
        List<ReviewResponseDTO> reviews = reviewService.getRecentReviews(serviceId, limit);
        return ResponseEntity.ok(reviews);
    }

    // ==================== ENDPOINTS PARA RESEÑAS DE PROVEEDORES ====================

    /**
     * Crea una nueva reseña para un proveedor
     */
    @PostMapping("/provider")
    public ResponseEntity<ReviewResponseDTO> createProviderReview(
            @Valid @RequestBody ProviderReviewRequestDTO requestDTO,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /api/reviews/provider - Usuario: {}, Proveedor: {}", userId, requestDTO.getProviderId());
        ReviewResponseDTO response = reviewService.createProviderReview(requestDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene todas las reseñas de un proveedor
     */
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<Page<ReviewResponseDTO>> getReviewsByProvider(
            @PathVariable Long providerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        log.info("GET /api/reviews/provider/{} - Page: {}, Size: {}", providerId, page, size);
        Page<ReviewResponseDTO> reviews = reviewService.getReviewsByProviderId(providerId, page, size, sortBy);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Obtiene las estadísticas de calificación de un proveedor
     */
    @GetMapping("/provider/{providerId}/rating")
    public ResponseEntity<ProviderRatingDTO> getProviderRating(@PathVariable Long providerId) {
        log.info("GET /api/reviews/provider/{}/rating", providerId);
        ProviderRatingDTO rating = reviewService.getProviderRating(providerId);
        return ResponseEntity.ok(rating);
    }

    /**
     * Obtiene las reseñas más recientes de un proveedor
     */
    @GetMapping("/provider/{providerId}/recent")
    public ResponseEntity<List<ReviewResponseDTO>> getRecentProviderReviews(
            @PathVariable Long providerId,
            @RequestParam(defaultValue = "5") int limit) {
        log.info("GET /api/reviews/provider/{}/recent - Limit: {}", providerId, limit);
        List<ReviewResponseDTO> reviews = reviewService.getRecentProviderReviews(providerId, limit);
        return ResponseEntity.ok(reviews);
    }
}
