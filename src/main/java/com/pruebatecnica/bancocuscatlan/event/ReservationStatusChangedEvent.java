package com.pruebatecnica.bancocuscatlan.event;

import com.pruebatecnica.bancocuscatlan.domain.enums.ReservationStatus;

public record ReservationStatusChangedEvent(Long reservationId, ReservationStatus status) {
}
