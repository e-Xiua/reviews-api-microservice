package com.iwellness.reviews.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.iwellness.reviews.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // Unified entity methods
    boolean existsByEntityTypeAndEntityIdAndUserId(String entityType, Long entityId, Long userId);
    
    Page<Review> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);
    
    Long countByEntityTypeAndEntityId(String entityType, Long entityId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.entityType = :entityType AND r.entityId = :entityId")
    Double calculateAverageRatingByEntity(@Param("entityType") String entityType, @Param("entityId") Long entityId);
    
    Long countByEntityTypeAndEntityIdAndRating(String entityType, Long entityId, Integer rating);
    
    @Query("SELECT r FROM Review r WHERE r.entityType = :entityType AND r.entityId = :entityId ORDER BY r.createdAt DESC")
    List<Review> findRecentByEntity(@Param("entityType") String entityType, 
                                   @Param("entityId") Long entityId, Pageable pageable);
    
    // Keep user-specific methods
    Page<Review> findByUserId(Long userId, Pageable pageable);
    
    // Additional methods for backward compatibility if needed
    boolean existsByServiceIdAndUserId(Long serviceId, Long userId);
    
    Page<Review> findByServiceId(Long serviceId, Pageable pageable);
    
    Long countByServiceId(Long serviceId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.serviceId = :serviceId")
    Double calculateAverageRating(@Param("serviceId") Long serviceId);
    
    Long countByServiceIdAndRating(Long serviceId, Integer rating);
    
    @Query("SELECT r FROM Review r WHERE r.serviceId = :serviceId ORDER BY r.createdAt DESC")
    List<Review> findRecentByServiceId(@Param("serviceId") Long serviceId, Pageable pageable);
}