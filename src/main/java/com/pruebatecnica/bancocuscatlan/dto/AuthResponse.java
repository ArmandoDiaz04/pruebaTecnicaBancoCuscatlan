package com.pruebaTecnica.BancoCuscatlan.dto;

import com.pruebaTecnica.BancoCuscatlan.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String tokenType;
    private String accessToken;
    private long expiresIn;
    private Long userId;
    private String email;
    private String name;
    private Role role;
}
