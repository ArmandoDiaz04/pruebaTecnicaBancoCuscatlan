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
- **MapStruct 1.6.3** - Mapeo de DTOs
- **Lombok** - Reducción de boilerplate
- **Springdoc OpenAPI 2.8.16** - Documentación API
- **Resilience4j** - Circuit Breaker
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
│   ├── SecurityConfig.java
│   ├── OpenApiConfig.java
│   └── CacheConfig.java
├── security/                       # Configuración de seguridad
├── client/                         # Clientes HTTP externos
├── event/                          # Eventos de dominio
└── strategy/                       # Patrones Strategy/State
```

## ⚙️ Configuración

### Base de Datos

Crear base de datos PostgreSQL:

```sql
CREATE DATABASE banco_cuscatlan;
```

### application.properties

La configuración por defecto espera:
- **Host:** localhost
- **Puerto:** 5432
- **Database:** banco_cuscatlan
- **Usuario:** postgres
- **Password:** postgres

Puedes modificar estos valores en `src/main/resources/application.properties`

## 🏃 Ejecución

### Opción 1: Maven

```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar la aplicación
mvn spring-boot:run
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
