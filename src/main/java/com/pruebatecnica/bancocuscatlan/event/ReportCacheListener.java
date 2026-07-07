package com.pruebatecnica.bancocuscatlan.event;

import com.pruebatecnica.bancocuscatlan.config.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ReportCacheListener {

    @EventListener
    @CacheEvict(value = CacheConfig.OCCUPANCY_REPORT_CACHE, allEntries = true)
    public void onReservationStatusChanged(ReservationStatusChangedEvent event) {
        // Invalida cache de ocupacion cuando cambian reservas relevantes.
    }

    @EventListener
    @CacheEvict(value = CacheConfig.OCCUPANCY_REPORT_CACHE, allEntries = true)
    public void onSpaceChanged(SpaceChangedEvent event) {
        // Invalida cache de ocupacion cuando cambia disponibilidad de espacios.
    }

    @EventListener
    @CacheEvict(value = CacheConfig.OCCUPANCY_REPORT_CACHE, allEntries = true)
    public void onReservationRescheduled(ReservationRescheduledEvent event) {
        // Invalida cache de ocupacion cuando cambian las fechas de una reserva.
    }
}
