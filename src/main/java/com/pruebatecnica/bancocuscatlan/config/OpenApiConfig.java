package com.pruebaTecnica.BancoCuscatlan.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banco Cuscatlán API")
                        .version("1.0.0")
                        .description("API REST para el sistema bancario de Banco Cuscatlán")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo")
                                .email("dev@bancocuscatlan.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desarrollo Local"),
                        new Server()
                                .url("https://api.bancocuscatlan.com")
                                .description("Servidor de Producción")
                ));
    }
}
