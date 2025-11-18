package com.iwellness.reviews.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iwellness.reviews.client.UserApiClient;
import com.iwellness.reviews.dto.RatingDTO;
import com.iwellness.reviews.dto.ReviewRequestDTO;
import com.iwellness.reviews.dto.ReviewResponseDTO;
import com.iwellness.reviews.dto.UsuarioDTO;
import com.iwellness.reviews.entity.Review;
import com.iwellness.reviews.exception.DuplicateReviewException;
import com.iwellness.reviews.exception.ReviewNotFoundException;
import com.iwellness.reviews.exception.UnauthorizedReviewAccessException;
import com.iwellness.reviews.model.Reviewable;
import com.iwellness.reviews.publisher.ReviewEventPublisher;
import com.iwellness.reviews.repository.ReviewRepository;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewEventPublisher reviewEventPublisher;
    private final UserApiClient userApiClient;
    private final ReviewableService reviewableService; // New service to fetch reviewable entities

    public ReviewService(ReviewRepository reviewRepository, 
                        ReviewEventPublisher reviewEventPublisher, 
                        UserApiClient userApiClient,
                        ReviewableService reviewableService) {
        this.reviewRepository = reviewRepository;
        this.reviewEventPublisher = reviewEventPublisher;
        this.userApiClient = userApiClient;
        this.reviewableService = reviewableService;
    }

    /**
     * Unified method to create a review for any Reviewable entity
     */
    @Transactional
    public ReviewResponseDTO createReview(ReviewRequestDTO requestDTO, Long userId) {
        log.info("Creating review for {} {} by user {}", 
                requestDTO.getEntityType(), requestDTO.getEntityId(), userId);

        // Verify the entity exists and is reviewable
        Reviewable reviewable = reviewableService.getReviewableEntity(
                requestDTO.getEntityType(), requestDTO.getEntityId());
        
        if (reviewable == null) {
            throw new IllegalArgumentException("Entity not found or not reviewable");
        }

        // Check for duplicate review
        if (reviewRepository.existsByEntityTypeAndEntityIdAndUserId(
                requestDTO.getEntityType().toString(), requestDTO.getEntityId(), userId)) {
            throw new DuplicateReviewException("User already has a review for this entity");
        }

        Review review = Review.builder()
                .entityType(requestDTO.getEntityType().toString())
                .entityId(requestDTO.getEntityId())
                .userId(userId)
                .rating(requestDTO.getRating())
                .comment(requestDTO.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("Review created with ID: {}", savedReview.getId());

        // Publish events
        reviewEventPublisher.publishReviewCreated(savedReview);
        
        return mapToResponseDTO(savedReview);
    }

    /**
     * Unified method to update any review
     */
    @Transactional
    public ReviewResponseDTO updateReview(Long reviewId, ReviewRequestDTO requestDTO, Long userId) {
        log.info("Updating review {} by user {}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found"));

        if (!review.getUserId().equals(userId)) {
            throw new UnauthorizedReviewAccessException("No permission to modify this review");
        }

        Integer oldRating = review.getRating();
        review.setRating(requestDTO.getRating());
        review.setComment(requestDTO.getComment());

        Review updatedReview = reviewRepository.save(review);
        log.info("Review updated: {}", reviewId);

        // Publish events
        reviewEventPublisher.publishReviewUpdated(updatedReview);

        return mapToResponseDTO(updatedReview);
    }

    /**
     * Unified method to delete any review
     */
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        log.info("Deleting review {} by user {}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found"));

        if (!review.getUserId().equals(userId)) {
            throw new UnauthorizedReviewAccessException("No permission to delete this review");
        }

        String entityType = review.getEntityType();
        Long entityId = review.getEntityId();
        
        reviewRepository.delete(review);
        log.info("Review deleted: {}", reviewId);

    }

    /**
     * Get a review by its ID
     */
    @Transactional(readOnly = true)
    public ReviewResponseDTO getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewNotFoundException("Review not found with ID: " + reviewId));
        
        return mapToResponseDTO(review);
    }

    /**
     * Unified method to get reviews by entity
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> getReviewsByEntity(Long entityId, String entityType, 
                                                     int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        Page<Review> reviews = reviewRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
        return reviews.map(this::mapToResponseDTO);
    }

    /**
     * Unified method to get rating for any entity
     */
    @Transactional(readOnly = true)
    public RatingDTO getRatingByEntity(Long entityId, String entityType) {
        Double averageRating = reviewRepository.calculateAverageRatingByEntity(entityType, entityId);
        Long totalReviews = reviewRepository.countByEntityTypeAndEntityId(entityType, entityId);

        RatingDTO.RatingDistribution distribution = RatingDTO.RatingDistribution.builder()
                .fiveStars(reviewRepository.countByEntityTypeAndEntityIdAndRating(entityType, entityId, 5))
                .fourStars(reviewRepository.countByEntityTypeAndEntityIdAndRating(entityType, entityId, 4))
                .threeStars(reviewRepository.countByEntityTypeAndEntityIdAndRating(entityType, entityId, 3))
                .twoStars(reviewRepository.countByEntityTypeAndEntityIdAndRating(entityType, entityId, 2))
                .oneStar(reviewRepository.countByEntityTypeAndEntityIdAndRating(entityType, entityId, 1))
                .build();

        return RatingDTO.builder()
                .entityId(entityId)
                .entityType(entityType)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalReviews(totalReviews)
                .distribution(distribution)
                .build();
    }

    /**
     * Unified method to get recent reviews
     */
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getRecentReviewsByEntity(Long entityId, String entityType, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Review> reviews = reviewRepository.findRecentByEntity(entityType, entityId, pageable);
        return reviews.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // Keep existing methods for user reviews, etc.

    /**
     * Unified mapping to response DTO
     */
    private ReviewResponseDTO mapToResponseDTO(Review review) {
        UsuarioDTO user = null;
        try {
            user = userApiClient.findById(review.getUserId());
        } catch (FeignException e) {
            log.error("Error fetching user data for ID: {}. Cause: {}", review.getUserId(), e.getMessage());
        }

        String username = (user != null && user.getNombre() != null) 
                ? user.getNombre() + " " + user.getApellido() 
                : "Usuario Anónimo";
        
        String userImageUrl = (user != null) ? user.getFoto() : null;
        
        return ReviewResponseDTO.builder()
                .id(review.getId())
                .entityId(review.getEntityId())
                .entityType(review.getEntityType())
                .userId(review.getUserId())
                .nombre(username)
                .foto(userImageUrl)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
