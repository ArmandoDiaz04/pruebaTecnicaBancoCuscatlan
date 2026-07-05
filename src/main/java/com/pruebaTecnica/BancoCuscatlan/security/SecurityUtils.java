package com.pruebaTecnica.BancoCuscatlan.security;

import com.pruebaTecnica.BancoCuscatlan.domain.enums.Role;
import com.pruebaTecnica.BancoCuscatlan.exception.UnauthorizedException;
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
