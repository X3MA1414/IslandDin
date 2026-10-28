# Isla

Isla dinámica estilo iPhone para Android (pensada para el Galaxy S24 Ultra), escrita desde cero en Kotlin y Jetpack Compose.

## Qué hace

- **Píldora** alrededor de la cámara frontal, con tamaño y posición ajustables.
- **Notificaciones**: se expande con el icono, título y texto. Al tocarla se abre la notificación.
- **Música**: carátula y ecualizador en modo compacto. Al tocarla se expande con título, artista, progreso y controles. Con una pulsación larga se abre la app.
- **Carga**: muestra un aviso al conectar o desconectar el cargador.
- **Ajustes**: color de acento, duración, apps permitidas u ocultas y ocultar en horizontal.

## Arquitectura

Clean Architecture en tres módulos. Las dependencias siempre apuntan hacia dentro:

```
app  ──►  data  ──►  domain
 └──────────────────►  ┘
```

| Módulo | Contenido | Depende de Android |
|---|---|---|
| `domain` | Modelos, interfaces de repositorio y puertos, políticas y casos de uso | No (Kotlin puro) |
| `data` | Implementaciones: listener de notificaciones, sesiones multimedia, batería, DataStore | Sí |
| `app` | Servicios, ventana overlay, UI en Compose, presenter y composition root (`AppContainer`) | Sí |

### Principios SOLID aplicados

- **S**: cada clase tiene una sola razón para cambiar. Por ejemplo, `IslandOverlayWindow` solo gestiona la ventana, `IslandUiMapper` solo calcula medidas, `IslandPresenter` solo lleva el estado de interacción y `NotificationMapper` solo traduce datos.
- **O**: la prioridad de contenidos (`ContentPriorityPolicy`) y el filtrado (`NotificationFilter`) son interfaces. Para añadir un comportamiento nuevo basta con crear otra implementación, sin tocar el caso de uso.
- **L**: todos los fakes de los tests sustituyen a las implementaciones reales sin cambiar el comportamiento esperado.
- **I**: hay interfaces pequeñas y separadas (`MediaRepository` y `MediaCommands`, `NotificationRepository` y `NotificationSink` y `NotificationActions`, `Clock`, `Ticker`).
- **D**: los casos de uso dependen de abstracciones. Solo `AppContainer` conoce las clases concretas, mediante inyección manual sin frameworks.

## Tests

```bash
./gradlew test
```

- `domain`: política de prioridades, filtro de notificaciones, normalización de ajustes y casos de uso (con fakes y tiempo virtual).
- `data`: mapeo de notificaciones y multimedia, reductor de batería, repositorio en memoria y round-trip con DataStore.
- `app`: mapper de UI, presenter (expansión y autocierre) y cálculo de posición sobre la cámara.

## Compilar e instalar

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Se necesita el JDK 17 o superior y el Android SDK, con `local.properties` o `ANDROID_HOME` configurados.

## Primer arranque

1. Abre **Isla** y pulsa **Activar** en *Acceso a notificaciones*.
2. Pulsa **Activar** en *Accesibilidad* y elige **Isla dinámica**.
   - Si aparece en gris porque la app no viene de Play Store, ve a *Información de la app → ⋮ → Permitir ajustes restringidos* y vuelve a intentarlo.
3. Opcional, pero recomendado en Samsung: quita la restricción de batería.
