package com.pruebaTecnica.BancoCuscatlan.event;

import com.pruebaTecnica.BancoCuscatlan.domain.enums.ReservationStatus;

public record ReservationStatusChangedEvent(Long reservationId, ReservationStatus status) {
}
