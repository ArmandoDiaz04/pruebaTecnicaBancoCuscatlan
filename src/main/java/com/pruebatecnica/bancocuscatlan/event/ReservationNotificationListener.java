package com.pruebatecnica.bancocuscatlan.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;

@Component
public class ReservationNotificationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservationNotificationListener.class);

    @Async
    @EventListener
    public void onReservationConfirmed(ReservationConfirmedEvent event) {
        LOGGER.info(
                "Simulando envio de correo: reserva confirmada id={} usuario={} email={} espacio={} desde={} hasta={}",
                event.reservationId(),
                event.userName(),
                event.userEmail(),
                event.spaceId(),
                event.startDateTime(),
                event.endDateTime()
        );
    }
}
