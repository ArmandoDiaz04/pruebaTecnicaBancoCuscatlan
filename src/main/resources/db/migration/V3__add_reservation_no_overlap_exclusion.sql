-- Requerida para poder usar el operador de igualdad (=) sobre space_id (bigint)
-- dentro de un índice GiST junto al operador de rango (&&).
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Garantiza a nivel de base de datos que no puedan coexistir dos reservas
-- solapadas para el mismo espacio, incluso bajo escritura concurrente
-- (defensa real contra la condición de carrera que el chequeo a nivel de
-- aplicación, hecho en READ COMMITTED, no puede prevenir por sí solo).
-- El límite '[)' (inclusivo-inicio, exclusivo-fin) preserva la semántica ya
-- usada por ReservationRepository.existsOverlappingReservation: dos reservas
-- back-to-back (fin de una == inicio de otra) NO se consideran solapadas.
ALTER TABLE reservations
    ADD CONSTRAINT excl_reservations_no_overlap
    EXCLUDE USING gist (
        space_id WITH =,
        tsrange(start_date_time, end_date_time, '[)') WITH &&
    )
    WHERE (status IN ('PENDING_PAYMENT', 'CONFIRMED'));
