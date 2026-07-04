package com.pruebaTecnica.BancoCuscatlan.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Centraliza todas las propiedades custom de la aplicación bajo el prefijo "app".
 *
 * Se prefiere @ConfigurationProperties sobre @Value porque:
 * - Agrupa propiedades relacionadas en objetos tipados y cohesivos.
 * - Permite validación con Bean Validation (@NotBlank, @Positive, etc.).
 * - Es más fácil de testear (se puede instanciar sin contexto de Spring).
 * - Evita la dispersión de literales de clave por todo el código.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Jwt jwt = new Jwt();
    private final Cache cache = new Cache();

    @Getter
    @Setter
    public static class Jwt {

        /** Clave secreta para firmar los JWT (mínimo 256 bits). */
        @NotBlank
        private String secret;

        /** Tiempo de expiración del token en milisegundos. */
        @Positive
        private long expiration = 86_400_000L; // 24 h por defecto
    }

    @Getter
    @Setter
    public static class Cache {

        /** Nombre del caché principal. */
        @NotBlank
        private String cacheName = "default";

        /** TTL en segundos para las entradas de caché. */
        @Positive
        private long ttl = 3600L;
    }
}
