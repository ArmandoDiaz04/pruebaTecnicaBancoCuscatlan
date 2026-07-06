package com.pruebaTecnica.BancoCuscatlan.mapper;

import com.pruebaTecnica.BancoCuscatlan.domain.entity.Space;
import com.pruebaTecnica.BancoCuscatlan.dto.CreateSpaceRequest;
import com.pruebaTecnica.BancoCuscatlan.dto.SpaceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SpaceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Space toEntity(CreateSpaceRequest request);

    SpaceResponse toResponse(Space space);
}
