package com.pruebatecnica.bancocuscatlan.mapper;

import com.pruebatecnica.bancocuscatlan.domain.entity.Space;
import com.pruebatecnica.bancocuscatlan.dto.CreateSpaceRequest;
import com.pruebatecnica.bancocuscatlan.dto.SpaceResponse;
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
