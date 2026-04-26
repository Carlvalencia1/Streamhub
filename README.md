# 📺 StreamHub

Plataforma de **live streaming** para Android construida con **Jetpack Compose**, **Clean Architecture** y **Firebase**. Permite a streamers transmitir en vivo vía RTMP, y a los espectadores descubrir, seguir y participar en tiempo real mediante chat y comunidades.

---

## ✨ Funcionalidades Principales

| Rol | Funcionalidades |
|---|---|
| **Streamer** | Crear y gestionar streams · Transmisión en vivo (RTMP) con cámara/micrófono · Panel de canal · Publicaciones en el canal · Comunidades propias |
| **Viewer** | Descubrir streams en vivo (Home) · Reproducción HLS · Chat en tiempo real · Suscripciones · Historial de visualización · Seguir a streamers |
| **Ambos** | Autenticación (registro, login, Google Sign-In) · Selección de rol · Perfil de usuario · Notificaciones push (FCM) · Comunidades y canales de chat · Tema claro/oscuro |

---

## 🏗️ Arquitectura

El proyecto sigue los principios de **Clean Architecture** organizado por **features**:

```
com.valencia.streamhub/
├── core/
│   ├── database/       # Room: AppDatabase, DAOs, entidades, mappers
│   ├── di/             # Módulos de Hilt (inyección de dependencias)
│   ├── hardware/       # Gestión de cámara y dispositivos
│   ├── network/        # Configuración de Retrofit (BackendConfig)
│   ├── services/       # StreamMessagingService (FCM) · StreamBroadcastForegroundService
│   ├── session/        # Gestión de sesión del usuario
│   ├── ui/theme/       # Tema Material 3 (claro/oscuro)
│   └── work/           # WorkManager (sincronización token FCM)
│
├── features/
│   ├── broadcasting/   # Pantalla de transmisión en vivo (cámara + RTMP)
│   ├── channelposts/   # Publicaciones del canal del streamer
│   ├── communities/    # Comunidades, detalle de comunidad, chat de canales
│   ├── followers/      # Listado de seguidores y seguidos
│   ├── hardware/       # Selección de hardware (cámara/micrófono)
│   ├── streams/        # Home, detalle de stream, crear stream, mis streams, suscripciones, historial
│   └── users/          # Login, registro, selección de rol, perfil, panel de canal
│
├── navigation/
│   ├── routes/Screen.kt   # Rutas tipadas (sealed class)
│   ├── AppNavGraph.kt     # Grafo de navegación principal
│   └── MainScreen.kt     # Scaffold con bottom navigation (tabs según rol)
│
├── DemoHiltApp.kt         # Application class (Hilt)
└── MainActivity.kt        # Activity principal (permisos, FCM, Edge-to-Edge)
```

Cada **feature** sigue la estructura de capas:

```
feature/
├── data/           # Repositorios, DTOs, Mappers, fuentes de datos
├── domain/         # Entidades de negocio y Use Cases
├── presentation/
│   ├── screens/    # Composables de pantalla
│   ├── components/ # Componentes reutilizables de UI
│   └── viewmodels/ # ViewModels con StateFlow
└── di/             # Módulo Hilt de la feature (cuando aplica)
```

---

## 🛠️ Stack Tecnológico

| Categoría | Tecnologías |
|---|---|
| **Lenguaje** | Kotlin |
| **UI** | Jetpack Compose · Material 3 · Google Fonts |
| **Navegación** | Navigation Compose |
| **Inyección de dependencias** | Hilt + KSP |
| **Networking** | Retrofit 2 + Kotlin Serialization |
| **Imágenes** | Coil Compose |
| **Base de datos local** | Room |
| **Streaming** | StreamPack (RTMP) · WebRTC · Media3 ExoPlayer (HLS) |
| **Notificaciones push** | Firebase Cloud Messaging (FCM) |
| **Autenticación** | Credential Manager · Google Identity |
| **Tareas en segundo plano** | WorkManager · Foreground Service |
| **Compilación** | Gradle Kotlin DSL · Version Catalogs · Product Flavors (dev/prod) |

---

## 📱 Pantallas

### Autenticación
- **Login** — Email/contraseña y Google Sign-In
- **Registro** — Creación de cuenta
- **Selección de Rol** — Streamer o Viewer

### Streamer
- **Home** — Feed de streams en vivo
- **Mis Streams** — Gestión de streams propios
- **Crear Stream** — Configuración de título, descripción y categoría
- **Broadcasting** — Transmisión en vivo con vista de cámara y controles
- **Comunidades** — Crear y gestionar comunidades
- **Canal** — Panel de publicaciones del streamer

### Viewer
- **Home** — Descubrimiento de streams en vivo
- **Comunidades** — Explorar y unirse a comunidades
- **Suscripciones** — Streams de canales suscritos
- **Historial** — Streams vistos recientemente
- **Detalle de Stream** — Reproductor HLS + chat en tiempo real

### Común
- **Perfil** — Información del usuario, followers, following, tema
- **Detalle de Comunidad** — Canales de la comunidad
- **Chat de Canal** — Mensajería dentro de un canal de comunidad

---

## 🔔 Notificaciones Push (FCM)

El sistema de notificaciones está integrado de extremo a extremo:

1. **`StreamMessagingService`** — Servicio que recibe mensajes FCM y muestra notificaciones nativas
2. **`FcmTokenSyncWorker`** — Sincroniza el token FCM con el backend al iniciar sesión
3. **Deep linking** — Al tocar una notificación de stream en vivo, se abre directamente el detalle del stream

---

## ⚙️ Configuración

### Prerrequisitos
- Android Studio Ladybug o superior
- JDK 21
- Android SDK 36 (compileSdk) / minSdk 26

### Pasos

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/Carlvalencia1/Streamhub.git
   cd Streamhub
   ```

2. **Configurar Firebase**
   - Crear un proyecto en [Firebase Console](https://console.firebase.google.com/)
   - Descargar `google-services.json` y colocarlo en `app/`
   - Habilitar Cloud Messaging

3. **Configurar `local.properties`**
   ```properties
   sdk.dir=C\:\\Users\\<tu_usuario>\\AppData\\Local\\Android\\Sdk
   ```

4. **Configurar URLs del backend** en `app/build.gradle.kts` (flavors `dev` y `prod`)

5. **Compilar y ejecutar**
   ```bash
   ./gradlew assembleDevDebug
   ```
   O directamente desde Android Studio seleccionando el build variant **devDebug**.

---

## 🔐 Permisos

| Permiso | Uso |
|---|---|
| `INTERNET` | Comunicación con el backend y streaming |
| `CAMERA` | Transmisión de video en vivo |
| `RECORD_AUDIO` | Captura de audio para el stream |
| `POST_NOTIFICATIONS` | Notificaciones push (Android 13+) |
| `FOREGROUND_SERVICE` | Servicio de broadcasting en segundo plano |
| `VIBRATE` / `WAKE_LOCK` | Notificaciones de alta prioridad |

---

## 📄 Licencia

Este proyecto es de uso académico / personal.
