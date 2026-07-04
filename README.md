# 🏦 Banco Cuscatlán API

API REST para el sistema bancario de Banco Cuscatlán construida con Spring Boot 3 y Java 21.

## 📋 Tabla de Contenidos
- [Tecnologías](#tecnologías)
- [Requisitos](#requisitos)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Configuración](#configuración)
- [Ejecución](#ejecución)
- [Endpoints](#endpoints)
- [Testing](#testing)

## 🚀 Tecnologías

- **Java 21** - Lenguaje de programación
- **Spring Boot 3.5.16** - Framework principal
- **Spring Data JPA** - Persistencia de datos
- **Spring Security** - Seguridad
- **PostgreSQL** - Base de datos
- **JJWT 0.12.6** - Generación y validación de JWT
- **MapStruct 1.6.3** - Mapeo de DTOs
- **Lombok** - Reducción de boilerplate
- **Springdoc OpenAPI 2.8.16** - Documentación API
- **Resilience4j** - Circuit Breaker y Retry
- **TestContainers** - Testing de integración
- **Maven** - Gestión de dependencias

## 📦 Requisitos

- Java 21 o superior
- Maven 3.9+
- PostgreSQL 13+ (o Docker para desarrollo)
- Docker Desktop (para tests con TestContainers)

## 📂 Estructura del Proyecto

```
src/main/java/com/pruebaTecnica/BancoCuscatlan/
├── BancoCuscatlanApplication.java  # Clase principal
├── controller/                      # Controladores REST
│   └── HealthController.java
├── service/                         # Lógica de negocio
├── repository/                      # Acceso a datos (JPA)
├── domain/entity/                   # Entidades JPA
├── dto/                            # Data Transfer Objects
│   └── HealthResponse.java
├── mapper/                         # Mappers (MapStruct)
├── exception/                      # Manejo de excepciones
│   ├── GlobalExceptionHandler.java
│   ├── ErrorResponse.java
│   ├── ResourceNotFoundException.java
│   └── BadRequestException.java
├── config/                         # Configuraciones
│   ├── AppProperties.java           # @ConfigurationProperties centralizadas
│   ├── SecurityConfig.java
│   ├── OpenApiConfig.java
│   └── CacheConfig.java
├── security/                       # Configuración de seguridad
├── client/                         # Clientes HTTP externos
├── event/                          # Eventos de dominio
└── strategy/                       # Patrones Strategy/State
```

## ⚙️ Configuración

### Perfiles disponibles

| Perfil | Archivo | Uso |
|--------|---------|-----|
| `dev` | `application-dev.yml` | Desarrollo local (activo por defecto) |
| `prod` | `application-prod.yml` | Producción (usa variables de entorno) |

### Base de Datos

Crear base de datos PostgreSQL local (perfil `dev`):

```sql
CREATE DATABASE banco_cuscatlan;
```

La configuración `dev` espera:
- **Host:** localhost
- **Puerto:** 5432
- **Database:** banco_cuscatlan
- **Usuario:** postgres
- **Password:** postgres

Puedes modificar estos valores en `src/main/resources/application-dev.yml`.

### Variables de entorno (perfil `prod`)

| Variable | Descripción |
|----------|-------------|
| `DB_URL` | URL JDBC de PostgreSQL |
| `DB_USER` | Usuario de la base de datos |
| `DB_PASS` | Contraseña de la base de datos |
| `JWT_SECRET` | Clave secreta para firmar JWT (mín. 256 bits) |
| `JWT_EXPIRATION` | Expiración del token en ms (default: 86400000) |
| `CACHE_TTL` | TTL del caché en segundos (default: 3600) |

---

## 🔧 `@ConfigurationProperties` vs `@Value`

Este proyecto utiliza `@ConfigurationProperties` en lugar de `@Value` dispersos por el código. A continuación se explica el por qué:

### ❌ Problema con `@Value`

```java
// Disperso en múltiples clases — difícil de mantener
@Value("${app.jwt.secret}")  private String secret;
@Value("${app.jwt.expiration}") private long expiration;
@Value("${app.cache.cache-name}") private String cacheName;
```

- Las claves de propiedades están **dispersas** por todo el código.
- No hay **autocompletado** ni validación en tiempo de compilación.
- Es **difícil de testear** (requiere contexto de Spring o mocks).
- Un typo en la clave solo falla **en tiempo de ejecución**.

### ✅ Ventajas de `@ConfigurationProperties`

```java
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private final Jwt jwt = new Jwt();
    private final Cache cache = new Cache();
    // ...
}
```

| Aspecto | `@Value` | `@ConfigurationProperties` |
|---------|----------|----------------------------|
| Organización | Disperso | Centralizado en una clase |
| Tipado | String/primitivos | Objetos anidados tipados |
| Validación | No | Sí (`@NotBlank`, `@Positive`) |
| Testabilidad | Requiere contexto Spring | Instanciable directamente |
| Autocompletado IDE | Limitado | Completo con `spring-boot-configuration-processor` |
| Refactoring | Riesgoso | Seguro |

La clase `AppProperties` agrupa las secciones `app.jwt.*` y `app.cache.*`, y es registrada en el arranque mediante `@EnableConfigurationProperties(AppProperties.class)`.

## 🏃 Ejecución

### Opción 1: Maven

```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar con perfil dev (activo por defecto)
mvn spring-boot:run

# Ejecutar con perfil específico
mvn spring-boot:run -Dspring-boot.run.profiles=dev
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Opción 2: JAR

```bash
# Empaquetar
mvn clean package -DskipTests

# Ejecutar
java -jar target/BancoCuscatlan-0.0.1-SNAPSHOT.jar
```

### Opción 3: IDE

Ejecutar la clase `BancoCuscatlanApplication.java` desde tu IDE favorito.

La aplicación estará disponible en: **http://localhost:8080**

## 🌐 Endpoints

### Health Check

```http
GET /api/health
```

**Respuesta:**
```json
{
  "status": "UP",
  "message": "Banco Cuscatlán API está funcionando correctamente",
  "timestamp": "2026-07-04T00:10:00",
  "version": "1.0.0"
}
```

### Actuator

```http
GET /actuator/health
GET /actuator/info
GET /actuator/metrics
```

### Documentación Swagger

Acceder a la documentación interactiva en:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api-docs

## 🧪 Testing

### Ejecutar Tests

```bash
# Todos los tests
mvn test

# Tests específicos
mvn test -Dtest=NombreDelTest
```

**Nota:** Los tests de integración requieren Docker Desktop en ejecución para usar TestContainers.

### Sin Docker

```bash
# Saltar tests
mvn clean package -DskipTests
```

## 🔒 Seguridad

La configuración actual es **permisiva para desarrollo**:
- `/api/health` - Acceso público
- `/actuator/**` - Acceso público
- `/swagger-ui/**` - Acceso público
- Resto de endpoints - Acceso público (temporal)

**⚠️ Importante:** En producción, implementar autenticación y autorización apropiadas.

## 📝 Próximos Pasos

- [ ] Implementar entidades de dominio (Cliente, Cuenta, Transacción, etc.)
- [ ] Crear servicios de negocio
- [ ] Implementar repositories
- [ ] Agregar mappers con MapStruct
- [ ] Implementar autenticación JWT
- [ ] Agregar tests unitarios y de integración
- [ ] Configurar perfiles (dev, test, prod)
- [ ] Implementar Circuit Breaker con Resilience4j
- [ ] Agregar métricas y monitoreo

## 🤝 Contribución

Para contribuir al proyecto:

1. Crear una rama feature: `git checkout -b feature/nueva-funcionalidad`
2. Hacer commit de cambios: `git commit -m 'feat: agregar nueva funcionalidad'`
3. Push a la rama: `git push origin feature/nueva-funcionalidad`
4. Crear Pull Request

## 📄 Licencia

Apache 2.0

## 📧 Contacto

Equipo de Desarrollo - dev@bancocuscatlan.com
