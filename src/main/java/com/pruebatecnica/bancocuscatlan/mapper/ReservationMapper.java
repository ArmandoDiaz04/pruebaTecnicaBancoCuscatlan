package com.pruebaTecnica.BancoCuscatlan.mapper;

import com.pruebaTecnica.BancoCuscatlan.domain.entity.Reservation;
import com.pruebaTecnica.BancoCuscatlan.dto.ReservationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "spaceId", source = "space.id")
    ReservationResponse toResponse(Reservation reservation);
}
