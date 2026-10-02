# Melody Generator — Backend

Backend del generador de melodías: genera melodías musicalmente correctas a partir de una
escala, y gestiona los usuarios de la aplicación.

Este repositorio es **la parte de servidor**. El frontend (Angular) tiene su propio repositorio.

## Qué hace el proyecto

El generador no produce notas al azar. Aplica teoría musical:

- **Todos los acordes son diatónicos** a la escala pedida. En `Do menor`, el `ii` es
  `D F A♭` (disminuido) y no `D F A`: la tabla de modos manda, no el nombre del grado.
- **Los tiempos fuertes llevan nota del acorde.** Las unidades 0 y 8 de cada compás
  (negras 1 y 3) son notas del acorde; la melodía respira con notas de paso en los tiempos
  débiles, y nunca más de una followed.
- **La melodía resuelve.** La última nota de cada melodía es la raíz o la quinta del acorde,
  nunca la tercera.
- **Los silencios tienen reglas.** Van detrás de la última nota del compás que está sobre la
  dominante, y solo si esa nota cae en un tiempo débil. Nunca en el último compás, porque
  entonces la cadencia final se quedaría sin resolver.
- **Es determinista.** `MelodyMaker.generateMelody(scale, seed)` con la misma semilla
  devuelve siempre la misma melodía. Es lo que hace que el resultado se pueda medir.

Todo esto se comprobó midiendo 8000 compases por modo (2000 melodías × 4 compases ×
`MAJOR` y `MINOR`): 0 notas fuera de la escala, todos los compases suman 16 unidades,
0 acentos con nota ajena al acorde, 0 melodías que acaban en la tercera.

## Stack

| | |
|---|---|
| Java | 21 (LTS) |
| Spring Boot | 4.1.1 |
| Persistencia | Spring Data JPA · Hibernate 7 · PostgreSQL |
| Seguridad | `spring-security-crypto` (BCrypt) · `jjwt` 0.12.6 |
| Documentación | springdoc-openapi (Swagger UI) |
| Otros | Lombok |

## Endpoints

| Método | Ruta | Qué hace |
|---|---|---|
| `POST` | `/api/melody/generate` | Genera una melodía. Body: `{ "rootNote": "C", "mode": "MAJOR" }` |
| `POST` | `/api/users` | Registra un usuario (contraseña hasheada con BCrypt) |
| `GET` | `/api/users` | Lista usuarios |
| `GET` | `/api/users/{id}` | Un usuario |
| `PATCH` | `/api/users/{id}` | Actualiza parcialmente |
| `DELETE` | `/api/users/{id}` | Borra un usuario |
| `GET` | `/api/dev/token/{id}/{email}` | **Temporal.** Firma un token a mano, solo para verlo en Swagger |

Swagger: `http://localhost:8080/swagger-ui.html`

`NoteName` acepta `C`, `C_SHARP`, `D_FLAT`… y también `REST`. `Mode` acepta `MAJOR` y `MINOR`.

## Cómo arrancarlo

Dos secretos vienen de variable de entorno, y la aplicación **no arranca sin ellos**
(fail fast):

```powershell
$env:DB_PASSWORD="..."
$env:JWT_SECRET="...al menos 32 caracteres..."
.\mvnw.cmd spring-boot:run
```

`JWT_SECRET` tiene un mínimo de 32 bytes: es lo que exige el algoritmo HMAC-SHA de JWT,
y `jjwt` falla antes de firmar si la clave es débil.

El esquema lo crea Hibernate (`ddl-auto=update`), así que hace falta una base de datos
vacía llamada `melody_generator` en PostgreSQL.

## Estructura

```
model/       las reglas de negocio: lo que usa el generador (NoteName, Mode, Scale, Duration…)
entity/      lo que se guarda en la base de datos (UserEntity, RoleEntity, MelodyEntity)
dto/         lo que sale por la API (dto/melody, dto/user, dto/auth)
repository/  las puertas a la base de datos
service/     la lógica de aplicación (UserService, MelodyService, TokenService)
controller/  los endpoints
musicLogic/  el generador: MusicTheory, MelodyMaker, MeasureGenerator, MeasureNoteGenerator
config/      los beans (PasswordEncoder)
```

La separación entre `model`, `entity` y `dto` es la decisión de diseño que más pesa aquí:
son tres cosas distintas con tres consumidores distintos. El mismo `Note` no puede ser las
tres, porque cualquier cambio en la base de datos cambiaría el JSON, y al revés.

## Qué abarca el proyecto

El backend tiene tres bloques:

1. **El generador de melodías** — el núcleo. Recibe una escala y devuelve una melodía que
   se pueda tocar sin que suene mal.
2. **Los usuarios** — registro, login y quién es quién.
3. **La protección de la API** — qué endpoints son públicos y cuáles no.

El 1 está terminado. El 2 está a medias. El 3 no ha empezado.

## Lo que HAY HECHO

- **Generador completo**: escala diatónica, acordes por tabla de modo, acentos en los
  tiempos fuertes, resolución de la cadencia, silencios, determinismo por semilla.
- **Contrato JSON cerrado con el frontend**: la respuesta envuelve la melodía en
  `{ "melody": … }` y el frontend la desenvuelve en un solo sitio.
- **Usuarios**: CRUD completo, con contraseñas hasheadas con BCrypt (`PasswordEncoder`
  como bean, aplicado en `save()` y en `partialUpdate()`).
- **Roles**: enum `Role` (`ADMIN`, `USER`) y entidad `RoleEntity` con `@Enumerated(STRING)`.
  La relación N:M usuario↔roles genera la tabla `user_roles`.
- **Firma de tokens**: `TokenService` firma un JWT con `jjwt` llevando `sub`, `email` y `exp`.
  Comprobado: el token se abre en jwt.io, sus dos primeras partes van en claro y la firma
  valida contra la clave.

## Lo que NO hay hecho todavía

Nada de lo de esta lista existe en el código. Está aquí para que se vea qué falta y por
qué, no para dar la impresión de que esté.

### Autenticación

- **`POST /api/auth/register` y `POST /api/auth/login`.** No existen. El token se firma
  pero **nadie lo verifica**: se genera con un endpoint de desarrollo (`/api/dev/token`)
  que regala un token de cualquier id. Eso es una puerta trasera y tiene que desaparecer.
- **`RoleRepository`** y el claim `roles` dentro del token.
- **El logout**, que en el frontend será borrar el token.

### Autorización y protección de la API

- **`SecurityFilterChain` en modo stateless.** Ahora mismo **no hay filtro**: todos los
  endpoints son públicos, incluido el borrado de usuarios.
- **Los códigos 401 y 403**, y decidir qué es público y qué no.
- **`@AuthenticationPrincipal`**, para no ir a la base de datos en cada request.
- **`MelodyRepository`.** Sin él, `MelodyEntity` **no se puede guardar**: no hay forma de
  guardar una melodía generada, así que la tabla `melodies` está vacía.
- **Autorización por propietario**: que nadie pueda borrar la melodía de otro.

### Manejo de errores

- **No hay manejador central de excepciones.** No existe `@RestControllerAdvice`. Cada
  controller decide su propio 404 a mano, y hay **dos formatos de error distintos** en la
  API según por dónde venga el fallo.

### Persistencia

- **Flyway**, en lugar de `ddl-auto=update`. El esquema lo crea Hibernate en cada arranque,
  lo cual significa que **nadie ha revisado el esquema**: no hay `V1__init.sql`, ni
  `ON DELETE CASCADE` al borrar un usuario, ni índice en `melodies(user_id, created_at)`.

### Tests

- **Cero tests.** Solo el archivo vacío que genera IntelliJ.

### Contrato

- `Duration` produce `SIXTEENTH` en el 29 % de las notas, y ese valor **no está
  documentado** en el contrato. El frontend ya lo pinta, pero el backend no lo declara.

## Estado

Es un proyecto en construcción. El bloque del generador está terminado y medido; el de
autenticación está empezado; el de protección de la API no ha empezado. Los tests y las
migraciones no han empezado.
