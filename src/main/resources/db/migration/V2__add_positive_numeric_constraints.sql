ALTER TABLE spaces
    ADD CONSTRAINT ck_spaces_capacity_positive CHECK (capacity > 0),
    ADD CONSTRAINT ck_spaces_hourly_rate_positive CHECK (hourly_rate > 0);

ALTER TABLE reservations
    ADD CONSTRAINT ck_reservations_total_amount_non_negative CHECK (total_amount >= 0);
