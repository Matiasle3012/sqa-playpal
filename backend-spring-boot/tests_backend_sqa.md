# Documentación de Casos de Prueba Backend — PlayPal Webapp

Implementación de los 8 casos de prueba backend seleccionados en el informe de SQA (4 de la primera tanda + 4 de `casos_a_ejecutar_2`), basados en **ISO/IEC/IEEE 29119-4** (técnicas de diseño de pruebas) e **ISO/IEC 25010** (subcaracterísticas de mantenibilidad). Todos los resultados reportados provienen de la ejecución real de la suite sobre este repositorio (Java 21.0.7, Maven Wrapper 3.9.9, Spring Boot 3.4.4, Docker 27.3.1 para Testcontainers).

**Resultado global de la ejecución:** suite por defecto `Tests run: 58, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS` + suite nightly (fuzzing) `Tests run: 60, Failures: 0 — BUILD SUCCESS`

| Caso | Clase(s) de prueba | Tests | Resultado |
|------|--------------------|-------|-----------|
| B-001 | `ReservationServiceAdvanceHoursTest` (3 contextos anidados) | 5 | ✅ 5/5 |
| B-002 | `UserDtoIntegrationTest` | 1 | ✅ 1/1 |
| B-003 | `BranchControllerFuzzingTest` (`@Tag("nightly")`) | 60 | ✅ 60/60 |
| B-005 | `SecurityConfigIntegrationTest` + `AuthorizationArchitectureTest` | 16 + 4 | ✅ 20/20 |
| B-006 | `UserServiceRepositoryMockTest`, `ReservationServiceRepositoryMockTest`, `CourtServiceRepositoryMockTest`, `BranchServiceRepositoryMockTest` | 25 | ✅ 25/25 |
| B-009 | `ReservationServiceMcdcTest` | 4 | ✅ 4/4 |
| S-001 | `ReservationFullFlowSystemTest` | 1 | ✅ 1/1 |
| S-002 | `DoubleBookingRegressionTest` | 1 | ⚠️ Verde documentando defecto conocido |
| (preexistente) | `PlayPalApplicationTests` | 1 | ✅ 1/1 |

---

## B-001 — Tabla de Decisiones sobre Parámetro de Antelación

| Campo | Valor |
|-------|-------|
| **ID** | B-001 |
| **Técnica ISO 29119-4** | Decision Table Testing (prueba de tabla de decisiones) |
| **Subcaracterística ISO 25010** | Mantenibilidad → **Modificabilidad** |
| **Nivel** | Unitario (con contexto Spring para verificar la inyección de configuración) |
| **Enfoque** | Caja blanca |
| **Prioridad** | CRÍTICA |
| **Herramientas** | JUnit 5 + Spring Boot Test + `@TestPropertySource` + `@DynamicPropertySource` + Mockito (`@MockitoBean`) |
| **Componentes** | `ReservationService`, `application.properties` |
| **Archivo de prueba** | `playpal/src/test/java/com/api/playpal/reservation/aplication/ReservationServiceAdvanceHoursTest.java` |

### Descripción

`ReservationService.save()` tenía la regla de antelación mínima *hardcodeada* como `.plusHours(12)`. Un cambio del negocio (p. ej. bajar la antelación a 6 horas) obligaba a recompilar y redesplegar. El caso exige primero un **refactor de modificabilidad** y luego demostrar con una tabla de decisiones que el comportamiento queda gobernado por configuración externa.

### Refactor realizado (código de producción)

1. `ReservationService` ahora recibe el umbral por **inyección de constructor** con `@Value("${reservation.advance-hours:12}")` y lo guarda en el campo final `advanceHours` (valor por defecto 12 si la propiedad no existe).
2. El mensaje de rechazo pasó a ser dinámico: `"Invalid date (must be " + advanceHours + " hours before minimum)"`.
3. Se agregó `reservation.advance-hours=12` a `playpal/src/main/resources/application.properties`.

### Precondiciones

- No se requiere MongoDB: los repositorios `ReservationRepositoryImp` y `CourtRepositoryImp` se sustituyen con `@MockitoBean`.
- Las fechas de prueba se calculan en tiempo de ejecución relativas a `LocalDateTime.now()` (el test no depende del calendario). Por la granularidad horaria del servicio (`start` es una hora entera), la antelación efectiva de una entrada "+N horas" cae en el intervalo (N−1, N]; los valores de la tabla se eligieron para que ese truncamiento nunca cambie el resultado esperado.

### Tabla de decisiones (condiciones × acciones)

Condiciones: **C1** = umbral configurado (`reservation.advance-hours`); **C2** = antelación de la fecha solicitada respecto de "ahora".

| Regla | C1: Umbral | C2: Antelación solicitada | Acción esperada | Test que la implementa | Resultado real |
|-------|-----------|---------------------------|-----------------|------------------------|----------------|
| R1 | 12 h | 14 h (≥ umbral) | Acepta y persiste la reserva | `TwelveHourThresholdFromApplicationProperties.acceptsReservationFourteenHoursAhead` | ✅ Pasa |
| R2 | 12 h | 8 h (< umbral) | Rechaza: `Invalid date (must be 12 hours before minimum)` | `TwelveHourThresholdFromApplicationProperties.rejectsReservationEightHoursAhead` | ✅ Pasa |
| R3 | 6 h | 8 h (≥ umbral) | Acepta y persiste la reserva | `SixHourThresholdFromTestPropertySource.acceptsReservationEightHoursAhead` | ✅ Pasa |
| R4 | 6 h | 3 h (< umbral) | Rechaza: `Invalid date (must be 6 hours before minimum)` | `SixHourThresholdFromTestPropertySource.rejectsReservationThreeHoursAhead` | ✅ Pasa |

El par **R2/R3 usa exactamente la misma entrada (reserva a 8 horas)**: solo cambia la propiedad de configuración y el resultado se invierte. Esa es la evidencia directa de modificabilidad.

- El umbral 12 h de R1/R2 proviene del `application.properties` real (no se simula).
- El umbral 6 h de R3/R4 se simula con `@TestPropertySource(properties = "reservation.advance-hours=6")`.
- El contexto anidado `SixHourThresholdFromDynamicPropertySource` registra `reservation.advance-hours=6` mediante `@DynamicPropertySource` (un `Supplier` evaluado en runtime) y vuelve a aceptar la reserva a 8 horas: demuestra que el binario compilado es el mismo y **solo cambió la configuración en tiempo de ejecución — no se requiere recompilar**.

### Criterios de aceptación y resultado

| Criterio | Objetivo | Resultado real |
|----------|----------|----------------|
| Las 4 reglas de la tabla pasan | 4/4 | ✅ 4/4 (más 1 test de demostración dinámica = 5/5) |
| Cambiar el umbral no toca código Java | Solo propiedad | ✅ El mismo `ReservationService.class` se ejecutó bajo 12 h y 6 h en la misma corrida |
| Mensaje de rechazo refleja el umbral vigente | Sí | ✅ `assertEquals` sobre `getMessage()` con 12 y con 6 |

Ejecución real: `Tests run: 2 (12h) + 2 (6h) + 1 (dinámico) = 5, Failures: 0` (contextos cargados en 0.6–2.1 s cada uno).

---

## B-005 — Decisión de Autorización Centralizada

| Campo | Valor |
|-------|-------|
| **ID** | B-005 |
| **Técnica ISO 29119-4** | Prueba basada en decisiones sobre las reglas de `authorizeHttpRequests()` (cada regla ejercitada en sus dos ramas: con y sin credencial) + análisis estático estructural (reglas de arquitectura) |
| **Subcaracterística ISO 25010** | Mantenibilidad → **Modularidad** |
| **Nivel** | Integración |
| **Enfoque** | Caja blanca |
| **Prioridad** | ALTA |
| **Herramientas** | Spring Boot Test (`@SpringBootTest(webEnvironment = RANDOM_PORT)`) + MockMvc + spring-security-test + ArchUnit 1.4.1 |
| **Componentes** | `SecurityConfig`, `JwtFilter`, `JwtUtil`, los 5 controllers (`AuthController`, `UserController`, `CourtController`, `BranchController`, `ReservationController`) |
| **Archivos de prueba** | `playpal/src/test/java/com/api/playpal/auth/infrastructure/SecurityConfigIntegrationTest.java` y `playpal/src/test/java/com/api/playpal/architecture/AuthorizationArchitectureTest.java` |

### Descripción

La decisión de autorización debe vivir en **un único módulo** (`SecurityConfig.securityFilterChain()` → `authorizeHttpRequests()`), no dispersa en los controllers. El caso verifica las dos caras de la modularidad: (a) dinámicamente, que cada regla declarada responde el código HTTP correcto con y sin JWT; (b) estáticamente, que ningún controller contiene lógica de autorización ni accede a repositorios.

### Precondiciones

- Los 4 repositorios se sustituyen con `@MockitoBean` (sin MongoDB).
- Los JWT usados son **reales**: se generan con el `JwtUtil` del contexto (firmados con el `jwt.secret` de `application.properties`) y el `JwtFilter` real los procesa; `userRepository.findById` se stubbed para resolver el principal.
- Las contraseñas del login se codifican con el bean `PasswordEncoder` (BCrypt) real.

### Matriz de decisión de autorización (resultados reales)

| Regla en `authorizeHttpRequests()` | Petición ejercitada | Sin JWT | Con JWT válido |
|------------------------------------|---------------------|---------|----------------|
| `OPTIONS /api/** → permitAll` | `OPTIONS /api/reservations/` (preflight CORS con `Origin` permitido) | **200** ✅ | n/a |
| `/api/auth/login → permitAll` | `POST /api/auth/login` (multipart, credenciales válidas) | **200** + token ✅ | n/a |
| `/api/auth/register → permitAll` | `POST /api/auth/register` (multipart) | **200** ✅ | n/a |
| `GET /api/courts/ → permitAll` | `GET /api/courts/` | **200** ✅ | **200** ✅ |
| `/images/** → permitAll` | `GET /images/missing.png` | **404** (atraviesa la seguridad; no existe el recurso) ✅ | n/a |
| `anyRequest().authenticated()` | `GET /api/reservations/`, `GET /api/reservations/court/{id}`, `GET /api/branches/{id}`, `POST /api/branches/`, `GET /api/users/{id}`, `GET /api/auth/current`, `DELETE /api/courts/` (7 endpoints parametrizados) | **403** ✅ | **200** ✅ (verificado en `/api/reservations/`, `/api/auth/current` con `$.id` correcto y `/api/users/{id}`) |

> **Hallazgo documentado:** sin JWT la aplicación responde **403 y no 401**. `SecurityConfig` no configura ningún `AuthenticationEntryPoint`, por lo que Spring Security 6 usa `Http403ForbiddenEntryPoint` por defecto. El acceso sigue correctamente denegado (el criterio de seguridad se cumple), pero semánticamente un 401 sería más preciso; queda registrado como observación de mejora, verificable y corregible en un único punto gracias a la centralización.

### Reglas ArchUnit (análisis estático, 4/4 pasan)

| Regla | Qué falla si se viola |
|-------|------------------------|
| `controllersDoNotAccessRepositoriesDirectly` | Un `@RestController` depende de cualquier clase asignable a `org.springframework.data.repository.Repository` o con nombre `*Repository` / `*RepositoryImp` |
| `controllersDoNotEvaluateRoles` | Un `@RestController` invoca `User.getRole()` (comparación de rol / if sobre permisos en la capa web) |
| `controllersDoNotDeclareMethodLevelAuthorization` | Un método de controller se anota con `@PreAuthorize`, `@Secured` o `@RolesAllowed` (autorización fuera del punto central) |
| `httpAuthorizationIsCentralizedInSecurityConfig` | Cualquier clase distinta de `SecurityConfig` depende de `HttpSecurity` o `SecurityFilterChain` |

Estas reglas se ejecutan sobre las clases de producción (`ImportOption.DoNotIncludeTests`) y quedan como **guardia de regresión arquitectónica**: si un desarrollador futuro mete un `if (user.getRole().equals(...))` en un controller, el build falla.

### Criterios de aceptación y resultado

| Criterio | Objetivo | Resultado real |
|----------|----------|----------------|
| Cada regla de `authorizeHttpRequests()` ejercitada con y sin JWT | 100% de las reglas | ✅ 6/6 reglas cubiertas, 16 tests |
| Endpoints públicos no exigen credencial | 200/404 sin token | ✅ |
| Endpoints protegidos deniegan sin token y sirven con token | 401/403 sin token, 200 con token | ✅ (403 sin token, 200 con token) |
| Ninguna violación arquitectónica | 0 violaciones | ✅ 4/4 reglas ArchUnit sin violaciones |

Ejecución real: `SecurityConfigIntegrationTest — Tests run: 16, Failures: 0` (12.0 s, incluye arranque del contexto web) y `AuthorizationArchitectureTest — Tests run: 4, Failures: 0` (2.7 s).

---

## B-006 — Mocking de Interfaces de Repositorio

| Campo | Valor |
|-------|-------|
| **ID** | B-006 |
| **Técnica ISO 29119-4** | Prueba unitaria de caja blanca con dobles de prueba (mock objects) sobre los puntos de decisión de los servicios |
| **Subcaracterística ISO 25010** | Mantenibilidad → **Testabilidad** |
| **Nivel** | Unitario |
| **Enfoque** | Caja blanca |
| **Prioridad** | ALTA |
| **Herramientas** | JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`, `when/thenReturn`, `verify`) + `@Timeout` |
| **Componentes** | `UserService`, `ReservationService`, `CourtService`, `BranchService` y las 4 interfaces de repositorio (`UserRepositoryImp`, `ReservationRepositoryImp`, `CourtRepositoryImp`, `BranchRepositoryImp`) |
| **Archivos de prueba** | `playpal/src/test/java/com/api/playpal/{user,reservation,court,branch}/application/*RepositoryMockTest.java` (4 clases) |

### Descripción

Demuestra que la capa de aplicación es comprobable **sin ninguna conexión a MongoDB**: al ser los repositorios interfaces inyectadas por constructor, Mockito los sustituye por dobles de prueba y cada servicio se verifica en aislamiento total. El patrón se replica idéntico sobre los 4 supra-componentes.

### Precondiciones

- Sin contexto Spring y sin base de datos: `MockitoExtension` puro.
- Cada clase lleva `@Timeout(value = 500, unit = TimeUnit.MILLISECONDS)` a nivel de clase: **cada test** debe terminar en menos de 500 ms, lo que hace medible la velocidad de la suite (un test que intentara abrir una conexión real reventaría el presupuesto).

### Escenarios cubiertos por interfaz (25 tests)

| Servicio (mocks inyectados) | Tests | Interacciones verificadas |
|------------------------------|-------|---------------------------|
| `UserService` (`UserRepositoryImp`, `StorageService`) | 4 | alta persiste (`verify(save)`), email duplicado no persiste (`verify(never()).save`), `findById` delega, `deleteById` delega |
| `ReservationService` (`ReservationRepositoryImp`, `CourtRepositoryImp`) | 10 | alta válida persiste con `totalPrice` calculado (precio × horas), `findById/findByUser/findByCourt` delegan, `update` desactiva/conserva estado, `deleteById` borra inactivas, rechaza activas futuras y falla con id inexistente |
| `CourtService` (`CourtRepositoryImp`, `BranchRepositoryImp`, `StorageService`) | 6 | alta persiste cancha y actualiza la sucursal (`verify(times(2)).save` — comportamiento real del código), sucursal inexistente no persiste, `findById/findAll/findAll(sport)` delegan, `deleteById` delega |
| `BranchService` (`BranchRepositoryImp`, `UserRepositoryImp`, `StorageService`) | 5 | alta para provider persiste sucursal y usuario, rol `user` rechazado con `verifyNoInteractions`, `findBranchesByProviderId` delega, `update` renombra, `deleteById` delega |

Patrón aplicado en todos: **Arrange** (`when(...).thenReturn(...)` / `thenAnswer` devolviendo el argumento como haría el save real) → **Act** (llamada al servicio) → **Assert** (aserciones sobre el resultado + `verify(...).save(any(...))` / `verify(never())` sobre las interacciones).

> **Nota técnica documentada:** `@InjectMocks` se usa en `UserService`, `CourtService` y `BranchService`. Para `ReservationService` no es posible: tras el refactor de B-001 su constructor recibe el primitivo `long advanceHours` (parámetro `@Value`) y Mockito no puede resolverlo (error real observado: *"Cannot instantiate @InjectMocks field... the type 'ReservationService' has no default constructor"*). Se instancia explícitamente en `@BeforeEach` con `new ReservationService(reservationRepository, courtRepository, 12)` — lo cual es en sí una demostración de testabilidad: el constructor permite fijar la configuración del SUT sin ningún framework.

### Criterios de aceptación y resultado

| Criterio | Objetivo | Resultado real |
|----------|----------|----------------|
| 0 conexiones a MongoDB | Ninguna dependencia de infraestructura | ✅ Sin contexto Spring ni driver Mongo involucrado |
| Patrón replicado en las 4 interfaces | 4/4 | ✅ 4 clases de test, 25 tests |
| Cada test < 500 ms (`@Timeout`) | 100% | ✅ Tiempos reales por clase: User 0.025 s, Court 0.034 s, Reservation 0.057 s, Branch 0.31 s — **suite completa B-006 ≈ 0.43 s** |
| Interacciones verificadas, no solo estado | `verify` en todos los caminos de escritura | ✅ |

Ejecución real: `Tests run: 25, Failures: 0, Errors: 0`.

---

## B-009 — MC/DC sobre Validación de Reserva

| Campo | Valor |
|-------|-------|
| **ID** | B-009 |
| **Técnica ISO 29119-4** | Modified Condition/Decision Coverage (MC/DC) + mutation testing como verificación de la fortaleza de la suite |
| **Subcaracterística ISO 25010** | Mantenibilidad → **Analizabilidad** |
| **Nivel** | Unitario |
| **Enfoque** | Caja blanca |
| **Prioridad** | ALTA |
| **Herramientas** | JUnit 5 (`@ParameterizedTest` + `@CsvSource`) + Mockito + Pitest 1.19.1 (pitest-junit5-plugin 1.2.2) |
| **Componentes** | `ReservationService.save()` |
| **Archivo de prueba** | `playpal/src/test/java/com/api/playpal/reservation/aplication/ReservationServiceMcdcTest.java` |

### Descripción

La decisión de aceptación de `save()` es la conjunción secuencial (con cortocircuito por excepción) de tres condiciones atómicas:

- **C1** — la cancha existe (`courtRepository.findById(...)` presente);
- **C2** — la fecha es válida y cumple la antelación mínima (parseable y no anterior a `now() + advanceHours`);
- **C3** — la hora de fin es mayor que la de inicio (`end > start`).

Decisión: `Reserva creada ⇔ C1 ∧ C2 ∧ C3`. Para una conjunción de n condiciones, la matriz MC/DC mínima es de **n + 1 = 4 casos**: el caso base todo-verdadero y n casos donde exactamente una condición es falsa, de modo que cada condición demuestra su influencia independiente sobre el resultado.

### Matriz MC/DC (4 casos, resultados reales)

| Caso | C1 Court existe | C2 Fecha válida y ≥ 12 h | C3 End > Start | Resultado esperado | Mensaje verificado con `assertEquals(getMessage())` | Test | Real |
|------|----|----|----|--------------------|--------------------------------------|------|------|
| CT1 | **V** | **V** (48 h de antelación) | **V** (duración 2 h) | Reserva creada, `totalPrice = 2000`, activa, `verify(save)` | — | `allConditionsTrueCreatesReservation` | ✅ |
| CT2 | **F** | V | V | `RuntimeException` | `Court not founded` | fila 1 de `singleFalseConditionRejectsReservationWithSpecificMessage` | ✅ |
| CT3 | V | **F** (8 h de antelación) | V | `RuntimeException` | `Invalid date (must be 12 hours before minimum)` | fila 2 del parametrizado | ✅ |
| CT4 | V | V | **F** (duración 0 h ⇒ end = start) | `RuntimeException` | `End must be greater than start` | fila 3 del parametrizado | ✅ |

Pares de independencia MC/DC: {CT1, CT2} aísla C1; {CT1, CT3} aísla C2; {CT1, CT4} aísla C3 — en cada par solo cambia una condición y cambia el resultado.

Notas de diseño:
- Por la evaluación en cortocircuito, en CT2 los valores V de C2/C3 son los **provistos como entrada** (el código no llega a evaluarlos); esto es lo esperable en decisiones secuenciales y no invalida la matriz.
- Los tres rechazos tienen **mensajes distintos entre sí** (analizabilidad: el mensaje identifica unívocamente la condición violada). El mensaje de C2 ya no está hardcodeado: refleja el umbral configurado (refactor de B-001). No hizo falta diferenciar mensajes genéricos: ya eran distintos; sí se hizo dinámico el de C2.
- La implementación usa 1 `@Test` (caso base) + 1 `@ParameterizedTest` con `@CsvSource` de 3 filas (los tres casos de condición única falsa), con el mock del repositorio parametrizado por la columna `courtExists`.

### Pitest — verificación por mutación (resultados reales)

Configurado en el `pom.xml` con `targetClasses = com.api.playpal.reservation.aplication.ReservationService` y `targetTests = ReservationServiceMcdcTest, ReservationServiceRepositoryMockTest` (solo tests unitarios puros, sin contexto Spring):

```
>> Line Coverage (for mutated classes only): 26/28 (93%)
>> Generated 19 mutations Killed 19 (100%)
>> Mutations with no coverage 0. Test strength 100%
```

| Métrica | Objetivo | Resultado real |
|---------|----------|----------------|
| Mutation score sobre `ReservationService` | Alto (≥ 80%) | ✅ **100%** (19/19 mutantes eliminados) |
| Test strength (mutantes cubiertos eliminados) | ≥ 80% | ✅ **100%** |
| Cobertura de línea de la clase mutada | — | 26/28 (93%) — las 2 líneas restantes son el bloque `catch (DateTimeParseException)` (fecha con formato no parseable), camino en el que Pitest no genera mutantes y que queda fuera del modelo MC/DC (la matriz modela fecha parseable bajo el umbral) |

Durante la primera corrida sobrevivieron 2 mutantes `NO_COVERAGE` en las lambdas de `orElseThrow` de `update()` y `deleteById()`; se agregaron los tests `updateFailsWhenReservationDoesNotExist` y `deleteByIdFailsWhenReservationDoesNotExist` a `ReservationServiceRepositoryMockTest` y el score final quedó en 100%. Este ciclo (mutante vivo → test nuevo → mutante muerto) es exactamente el uso previsto de Pitest como control de calidad de la suite.

### Cobertura JaCoCo de `ReservationService` (real, tras la suite completa)

| Métrica | Cubierto / Total | % |
|---------|------------------|---|
| Instrucciones | 163 / 169 | 96.4% |
| Ramas | 10 / 12 | 83.3% |
| Líneas | 26 / 28 | 92.9% |
| Métodos | 10 / 10 | 100% |

(Las 2 ramas no cubiertas corresponden a combinaciones residuales de `update`/`deleteById` — reserva ya inactiva reactivada y reserva activa con fecha pasada — fuera del alcance de los 4 casos seleccionados.)

---

## B-002 — Escenario de Reutilización de DTOs por Rol

| Campo | Valor |
|-------|-------|
| **ID** | B-002 |
| **Técnica ISO 29119-4** | Prueba basada en escenarios (scenario testing) con partición de equivalencia sobre el campo `role` |
| **Subcaracterística ISO 25010** | Mantenibilidad → **Reusabilidad** |
| **Nivel** | Integración |
| **Enfoque** | Caja gris (se ejercita la API HTTP real, pero se inspecciona el esquema JSON interno y se conocen las clases de dominio) |
| **Prioridad** | MEDIA |
| **Herramientas** | Spring Boot Test (`RANDOM_PORT`) + TestRestTemplate + Jackson `ObjectMapper` + AssertJ |
| **Componentes** | `User` (modelo único para ambos roles), `AuthController`, `UserController`, `BranchController` |
| **Archivo de prueba** | `playpal/src/test/java/com/api/playpal/user/infrastructure/UserDtoIntegrationTest.java` |

### Descripción

PlayPal modela usuarios finales y proveedores con **una única clase `User`** diferenciada por el campo `role` (no existe un `ProviderDTO` separado). El caso demuestra esa reutilización: dos peticiones de registro estructuralmente idénticas que solo varían `role` producen recursos servidos por el mismo esquema JSON, y la lista `branches` es la única diferencia de contenido (vacía para `user`, poblada para `provider` tras crear una sede).

### Precondiciones

- Sin MongoDB: `UserRepositoryImp` y `BranchRepositoryImp` se sustituyen con `@MockitoBean` respaldados por un **almacén en memoria** (`ConcurrentHashMap`) que emula el ciclo save/findByEmail/findById — necesario porque el escenario encadena registro → login → creación de sede → consulta de perfil a través de HTTP real.
- El flujo es 100 % vía TestRestTemplate contra el puerto aleatorio: registro multipart, login multipart (BCrypt real), `GET /api/auth/current` para obtener el id, `POST /api/branches/` con Bearer real, `GET /api/users/{id}`.

### Escenario y verificaciones

| Paso | Acción | Verificación |
|------|--------|--------------|
| 1 | `POST /api/auth/register` con `role=user` | 200 |
| 2 | `POST /api/auth/register` con payload idéntico salvo `role=provider` | 200 |
| 3 | Login de ambos, `POST /api/branches/` con el token del provider | 200 |
| 4 | `GET /api/users/{id}` de cada uno | `assertThat(fieldNames(providerJson)).isEqualTo(fieldNames(userJson))` — **mismo conjunto de campos JSON** |
| 5 | `objectMapper.treeToValue(json, User.class)` sobre ambas respuestas | Ambas deserializan a la **misma clase `User`** sin DTOs alternativos |
| 6 | Contenido diferencial | `plainUser.getBranches()` vacío; `provider.getBranches()` con 1 sede ("Sede Centro") |

### Criterios de aceptación y resultado

| Criterio | Objetivo | Resultado real |
|----------|----------|----------------|
| Mismo esquema JSON para ambos roles | Conjuntos de campos idénticos | ✅ (`branches, created_at, email, id, password, role, thumbnail_url, username`) |
| Ambas respuestas deserializan a `User.class` | Sin `ProviderDTO` separado | ✅ Jackson usa el constructor único de `User` (parameter-names + `-parameters` de Boot) |
| `branches` vacío para `user`, poblado para `provider` | AssertJ | ✅ `isEmpty()` / `hasSize(1)` |

Ejecución real: `Tests run: 1, Failures: 0` (1.46 s dentro de la suite completa).

> **Hallazgo documentado:** el JSON de `GET /api/users/{id}` expone el campo `password` (hash BCrypt). El test lo evidencia en el conjunto de campos del esquema. No es explotable directamente (es un hash), pero serializar el hash al cliente es una mala práctica: la reutilización de la entidad de dominio como DTO de respuesta tiene este costo. Recomendación de backlog: `@JsonIgnore`/`@JsonProperty(access = WRITE_ONLY)` sobre `password` o un DTO de salida.

---

## B-003 — Fuzzing de Headers HTTP en BranchController

| Campo | Valor |
|-------|-------|
| **ID** | B-003 |
| **Técnica ISO 29119-4** | Prueba de robustez por valores erróneos/aleatorios (error guessing + random/fuzz testing) sobre la interfaz HTTP |
| **Subcaracterística ISO 25010** | Mantenibilidad → **Modularidad** (la capa de seguridad contiene el fallo; el módulo de negocio nunca se entera) |
| **Nivel** | Integración |
| **Enfoque** | Caja negra |
| **Prioridad** | ALTA |
| **Herramientas** | MockMvc + generador de fuzzing con semilla fija + Mockito (`@MockitoBean` de `BranchService` + `verifyNoInteractions`) + `@Tag("nightly")` |
| **Componentes** | `SecurityConfig`, `JwtFilter`, `BranchController`, `BranchService` |
| **Archivo de prueba** | `playpal/src/test/java/com/api/playpal/branch/infrastructure/BranchControllerFuzzingTest.java` |

### Descripción

Se generan **60 variaciones** (12 mutaciones × 5 endpoints de `BranchController`) de headers HTTP malformados y se disparan contra la API. La barrera de seguridad debe rechazarlas **antes** de que ninguna alcance la capa de negocio, y ninguna respuesta puede ser 5xx.

### Refactor de código real exigido por el caso

La primera inspección detectó que `JwtFilter` invocaba `extractId(token)` **antes** de `isTokenValid(token)`: cualquier token malformado/corrupto hacía que `parseClaimsJws` lanzara una excepción no manejada que escapaba del filtro (→ 500 en producción). Además, un token válido de un usuario inexistente lanzaba `RuntimeException("Usuario no encontrado")` (→ 500). **Se endureció `JwtFilter`**: todo el procesamiento del token quedó dentro de un `try/catch`; ante cualquier fallo se limpia el `SecurityContext` y la petición sigue como anónima, que `SecurityConfig` rechaza con 403. El `orElseThrow` se reemplazó por `ifPresent`. Este es exactamente el tipo de defecto que el fuzzing está diseñado a exponer: sin el refactor, 50 de los 60 casos (los que llevan `Bearer` corrupto) habrían producido error de servidor.

### Diseño del fuzzer

- Semilla fija `20260714` (`new Random(SEED)`): los 60 casos son aleatorios pero **reproducibles**; el caso se marca `@Tag("nightly")` igualmente por su naturaleza no determinista si se cambia la semilla.
- 5 endpoints: `GET /api/branches/{id}`, `GET /api/branches/provider/{id}`, `POST /api/branches/`, `PUT /api/branches/{id}`, `DELETE /api/branches/`.
- 12 mutaciones por endpoint: token aleatorio, token vacío, token de 16 KB, JWT truncado, JWT con firma corrupta, token con caracteres Unicode/encoding inválido, esquema `bearer` en minúsculas, esquema `Basic`, header `Authorization` vacío, token con caracteres corruptos (`..%%$$..`), header arbitrario de 8 KB sin credencial, `Content-Type` inválido sin credencial.
- Oráculo doble por cada caso: `assertThat(status).isIn(400, 401, 403)` (nunca 5xx) **y** `verifyNoInteractions(branchService)` (el mock de `BranchService` jamás recibe una invocación — la petición fue contenida en el módulo de seguridad).

### Aislamiento en suite separada

La clase está anotada `@Tag("nightly")` y Surefire excluye ese tag por defecto (`<excludedGroups>${surefire.excluded.groups}</excludedGroups>` con la propiedad `surefire.excluded.groups=nightly`). El profile Maven `nightly` levanta la exclusión. La suite estándar (58 tests) no la ejecuta; se corre explícitamente (comando al final del documento).

### Criterios de aceptación y resultado

| Criterio | Objetivo | Resultado real |
|----------|----------|----------------|
| ≥ 50 variaciones de headers malformados | ≥ 50 | ✅ 60 casos generados |
| Ninguna respuesta 5xx | 0 | ✅ 60/60 respondieron 403 (dentro del conjunto permitido 400/401/403) |
| `BranchService` nunca invocado | `verifyNoInteractions` en 60/60 | ✅ |
| Aislada de la suite estándar | Tag `nightly` excluido por defecto | ✅ La suite por defecto reporta 58 tests; con `-Pnightly` corre el fuzzing |

Ejecución real: `Tests run: 60, Failures: 0, Errors: 0` en 11.09 s (con el `JwtFilter` original, estos casos producían excepción de servlet no manejada — defecto corregido y documentado).

---

## S-001 — Flujo Completo de Reserva vía API REST

| Campo | Valor |
|-------|-------|
| **ID** | S-001 |
| **Técnica ISO 29119-4** | Prueba de escenario end-to-end (use case testing) sobre el sistema desplegado |
| **Subcaracterística ISO 25010** | Mantenibilidad → **Testabilidad** (el sistema completo es comprobable de forma automatizada y hermética) |
| **Nivel** | Sistema |
| **Enfoque** | Caja negra |
| **Prioridad** | CRÍTICA |
| **Herramientas** | RestAssured + Testcontainers (`MongoDBContainer` sobre `mongo:7`) + `@DynamicPropertySource` + `@Timeout(15)` |
| **Componentes** | Toda la aplicación real: auth (registro/login/JWT), branches, courts, reservations, MongoDB real en contenedor |
| **Archivos de prueba** | `playpal/src/test/java/com/api/playpal/system/ReservationFullFlowSystemTest.java` (fixtures compartidos en `MongoSystemTestSupport.java`) |

### Descripción

Prueba de sistema hermética: la aplicación completa (sin ningún mock) contra un MongoDB **real** levantado por Testcontainers. Un único test encadena el caso de uso de negocio completo, de proveedor a reserva cobrada.

### Infraestructura

- `@Container static MongoDBContainer MONGO = new MongoDBContainer(DockerImageName.parse("mongo:7"))` — arrancó en 2.8 s (imagen ya presente localmente).
- `@DynamicPropertySource` sobreescribe `spring.data.mongodb.uri` con `MONGO::getReplicaSetUrl` — el `application.properties` de producción no se toca.
- `RestAssured.port` se fija en `@BeforeEach` con el `@LocalServerPort` aleatorio.
- **Precondición:** Docker en ejecución (verificado: Docker 27.3.1).

### Cadena verificada (un solo test, orden estricto)

| Paso | Acción | Verificación real |
|------|--------|--------------------|
| 1 | Registro de provider + login (multipart) | 200 + JWT capturado |
| 2 | `POST /api/branches/` con Bearer del provider | 200 + id de sede |
| 3 | `POST /api/courts/` (multipart, precio 1500/h) | 200 + id de cancha |
| 4 | `GET /api/courts/` **sin token** (catálogo público) | 200 y `content.id` contiene la cancha creada — visibilidad confirmada |
| 5 | Registro + login de un segundo usuario `role=user` | 200 + JWT propio |
| 6 | `POST /api/reservations/` (fecha +2 días, start=10, end=13) | 200 + id de reserva |
| 7 | `GET /api/reservations/{id}` con el token del usuario | 200, `totalPrice = 4500` = **1500 × (13 − 10)**, `date` correcta, `active = true` |

### Criterios de aceptación y resultado

| Criterio | Objetivo | Resultado real |
|----------|----------|----------------|
| Flujo completo sin mocks contra Mongo real | 7 pasos verdes | ✅ |
| Precio calculado = `price × (end − start)` | 4500 | ✅ `totalPrice = 4500` |
| Escenario completo < 15 s (`@Timeout(15)`) | < 15 s | ✅ **1.05 s** de ejecución del método (el arranque de contenedor/contexto queda fuera del timeout, como corresponde) |

Ejecución real: `Tests run: 1, Failures: 0` (clase completa 6.7 s incluyendo arranque de contexto).

> **Nota de infraestructura documentada:** la primera versión compartía contenedor y contexto entre S-001 y S-002, y falló: el *extension* de `@Container` **detiene el contenedor estático al terminar cada clase**, pero el contexto Spring cacheado conservaba la URI del contenedor muerto (timeout de conexión de 30 s). Se resolvió con `@DirtiesContext(classMode = AFTER_CLASS)` en la base común: cada clase de sistema arranca contexto fresco contra su contenedor fresco. Pitfall clásico de Testcontainers + caché de contextos, dejado registrado a propósito.

---

## S-002 — Prevención de Doble Reserva (Defecto de Regla de Negocio)

| Campo | Valor |
|-------|-------|
| **ID** | S-002 |
| **Técnica ISO 29119-4** | Prueba negativa de regla de negocio, documentada como **test de regresión de defecto conocido** |
| **Subcaracterística ISO 25010** | Mantenibilidad → **Analizabilidad** (el defecto queda diagnosticado, reproducible y monitoreado por la suite) |
| **Nivel** | Sistema |
| **Enfoque** | Caja negra |
| **Prioridad** | CRÍTICA |
| **Herramientas** | RestAssured + Testcontainers (misma infraestructura `MongoSystemTestSupport` que S-001) |
| **Componentes** | `ReservationService.save()` (regla ausente), `ReservationController`, MongoDB real |
| **Archivo de prueba** | `playpal/src/test/java/com/api/playpal/system/DoubleBookingRegressionTest.java` |

### Descripción

`ReservationService.save()` contiene el `// TODO: Validar si ya existe una reserva a esa hora` y **no valida solapamientos**: dos usuarios distintos pueden reservar la misma cancha, misma fecha y mismo rango horario, y ambos pagan. El caso NO corrige el defecto (decisión explícita del informe): lo **fija como test de regresión** que describe el comportamiento actual, de modo que cuando alguien implemente la validación, este test fallará y obligará a actualizarlo conscientemente (invirtiendo la aserción a 4xx).

### Escenario (fixtures compartidos con S-001)

| Paso | Acción | Resultado real |
|------|--------|----------------|
| 1 | Provider registra sede + cancha (precio 1000/h) | 200 |
| 2 | Se registran y autentican **dos usuarios distintos** | 200 + 2 JWT |
| 3 | Usuario A reserva cancha, fecha +3 días, 18–20 h | **200** |
| 4 | Usuario B reserva **la misma cancha, misma fecha, mismo rango 18–20 h** | **200** ← aquí debería ser 4xx |
| 5 | `GET /api/reservations/court/{courtId}` | 200 con **2 reservas solapadas persistidas** en Mongo real |

La aserción central es deliberadamente `assertEquals(200, secondBookingStatus)` dentro del test `whenOverlappingBooking_currentlySucceeds_butShouldFail`: el nombre y la aserción documentan que el éxito del segundo booking es el **estado actual defectuoso**, no el deseado.

### Criterios de aceptación y resultado

| Criterio | Objetivo | Resultado real |
|----------|----------|----------------|
| Reproducir el defecto contra el sistema real | Doble reserva persistida | ✅ 2 reservas solapadas confirmadas en Mongo |
| Test verde mientras el defecto exista | `assertEquals(200, ...)` | ✅ `Tests run: 1, Failures: 0` |
| El test actúa como alarma de regresión | Fallará al implementarse la validación | ✅ Por construcción: implementar el TODO rompe este test y fuerza su actualización consciente |

**Estado: DEFECTO CONOCIDO — VER BACKLOG.** Acción recomendada al implementarlo: en `save()`, consultar `findReservationsByCourt(courtId)` filtrando por fecha y solapamiento de rango `[start, end)` antes de persistir, con mensaje propio (p. ej. `"Court already booked for that time"`), y actualizar este test para exigir 4xx.

---

## Patrones y técnicas aplicados en B-002, B-003, S-001 y S-002

### Test doubles para verificar la NO-invocación (mock como barrera de observación)
En B-003 el `@MockitoBean` de `BranchService` no se usa para stubbing sino como **sensor**: `verifyNoInteractions(branchService)` tras cada petición malformada prueba que la capa de negocio quedó completamente aislada del tráfico hostil. **Por qué habilita modularidad:** demuestra que la responsabilidad de rechazo pertenece por completo al módulo de seguridad; una violación del encapsulamiento (una petición corrupta alcanzando el servicio) falla el test aunque la respuesta HTTP pareciera correcta.

### Testcontainers para aislamiento de infraestructura real
S-001/S-002 no simulan MongoDB: levantan `mongo:7` efímero por clase, sobreescribiendo solo `spring.data.mongodb.uri` vía `@DynamicPropertySource`. **Por qué habilita testabilidad:** el sistema completo es comprobable en cualquier máquina con Docker, sin entorno compartido, sin datos residuales entre corridas y sin tocar la configuración de producción — el test de sistema deja de depender de un Mongo "de la oficina". El pitfall de caché de contextos encontrado (y su solución con `@DirtiesContext(AFTER_CLASS)`) quedó documentado en S-001.

### Fixtures reutilizables entre casos de sistema
`MongoSystemTestSupport` centraliza contenedor, override de URI, puerto de RestAssured y los cuatro fixtures de negocio (`registerAndLogin`, `createBranch`, `createCourt`, `bookCourt`). S-001 y S-002 son solo la narrativa de su escenario. **Por qué habilita mantenibilidad de la propia suite:** si cambia el contrato de un endpoint (p. ej. login deja de ser multipart), se corrige un único helper y ambos casos de sistema siguen siendo válidos — la suite aplica los mismos principios DRY que exige al código productivo.

### Documentación de defectos como test de regresión (defect pinning)
S-002 codifica el comportamiento defectuoso actual con una aserción verde y un nombre explícito (`whenOverlappingBooking_currentlySucceeds_butShouldFail`). **Por qué habilita analizabilidad:** el defecto deja de ser un TODO en un comentario y pasa a ser un artefacto ejecutable: reproducible al instante, visible en cada corrida, y con un mecanismo automático que exige revisar el test cuando la regla se implemente. Es la técnica estándar para gestionar defectos aceptados en backlog sin perderles el rastro.

### Fuzzing con semilla fija y suite aislada por tags
B-003 genera sus 60 casos con `Random(20260714)`: aleatoriedad reproducible (misma falla → mismo caso → depurable). El `@Tag("nightly")` + `excludedGroups` de Surefire y el profile `nightly` separan el ciclo rápido de feedback (58 tests deterministas) del ciclo de robustez. **Por qué habilita modularidad del proceso de prueba:** cada suite tiene contrato y cadencia propios, y el fuzzing puede crecer (más semillas, más mutaciones) sin penalizar el build estándar.

### Caja gris con esquema JSON como contrato
B-002 no compara objetos: compara **conjuntos de nombres de campos** del JSON y luego deserializa ambas respuestas a `User.class` con el `ObjectMapper` real de la aplicación. **Por qué habilita reusabilidad:** verifica en el nivel correcto (el contrato serializado que consumen los clientes) que un único modelo sirve a ambos roles; si alguien introdujera un `ProviderDTO` divergente, el test falla por diferencia de esquema, no por detalles internos.

### Endurecimiento del código real guiado por el caso (B-003 → `JwtFilter`)
El fuzzing exigía "ninguna respuesta 500", y el `JwtFilter` original no lo cumplía (parse de token fuera de todo `try/catch`, `orElseThrow` ante usuario inexistente). El refactor mínimo (envolver el procesamiento del token, degradar a petición anónima ante cualquier fallo) es el ejemplo de cómo un caso de prueba de robustez repara diseño: el manejo de errores queda contenido en el módulo del filtro y la política de rechazo sigue centralizada en `SecurityConfig`.

---

## Patrones y técnicas aplicados en B-001, B-005, B-006 y B-009

Toda la documentación que normalmente iría en comentarios/Javadoc está en este archivo; las clases de test contienen solo código con nombres descriptivos.

### Arrange–Act–Assert (AAA)
Cada test tiene tres bloques separados por líneas en blanco: preparación de stubs y datos, una única invocación al SUT, y aserciones (de estado y de interacción). **Por qué habilita mantenibilidad:** un test legible en tres actos es en sí un artefacto de *analizabilidad* — documenta el contrato de cada método (entrada → comportamiento observable) mejor que cualquier comentario, y localiza al instante qué expectativa se rompió cuando falla.

### Test Doubles (mocks) con Mockito
`@Mock` + `when/thenReturn` (y `thenAnswer(inv -> inv.getArgument(0))` para simular el eco del `save` de MongoDB) sustituyen la persistencia; `verify(...).save(any(...))`, `verify(never())` y `verifyNoInteractions` comprueban el protocolo de colaboración, no solo el estado. **Por qué habilita testabilidad (B-006):** demuestra que cada servicio es comprobable en milisegundos y sin infraestructura — la definición operativa de la subcaracterística *testabilidad* de ISO 25010. El `@Timeout(500 ms)` convierte esa propiedad en una métrica ejecutable.

### Inyección de dependencias por constructor
Todos los servicios reciben sus colaboradores (y ahora también su configuración) por constructor y los guardan en campos `final`. **Por qué habilita testabilidad y modificabilidad:** el SUT se puede construir en un test con cualquier combinación de dobles y parámetros (`new ReservationService(mock, mock, 12)`) sin reflexión ni contenedor; y las dependencias quedan explícitas, lo que reduce el análisis de impacto ante cambios.

### Inversión de dependencias vía interfaces de repositorio
Los repositorios son interfaces (`UserRepositoryImp`, etc., sobre `MongoRepository`), por lo que Mockito genera implementaciones sintéticas sin tocar MongoDB. **Por qué habilita testabilidad:** es el "puerto" de la arquitectura hexagonal — el dominio de aplicación habla con una abstracción y el adaptador Mongo es intercambiable por un doble. *Observación honesta registrada:* los servicios dependen de las interfaces `*RepositoryImp` (capa infrastructure) y no de las interfaces de dominio (`ReservationRepository`, etc.), una desviación de la hexagonal pura que no impide el mocking (siguen siendo interfaces) pero que sería el siguiente refactor natural; la regla ArchUnit ya vigila la frontera controller→repositorio.

### Externalización de configuración con `@Value` (+ `@TestPropertySource` / `@DynamicPropertySource`)
La regla de negocio "antelación mínima" pasó de constante compilada a propiedad `reservation.advance-hours` con default. **Por qué habilita modificabilidad (B-001):** el cambio de comportamiento se logra editando configuración, no código; la tabla de decisiones lo prueba ejecutando el mismo binario bajo dos umbrales en la misma corrida, y `@DynamicPropertySource` demuestra la inyección del valor en tiempo de ejecución (cero recompilación).

### Verificación de arquitectura con ArchUnit
Reglas declarativas sobre el bytecode de producción codifican las restricciones de la arquitectura hexagonal y de la autorización centralizada. **Por qué habilita modularidad (B-005):** la modularidad no se prueba solo ejecutando — hay que impedir que se erosione. Las 4 reglas convierten "los controllers no autorizan ni persisten" y "solo `SecurityConfig` decide autorización HTTP" en un test que rompe el build ante la primera violación, manteniendo el único punto de cambio de la política de seguridad.

### `@MockitoBean` en tests de integración
En los contextos `@SpringBootTest` (B-001 y B-005) los repositorios reales se sobreescriben con mocks administrados por Spring (reemplazo del deprecado `@MockBean` en Boot 3.4). **Por qué ayuda:** permite probar la configuración real (Security filter chain, JWT, resolución de `@Value`) aislando únicamente la infraestructura de datos — integración selectiva, otra cara de la modularidad.

### `@ParameterizedTest` + `@CsvSource` y clases `@Nested`
Las filas de la tabla de decisiones, la matriz MC/DC y la batería de endpoints protegidos se expresan como datos tabulares junto al test; los contextos de B-001 se agrupan en clases anidadas por umbral. **Por qué habilita analizabilidad (B-009):** la tabla del informe SQA y el código quedan isomorfos — se puede auditar caso por caso que la matriz mínima de 4 combinaciones está implementada, y cada rechazo se distingue por su mensaje (`assertEquals` sobre `getMessage()`).

### Mutation testing con Pitest
Pitest muta `ReservationService` y exige que la suite detecte cada mutante. **Por qué habilita analizabilidad:** el mutation score mide si los tests realmente *analizan* el comportamiento (una suite con cobertura alta pero aserciones débiles deja mutantes vivos). El resultado 19/19 con test strength 100% certifica que la matriz MC/DC no es decorativa.

---

## Comandos de ejecución (PowerShell, desde `playpal/`)

```powershell
cd playpal

# Suite completa (55 tests) + reporte JaCoCo (enganchado a la fase test)
.\mvnw.cmd test

# B-001 — Tabla de decisiones del parámetro de antelación (5 tests, 3 contextos)
.\mvnw.cmd test "-Dtest=ReservationServiceAdvanceHoursTest"

# B-005 — Autorización centralizada: integración MockMvc + reglas ArchUnit (20 tests)
.\mvnw.cmd test "-Dtest=SecurityConfigIntegrationTest,AuthorizationArchitectureTest"

# B-006 — Mocking de las 4 interfaces de repositorio (25 tests, < 500 ms c/u)
.\mvnw.cmd test "-Dtest=*RepositoryMockTest"

# B-009 — Matriz MC/DC sobre ReservationService.save() (4 tests)
.\mvnw.cmd test "-Dtest=ReservationServiceMcdcTest"

# Mutation testing con Pitest sobre ReservationService
.\mvnw.cmd org.pitest:pitest-maven:mutationCoverage

# B-002 — Reutilización de DTOs por rol (1 test)
.\mvnw.cmd test "-Dtest=UserDtoIntegrationTest"

# B-003 — Fuzzing de headers (60 tests; excluido de la suite estándar, requiere el profile nightly)
.\mvnw.cmd test -Pnightly "-Dtest=BranchControllerFuzzingTest"

# S-001 + S-002 — Pruebas de sistema con Testcontainers (requieren Docker en ejecución)
.\mvnw.cmd test "-Dtest=ReservationFullFlowSystemTest,DoubleBookingRegressionTest"

# S-002 solo (defecto conocido documentado)
.\mvnw.cmd test "-Dtest=DoubleBookingRegressionTest"

# Toda la bateria incluyendo nightly
.\mvnw.cmd test -Pnightly
```

(En Git Bash: `./mvnw test`, mismos argumentos.)

## Rutas de los reportes

| Reporte | Ruta |
|---------|------|
| JaCoCo (HTML) | `playpal/target/site/jacoco/index.html` |
| JaCoCo (CSV/XML) | `playpal/target/site/jacoco/jacoco.csv`, `playpal/target/site/jacoco/jacoco.xml` |
| Pitest (HTML) | `playpal/target/pit-reports/index.html` |
| Pitest (XML) | `playpal/target/pit-reports/mutations.xml` |
| Surefire (resultados por clase) | `playpal/target/surefire-reports/` |

## Dependencias y plugins agregados al `pom.xml`

| Artefacto | Versión | Alcance / uso |
|-----------|---------|----------------|
| `spring-boot-starter-test` | (gestionada por Boot 3.4.4 — ya existía) | JUnit 5, Mockito, MockMvc, AssertJ |
| `spring-security-test` | gestionada por Boot | test |
| `junit-jupiter-params` | gestionada por Boot (5.11.x) | `@ParameterizedTest`/`@CsvSource` |
| `com.tngtech.archunit:archunit-junit5` | 1.4.1 | test |
| `org.jacoco:jacoco-maven-plugin` | 0.8.13 | `prepare-agent` + `report` en fase `test` |
| `org.pitest:pitest-maven` | 1.19.1 | goal `mutationCoverage`, target `ReservationService` |
| `org.pitest:pitest-junit5-plugin` | 1.2.2 | soporte JUnit Platform para Pitest |
| `org.testcontainers:junit-jupiter` | gestionada por Boot | test (S-001/S-002) |
| `org.testcontainers:mongodb` | gestionada por Boot | test — `MongoDBContainer` sobre `mongo:7` |
| `io.rest-assured:rest-assured` | gestionada por Boot | test (S-001/S-002) |
| `maven-surefire-plugin` (config) | gestionada por Boot | `excludedGroups=nightly` por defecto + profile `nightly` que lo levanta |

---

## Tabla consolidada — los 8 casos

Resultados de la ejecución real del 2026-07-14 (suite por defecto: **58 tests, 0 fallos**; suite nightly: **60 tests, 0 fallos**; Pitest sobre `ReservationService`: **100 %**).

| ID | Caso | Subcaracterística ISO 25010 | Nivel | Enfoque | Prioridad | Tests | Estado |
|----|------|------------------------------|-------|---------|-----------|-------|--------|
| B-001 | Tabla de decisiones — parámetro de antelación | Modificabilidad | Unitario | Caja blanca | CRÍTICA | 5 | ✅ Verde |
| B-002 | Reutilización de DTOs por rol | Reusabilidad | Integración | Caja gris | MEDIA | 1 | ✅ Verde |
| B-003 | Fuzzing de headers en BranchController | Modularidad | Integración | Caja negra | ALTA | 60 | ✅ Verde (suite `nightly`) |
| B-005 | Decisión de autorización centralizada | Modularidad | Integración | Caja blanca | ALTA | 20 | ✅ Verde |
| B-006 | Mocking de interfaces de repositorio | Testabilidad | Unitario | Caja blanca | ALTA | 25 | ✅ Verde (< 500 ms c/u) |
| B-009 | MC/DC sobre validación de reserva | Analizabilidad | Unitario | Caja blanca | ALTA | 4 | ✅ Verde (mutation score 100 %) |
| S-001 | Flujo completo de reserva vía API REST | Testabilidad | Sistema | Caja negra | CRÍTICA | 1 | ✅ Verde (1.05 s < 15 s) |
| S-002 | Prevención de doble reserva | Analizabilidad | Sistema | Caja negra | CRÍTICA | 1 | ⚠️ **Defecto conocido, ver backlog** — test verde que documenta el solapamiento permitido y fallará cuando se implemente la validación |

Cobertura JaCoCo global tras la suite por defecto: instrucciones **63.4 %** (1189/1874), líneas **64.3 %** (292/454); clases directamente apuntadas por los casos: `ReservationService` 92.9 % líneas, `JwtFilter` 100 %, `SecurityConfig` 100 %, `AuthService` 91.7 %, `BranchService` 84 %.
