package com.pruebatecnica.bancocuscatlan.security;

import com.pruebatecnica.bancocuscatlan.domain.enums.Role;
import com.pruebatecnica.bancocuscatlan.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static AuthenticatedUserPrincipal currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal)) {
            throw new UnauthorizedException("No hay usuario autenticado");
        }
        return principal;
    }

    public static boolean isAdmin() {
        return currentUser().role() == Role.ADMIN;
    }
}
