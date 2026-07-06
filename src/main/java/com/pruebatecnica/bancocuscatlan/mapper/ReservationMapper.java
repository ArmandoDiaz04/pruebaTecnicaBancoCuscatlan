package com.pruebatecnica.bancocuscatlan.mapper;

import com.pruebatecnica.bancocuscatlan.domain.entity.Reservation;
import com.pruebatecnica.bancocuscatlan.dto.ReservationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "spaceId", source = "space.id")
    ReservationResponse toResponse(Reservation reservation);
}
