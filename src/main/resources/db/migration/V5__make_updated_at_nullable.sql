-- updated_at ahora representa "última modificación real" (vía @PreUpdate),
-- no un duplicado de created_at al momento de la inserción. Queda NULL hasta
-- que el registro sufra su primer UPDATE.
ALTER TABLE users ALTER COLUMN updated_at DROP NOT NULL;
ALTER TABLE spaces ALTER COLUMN updated_at DROP NOT NULL;
ALTER TABLE reservations ALTER COLUMN updated_at DROP NOT NULL;
