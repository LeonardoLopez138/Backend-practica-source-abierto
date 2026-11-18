# Guía de aprendizaje: DDD + Spring Boot (base: learning-center-platform)

Base de referencia: `Desktop\learning-center-platform-master-main (1)\learning-center-platform-master-main`
Tu proyecto: `Documents\Intellij Idea\com-acme-nubi-platform`

Todas las rutas de abajo son relativas a `src/main/java/com/acme/center/platform/` del proyecto base.

---

## 1. El mapa mental

Cada *bounded context* (carpeta de primer nivel: `profiles`, `iam`, `learning`, `shared`) tiene **4 capas**.
Una regla: **las dependencias apuntan hacia adentro**. `domain` no conoce a nadie.

```
interfaces      ← entra el HTTP (Controller, Resource, Assembler)
   ↓
application     ← orquesta el caso de uso (Command, CommandService, Query, QueryService)
   ↓
domain          ← el negocio puro (Aggregate, ValueObject, Repository = interfaz)
   ↑
infrastructure  ← implementa lo técnico (JPA, adapters, converters, config)
```

Qué hace cada capa en el proyecto base (usa `profiles` como modelo, es el más simple):

| Capa | Archivo del base | Para qué sirve |
|---|---|---|
| domain / valueobjects | `profiles/domain/model/valueobjects/EmailAddress.java` | Un valor con reglas. **Se valida a sí mismo en el constructor**. |
| domain / aggregates | `profiles/domain/model/aggregates/Profile.java` | Raíz del agregado. Sin anotaciones JPA. Tiene `reconstitute(...)`. |
| domain / repositories | `profiles/domain/repositories/ProfileRepository.java` | **Interfaz** (puerto). Habla en términos de dominio. |
| application / commands | `profiles/application/commands/CreateProfileCommand.java` | Un `record` con datos primitivos: la intención del usuario. |
| application / commandservices | `ProfileCommandService.java` (interfaz) | Contrato del caso de uso. |
| application / internal | `ProfileCommandServiceImpl.java` | Aquí van las reglas que necesitan el repositorio (ej. "email ya existe" → conflict). |
| infrastructure / persistence | `ProfilePersistenceEntity.java`, `PersonNamePersistenceEmbeddable.java`, `EmailAddressPersistenceConverter.java` | El modelo **de base de datos**, separado del dominio. |
| infrastructure / assemblers | `ProfilePersistenceAssembler.java` | Traduce dominio ⇄ entidad JPA. |
| infrastructure / adapters | `ProfileRepositoryImpl.java` | Implementa el puerto usando Spring Data + el assembler. |
| interfaces / rest | `ProfilesController.java` | Solo traduce HTTP ⇄ casos de uso. Cero lógica de negocio. |
| interfaces / resources | `CreateProfileResource.java`, `ProfileResource.java` | JSON de entrada y de salida. |
| interfaces / transform | `CreateProfileCommandFromResourceAssembler.java`, `ProfileResourceFromEntityAssembler.java` | Resource → Command, y Aggregate → Resource. |

> Diferencia clave con muchos tutoriales: aquí el agregado de dominio **no es** la entidad JPA.
> Hay dos clases: `Profile` (dominio) y `ProfilePersistenceEntity` (JPA). El assembler las conecta.
> Por eso en el examen te piden "separación de Domain Concepts de Persistence Concepts".

## 2. El flujo completo de un POST (síguelo con el IDE abierto)

`POST /api/v1/profiles` →

1. `ProfilesController.createProfile` recibe `CreateProfileResource` (el `@Valid` revisa solo formato: no vacío, email).
2. `CreateProfileCommandFromResourceAssembler` lo convierte en `CreateProfileCommand`.
3. `ProfileCommandServiceImpl.handle(command)`:
   - construye los **Value Objects** (`new EmailAddress(...)`) → si algo está mal, lanzan `IllegalArgumentException`;
   - pregunta al repositorio si existe → `ApplicationError.conflict(...)`;
   - crea el `Profile`, llama `profileRepository.save(...)`;
   - el `catch (IllegalArgumentException)` lo convierte en `ApplicationError.validationError(...)`.
   - Devuelve `Result<Profile, ApplicationError>` (éxito **o** error, sin excepciones viajando).
4. `ProfileRepositoryImpl.save` → `ProfilePersistenceAssembler.toPersistenceFromDomain` → `ProfilePersistenceRepository.save` (JPA) → de vuelta a dominio con el `id` generado.
5. `ResponseEntityAssembler.toResponseEntityFromResult` decide: éxito → `201` + `ProfileResource`; error → `ProblemDetail` con el status correcto.
6. `ProblemDetailFromApplicationErrorAssembler` mapea el código a HTTP (`VALIDATION_ERROR`→400, `*_NOT_FOUND`→404, `*_CONFLICT`→409, `BUSINESS_RULE_VIOLATION`→422) y busca el mensaje en `messages*.properties` según el `Accept-Language`.

Esa tabla de códigos → status es **lo más importante a entender** para "responder con el HTTP correcto".

## 3. Cómo se escribe cada pieza (patrones)

### Value Object = `record` con validación en el constructor compacto
```java
public record AcmeStudentRecordId(String studentRecordId) {
    public AcmeStudentRecordId {
        if (studentRecordId == null || studentRecordId.isBlank())
            throw new IllegalArgumentException("...");
        // más reglas...
    }
}
```
Ver `EmailAddress.java` (regex) y `AcmeStudentRecordId.java` (UUID). Un record es inmutable e igual por valor: perfecto para VO.

### Enum (ej. `EnrollmentStatus`)
Se declara en `valueobjects`. En JPA se guarda con `@Enumerated(EnumType.STRING)` (ver `EnrollmentPersistenceEntity.java`). "Valor por defecto Draft" = lo decides en el agregado/command service, no en la BD.

### Aggregate
Clase normal, sin JPA. Constructor que valida `requireNonNull`, y un `static reconstitute(...)` para rearmarlo desde la BD. Extiende `AbstractDomainAggregateRoot` (en `shared`).

### VO en la base de datos: 3 opciones
- VO de **un solo valor** (EmailAddress) → `AttributeConverter` (`EmailAddressPersistenceConverter`).
- VO **compuesto** (PersonName, StreetAddress) → `@Embeddable` + `@Embedded` con `@AttributeOverrides` para nombrar columnas.
- Enum → `@Enumerated(STRING)`.

### Auditoría (createdAt / updatedAt)
`shared/.../AuditableAbstractPersistenceEntity.java` + `@EnableJpaAuditing` en la clase principal. Tu entidad solo hereda.

### Naming de tablas/columnas
`SnakeCaseWithPluralizedTablePhysicalNamingStrategy` (en `shared`) + la línea en `application.properties`. Necesita la dependencia `io.github.encryptorcode:pluralize:1.0.0` del `pom.xml` base.

### i18n
`LocaleConfiguration` (lee `Accept-Language`, soporta `en` y `es`) + `messages.properties` / `messages_es.properties`. Cada código de error puede tener una clave `error.<codigo-en-minusculas-con-guiones>.message`.

## 4. Qué debes copiar del `shared` y qué escribir tú

El examen dice que *shared* debe seguir el del proyecto base. Copia (y entiende línea por línea) estos:

- `shared/application/result/Result.java` y `ApplicationError.java`
- `shared/domain/model/aggregates/AbstractDomainAggregateRoot.java`
- `shared/infrastructure/persistence/jpa/entities/AuditableAbstractPersistenceEntity.java`
- `shared/infrastructure/persistence/jpa/configuration/strategy/SnakeCaseWithPluralizedTablePhysicalNamingStrategy.java`
- `shared/infrastructure/i18n/configuration/LocaleConfiguration.java`
- `shared/infrastructure/documentation/openapi/configuration/OpenApiConfiguration.java`
- `shared/interfaces/rest/GlobalExceptionHandler.java`
- `shared/interfaces/rest/transform/ResponseEntityAssembler.java` y `ProblemDetailFromApplicationErrorAssembler.java`

No necesitas para el examen: `iam` (seguridad/JWT), `DomainEventPublisher` y eventos, `ProfilesContextFacade` (ACL), Loom/virtual threads. El enunciado del examen dice que seguridad y testing no entran.

Todo el contexto `learning` es lo "grande": ignóralo salvo `StudentEnrollmentsController` si quieres ver una URL anidada.

## 5. Arreglos que necesita tu `pom.xml` / proyecto

1. `spring-boot-starter-parent` → `4.1.1` (tienes 4.1.2).
2. `java.version` → `27` (tienes 25; tu JDK instalado es 27).
3. Agrega la dependencia `pluralize` (`io.github.encryptorcode`, `1.0.0`).
4. Quita las dependencias sueltas `spring-boot` y `spring-boot-autoconfigure` con versión fija: el parent ya las gestiona.
5. `application.properties`: en MySQL "esquema" = base de datos. Para el examen usarías `.../ro_wa?createDatabaseIfNotExist=true`. En el ejercicio: `.../cold_chain_ws`.
6. Agrega al `application.properties` la línea del `physical-strategy` y `spring.messages.basename=messages`.

---

## 6. EL EJERCICIO (dominio ajeno al base)

Es **estructuralmente idéntico** a lo que pide el examen, pero con otro negocio: así practicas el patrón y no el caso.

### Caso: ColdChain (almacenes refrigerados)
ColdChain gestiona **Reservas de espacio refrigerado** (`StorageReservation`) hechas sobre un **almacén** (warehouse) específico.

**Aggregate Root `StorageReservation`**: `Id (Long)`, `ReservationCode (ReservationCode)`, `WarehouseId (WarehouseId)`, `StoragePeriod (StoragePeriod)`, `CargoProfile (CargoProfile)`, `Status (ReservationStatus)`, `HandlingNotes (String)`.

### Reglas de negocio
1. No pueden existir dos reservas con el mismo `ReservationCode` (global) → **conflict**.
2. `ReservationCode` (VO): envuelve un `String`. Debe cumplir la regex: prefijo `CC-`, luego **exactamente 2 letras mayúsculas**, un guion y **exactamente 5 dígitos** (ej. `CC-BX-20417`). Si no, `IllegalArgumentException`.
3. `WarehouseId` (VO): envuelve un `UUID`. No puede ser null ni el UUID nulo `00000000-0000-0000-0000-000000000000`.
4. `StoragePeriod` (VO): `LocalDate startDate`, `LocalDate endDate`.
   - Si `endDate` es **menor o igual** a `startDate` → `IllegalArgumentException`.
   - Si la duración supera **30 días** → `IllegalArgumentException` explicando el motivo.
   - Método `boolean isOccupiedOn(LocalDate date)`: true si `date` está entre start y end **inclusive**.
   - Método `long getDurationInDays()`.
5. `CargoProfile` (VO): `int palletCount` y `String cargoType`.
   - `palletCount` debe ser > 0 y ≤ 40; si no → excepción de **argumento fuera de rango** (`IllegalArgumentException` con mensaje de rango; opcionalmente crea tu propia excepción).
   - `cargoType`: texto **en mayúsculas**, entre **4 y 12 caracteres**, no nulo ni vacío (ej. `VACCINES`, `SEAFOOD`, `DAIRY`).
   - Método `int calculatePalletDays(int totalDays)` = `palletCount * totalDays`.
6. `ReservationStatus` (enum): `PENDING` (por defecto si no se especifica), `CONFIRMED`, `RELEASED`.
7. **Consistencia del subrecurso**: el `warehouseId` de la URL debe ser **exactamente igual** al `warehouseId` del body. Si no → `ApplicationError.conflict` (**409**) en la **capa de aplicación** (en el CommandService, no en el controller).
8. Es auditable (`createdAt`, `updatedAt`).
9. Mensajes de error localizados en `en` y `es` según `Accept-Language`.

### Endpoint (solo uno)
`POST /api/v1/warehouses/{warehouseId}/storage-reservations`

Body (VOs aplanados como primitivos):
```json
{
  "reservationCode": "CC-BX-20417",
  "warehouseId": "3f2b8c1e-5a9d-4c77-8d2e-1b6a9f0c4e55",
  "startDate": "2026-12-01",
  "endDate": "2026-12-20",
  "palletCount": 12,
  "cargoType": "VACCINES",
  "status": "PENDING",
  "handlingNotes": "Keep between 2 and 8 C"
}
```
Response `201`:
```json
{
  "id": 1,
  "reservationCode": "CC-BX-20417",
  "warehouseId": "3f2b8c1e-5a9d-4c77-8d2e-1b6a9f0c4e55",
  "startDate": "2026-12-01",
  "endDate": "2026-12-20",
  "palletCount": 12,
  "cargoType": "VACCINES",
  "status": "PENDING",
  "handlingNotes": "Keep between 2 and 8 C"
}
```

### Restricciones técnicas
- Java 27, Spring Boot 4.1.1, MySQL 8.0+, base de datos `cold_chain_ws`.
- Bounded contexts: `storage` (reserva, `ReservationCode`, `WarehouseId`, `ReservationStatus`), `shared` (`StoragePeriod` y `CargoProfile` van aquí, como pide el examen para los VO complejos; más toda la infraestructura común).
- Capas `domain / application / infrastructure / interfaces` en cada contexto.
- Dominio separado de persistencia (patrón `Persistence Entity` + `PersistenceAssembler`).
- URLs en kebab-case; tablas en plural y snake_case; PK llamada `id`.
- Documentación OpenAPI en inglés (`@Operation`, `@ApiResponses`, `@Schema`), Javadoc con `@author` tu nombre.
- `README.md` con descripción y autor.

---

## 7. Orden de trabajo sugerido (el que conviene practicar para el examen)

Haz cada paso y **compila** antes de pasar al siguiente (`./mvnw compile`).

1. **Setup**: arregla el pom (sección 5), copia `shared`, arranca con la BD vacía. Meta: la app arranca.
2. **Value Objects** (`WarehouseId`, `ReservationCode`, `StoragePeriod`, `CargoProfile`, enum). Pruébalos con un `main` o un test rápido tuyo (aunque testing no cuente en el examen, te ahorra tiempo).
3. **Aggregate** `StorageReservation` + `reconstitute`.
4. **Puerto** `StorageReservationRepository` (`save`, `existsByReservationCode`).
5. **Command** `CreateStorageReservationCommand(warehouseId path, + todos los campos)` y `CommandService` (interfaz).
6. **Persistencia**: `Entity`, converters/embeddables, `PersistenceAssembler`, `PersistenceRepository`, `RepositoryImpl`.
7. **CommandServiceImpl**: regla 7 (path vs body → conflict), regla 1 (código duplicado → conflict), captura de `IllegalArgumentException` → validation error.
8. **Interfaces**: `CreateStorageReservationResource`, `StorageReservationResource`, 2 assemblers, `Controller` con OpenAPI.
9. **i18n**: agrega las claves en `messages.properties` y `messages_es.properties`.
10. **Prueba manual** (Swagger en `/swagger-ui.html` o Postman) con los casos de la tabla de abajo.

### Casos de prueba (tu checklist de "terminado")

| Caso | Esperado |
|---|---|
| Body válido | 201 + `id` generado, fila en `storage_reservations` con `created_at`/`updated_at` |
| Mismo `reservationCode` otra vez | 409 |
| `warehouseId` de URL ≠ body | 409 |
| `reservationCode` = `CC-BX-2041` (4 dígitos) | 400 |
| `warehouseId` = UUID nulo | 400 |
| `endDate` = `startDate` | 400 |
| Periodo de 31 días | 400 |
| `palletCount` = 0 o 41 | 400 |
| `cargoType` = `vaccines` (minúsculas) o `ICE` (3 chars) | 400 |
| `status` omitido | 201 con `PENDING` |
| Header `Accept-Language: es` en cualquier error | mensaje en español |

> Nota: la regla "fuera de rango" del examen pide un tipo de excepción distinto al de "inválido". En el base todo es `IllegalArgumentException` y el handler lo mapea a 400. Para el ejercicio mantén eso; si quieres ir más allá, crea `ArgumentOutOfRangeException` en `shared/domain/exceptions` y mapéala igual a 400.

## 8. Cómo pedirme ayuda sin quitarte el aprendizaje

Cuando termines un paso, dime "revisa el paso N" y te hago code review contra las convenciones del base (capas, naming, dónde va cada regla), sin reescribirte el código. Si te atascas en uno, pídeme una pista, no la solución.
