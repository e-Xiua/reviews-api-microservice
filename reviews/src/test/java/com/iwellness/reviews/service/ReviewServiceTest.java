package com.iwellness.reviews.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.iwellness.reviews.client.UserApiClient;
import com.iwellness.reviews.dto.RatingDTO;
import com.iwellness.reviews.dto.ReviewRequestDTO;
import com.iwellness.reviews.dto.ReviewResponseDTO;
import com.iwellness.reviews.dto.UsuarioDTO;
import com.iwellness.reviews.entity.Review;
import com.iwellness.reviews.exception.DuplicateReviewException;
import com.iwellness.reviews.exception.UnauthorizedReviewAccessException;
import com.iwellness.reviews.model.EntityType;
import com.iwellness.reviews.model.Reviewable;
import com.iwellness.reviews.publisher.ReviewEventPublisher;
import com.iwellness.reviews.repository.ReviewRepository;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ReviewEventPublisher reviewEventPublisher;
    @Mock
    private UserApiClient userApiClient;
    @Mock
    private ReviewableService reviewableService;

    @InjectMocks
    private ReviewService reviewService;

    private ReviewRequestDTO requestDTO;
    private Review mockReview;
    private UsuarioDTO mockUser;

    @BeforeEach
    void setUp() {
        requestDTO = ReviewRequestDTO.builder()
                .entityType(EntityType.SERVICE)
                .entityId(1L)
                .rating(5)
                .comment("Great service!")
                .build();

        mockReview = Review.builder()
                .id(1L)
                .entityType("SERVICE")
                .entityId(1L)
                .userId(100L)
                .rating(5)
                .comment("Great service!")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        mockUser = new UsuarioDTO();
        mockUser.setNombre("John");
        mockUser.setApellido("Doe");
        mockUser.setFoto("http://example.com/photo.jpg");
    }

    @Test
    @DisplayName("createReview - Success")
    void createReview_Success() {
        // Arrange
        Reviewable mockReviewable = mock(Reviewable.class);
        when(reviewableService.getReviewableEntity(any(EntityType.class), anyLong()))
                .thenReturn(mockReviewable);
        when(reviewRepository.existsByEntityTypeAndEntityIdAndUserId(anyString(), anyLong(), anyLong()))
                .thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(mockReview);
        when(userApiClient.findById(anyLong())).thenReturn(mockUser);

        // Act
        ReviewResponseDTO result = reviewService.createReview(requestDTO, 100L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getNombre()).isEqualTo("John Doe");
        verify(reviewEventPublisher).publishReviewCreated(any(Review.class));
    }

    @Test
    @DisplayName("createReview - Duplicate review throws exception")
    void createReview_DuplicateReview_ThrowsException() {
        when(reviewableService.getReviewableEntity(any(EntityType.class), anyLong()))
                .thenReturn(mock(Reviewable.class));
        when(reviewRepository.existsByEntityTypeAndEntityIdAndUserId(anyString(), anyLong(), anyLong()))
                .thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(requestDTO, 100L))
                .isInstanceOf(DuplicateReviewException.class)
                .hasMessageContaining("already has a review");
    }

    @Test
    @DisplayName("updateReview - Unauthorized user throws exception")
    void updateReview_UnauthorizedUser_ThrowsException() {
        Review existingReview = Review.builder()
                .id(1L)
                .userId(999L) // Different user
                .build();
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.of(existingReview));

        assertThatThrownBy(() -> reviewService.updateReview(1L, requestDTO, 100L))
                .isInstanceOf(UnauthorizedReviewAccessException.class);
    }

    @Test
    @DisplayName("getRatingByEntity - Success")
    void getRatingByEntity_Success() {
        when(reviewRepository.calculateAverageRatingByEntity(anyString(), anyLong()))
                .thenReturn(4.5);
        when(reviewRepository.countByEntityTypeAndEntityId(anyString(), anyLong()))
                .thenReturn(10L);
        when(reviewRepository.countByEntityTypeAndEntityIdAndRating(anyString(), anyLong(), anyInt()))
                .thenReturn(2L);

        RatingDTO result = reviewService.getRatingByEntity(1L, "SERVICE");

        assertThat(result.getAverageRating()).isEqualTo(4.5);
        assertThat(result.getTotalReviews()).isEqualTo(10);
        assertThat(result.getDistribution().getFiveStars()).isEqualTo(2);
    }

}
