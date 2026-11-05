# Autenticación entre el servicio de turnos y el de catálogo

## Decisión
El servicio de turnos se identifica ante el catálogo con un **JWT técnico de servicio**, propio, en lugar de propagar el JWT del usuario final. Decisión acordada con el docente en la consulta del 21/10/2026.

| Comunicación | Token |
|---|---|
| App KMP → turnos y catálogo | JWT del usuario final (`ROLE_USER`, 24 h), emitido por turnos al iniciar sesión |
| Turnos → catálogo | JWT de servicio (`sub = turnos-service`, `ROLE_SERVICE`, 5 min) |

Ambos se firman con HS256 y la clave compartida `JWT_SECRET`, y el catálogo los valida de la misma forma (firma, vigencia y roles del claim `auth`).

## Justificación
- **Identidad:** el catálogo sabe con certeza que quien consulta es el servicio de turnos (`sub = turnos-service`), y no un usuario que intenta acceder a operaciones internas.
- **Autorización:** las operaciones internas del catálogo pueden exigir `ROLE_SERVICE`. Un token de usuario no las habilita y un token de servicio no habilita operaciones de usuario (por ejemplo, `GET /api/account` responde 403).
- **Independencia del usuario:** las consultas de turnos al catálogo (profesional y horarios vigentes) no dependen de quién es el usuario; funcionan aunque el token del usuario esté por vencer, y no requieren reenviarlo.
- **Trazabilidad:** la identidad del usuario se conserva en el servicio de turnos, que registra qué usuario inició cada operación; el catálogo registra que la consulta provino del servicio.

## Medidas de seguridad
- Vida corta (5 minutos), con renovación automática 30 segundos antes del vencimiento.
- Se genera y se usa solo dentro del backend; nunca se envía a la aplicación KMP ni se registra en los logs.
- La clave `JWT_SECRET` se configura por variable de entorno y no se versiona.

## Limitaciones
- Al usar una clave simétrica compartida, cualquier servicio que la conozca podría emitir tokens válidos; su custodia es crítica y debe rotarse si se expone.
- No existe revocación individual de tokens: se mitiga con la vida corta del token de servicio.