package com.pruebatecnica.bancocuscatlan.mapper;

import com.pruebatecnica.bancocuscatlan.domain.entity.User;
import com.pruebatecnica.bancocuscatlan.dto.CreateUserRequest;
import com.pruebatecnica.bancocuscatlan.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(CreateUserRequest request);

    UserResponse toResponse(User user);
}
