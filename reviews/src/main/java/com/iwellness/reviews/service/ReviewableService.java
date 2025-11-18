package com.iwellness.reviews.service;

import org.springframework.stereotype.Service;

import com.iwellness.reviews.client.ServicioApiClient;
import com.iwellness.reviews.client.UserApiClient;
import com.iwellness.reviews.model.EntityType;
import com.iwellness.reviews.model.Reviewable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReviewableService {

    private final ServicioApiClient servicioApiClient;
    private final UserApiClient userApiClient;

    public ReviewableService(ServicioApiClient servicioApiClient, UserApiClient userApiClient) {
        this.servicioApiClient = servicioApiClient;
        this.userApiClient = userApiClient;
    }

    // Method that accepts EntityType enum
    public Reviewable getReviewableEntity(EntityType entityType, Long entityId) {
        try {
            switch (entityType) {
                case SERVICE:
                    return servicioApiClient.getServicioById(entityId);
                case PROVIDER:
                    return userApiClient.findById(entityId);
                default:
                    log.warn("Unsupported entity type: {}", entityType);
                    return null;
                
            }
        } catch (Exception e) {
            log.error("Error fetching {} with ID {}: {}", entityType, entityId, e.getMessage());
            return null;
        }
    }

    // Overloaded method that accepts String for backward compatibility
    public Reviewable getReviewableEntity(String entityType, Long entityId) {
        try {
            EntityType type = EntityType.valueOf(entityType.toUpperCase());
            return getReviewableEntity(type, entityId);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid entity type string: {}", entityType);
            return null;
        }
    }
}
