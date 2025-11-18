-- Migración inicial para crear la tabla de reseñas
-- Fecha: 18 de noviembre de 2025

CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT,
    user_id BIGINT NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uk_service_user UNIQUE (service_id, user_id)
);

-- Índices para mejorar el rendimiento
CREATE INDEX idx_reviews_service_id ON reviews(service_id);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);
