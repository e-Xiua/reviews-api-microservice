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

import com.iwellness.reviews.dto.RatingDTO;
import com.iwellness.reviews.dto.ReviewRequestDTO;
import com.iwellness.reviews.dto.ReviewResponseDTO;
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

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(
            @Valid @RequestBody ReviewRequestDTO requestDTO,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /api/reviews - User: {}, Entity: {} ({})", 
                userId, requestDTO.getEntityType(), requestDTO.getEntityId());
        ReviewResponseDTO response = reviewService.createReview(requestDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequestDTO requestDTO,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("PUT /api/reviews/{} - User: {}", reviewId, userId);
        ReviewResponseDTO response = reviewService.updateReview(reviewId, requestDTO, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("DELETE /api/reviews/{} - User: {}", reviewId, userId);
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<Page<ReviewResponseDTO>> getReviewsByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        log.info("GET /api/reviews/entity/{}/{} - Page: {}, Size: {}", entityType, entityId, page, size);
        Page<ReviewResponseDTO> reviews = reviewService.getReviewsByEntity(entityId, entityType, page, size, sortBy);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/entity/{entityType}/{entityId}/rating")
    public ResponseEntity<RatingDTO> getEntityRating(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        log.info("GET /api/reviews/entity/{}/{}/rating", entityType, entityId);
        RatingDTO rating = reviewService.getRatingByEntity(entityId, entityType);
        return ResponseEntity.ok(rating);
    }

    @GetMapping("/entity/{entityType}/{entityId}/recent")
    public ResponseEntity<List<ReviewResponseDTO>> getRecentReviews(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam(defaultValue = "5") int limit) {
        log.info("GET /api/reviews/entity/{}/{}/recent - Limit: {}", entityType, entityId, limit);
        List<ReviewResponseDTO> reviews = reviewService.getRecentReviewsByEntity(entityId, entityType, limit);
        return ResponseEntity.ok(reviews);
    }

}

