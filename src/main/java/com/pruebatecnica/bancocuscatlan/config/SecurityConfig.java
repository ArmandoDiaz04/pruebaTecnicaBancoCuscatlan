package com.pruebatecnica.bancocuscatlan.config;

import com.pruebatecnica.bancocuscatlan.security.JwtAccessDeniedHandler;
import com.pruebatecnica.bancocuscatlan.security.JwtAuthenticationEntryPoint;
import com.pruebatecnica.bancocuscatlan.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final Environment environment;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAccessDeniedHandler jwtAccessDeniedHandler,
            Environment environment
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.environment = environment;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        boolean isProd = environment.acceptsProfiles(Profiles.of("prod"));

        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            .authorizeHttpRequests(authorize -> {
                // Endpoints públicos
                authorize.requestMatchers(
                    "/",
                    "/api/auth/register",
                    "/api/auth/login",
                    "/mock/payments/**",
                    "/api/health",
                    "/actuator/health",
                    "/actuator/health/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/api-docs/**"
                ).permitAll();

                // En prod, el resto de Actuator (metrics, circuitbreakers, etc.) exige
                // ADMIN; en dev/test queda abierto por comodidad de desarrollo.
                if (isProd) {
                    authorize.requestMatchers("/actuator/**").hasRole("ADMIN");
                } else {
                    authorize.requestMatchers("/actuator/**").permitAll();
                }

                // Reglas por rol
                authorize.requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/spaces").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/spaces/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/spaces/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/spaces/inactive").hasRole("ADMIN")
                        .requestMatchers("/api/reports/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/reservations").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/reservations/*/status").hasRole("ADMIN")
                        .requestMatchers("/api/reservations/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/spaces").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/spaces/**").authenticated()
                        .anyRequest().authenticated();
            })
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
