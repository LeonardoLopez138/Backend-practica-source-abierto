# Ruta: construir el examen Caterpillar paso a paso

Reemplaza a `RUTA_DE_APRENDIZAJE.md` (el caso Staff). Ahora construyes **el examen real**, un archivo a la vez.

- **BASE** = `...\learning-center-platform-master-main\src\main\java\com\acme\center\platform\`
- **TUYO** = `...\com-acme-nubi-platform\src\main\java\com\acme\nubi\platform\`

Método de cada paso: **estudias el archivo del profesor → escribes el tuyo → compilas → me dices "revisa paso N"**.
Yo no te doy el código; te doy pistas y reviso.

> Sobre el package raíz: el examen pedirá `com.caterpillar.platform.u<código>`. Tú trabajas en `com.acme.nubi.platform` para no perder tiempo ahora. Al final, practica el cambio con IntelliJ (clic derecho al package → Refactor → Rename) y verifica que compile.

---

## Lo que cambia respecto al caso Ro (las trampas de este examen)

| Novedad | Dónde te toca |
|---|---|
| `equipmentUnitId` **se genera en el backend** (no viene en el body) | Paso 1 y 11 |
| La unicidad es **compuesta**: `serialNumber` + `assignedMineSite` | Paso 6, 10, 11 |
| `serialNumber` va **encriptado en BD** y se desencripta al responder | Paso 7 (el más difícil) |
| El response incluye `createdAt` y `updatedAt` (el dominio del profesor no los tiene) | Paso 5 y 9 |
| `GeoCoordinate` son `double` con rangos, y va en **shared** | Paso 2 |
| Endpoint **sin path variable**: `POST /api/v1/equipment-units` | Paso 14 |
| Esquema MySQL llamado `caterpillar`, puerto 8080 | Paso 16 |

---

## Reglas del examen, resumidas

**Aggregate `MiningEquipmentUnit`** (bounded context `mining`):

| Campo | Tipo | Notas |
|---|---|---|
| id | Long | autogenerado, PK |
| equipmentUnitId | `CatMiningEquipmentUnitId` | autogenerado por ti, obligatorio |
| model | String | obligatorio |
| serialNumber | String | obligatorio, **encriptado en BD** |
| operationStatus | `OperationStatus` | `ACTIVE`, `IN_MAINTENANCE`, `INACTIVE` |
| assignedMineSite | String | obligatorio |
| gpsLocation | `GeoCoordinate` (shared) | latitude [-90, 90], longitude [-180, 180] |
| hoursOfOperation | int | obligatorio |
| createdAt / updatedAt | fechas | auditoría |

**Reglas**: no dos unidades con el mismo `serialNumber` en el mismo `assignedMineSite` (409); coordenadas inválidas → 400; `CatMiningEquipmentUnitId` = UUID no nulo y no vacío.

**Request (body de POST)**: `model, serialNumber, operationStatus, assignedMineSite, gpsLatitude, gpsLongitude, hoursOfOperation`.
**Response 201**: `id, equipmentUnitId, model, serialNumber, operationStatus, assignedMineSite, gpsLatitude, gpsLongitude, hoursOfOperation, createdAt, updatedAt`.

---

## La cadena

```
Paso 0 Setup
   ↓
Value Objects: CatMiningEquipmentUnitId → GeoCoordinate (shared) → OperationStatus
   ↓
Command → Aggregate → Repository (puerto) + CommandService (interfaz)   ← "a la par"
   ↓
Persistencia: Converters (id y serialNumber encriptado) → Embeddable → Entity → PersistenceAssembler → PersistenceRepository → RepositoryImpl
   ↓
CommandServiceImpl
   ↓
Resources → Assemblers → Controller
   ↓
Errores + i18n → Config/BD → Pruebas → Entrega
```

---

## Paso 0: Setup (casi listo, te falta confirmar)

Ya compila. Verifica estas 3 cosas, que aún no vi en tu proyecto:
- `@EnableJpaAuditing` en `ComAcmeNubiPlatformApplication`.
- `application.properties`: `spring.jpa.hibernate.naming.physical-strategy=...SnakeCaseWithPluralizedTablePhysicalNamingStrategy`, `spring.messages.basename=messages`, `spring.messages.encoding=UTF-8`.
- `messages.properties`, `messages_es.properties`, `messages_en.properties`, `ValidationMessages*.properties` en `resources`.

---

## Paso 1: `CatMiningEquipmentUnitId` (VO de UUID, se auto-genera)

**Estudia**: `learning/domain/model/valueobjects/AcmeStudentRecordId.java`

**Fíjate**: tiene un constructor **sin argumentos** que genera un UUID nuevo. Eso es exactamente "se genera al momento del registro".

**Tu tarea**: `mining/domain/model/valueobjects/CatMiningEquipmentUnitId.java`. UUID no nulo y no vacío (piensa qué significa "vacío" para un UUID, y si guardas `UUID` o `String`). Constructor sin argumentos que genera uno nuevo.

**Listo cuando**: el constructor vacío genera valores distintos; null y el UUID de ceros fallan.

## Paso 2: `GeoCoordinate` (VO compuesto, en **shared**)

**Estudia**: `profiles/domain/model/valueobjects/PersonName.java` y `StreetAddress.java`

**Fíjate**: un record con varios campos y una validación por campo, cada una con su mensaje.

**Tu tarea**: `shared/domain/model/valueobjects/GeoCoordinate.java` con `double latitude, double longitude`. Pistas: ¿qué pasa con `NaN`? Un `double` puede serlo y `NaN < -90` es `false`. Piénsalo y cúbrelo.

**Listo cuando**: -90, 90, -180, 180 pasan; -90.0001, 90.0001, ±180.0001 y `NaN` fallan.

## Paso 3: `OperationStatus`

**Estudia**: `learning/domain/model/valueobjects/EnrollmentStatus.java`

**Tu tarea**: `mining/domain/model/valueobjects/OperationStatus.java`. El enunciado dice "VO que encapsula un enum"; un enum en `valueobjects` como `EnrollmentStatus` cumple. Valores: `ACTIVE, IN_MAINTENANCE, INACTIVE`.
Pregunta: ¿qué pasa si en el JSON llega `"BROKEN"`? ¿Quién lo detecta (Jackson, tu código)? Lo resolvemos en el Paso 11/15.

## Paso 4: Command

**Estudia**: `profiles/application/commands/CreateProfileCommand.java`

**Tu tarea**: `mining/application/commands/CreateMiningEquipmentUnitCommand.java`, un record de **primitivos**. Sin `id` ni `equipmentUnitId`.

## Paso 5: Aggregate

**Estudia**: `profiles/domain/model/aggregates/Profile.java`

**Fíjate**: sin JPA, `requireNonNull`, `reconstitute`, constructor desde primitivos que dispara las validaciones de los VO.

**Tu tarea**: `mining/domain/model/aggregates/MiningEquipmentUnit.java`.
Trampa: el response pide `createdAt`/`updatedAt`, pero el aggregate del profesor no los tiene (viven en la entidad JPA). Decide cómo llegan al resource: por ejemplo, campos de solo lectura en el aggregate que se llenan **solo** en `reconstitute`. Piensa por qué un objeto recién creado no los tiene todavía.

## Paso 6 (a la par): Repository + CommandService

**Estudia**: `ProfileRepository.java` y `ProfileCommandService.java`

**Tu tarea**:
- `MiningEquipmentUnitRepository`: `save` y `existsBySerialNumberAndAssignedMineSite(...)`.
- `MiningEquipmentUnitCommandService` con `Result<MiningEquipmentUnit, ApplicationError> handle(CreateMiningEquipmentUnitCommand)`.

## Paso 7: Converters y Embeddable (el paso difícil por la encriptación)

**Estudia**: `EmailAddressPersistenceConverter.java` y `PersonNamePersistenceEmbeddable.java`

**Tu tarea**:
1. `CatMiningEquipmentUnitIdPersistenceConverter` (id ⇄ String), igual que el de email.
2. `GeoCoordinatePersistenceEmbeddable` (en `shared/infrastructure/persistence/jpa/embeddables`).
3. `SerialNumberEncryptionConverter` (`AttributeConverter<String, String>`): encripta en `convertToDatabaseColumn`, desencripta en `convertToEntityAttribute`.

**Pistas para la encriptación**:
- Usa `javax.crypto` (AES) + `Base64` para guardar el texto cifrado.
- La clave va en `application.properties`, no en el código. Los converters pueden ser `@Component` y recibir `@Value`.
- **Ojo con la regla de unicidad**: para buscar "mismo serialNumber en el mismo site" comparas el valor cifrado. Si cada cifrado sale distinto (IV aleatorio), el `exists` nunca encontrará nada. ¿Qué tipo de cifrado necesitas para que el mismo texto dé siempre el mismo resultado? Es un trade-off de seguridad que puedes comentar en Javadoc.
- La columna debe ser lo bastante larga: un texto cifrado y en Base64 es más largo que el original.

**Listo cuando**: guardas una unidad y en MySQL el `serial_number` se ve como texto ilegible; al leerla vuelve en claro.

## Paso 8: Persistence Entity

**Estudia**: `ProfilePersistenceEntity.java` y `AuditableAbstractPersistenceEntity.java`

**Tu tarea**: `MiningEquipmentUnitPersistenceEntity` (`@Table(name = "mining_equipment_units")`). Hereda de la auditable. Usa `@Convert` en `equipmentUnitId` y `serialNumber`, `@Embedded` para la coordenada, `@Enumerated(EnumType.STRING)` para el status. Piensa qué columnas son `nullable = false` y cuáles `unique`; ¿`serialNumber` solo es unique? (no: la unicidad es compuesta con el site).

## Paso 9: Persistence Assembler

**Estudia**: `ProfilePersistenceAssembler.java`

**Tu tarea**: `MiningEquipmentUnitPersistenceAssembler` en ambas direcciones. En `toDomainFromPersistence` pasa `createdAt` y `updatedAt` que decidiste en el Paso 5. Ojo: la entidad usa `java.util.Date`.

## Paso 10: Spring Data repository + adapter

**Estudia**: `ProfilePersistenceRepository.java` y `ProfileRepositoryImpl.java` (ignora eventos)

**Tu tarea**: `MiningEquipmentUnitPersistenceRepository` con la consulta de existencia (serialNumber + assignedMineSite) y `MiningEquipmentUnitRepositoryImpl`.

**Listo cuando**: la app arranca y crea la tabla `mining_equipment_units`.

## Paso 11: CommandServiceImpl

**Estudia**: `ProfileCommandServiceImpl.java`

**Tu tarea**: `MiningEquipmentUnitCommandServiceImpl`:
1. Construye VO dentro de un `try` (coordenadas inválidas → `IllegalArgumentException` → 400).
2. Consulta duplicado → `ApplicationError.conflict(...)`.
3. Genera `new CatMiningEquipmentUnitId()`.
4. `save` y `Result.success`.
5. `catch (IllegalArgumentException)` → validationError; `catch (Exception)` → unexpected.

Pregunta: `operationStatus` llega como String en el command. ¿Cómo lo conviertes a enum sin que un valor desconocido te devuelva 500? (`valueOf` lanza `IllegalArgumentException`... ¿y dónde lo atrapas?)

## Paso 12: Resources

**Estudia**: `CreateProfileResource.java` y `ProfileResource.java`

**Tu tarea**: `CreateMiningEquipmentUnitResource` (con `@NotBlank`, `@NotNull`, `@Schema`) y `MiningEquipmentUnitResource` con exactamente los nombres del enunciado (`gpsLatitude`, `gpsLongitude`, `createdAt`, `updatedAt`...).

## Paso 13: Assemblers REST

**Estudia**: `CreateProfileCommandFromResourceAssembler.java` y `ProfileResourceFromEntityAssembler.java`

**Tu tarea**: `CreateMiningEquipmentUnitCommandFromResourceAssembler` y `MiningEquipmentUnitResourceFromEntityAssembler` (aquí se aplanan `GeoCoordinate` y los VO).

## Paso 14: Controller

**Estudia**: `ProfilesController.java` (solo `createProfile`) y `ResponseEntityAssembler.java`

**Tu tarea**: `MiningEquipmentUnitsController` en `/api/v1/equipment-units`, solo `@PostMapping`, 201 en éxito. `@Tag`, `@Operation`, `@ApiResponses` (201, 400, 409) en inglés.

## Paso 15: Errores e i18n

**Estudia**: `GlobalExceptionHandler`, `ProblemDetailFromApplicationErrorAssembler`, `messages*.properties`

**Tu tarea**: agrega las claves para tu código de conflicto en `messages.properties` (inglés; el examen pide mensajes en inglés). Prueba un enum inválido en el JSON (`"operationStatus": "BROKEN"`) y un JSON mal formado: ¿qué status devuelve hoy? Si es 500, mira qué excepción de Spring se lanza (`HttpMessageNotReadableException`) y decide si el `GlobalExceptionHandler` debe manejarla.

## Paso 16: Config y pruebas manuales

- `application.properties`: puerto `8080`, URL con la BD `caterpillar` (`createDatabaseIfNotExist=true`), clave de encriptación.
- Prueba con Swagger UI (`/swagger-ui.html`):

| Caso | Esperado |
|---|---|
| Body válido | 201 con `id`, `equipmentUnitId` (UUID), `createdAt`, `updatedAt` |
| `serial_number` en MySQL | ilegible; en el response, claro |
| Mismo `serialNumber` + mismo site | 409 |
| Mismo `serialNumber`, otro site | 201 |
| `gpsLatitude` 91 | 400 |
| `gpsLongitude` -181 | 400 |
| `operationStatus` = `BROKEN` | 400 |
| Falta `model` | 400 |

## Paso 17: Entrega

- Javadoc en inglés con `@author` (tu nombre completo) en clases y métodos relevantes.
- `README.md` en inglés: descripción y autor.
- URL en minúsculas y plural; tablas plural y snake_case.
- Cambiar el package raíz (nota del inicio) y comprobar que compila.
- Comprimir **sin** `target`.

---

## Tabla rápida: tuyo ← profesor

| Tuyo | Profesor |
|---|---|
| `CatMiningEquipmentUnitId` | `AcmeStudentRecordId` |
| `GeoCoordinate` | `PersonName` / `StreetAddress` |
| `OperationStatus` | `EnrollmentStatus` |
| `CreateMiningEquipmentUnitCommand` | `CreateProfileCommand` |
| `MiningEquipmentUnit` | `Profile` |
| `MiningEquipmentUnitRepository` | `ProfileRepository` |
| `MiningEquipmentUnitCommandService` / `Impl` | `ProfileCommandService` / `Impl` |
| `*PersistenceConverter`, `SerialNumberEncryptionConverter` | `EmailAddressPersistenceConverter` |
| `GeoCoordinatePersistenceEmbeddable` | `PersonNamePersistenceEmbeddable` |
| `MiningEquipmentUnitPersistenceEntity` | `ProfilePersistenceEntity` |
| `MiningEquipmentUnitPersistenceAssembler` | `ProfilePersistenceAssembler` |
| `MiningEquipmentUnitPersistenceRepository` / `RepositoryImpl` | `ProfilePersistenceRepository` / `ProfileRepositoryImpl` |
| Resources y assemblers REST | `CreateProfileResource`, `ProfileResource` y sus assemblers |
| `MiningEquipmentUnitsController` | `ProfilesController` |

---

## Empieza aquí

Termina el **Paso 0** y escribe el **Paso 1** (`CatMiningEquipmentUnitId`). Cuando compile, dime "revisa paso 1".
