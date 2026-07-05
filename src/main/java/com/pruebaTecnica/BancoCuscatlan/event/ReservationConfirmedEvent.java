package com.pruebaTecnica.BancoCuscatlan.event;

import java.time.LocalDateTime;

public record ReservationConfirmedEvent(
        Long reservationId,
        Long userId,
        String userEmail,
        String userName,
        Long spaceId,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime
) {
}
