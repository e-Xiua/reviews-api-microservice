-- Migración para agregar soporte de reseñas de proveedores
-- Fecha: 22 de octubre de 2025

-- Paso 1: Agregar nuevas columnas
ALTER TABLE reviews
ADD COLUMN entity_type VARCHAR(20),
ADD COLUMN entity_id BIGINT;

-- Paso 2: Migrar datos existentes (todos los reviews actuales son de servicios)
UPDATE reviews
SET entity_type = 'servicio',
    entity_id = service_id
WHERE entity_type IS NULL;

-- Paso 3: Hacer las columnas NOT NULL ahora que tienen valores
ALTER TABLE reviews
ALTER COLUMN entity_type SET NOT NULL,
ALTER COLUMN entity_id SET NOT NULL;

-- Paso 4: Crear índice para mejorar rendimiento de consultas por entidad
CREATE INDEX idx_reviews_entity ON reviews(entity_type, entity_id);

-- Paso 5: Crear índice para consultas por usuario y entidad
CREATE INDEX idx_reviews_entity_user ON reviews(entity_type, entity_id, user_id);

-- Paso 6: Agregar constraint único para evitar duplicados
-- Primero eliminamos el constraint anterior si existe
ALTER TABLE reviews DROP CONSTRAINT IF EXISTS uk_service_user;

-- Ahora agregamos el nuevo constraint único
ALTER TABLE reviews
ADD CONSTRAINT uk_entity_user UNIQUE (entity_type, entity_id, user_id);

-- Nota: La columna service_id se mantiene por compatibilidad con código existente
-- pero solo se usará para reviews de tipo 'servicio'
