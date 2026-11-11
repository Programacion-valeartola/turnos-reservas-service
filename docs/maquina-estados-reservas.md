# Decisiones tomadas en las consultas con el profesor

## Consulta 1 — miércoles 21/10/2026

**Estado presentado:** M1 del catálogo completo (7/7), M2 en curso (modelo de dominio y cliente del snapshot), T1 de turnos 4/5, cuenta técnica `proyecto-artola` registrada y snapshot verificado en Postman.

### Decisiones
1. **Autenticación entre servicios:** los dos servicios se comunican entre sí con un **JWT especial de servicio** (no se propaga el JWT del usuario final).
    - App → servicios: JWT del usuario final.
    - Turnos → Catálogo: JWT de servicio.
    - Resuelve el issue de T2 "Definir la autenticación entre el servicio de turnos y el de catálogo".
2. **Kafka:** ambos servicios se conectan al mismo Kafka, cada uno con su propio tópico:
    - Catálogo escucha `catedra.catalog.proyecto-artola` (`CatalogUpdated`).
    - Turnos escucha `alumnos.turnos.acciones.proyecto-artola` y publica en `catedra.turnos.telefono.proyecto-artola`.
3. **Fecha de entrega final:** fines de noviembre de 2026, sin fecha exacta definida todavía.

---

## Avisos del profesor por el canal — miércoles 04/11/2026

- **Fecha de regularización:** fines de noviembre (~30/11). Si el trabajo está listo antes, se evalúa antes.
- **Requisito para regularizar:** los **dos servicios Java funcionando**. La **app mobile (KMP) puede aplazarse hasta el examen final**.
- **Cambios automáticos del catálogo:** desde el 04/11 a la mañana, un proceso de la cátedra cambia cada **una hora** al primer profesional (enabled ↔ disabled). Sirve para probar las sincronizaciones parciales (incrementales) del catálogo.

---

## Consulta 2 — viernes 06/11/2026, 14:00

**Estado presentado:** catálogo completo (M1–M3) con sincronización incremental real (versión local 56, `INCREMENTAL`, sin errores); turnos con registro, inicio de sesión con JWT, protección de endpoints y JWT de servicio.

### Decisiones
1. **Regularización:** el profesor evalúa el progreso a lo largo de las consultas y el movimiento del repositorio (commits, PRs, issues).
2. **A partir de la próxima consulta se analiza el código** en detalle; hay que seguir avanzando.
3. **Máquina de estados:** no se llegó a mostrar; se adopta la diseñada (`docs/maquina-estados-reservas.md`).
4. **Teléfono:** **no** se valida el formato en el backend. El rechazo del teléfono por parte de la cátedra es aleatorio; se maneja con `AdditionalInformationRejected` y el reintento del usuario hasta `expiresAt`.

---

## Consulta 3 — lunes 16/11/2026, 17:30

### Objetivo
- Avanzar el T3: endpoint interno del catálogo, cliente del catálogo, cliente REST de la cátedra, disponibilidad y, si es posible, inicio de la reserva con hold.
- Estar preparada para explicar el código (empieza el análisis de código).

### Plan posterior (tentativo, actualizado 10/11)
- 10–19/11: T3 completo (disponibilidad y reserva de punta a punta) + M4 del catálogo.
- 20–25/11: T4 (reservas propias, cancelación, vencimientos, recuperación, tests).
- 26–30/11: documentación obligatoria de ambos servicios y pruebas de punta a punta.
- App KMP: después de la regularización, para el examen final.