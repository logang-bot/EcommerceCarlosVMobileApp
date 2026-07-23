# Changelog

Notas de versión que se envían al canal de Telegram con cada build de producción.

El workflow extrae la sección cuyo número coincide con `APP_VERSION_NAME` en
`gradle.properties`. Si no encuentra una sección para esa versión, el build de
producción falla antes de compilar.

**Cómo escribir estas notas:** las lee el dueño del negocio, no un desarrollador.
En español, en frases cortas, describiendo qué cambia para quien usa la app —
no qué archivo se modificó. Máximo ~800 caracteres por versión (Telegram limita
el texto que acompaña al archivo).

> Los cambios aún **sin publicar** se van anotando en `CHANGELOG.unreleased.md`. Al
> preparar una versión, se resumen en una sección `## [VERSION]` aquí y ese archivo se
> vacía. Ver la skill `updating-changelog-or-release-notes`.

---

## [1.0.0]
- Primera versión distribuida por Telegram.
