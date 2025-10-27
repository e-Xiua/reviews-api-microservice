package com.iwellness.reviews.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iwellness.reviews.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // =============== MÉTODOS NUEVOS PARA PROVEEDORES Y SERVICIOS ===============
    
    /**
     * Encuentra todas las reseñas de una entidad específica (servicio o proveedor)
     */
    Page<Review> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

    /**
     * Encuentra una reseña específica por tipo de entidad, ID de entidad y usuario
     */
    Optional<Review> findByEntityTypeAndEntityIdAndUserId(String entityType, Long entityId, Long userId);

    /**
     * Verifica si un usuario ya ha hecho una reseña para una entidad
     */
    boolean existsByEntityTypeAndEntityIdAndUserId(String entityType, Long entityId, Long userId);

    /**
     * Calcula el promedio de calificaciones de una entidad
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.entityType = :entityType AND r.entityId = :entityId")
    Double calculateAverageRatingByEntity(@Param("entityType") String entityType, @Param("entityId") Long entityId);

    /**
     * Cuenta el total de reseñas de una entidad
     */
    Long countByEntityTypeAndEntityId(String entityType, Long entityId);

    /**
     * Cuenta reseñas por calificación para una entidad
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.entityType = :entityType AND r.entityId = :entityId AND r.rating = :rating")
    Long countByEntityTypeAndEntityIdAndRating(@Param("entityType") String entityType, @Param("entityId") Long entityId, @Param("rating") Integer rating);

    /**
     * Obtiene las reseñas más recientes de una entidad
     */
    @Query("SELECT r FROM Review r WHERE r.entityType = :entityType AND r.entityId = :entityId ORDER BY r.createdAt DESC")
    List<Review> findRecentByEntity(@Param("entityType") String entityType, @Param("entityId") Long entityId, Pageable pageable);

    // =============== MÉTODOS LEGACY PARA SERVICIOS (COMPATIBILIDAD) ===============
    
    /**
     * Encuentra todas las reseñas de un servicio específico
     */
    Page<Review> findByServiceId(Long serviceId, Pageable pageable);

    /**
     * Encuentra todas las reseñas de un usuario específico
     */
    Page<Review> findByUserId(Long userId, Pageable pageable);

    /**
     * Encuentra una reseña específica por usuario y servicio
     */
    Optional<Review> findByServiceIdAndUserId(Long serviceId, Long userId);

    /**
     * Verifica si un usuario ya ha hecho una reseña para un servicio
     */
    boolean existsByServiceIdAndUserId(Long serviceId, Long userId);

    /**
     * Calcula el promedio de calificaciones de un servicio
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.serviceId = :serviceId")
    Double calculateAverageRating(@Param("serviceId") Long serviceId);

    /**
     * Cuenta el total de reseñas de un servicio
     */
    Long countByServiceId(Long serviceId);

    /**
     * Cuenta reseñas por calificación para un servicio
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.serviceId = :serviceId AND r.rating = :rating")
    Long countByServiceIdAndRating(@Param("serviceId") Long serviceId, @Param("rating") Integer rating);

    /**
     * Obtiene las reseñas más recientes de un servicio
     */
    @Query("SELECT r FROM Review r WHERE r.serviceId = :serviceId ORDER BY r.createdAt DESC")
    List<Review> findRecentByServiceId(@Param("serviceId") Long serviceId, Pageable pageable);

    /**
     * Obtiene las reseñas con mejor calificación de un servicio
     */
    @Query("SELECT r FROM Review r WHERE r.serviceId = :serviceId ORDER BY r.rating DESC, r.createdAt DESC")
    List<Review> findTopRatedByServiceId(@Param("serviceId") Long serviceId, Pageable pageable);

    /**
     * Elimina todas las reseñas de un servicio
     */
    void deleteByServiceId(Long serviceId);

    /**
     * Elimina todas las reseñas de un usuario
     */
    void deleteByUserId(Long userId);
}
