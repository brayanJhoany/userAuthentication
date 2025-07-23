# userAuthentication

Proyecto de autenticación de usuarios con Spring Boot, JWT y MySQL, dockerizado para desarrollo y pruebas.

## Características
- Registro y login de usuarios
- Autenticación con JWT
- Persistencia en MySQL
- Variables de entorno configurables
- Docker y Docker Compose listos para desarrollo

## Requisitos
- Docker
- Docker Compose

## Uso rápido

1. Clona el repositorio:
   ```sh
   git clone git@github.com:brayanJhoany/userAuthentication.git
   cd userAuthentication
   ```
2. Levanta los servicios:
   ```sh
   docker-compose up --build
   ```
3. La API estará disponible en: `http://localhost:80/api/v1/auth`


## Endpoints principales

### Autenticación (`/api/v1/auth`)
- `POST /api/v1/auth/login` — Inicia sesión de usuario y retorna un JWT.
- `POST /api/v1/auth/register` — Registra un nuevo usuario y retorna un JWT.

### Usuarios (`/api/v1/users`)
- `GET /api/v1/users` — Lista usuarios paginados, permite filtrar por email y username.
- `GET /api/v1/users/{id}` — Obtiene un usuario por su ID.
- `POST /api/v1/users` — Crea un usuario (requiere datos válidos).
- `PUT /api/v1/users/{id}` — Actualiza los datos de un usuario existente.
- `DELETE /api/v1/users/{id}` — Elimina un usuario por su ID.

## Librerías principales utilizadas

- **Spring Boot Web**: Para la creación de APIs REST.
- **Spring Boot Data JPA**: Acceso y persistencia en base de datos relacional.
- **Spring Boot Security**: Seguridad y autenticación.
- **Spring Boot Validation**: Validación de datos con anotaciones.
- **Lombok**: Reducción de código boilerplate (getters, setters, etc.).
- **MySQL Connector/J**: Conector JDBC para MySQL.
- **JJWT**: Manejo de JSON Web Tokens (JWT) para autenticación.
- **DevTools**: Recarga automática en desarrollo.
- **JUnit, AssertJ, Spring Security Test, H2**: Pruebas unitarias y de integración.



## Gestión de variables de entorno

Las variables de entorno NO se declaran directamente en la carpeta `resources`. En este proyecto, la configuración se gestiona mediante archivos `.env` en la raíz del proyecto:

- `.env.dev` — Variables para desarrollo local (MySQL, perfil dev, etc.)
- `.env.test` — Variables para pruebas (H2 en memoria, perfil test)
- `.env.prod` — Variables para producción (ajusta según tu entorno real)


El archivo `.env` adecuado se referencia en `docker-compose.yml` usando la opción `env_file`, y Docker lo pasa como variables de entorno al contenedor. Spring Boot toma estas variables y sobrescribe las propiedades de los archivos en `src/main/resources` (`application.properties`, `application-dev.properties`, etc.).

Esto permite separar la configuración sensible y específica de cada entorno fuera del código fuente.

## Configuración del ambiente de test

Para pruebas, el proyecto utiliza un archivo `.env.test` con una base de datos H2 en memoria y configuración segura para testeo. Puedes crear este archivo con el siguiente contenido de ejemplo:

```env
spring.application.name=SpringDocker
server.port=80
spring.profiles.active=test

jwt.secret_key=N9D5wYWWb69ZFZUyEcig+lRvIpUpLRnoNq/FT1QlYzRh2yecZSRULWEcHkdhMm5Q
jwt.expiration.time=86400000

spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
spring.jpa.show-sql=true
```

Este archivo no contiene datos sensibles y puede ser usado para ejecutar pruebas locales o CI/CD sin riesgo de exponer información privada.

## Estructura del proyecto
- `src/main/java/com/app` — Código fuente principal
- `src/main/resources` — Archivos de configuración
- `Dockerfile` y `docker-compose.yml` — Contenedores y orquestación

## Notas
- El servicio MySQL y la app se comunican por red interna de Docker.
- Cambia los valores de las variables de entorno según tu entorno de desarrollo.


