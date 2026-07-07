package com.pruebatecnica.bancocuscatlan.repository;

import com.pruebatecnica.bancocuscatlan.domain.entity.Reservation;
import com.pruebatecnica.bancocuscatlan.domain.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
            FROM Reservation r
            WHERE r.space.id = :spaceId
            AND r.status IN :statuses
            AND r.startDateTime < :endDateTime
            AND r.endDateTime > :startDateTime
            """)
    boolean existsOverlappingReservation(
            @Param("spaceId") Long spaceId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("statuses") Collection<ReservationStatus> statuses
    );

    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
            FROM Reservation r
            WHERE r.space.id = :spaceId
            AND r.id <> :excludeReservationId
            AND r.status IN :statuses
            AND r.startDateTime < :endDateTime
            AND r.endDateTime > :startDateTime
            """)
    boolean existsOverlappingReservationExcludingId(
            @Param("spaceId") Long spaceId,
            @Param("excludeReservationId") Long excludeReservationId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("statuses") Collection<ReservationStatus> statuses
    );

    List<Reservation> findByUserId(Long userId);

        long countByStatus(ReservationStatus status);

    @Query("""
            SELECT r FROM Reservation r
            WHERE r.space.id = :spaceId
              AND r.status = 'CONFIRMED'
              AND r.startDateTime < :toDateTime
              AND r.endDateTime > :fromDateTime
            """)
    List<Reservation> findConfirmedBySpaceAndRange(
            @Param("spaceId") Long spaceId,
            @Param("fromDateTime") LocalDateTime fromDateTime,
            @Param("toDateTime") LocalDateTime toDateTime
    );
}
