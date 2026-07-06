package com.pruebatecnica.bancocuscatlan.event;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ReportCacheListener {

    @EventListener
    @CacheEvict(value = "occupancyReport", allEntries = true)
    public void onReservationStatusChanged(ReservationStatusChangedEvent event) {
        // Invalida cache de ocupacion cuando cambian reservas relevantes.
    }

    @EventListener
    @CacheEvict(value = "occupancyReport", allEntries = true)
    public void onSpaceChanged(SpaceChangedEvent event) {
        // Invalida cache de ocupacion cuando cambia disponibilidad de espacios.
    }
}
