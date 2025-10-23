package com.iwellness.reviews.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

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

    /**
     * Topic Exchange para eventos de reseñas
     */
    @Bean
    public TopicExchange reviewExchange() {
        return new TopicExchange(exchangeName);
    }

    /**
     * Queue para eventos de reseñas creadas
     */
    @Bean
    public Queue reviewCreatedQueue() {
        return new Queue("review.created.queue", true);
    }

    /**
     * Queue para eventos de reseñas actualizadas
     */
    @Bean
    public Queue reviewUpdatedQueue() {
        return new Queue("review.updated.queue", true);
    }

    /**
     * Queue para eventos de reseñas eliminadas
     */
    @Bean
    public Queue reviewDeletedQueue() {
        return new Queue("review.deleted.queue", true);
    }

    /**
     * Queue para eventos de cambios de calificación
     */
    @Bean
    public Queue ratingChangedQueue() {
        return new Queue("review.rating.changed.queue", true);
    }

    /**
     * Binding entre exchange y queues
     */
    @Bean
    public Binding reviewCreatedBinding(Queue reviewCreatedQueue, TopicExchange reviewExchange) {
        return BindingBuilder.bind(reviewCreatedQueue)
                .to(reviewExchange)
                .with(reviewCreatedRoutingKey);
    }

    @Bean
    public Binding reviewUpdatedBinding(Queue reviewUpdatedQueue, TopicExchange reviewExchange) {
        return BindingBuilder.bind(reviewUpdatedQueue)
                .to(reviewExchange)
                .with(reviewUpdatedRoutingKey);
    }

    @Bean
    public Binding reviewDeletedBinding(Queue reviewDeletedQueue, TopicExchange reviewExchange) {
        return BindingBuilder.bind(reviewDeletedQueue)
                .to(reviewExchange)
                .with(reviewDeletedRoutingKey);
    }

    @Bean
    public Binding ratingChangedBinding(Queue ratingChangedQueue, TopicExchange reviewExchange) {
        return BindingBuilder.bind(ratingChangedQueue)
                .to(reviewExchange)
                .with(ratingChangedRoutingKey);
    }

    /**
     * Message Converter para serialización JSON
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate configurado con JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
