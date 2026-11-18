# Ruta de aprendizaje: construir el proyecto "Staff" archivo por archivo

Base del profesor (le llamo **BASE**):
`C:\Users\Leonardo Lopez\Desktop\learning-center-platform-master-main (1)\learning-center-platform-master-main\src\main\java\com\acme\center\platform\`

Tu proyecto (le llamo **TUYO**):
`C:\Users\Leonardo Lopez\Documents\Intellij Idea\com-acme-nubi-platform\src\main\java\com\acme\nubi\platform\`

Método de cada paso: **1) estudias el archivo del profesor → 2) yo te marco qué mirar → 3) tú escribes el equivalente de Staff → 4) me dices "revisa el paso N".**
No avanzas al siguiente hasta que el actual compile (`./mvnw compile`).

---

## El caso: Staff (personal)

Una empresa asigna empleados a turnos de trabajo. El examen tendrá la misma forma con otro negocio, así que practicamos la forma.

**Bounded contexts**
- `staff`: lo propio del negocio (aggregate, `AssignmentCode`, `EmployeeId`, `AssignmentStatus`).
- `shared`: infraestructura común del profesor **+** los VO complejos `ShiftPeriod` y `WorkLoad` (igual que en el examen, `TreatmentDuration` y `MedicationDosage` van en shared).

**Aggregate Root `ShiftAssignment`**

| Campo | Tipo | Obligatorio |
|---|---|---|
| id | Long (autogenerado, PK) | sí |
| assignmentCode | `AssignmentCode` | sí |
| employeeId | `EmployeeId` | sí |
| shiftPeriod | `ShiftPeriod` | sí |
| workLoad | `WorkLoad` | sí |
| status | `AssignmentStatus` | sí |
| notes | String | no |

**Reglas de negocio**
1. `AssignmentCode`: envuelve un String con el formato `ST-` + 2 letras mayúsculas + 4 dígitos (ej. `ST-HR2041`). Si no cumple → `IllegalArgumentException`.
2. No puede haber dos asignaciones con el mismo `assignmentCode` (global) → **conflict (409)**.
3. `EmployeeId`: envuelve un `UUID`; no puede ser null ni el UUID nulo (`00000000-0000-0000-0000-000000000000`).
4. `ShiftPeriod(startDate, endDate)` con `LocalDate`:
   - `endDate` ≤ `startDate` → `IllegalArgumentException`.
   - duración > 14 días → `IllegalArgumentException` explicando el motivo.
   - `boolean isActiveOn(LocalDate date)`: true si está entre start y end **inclusive**.
5. `WorkLoad(hoursPerDay, roleTitle)`:
   - `hoursPerDay` > 0 y ≤ 12; si no → excepción de **fuera de rango**.
   - `roleTitle`: mayúsculas, 4 a 14 caracteres, no nulo ni vacío (ej. `CASHIER`, `SUPERVISOR`).
   - `int calculateTotalHours(int totalDays)` = `hoursPerDay * totalDays`.
6. `AssignmentStatus`: enum `PLANNED` (por defecto), `CONFIRMED`, `COMPLETED`.
7. **Subrecurso**: el `employeeId` de la URL debe ser idéntico al `employeeId` del body; si no → conflict (409) lanzado **en la capa de aplicación**.
8. Auditable (`createdAt`, `updatedAt`).
9. Mensajes de error en `en` / `es` según `Accept-Language`.

**Único endpoint**: `POST /api/v1/employees/{employeeId}/shift-assignments`

Request:
```json
{
  "assignmentCode": "ST-HR2041",
  "employeeId": "3f2b8c1e-5a9d-4c77-8d2e-1b6a9f0c4e55",
  "startDate": "2026-12-01",
  "endDate": "2026-12-10",
  "hoursPerDay": 8,
  "roleTitle": "CASHIER",
  "status": "PLANNED",
  "notes": "Morning shift"
}
```
Response 201 (VO aplanados, `id` generado):
```json
{
  "id": 1,
  "assignmentCode": "ST-HR2041",
  "employeeId": "3f2b8c1e-5a9d-4c77-8d2e-1b6a9f0c4e55",
  "startDate": "2026-12-01",
  "endDate": "2026-12-10",
  "hoursPerDay": 8,
  "roleTitle": "CASHIER",
  "status": "PLANNED",
  "notes": "Morning shift"
}
```

---

## La cadena completa (el orden en que se construye)

```
Setup shared
   ↓
ValueObject ─→ Enum ─→ Command ─→ Aggregate ─→ Repository (puerto) + CommandService (interfaz)   ← "a la par"
   ↓
Persistencia: Converter ─→ Embeddable ─→ PersistenceEntity ─→ PersistenceAssembler ─→ PersistenceRepository ─→ RepositoryImpl
   ↓
CommandServiceImpl (reglas: conflict, validación)
   ↓
Resource ─→ ResourceAssemblers ─→ Controller
   ↓
Errores + i18n ─→ application.properties/BD ─→ pruebas manuales ─→ OpenAPI/Javadoc/README
```

Por qué este orden: cada archivo solo depende de los anteriores, así siempre puedes compilar.

---

## Paso 0: Setup (shared del profesor)

**Estudia (BASE/shared)**: `application/result/Result.java`, `ApplicationError.java` · `domain/model/aggregates/AbstractDomainAggregateRoot.java` · `infrastructure/persistence/jpa/entities/AuditableAbstractPersistenceEntity.java` · `.../configuration/strategy/SnakeCaseWithPluralizedTablePhysicalNamingStrategy.java` · `infrastructure/i18n/configuration/LocaleConfiguration.java` · `infrastructure/documentation/openapi/configuration/OpenApiConfiguration.java` · `interfaces/rest/GlobalExceptionHandler.java` · `interfaces/rest/transform/ResponseEntityAssembler.java` y `ProblemDetailFromApplicationErrorAssembler.java`.

**Fíjate**: `Result` es "éxito o error" (sealed interface). `ApplicationError` tiene un `code` y de ese código sale el HTTP.

**Tu tarea**: copia esos archivos a `TUYO/shared/...` con el mismo árbol de carpetas, cambiando solo el `package`. Agrega `@EnableJpaAuditing` a tu clase principal y las líneas de `application.properties` (physical-strategy, `spring.messages.basename=messages`). Copia `messages*.properties`.

**Listo cuando**: `./mvnw compile` pasa. (El pom ya está arreglado.)

---

## Paso 1: Value Object de un solo valor con regex → `AssignmentCode`

**Estudia**: `profiles/domain/model/valueobjects/EmailAddress.java`

**Fíjate**:
- Es un `record`: inmutable e igual por valor.
- La validación vive en el **constructor compacto** (`public EmailAddress { ... }`).
- `Pattern.compile(...)` es `static final` (se compila una vez).
- Si algo falla lanza `IllegalArgumentException` con mensaje claro.

**Tu tarea**: `TUYO/staff/domain/model/valueobjects/AssignmentCode.java` (regla 1). Valida null/blank primero y luego la regex.

**Listo cuando**: un `main` o test tuyo prueba `ST-HR2041` (ok) y `ST-hr2041`, `ST-HR204`, `XX-HR2041`, `null` (todos fallan).

---

## Paso 2: Value Object UUID → `EmployeeId`

**Estudia**: `learning/domain/model/valueobjects/AcmeStudentRecordId.java`

**Fíjate**: usa `UUID.fromString` dentro de un try/catch para convertir el error de formato en un mensaje propio. Esa clase guarda el UUID como String.

**Tu tarea**: `TUYO/staff/domain/model/valueobjects/EmployeeId.java` (regla 3). Decide tú: guardar `UUID` o `String`. Pista: en el body llega como String, y rechazar el UUID nulo se compara con `new UUID(0L, 0L)`.

**Listo cuando**: falla con null, con texto basura y con el UUID de ceros; pasa con un UUID real.

---

## Paso 3: Value Object compuesto con comportamiento → `ShiftPeriod` (en shared)

**Estudia**: `profiles/domain/model/valueobjects/PersonName.java`

**Fíjate**: un record con **varios campos** y validación de cada uno; y un método de comportamiento (`getFullName`) dentro del VO. Aquí está la idea: el VO no solo guarda datos, también sabe responder preguntas sobre sí mismo.

**Tu tarea**: `TUYO/shared/domain/model/valueobjects/ShiftPeriod.java` (regla 4): validaciones, `isActiveOn(LocalDate)` y `getDurationInDays()`. Para días usa `ChronoUnit.DAYS.between(...)`.
Ojo: ¿duración de 14 días es start=1 y end=15, o end=14? Define y documenta tu criterio en el Javadoc (en el examen esto se pregunta con "supera los N días").

**Listo cuando**: casos de borde probados: end==start, end<start, exactamente 14 días, 15 días, `isActiveOn` en start y en end.

---

## Paso 4: Value Object con rango + mayúsculas → `WorkLoad` (en shared)

**Estudia**: `profiles/domain/model/valueobjects/StreetAddress.java` (varios campos con validación) y vuelve a mirar `PersonName`.

**Tu tarea**: `TUYO/shared/domain/model/valueobjects/WorkLoad.java` (regla 5) con `calculateTotalHours(int totalDays)`.
Pista para "mayúsculas": `roleTitle.equals(roleTitle.toUpperCase())`.
Pregunta para ti: ¿qué hace `calculateTotalHours` si `totalDays` es negativo? Decide y valida.

**Listo cuando**: 0 y 13 horas fallan, 12 pasa; `cashier` falla, `CASHIER` pasa; 4 y 14 caracteres pasan, 3 y 15 fallan.

---

## Paso 5: Enum → `AssignmentStatus`

**Estudia**: `learning/domain/model/valueobjects/EnrollmentStatus.java`

**Tu tarea**: `TUYO/staff/domain/model/valueobjects/AssignmentStatus.java` con `PLANNED, CONFIRMED, COMPLETED`, con Javadoc en cada valor.
Pista para el "por defecto": no lo pongas en el enum. Más adelante lo resolverás en el command service (si el body no trae status → `PLANNED`). Piensa dónde.

---

## Paso 6: Command

**Estudia**: `profiles/application/commands/CreateProfileCommand.java`

**Fíjate**: es un `record` de **primitivos** (String, int). Un command es "lo que el usuario quiere hacer", no es dominio, no valida.

**Tu tarea**: `TUYO/staff/application/commands/CreateShiftAssignmentCommand.java`.
Campos: lo que viene en el body **más** el `employeeId` de la URL por separado (por ejemplo `pathEmployeeId` y `employeeId`). Esa diferencia es la que luego comparas en la regla 7.

---

## Paso 7: Aggregate

**Estudia**: `profiles/domain/model/aggregates/Profile.java`

**Fíjate**:
- No tiene ninguna anotación JPA.
- `requireNonNull` en el constructor.
- `reconstitute(...)` existe para reconstruir desde la BD.
- Constructor que recibe primitivos y construye los VO (`new EmailAddress(email)`), que es donde dispara las validaciones.
- Extiende `AbstractDomainAggregateRoot`.
- (Ignora `onCreated()` y los eventos: no los usamos.)

**Tu tarea**: `TUYO/staff/domain/model/aggregates/ShiftAssignment.java` con id, y los 5 VO/enum + notes. Dos constructores: uno de VO ya construidos y otro de primitivos; más `reconstitute`.

**Listo cuando**: puedes crear un `ShiftAssignment` desde un `main` y los VO inválidos lo rompen.

---

## Paso 8 (a la par): Repository (puerto) + CommandService (interfaz)

**Estudia**: `profiles/domain/repositories/ProfileRepository.java` y `profiles/application/commandservices/ProfileCommandService.java`

**Fíjate**: ambos son **interfaces**. El repositorio habla en dominio (`Profile`, `EmailAddress`), nunca en JPA. El command service devuelve `Result<Profile, ApplicationError>`.

**Tu tarea**:
- `TUYO/staff/domain/repositories/ShiftAssignmentRepository.java` con `save` y `existsByAssignmentCode(AssignmentCode)`.
- `TUYO/staff/application/commandservices/ShiftAssignmentCommandService.java` con `Result<ShiftAssignment, ApplicationError> handle(CreateShiftAssignmentCommand)`.

---

## Paso 9: Converter y Embeddables (persistencia, parte 1)

**Estudia**:
- `profiles/infrastructure/persistence/jpa/converters/EmailAddressPersistenceConverter.java`
- `profiles/infrastructure/persistence/jpa/embeddables/PersonNamePersistenceEmbeddable.java`

**Fíjate**: la regla para elegir:
- VO de **un valor** → `AttributeConverter<VO, String>`.
- VO **compuesto** → `@Embeddable` (clase con getters/setters y constructor vacío).
- Las clases de persistencia viven en `infrastructure`, y por convención llevan `Persistence` en el nombre.

**Tu tarea**:
- `AssignmentCodePersistenceConverter`, `EmployeeIdPersistenceConverter` (en `staff/infrastructure/persistence/jpa/converters`).
- `ShiftPeriodPersistenceEmbeddable` y `WorkLoadPersistenceEmbeddable` (en `shared/infrastructure/persistence/jpa/embeddables`, porque los VO son de shared).
- Cuidado: `@Converter(autoApply = false)` y se aplica con `@Convert` en la entidad.

---

## Paso 10: Persistence Entity

**Estudia**: `profiles/infrastructure/persistence/jpa/entities/ProfilePersistenceEntity.java` y `shared/.../AuditableAbstractPersistenceEntity.java`

**Fíjate**: hereda de `AuditableAbstractPersistenceEntity` (id + createdAt + updatedAt gratis). Usa `@Embedded` + `@AttributeOverrides` para nombrar columnas, `@Convert` para el VO simple, `@Column(unique = true)`.

**Tu tarea**: `TUYO/staff/infrastructure/persistence/jpa/entities/ShiftAssignmentPersistenceEntity.java`
- `@Table(name = "shift_assignments")`.
- `assignmentCode` unique y not null; status con `@Enumerated(EnumType.STRING)`; notes nullable.
- Nombra las columnas de los embeddables para que queden `start_date`, `end_date`, `hours_per_day`, `role_title`.

---

## Paso 11: Persistence Assembler (dominio ⇄ JPA)

**Estudia**: `profiles/infrastructure/persistence/jpa/assemblers/ProfilePersistenceAssembler.java`

**Fíjate**: dos direcciones: `toDomainFromPersistence` y `toPersistenceFromDomain`. Aquí se usa `reconstitute`. Es una clase `final` con métodos estáticos.

**Tu tarea**: `ShiftAssignmentPersistenceAssembler` en `staff/infrastructure/persistence/jpa/assemblers`.

---

## Paso 12: Spring Data repository + adapter

**Estudia**: `ProfilePersistenceRepository.java` y `ProfileRepositoryImpl.java`

**Fíjate**: `ProfilePersistenceRepository` extiende `JpaRepository` y trabaja con la **entidad JPA**. `ProfileRepositoryImpl` implementa el puerto del dominio y traduce con el assembler. (Ignora todo lo de `DomainEventPublisher` y `isNew`.)

**Tu tarea**:
- `ShiftAssignmentPersistenceRepository` (con una consulta `existsByAssignmentCode`; puedes usar `@Query` como el profesor o un derived query).
- `ShiftAssignmentRepositoryImpl` con `@Repository`.

**Listo cuando**: la app arranca y Hibernate crea la tabla `shift_assignments` con las columnas esperadas (revisa en MySQL).

---

## Paso 13: CommandServiceImpl (aquí viven las reglas de la capa de aplicación)

**Estudia**: `profiles/application/internal/commandservices/ProfileCommandServiceImpl.java`

**Fíjate**:
- Construye los VO dentro de un `try`.
- Compara/consulta antes de guardar → `ApplicationError.conflict(...)`.
- `catch (IllegalArgumentException)` → `ApplicationError.validationError(...)` (400).
- `catch (Exception)` → `ApplicationError.unexpected(...)`.
- Siempre devuelve `Result`, no deja salir excepciones.

**Tu tarea**: `TUYO/staff/application/internal/commandservices/ShiftAssignmentCommandServiceImpl.java`. Orden sugerido:
1. Compara `pathEmployeeId` vs `employeeId` del body → conflict (regla 7). ¿Antes o después de construir los VO? Piénsalo: es más barato antes.
2. Construye los VO.
3. `existsByAssignmentCode` → conflict (regla 2).
4. Status por defecto (regla 6).
5. `save` y `Result.success`.

---

## Paso 14: Resources (JSON de entrada y salida)

**Estudia**: `profiles/interfaces/rest/resources/CreateProfileResource.java` y `ProfileResource.java`

**Fíjate**: son `record`s. En la entrada hay `@NotBlank(message = "{validation.not-blank}")` (el `{...}` busca el mensaje en `ValidationMessages*.properties`) y `@Schema` para OpenAPI.

**Tu tarea**: `CreateShiftAssignmentResource` y `ShiftAssignmentResource` en `staff/interfaces/rest/resources`. Todos primitivos, nombres exactamente como el JSON de arriba. En la entrada, `status` y `notes` son opcionales.

---

## Paso 15: Assemblers REST

**Estudia**: `CreateProfileCommandFromResourceAssembler.java` y `ProfileResourceFromEntityAssembler.java`

**Fíjate**: son los puentes `Resource → Command` y `Aggregate → Resource`. Aquí es donde se "aplana" el VO (`entity.getEmailAddress()`).

**Tu tarea**: `CreateShiftAssignmentCommandFromResourceAssembler` (recibe también el `employeeId` de la URL) y `ShiftAssignmentResourceFromEntityAssembler`.

---

## Paso 16: Controller

**Estudia**: `profiles/interfaces/rest/ProfilesController.java` (el método `createProfile`) y `ResponseEntityAssembler`.

**Fíjate**: el controller **no decide nada**: arma el command, llama al service, y `ResponseEntityAssembler.toResponseEntityFromResult(...)` transforma el `Result` en 201 o en error.

**Tu tarea**: `ShiftAssignmentsController` con `@RequestMapping("/api/v1/employees/{employeeId}/shift-assignments")`, `@PostMapping`, `@PathVariable UUID/String employeeId`, `@Valid @RequestBody`. Con `@Operation`, `@ApiResponses` (201, 400, 409) y `@Tag` en inglés.

**Listo cuando**: el POST feliz devuelve 201 con id y la fila aparece en MySQL.

---

## Paso 17: Errores e i18n

**Estudia**: `GlobalExceptionHandler`, `ProblemDetailFromApplicationErrorAssembler`, `messages.properties`, `messages_es.properties`.

**Fíjate**: el código `ERRORNAME_CONFLICT` se convierte en la clave `error.<errorname>-conflict.message`. Las claves específicas ganan sobre las genéricas.

**Tu tarea**: agrega claves en inglés y en español para tus conflictos (`SHIFTASSIGNMENT`... ¿cómo quedó tu código de error?). Prueba con `Accept-Language: es` y `en`.

---

## Paso 18: Config y pruebas manuales

`application.properties`: URL de MySQL con tu base de datos (en el examen sería el esquema `ro_wa`), `ddl-auto=update`.

Prueba con Swagger (`/swagger-ui.html`) o Postman:

| Caso | Esperado |
|---|---|
| Body válido | 201 + id, fila con `created_at` y `updated_at` |
| Mismo `assignmentCode` | 409 |
| `employeeId` URL ≠ body | 409 |
| `assignmentCode` mal formado | 400 |
| `employeeId` = UUID de ceros | 400 |
| `endDate` = `startDate` | 400 |
| Periodo mayor a 14 días | 400 |
| `hoursPerDay` 0 o 13 | 400 |
| `roleTitle` en minúsculas o de 3 letras | 400 |
| Sin `status` | 201 con `PLANNED` |
| Cualquier error con `Accept-Language: es` | mensaje en español |

---

## Paso 19: Entrega estilo examen

- Javadoc en clases y métodos principales con `@author` (en el examen: tu nombre en un tag de remarks).
- `README.md` en inglés: descripción de la app y autor.
- Nombres de URL en kebab-case, tablas en plural/snake_case, PK `id`.
- Comprimir el proyecto sin la carpeta `target`.

---

## Paso 20 (simulacro): rehacerlo en 100 minutos

Cuando termines, yo te doy **otro caso nuevo** con reglas distintas y lo haces cronometrado sin mirar este documento, usando solo el proyecto del profesor como referencia.

---

## Tabla rápida: mi archivo ← archivo del profesor

| Tuyo | Profesor |
|---|---|
| `AssignmentCode` | `EmailAddress` |
| `EmployeeId` | `AcmeStudentRecordId` |
| `ShiftPeriod`, `WorkLoad` | `PersonName`, `StreetAddress` |
| `AssignmentStatus` | `EnrollmentStatus` |
| `CreateShiftAssignmentCommand` | `CreateProfileCommand` |
| `ShiftAssignment` | `Profile` |
| `ShiftAssignmentRepository` | `ProfileRepository` |
| `ShiftAssignmentCommandService` / `Impl` | `ProfileCommandService` / `Impl` |
| `*PersistenceConverter` | `EmailAddressPersistenceConverter` |
| `*PersistenceEmbeddable` | `PersonNamePersistenceEmbeddable` |
| `ShiftAssignmentPersistenceEntity` | `ProfilePersistenceEntity` |
| `ShiftAssignmentPersistenceAssembler` | `ProfilePersistenceAssembler` |
| `ShiftAssignmentPersistenceRepository` | `ProfilePersistenceRepository` |
| `ShiftAssignmentRepositoryImpl` | `ProfileRepositoryImpl` |
| `*Resource` | `CreateProfileResource`, `ProfileResource` |
| `*Assembler` (REST) | `CreateProfileCommandFromResourceAssembler`, `ProfileResourceFromEntityAssembler` |
| `ShiftAssignmentsController` | `ProfilesController` |

---

## Empieza aquí

Haz el **Paso 0** y el **Paso 1**. Cuando compile y `AssignmentCode` esté probado, escríbeme "revisa paso 1" y te doy feedback antes de seguir con el Paso 2.
