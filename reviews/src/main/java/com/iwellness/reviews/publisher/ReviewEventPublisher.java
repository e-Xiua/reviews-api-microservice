package com.iwellness.reviews.publisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.iwellness.reviews.entity.Review;
import com.iwellness.reviews.event.RatingChangedEvent;
import com.iwellness.reviews.event.ReviewCreatedEvent;
import com.iwellness.reviews.event.ReviewDeletedEvent;
import com.iwellness.reviews.event.ReviewUpdatedEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ReviewEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing-keys.review-created}")
    private String reviewCreatedRoutingKey;

    @Value("${rabbitmq.routing-keys.review-updated}")
    private String reviewUpdatedRoutingKey;

    @Value("${rabbitmq.routing-keys.review-deleted}")
    private String reviewDeletedRoutingKey;

    @Value("${rabbitmq.routing-keys.rating-changed}")
    private String ratingChangedRoutingKey;

    public ReviewEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publica evento de reseña creada
     */
    public void publishReviewCreated(Review review) {
        ReviewCreatedEvent event = ReviewCreatedEvent.builder()
                .reviewId(review.getId())
                .serviceId(review.getServiceId())
                .userId(review.getUserId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();

        try {
            rabbitTemplate.convertAndSend(exchangeName, reviewCreatedRoutingKey, event);
            log.info("Evento publicado: ReviewCreated - Review ID: {}", review.getId());
        } catch (Exception e) {
            log.error("Error al publicar evento ReviewCreated: {}", e.getMessage(), e);
        }
    }

    /**
     * Publica evento de reseña actualizada
     */
    public void publishReviewUpdated(Review review) {
        ReviewUpdatedEvent event = ReviewUpdatedEvent.builder()
                .reviewId(review.getId())
                .serviceId(review.getServiceId())
                .userId(review.getUserId())
                .rating(review.getRating())
                .comment(review.getComment())
                .updatedAt(review.getUpdatedAt())
                .build();

        try {
            rabbitTemplate.convertAndSend(exchangeName, reviewUpdatedRoutingKey, event);
            log.info("Evento publicado: ReviewUpdated - Review ID: {}", review.getId());
        } catch (Exception e) {
            log.error("Error al publicar evento ReviewUpdated: {}", e.getMessage(), e);
        }
    }

    /**
     * Publica evento de reseña eliminada
     */
    public void publishReviewDeleted(Long reviewId, Long serviceId, Long userId) {
        ReviewDeletedEvent event = ReviewDeletedEvent.builder()
                .reviewId(reviewId)
                .serviceId(serviceId)
                .userId(userId)
                .deletedAt(java.time.LocalDateTime.now())
                .build();

        try {
            rabbitTemplate.convertAndSend(exchangeName, reviewDeletedRoutingKey, event);
            log.info("Evento publicado: ReviewDeleted - Review ID: {}", reviewId);
        } catch (Exception e) {
            log.error("Error al publicar evento ReviewDeleted: {}", e.getMessage(), e);
        }
    }

    /**
     * Publica evento de cambio de calificación
     */
    public void publishRatingChanged(Long serviceId) {
        RatingChangedEvent event = RatingChangedEvent.builder()
                .serviceId(serviceId)
                .timestamp(java.time.LocalDateTime.now())
                .build();

        try {
            rabbitTemplate.convertAndSend(exchangeName, ratingChangedRoutingKey, event);
            log.info("Evento publicado: RatingChanged - Service ID: {}", serviceId);
        } catch (Exception e) {
            log.error("Error al publicar evento RatingChanged: {}", e.getMessage(), e);
        }
    }
}
