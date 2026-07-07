# 🏦 Banco Cuscatlán API

API REST para el sistema bancario de Banco Cuscatlán construida con Spring Boot 3 y Java 21.

## 📋 Tabla de Contenidos
- [Tecnologías](#tecnologías)
- [Requisitos](#requisitos)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Configuración](#configuración)
- [Ejecución](#ejecución)
- [Docker](#docker)
- [Requests HTTP](#requests-http)
- [Endpoints](#endpoints)
- [Testing](#testing)
- [Decisiones Técnicas](#decisiones-técnicas)

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
src/main/java/com/pruebatecnica/bancocuscatlan/
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
CREATE DATABASE coworking;
```

La configuración `dev` espera:
- **Host:** localhost
- **Puerto:** 5432
- **Database:** coworking
- **Usuario:** postgres
- **Password:** 12345

Puedes modificar estos valores en `src/main/resources/application-dev.yml`.

### Conexión Docker vs conexión local

Si levantas la app con `docker compose up --build`, la base de datos del contenedor usa:

- **Host:** localhost
- **Puerto:** 5433
- **Database:** coworking
- **Usuario:** postgres
- **Password:** 12345

Esa conexión es independiente de la que tengas en DBeaver para tu PostgreSQL local. Si tu conexión actual usa otra contraseña, no vas a ver la base `coworking` del contenedor porque estás apuntando a otra instancia.

Si en DBeaver ves `coworking` pero no aparecen tablas, revisa que la conexión esté apuntando al mismo host/puerto que la app:

- Para el contenedor con el compose de desarrollo: `localhost:5433`
- Para `docker-compose.prod.yml` no hay puerto publicado al host, así que DBeaver no puede verlo desde Windows salvo que agregues un mapeo temporal

En la conexión de DBeaver verifica también que el esquema `public` esté expandido y que no tengas filtros de objetos activos.

En DBeaver, crea una conexión nueva o edita una existente para el contenedor con los datos anteriores y luego refresca el árbol de bases.

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

## 🐳 Docker

El proyecto incluye un `Dockerfile` multi-stage y dos archivos de compose:

- `docker-compose.yml` para desarrollo local.
- `docker-compose.prod.yml` para un despliegue más cercano a producción.

### Levantar todo con Docker Compose

```bash
docker compose up --build
```

Ese comando usa por defecto `docker-compose.yml`, que es el entorno `dev`.

### Levantar el entorno prod

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up --build
```

La API queda disponible en:

- `http://localhost:8080`

La base de datos queda disponible en:

- `localhost:5432`

El compose de desarrollo deja la contraseña `12345` para facilitar las pruebas locales. El compose de producción toma secretos y contraseñas desde variables de entorno.

En producción no se publican puertos al host; el despliegue está pensado para una red interna o un proxy reverso.

## 🧾 Requests HTTP

Se incluye el archivo [requests.http](requests.http) con ejemplos listos para usar en VS Code o IntelliJ:

- registro y login
- creación y listado de espacios
- creación y cancelación de reservas
- reporte de reservas y ocupación
- health y actuator
- validación mock de pago

El archivo usa variables `@baseUrl`, `@userToken` y `@adminToken` para que puedas pegar el JWT real de cada rol.

## 🗃️ Migraciones con Flyway

Flyway se encarga de versionar el esquema de la base de datos y ejecuta automáticamente las migraciones ubicadas en `src/main/resources/db/migration` al arrancar la aplicación o al correr los tests.

Puntos importantes:

- La base de datos como tal debe existir antes de que Flyway conecte; eso se resuelve con Docker Compose, Testcontainers o un script de bootstrap externo.
- La primera migración del proyecto es la creación del esquema/tablas: `V1__create_coworking_schema.sql`.
- Flyway guarda cada ejecución en la tabla `flyway_schema_history`, que es normal y esperada; esa tabla evita re-ejecutar scripts ya aplicados y permite saber qué versión se ejecutó.
- Si agregas cambios de base de datos, crea una nueva migración numerada, por ejemplo `V2__add_indexes.sql`.

Si necesitas crear la base manualmente en local, usa el script raíz [`database-schema.sql`](database-schema.sql) como referencia o ejecuta `CREATE DATABASE coworking;` antes de levantar la app.

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

## 💳 Pago Simulado y Resiliencia

### Validación de pago externa simulada

Se implementó un proveedor de pago mock para simular una integración externa:

```http
POST /mock/payments/validate
```

La creación de reservas invoca este servicio. Reglas:

- Si el pago es aprobado, la reserva pasa a `CONFIRMED`.
- Si el pago falla o el servicio externo no responde, la reserva se crea en `PENDING_PAYMENT`.

### Circuit Breaker y fallback

La llamada al pago está protegida con Resilience4j (`paymentValidation`) para evitar que una caída externa tumbe la API.

Configuración clave:

- failure rate threshold
- sliding window size
- wait duration in open state
- timeout (TimeLimiter)

Fallback aplicado:

- no rompe la petición HTTP de reserva
- deja la reserva en `PENDING_PAYMENT`

El estado del circuito se expone por Actuator (`/actuator/circuitbreakers`, `/actuator/circuitbreakerevents`).

## 📣 Observer y Notificación Asíncrona

Se aplicó explícitamente el patrón GoF **Observer** con eventos de dominio:

- `ReservationConfirmedEvent`
- `ReservationStatusChangedEvent`
- `SpaceChangedEvent`

Flujo:

1. El servicio de reservas confirma o cambia estado.
2. Publica un evento con `ApplicationEventPublisher`.
3. Listeners reaccionan de forma desacoplada:
  - notificación simulada por log (asíncrona con `@Async`)
  - invalidación de cache de reportes

Este enfoque evita acoplar lógica transversal (correo, cache, auditoría) al `ReservationService` con cadenas de `if/else`.

## 📊 Reporte de Ocupación y Cache

Endpoint:

```http
GET /api/reports/occupancy?from=2026-07-01&to=2026-07-31
```

Disponible solo para `ADMIN`.

El reporte usa `@Cacheable` para evitar recálculos costosos. Se calcula por espacio:

`horas confirmadas en rango / horas disponibles del rango * 100`

Modelo actual de horas disponibles: **24h por día** en el rango.

Invalidación (`@CacheEvict`) cuando cambia información crítica:

- reserva confirmada
- reserva cancelada
- espacio creado/actualizado/desactivado

Motivo de cache: reducir latencia y carga de consultas agregadas sobre reservas históricas, manteniendo consistencia mediante invalidación por evento.

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

La API usa **Spring Security + JWT (Bearer Token)** con autorización por roles.

### Endpoints públicos

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/health`
- `/actuator/**`
- `/swagger-ui/**`
- `/api-docs/**`

### Flujo de autenticación

1. Registrar usuario:

## 🧠 Decisiones Técnicas

### Arquitectura

La aplicación está organizada en capas: controller, service, repository, dto, mapper, security y config. Esa separación permite cambiar reglas de negocio sin mezclar validación HTTP, persistencia y seguridad.

### JWT y roles

Se usa Spring Security con JWT Bearer para mantener sesiones sin estado. `USER` puede operar sobre sus propias reservas y `ADMIN` puede gestionar recursos globales.

### DTOs y validación

Los controladores exponen DTOs de entrada y salida en lugar de entidades JPA. Eso reduce acoplamiento y evita filtrar campos internos. Bean Validation protege el borde HTTP.

### `@ConfigurationProperties`

Las propiedades `app.jwt`, `app.cache` y `app.payment` se agrupan en una clase tipada para evitar cadenas sueltas con `@Value`.

### Cache y eventos

El reporte de ocupación se cachea y se invalida por eventos de dominio. Esto mantiene un buen balance entre rendimiento y consistencia.

### Resilience4j

La validación de pago está protegida con Circuit Breaker y TimeLimiter. Si el proveedor simulado falla, la reserva no se cae: queda en `PENDING_PAYMENT`.

### Java 21

El proyecto compila y corre sobre Java 21. Esa es la versión correcta para esta base de código y es la que se documenta en este repositorio.

```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "Raul Valencia",
  "email": "raul@email.com",
  "password": "Password123"
}
```

2. Iniciar sesión:

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "raul@email.com",
  "password": "Password123"
}
```

3. Usar token en rutas protegidas:

```http
Authorization: Bearer <jwt>
```

### Matriz de permisos

- **ADMIN**
  - CRUD completo de espacios
  - Ver todas las reservas
  - Gestionar estado de todas las reservas
  - Ver reportes
- **USER**
  - Crear reservas
  - Ver solo sus reservas
  - Cancelar solo sus reservas

### Respuestas de seguridad

- `401 Unauthorized`: token ausente/inválido
- `403 Forbidden`: autenticado pero sin permisos

## 📝 Próximos Pasos

- [ ] Implementar entidades de dominio (Cliente, Cuenta, Transacción, etc.)
- [ ] Crear servicios de negocio
- [ ] Implementar repositories
- [ ] Agregar mappers con MapStruct
- [x] Implementar autenticación JWT
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
