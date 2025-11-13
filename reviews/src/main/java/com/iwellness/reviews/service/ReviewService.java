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
import com.iwellness.reviews.dto.ProviderRatingDTO;
import com.iwellness.reviews.dto.ProviderReviewRequestDTO;
import com.iwellness.reviews.dto.ReviewRequestDTO;
import com.iwellness.reviews.dto.ReviewResponseDTO;
import com.iwellness.reviews.dto.ServiceRatingDTO;
import com.iwellness.reviews.dto.UsuarioDTO;
import com.iwellness.reviews.entity.Review;
import com.iwellness.reviews.exception.DuplicateReviewException;
import com.iwellness.reviews.exception.ReviewNotFoundException;
import com.iwellness.reviews.exception.UnauthorizedReviewAccessException;
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

    public ReviewService(ReviewRepository reviewRepository, ReviewEventPublisher reviewEventPublisher, UserApiClient userApiClient) {
        this.reviewRepository = reviewRepository;
        this.reviewEventPublisher = reviewEventPublisher;
        this.userApiClient = userApiClient;
    }

    /**
     * Crea una nueva reseña
     */
    @Transactional
    public ReviewResponseDTO createReview(ReviewRequestDTO requestDTO, Long userId) {
        log.info("Creando reseña para servicio {} por usuario {}", requestDTO.getServiceId(), userId);

        // Verificar si el usuario ya tiene una reseña para este servicio
        if (reviewRepository.existsByServiceIdAndUserId(requestDTO.getServiceId(), userId)) {
            throw new DuplicateReviewException("Ya existe una reseña de este usuario para este servicio");
        }

        Review review = Review.builder()
                .entityType("servicio")
                .entityId(requestDTO.getServiceId())
                .serviceId(requestDTO.getServiceId()) // Compatibilidad
                .userId(userId)
                .rating(requestDTO.getRating())
                .comment(requestDTO.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("Reseña creada con ID: {}", savedReview.getId());

        // Publicar evento de reseña creada
        reviewEventPublisher.publishReviewCreated(savedReview);

        // Publicar evento de cambio de calificación
        reviewEventPublisher.publishRatingChanged(savedReview.getServiceId());

        return mapToResponseDTO(savedReview);
    }

    /**
     * Crea una nueva reseña para un proveedor
     */
    @Transactional
    public ReviewResponseDTO createProviderReview(ProviderReviewRequestDTO requestDTO, Long userId) {
        log.info("Creando reseña para proveedor {} por usuario {}", requestDTO.getProviderId(), userId);

        // Verificar si el usuario ya tiene una reseña para este proveedor
        if (reviewRepository.existsByEntityTypeAndEntityIdAndUserId("proveedor", requestDTO.getProviderId(), userId)) {
            throw new DuplicateReviewException("Ya existe una reseña de este usuario para este proveedor");
        }

        Review review = Review.builder()
                .entityType("proveedor")
                .entityId(requestDTO.getProviderId())
                .userId(userId)
                .rating(requestDTO.getRating())
                .comment(requestDTO.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("Reseña de proveedor creada con ID: {}", savedReview.getId());

        // Publicar eventos (el proveedor ID va en entityId)
        reviewEventPublisher.publishReviewCreated(savedReview);

        return mapToResponseDTO(savedReview);
    }

    /**
     * Actualiza una reseña existente
     */
    @Transactional
    public ReviewResponseDTO updateReview(Long reviewId, ReviewRequestDTO requestDTO, Long userId) {
        log.info("Actualizando reseña {} por usuario {}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Reseña no encontrada"));

        // Verificar que el usuario sea el propietario de la reseña
        if (!review.getUserId().equals(userId)) {
            throw new UnauthorizedReviewAccessException("No tienes permiso para modificar esta reseña");
        }

        Integer oldRating = review.getRating();
        review.setRating(requestDTO.getRating());
        review.setComment(requestDTO.getComment());

        Review updatedReview = reviewRepository.save(review);
        log.info("Reseña actualizada: {}", reviewId);

        // Publicar evento de reseña actualizada
        reviewEventPublisher.publishReviewUpdated(updatedReview);

        // Si cambió la calificación, publicar evento de cambio
        if (!oldRating.equals(requestDTO.getRating())) {
            reviewEventPublisher.publishRatingChanged(updatedReview.getServiceId());
        }

        return mapToResponseDTO(updatedReview);
    }

    /**
     * Elimina una reseña
     */
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        log.info("Eliminando reseña {} por usuario {}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Reseña no encontrada"));

        // Verificar que el usuario sea el propietario de la reseña
        if (!review.getUserId().equals(userId)) {
            throw new UnauthorizedReviewAccessException("No tienes permiso para eliminar esta reseña");
        }

        Long serviceId = review.getServiceId();
        reviewRepository.delete(review);
        log.info("Reseña eliminada: {}", reviewId);

        // Publicar evento de reseña eliminada
        reviewEventPublisher.publishReviewDeleted(reviewId, serviceId, userId);

        // Publicar evento de cambio de calificación
        reviewEventPublisher.publishRatingChanged(serviceId);
    }

    /**
     * Obtiene una reseña por ID
     */
    @Transactional(readOnly = true)
    public ReviewResponseDTO getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Reseña no encontrada"));
        return mapToResponseDTO(review);
    }

    /**
     * Obtiene todas las reseñas de un servicio
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> getReviewsByServiceId(Long serviceId, int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        Page<Review> reviews = reviewRepository.findByServiceId(serviceId, pageable);
        return reviews.map(this::mapToResponseDTO);
    }

    /**
     * Obtiene todas las reseñas de un proveedor
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> getReviewsByProviderId(Long providerId, int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        Page<Review> reviews = reviewRepository.findByEntityTypeAndEntityId("proveedor", providerId, pageable);
        return reviews.map(this::mapToResponseDTO);
    }

    /**
     * Obtiene todas las reseñas de un usuario
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> getReviewsByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewRepository.findByUserId(userId, pageable);
        return reviews.map(this::mapToResponseDTO);
    }

    /**
     * Obtiene las estadísticas de calificación de un servicio
     */
    @Transactional(readOnly = true)
    public ServiceRatingDTO getServiceRating(Long serviceId) {
        Double averageRating = reviewRepository.calculateAverageRating(serviceId);
        Long totalReviews = reviewRepository.countByServiceId(serviceId);

        ServiceRatingDTO.RatingDistribution distribution = ServiceRatingDTO.RatingDistribution.builder()
                .fiveStars(reviewRepository.countByServiceIdAndRating(serviceId, 5))
                .fourStars(reviewRepository.countByServiceIdAndRating(serviceId, 4))
                .threeStars(reviewRepository.countByServiceIdAndRating(serviceId, 3))
                .twoStars(reviewRepository.countByServiceIdAndRating(serviceId, 2))
                .oneStar(reviewRepository.countByServiceIdAndRating(serviceId, 1))
                .build();

        return ServiceRatingDTO.builder()
                .serviceId(serviceId)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalReviews(totalReviews)
                .distribution(distribution)
                .build();
    }

    /**
     * Obtiene las estadísticas de calificación de un proveedor
     */
    @Transactional(readOnly = true)
    public ProviderRatingDTO getProviderRating(Long providerId) {
        Double averageRating = reviewRepository.calculateAverageRatingByEntity("proveedor", providerId);
        Long totalReviews = reviewRepository.countByEntityTypeAndEntityId("proveedor", providerId);

        ProviderRatingDTO.RatingDistribution distribution = ProviderRatingDTO.RatingDistribution.builder()
                .fiveStars(reviewRepository.countByEntityTypeAndEntityIdAndRating("proveedor", providerId, 5))
                .fourStars(reviewRepository.countByEntityTypeAndEntityIdAndRating("proveedor", providerId, 4))
                .threeStars(reviewRepository.countByEntityTypeAndEntityIdAndRating("proveedor", providerId, 3))
                .twoStars(reviewRepository.countByEntityTypeAndEntityIdAndRating("proveedor", providerId, 2))
                .oneStar(reviewRepository.countByEntityTypeAndEntityIdAndRating("proveedor", providerId, 1))
                .build();

        return ProviderRatingDTO.builder()
                .providerId(providerId)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalReviews(totalReviews)
                .distribution(distribution)
                .build();
    }

    /**
     * Obtiene las reseñas más recientes de un servicio
     */
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getRecentReviews(Long serviceId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Review> reviews = reviewRepository.findRecentByServiceId(serviceId, pageable);
        return reviews.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las reseñas más recientes de un proveedor
     */
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getRecentProviderReviews(Long providerId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Review> reviews = reviewRepository.findRecentByEntity("proveedor", providerId, pageable);
        return reviews.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Mapea una entidad Review a ReviewResponseDTO
     */
    private ReviewResponseDTO mapToResponseDTO(Review review) {

        UsuarioDTO user = null;
        try {
            // 3. Llamar al microservicio de usuarios para obtener los datos del autor de la reseña
            user = userApiClient.findById(review.getUserId());
        } catch (FeignException e) {
            // 4. Manejo de errores: si el servicio de usuarios falla o el usuario no existe,
            // no detenemos la operación. Simplemente registramos el error.
            log.error("Error al obtener datos para el usuario ID: {}. Causa: {}", review.getUserId(), e.getMessage());
        }

        String username = (user != null && user.getNombre() != null) 
                            ? user.getNombre() + " " + user.getApellido() 
                            : "Usuario Anónimo";
        
        String userImageUrl = (user != null) ? user.getFoto() : null;
        return ReviewResponseDTO.builder()
                .id(review.getId())
                .serviceId(review.getServiceId())
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
