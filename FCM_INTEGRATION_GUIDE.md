# 🔐 Integración de Firebase Cloud Messaging (FCM)

## ✅ Cambios Realizados

### 1. Dependencias Agregadas

#### En `gradle/libs.versions.toml`:
- Firebase Messaging versión `33.0.0`
- Google Services plugin versión `4.4.2`

#### En `app/build.gradle.kts`:
- Plugin: `alias(libs.plugins.google.services)`
- Dependencia: `implementation(libs.firebase.messaging)`

### 2. Archivo de Servicio Creado
- **Archivo**: `app/src/main/java/com/valencia/streamhub/core/services/StreamMessagingService.kt`
- **Propósito**: Recibir y procesar mensajes FCM
- **Características**:
  - Manejo de mensajes con datos personalizados
  - Manejo de notificaciones
  - Gestión de tokens FCM
  - Integración con Hilt
  - Soporte para diferentes tipos de mensajes (broadcast_start, broadcast_stop, broadcast_update)

### 3. Configuración en AndroidManifest.xml
- Registrado el servicio `StreamMessagingService`
- Agregado el intent-filter para `com.google.firebase.MESSAGING_EVENT`

---

## 📋 Próximos Pasos - IMPORTANTE

### Paso 1: Descargar `google-services.json`

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona o crea tu proyecto
3. Descarga el archivo `google-services.json`
4. Coloca el archivo en: `app/google-services.json`

**⚠️ Nota**: El archivo debe estar en la carpeta `app/` del proyecto, no en la raíz.

### Paso 2: Sincronizar Gradle

1. En Android Studio, ve a: `File > Sync Now`
2. O ejecuta:
   ```bash
   gradlew build
   ```

### Paso 3: Verificar la Compilación

```bash
gradlew clean build
```

### Paso 4: Obtener el Token FCM (Opcional - Para Testing)

Puedes obtener el token FCM en tiempo de ejecución:

```kotlin
FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
    if (!task.isSuccessful) {
        Log.w("FCM", "Fetching FCM registration token failed", task.exception)
        return@OnCompleteListener
    }

    val token = task.result
    Log.d("FCM", "Token FCM: $token")
})
```

---

## 🔧 Personalización de Comportamientos

### En `StreamMessagingService.kt`:

#### Manejar diferentes tipos de mensajes:
```kotlin
private fun handleDataMessage(data: Map<String, String>) {
    when (data["type"]) {
        "broadcast_start" -> { /* Tu lógica */ }
        "broadcast_stop" -> { /* Tu lógica */ }
        "broadcast_update" -> { /* Tu lógica */ }
        else -> { /* Valor por defecto */ }
    }
}
```

#### Enviar el token al servidor:
```kotlin
private fun sendTokenToServer(token: String) {
    // Implementa aquí el envío del token a tu backend
    // Ejemplo:
    // val request = SendTokenRequest(token)
    // apiService.sendToken(request)
}
```

---

## 📤 Enviar Mensajes desde Firebase Console

1. En [Firebase Console](https://console.firebase.google.com/)
2. Ve a **Cloud Messaging** → **Enviar tu primer mensaje**
3. Completa los detalles:
   - **Título**: Título del mensaje
   - **Cuerpo**: Contenido del mensaje
   - **Datos personalizados** (opcional):
     - `type`: `broadcast_start` | `broadcast_stop` | `broadcast_update`
     - `title`: Título adicional
     - `message`: Mensaje adicional

---

## 📱 Estructura de Mensajes Soportados

### Tipo: `broadcast_start`
```json
{
  "type": "broadcast_start"
}
```

### Tipo: `broadcast_stop`
```json
{
  "type": "broadcast_stop"
}
```

### Tipo: `broadcast_update`
```json
{
  "type": "broadcast_update",
  "title": "Título actualizado",
  "message": "Nuevo mensaje"
}
```

---

## 🐛 Troubleshooting

### ❌ Error: "google-services.json" no encontrado
- **Solución**: Asegúrate de descargar y colocar el archivo en `app/google-services.json`

### ❌ Error: Plugin "com.google.gms.google-services" no encontrado
- **Solución**: Sincroniza Gradle (`File > Sync Now`)

### ❌ El token no se genera
- **Solución**: Asegúrate de tener Google Play Services instalado en el dispositivo/emulador

### ❌ Las notificaciones no llegan
- **Solución**: 
  - Verifica que el permiso `POST_NOTIFICATIONS` esté otorgado
  - Revisa los logs de Android Studio
  - Asegúrate de que la app no esté excluida de optimizaciones de batería

---

## 📝 Nota Importante

Este servicio (`StreamMessagingService`) es **complementario** a `StreamBroadcastForegroundService`. Ambos pueden coexistir:

- **`StreamBroadcastForegroundService`**: Maneja las transmisiones locales y el foreground service
- **`StreamMessagingService`**: Recibe notificaciones y comandos remotos desde Firebase

---

## ✨ Cambios Realizados en Resumen

| Archivo | Cambio |
|---------|--------|
| `gradle/libs.versions.toml` | ✅ Agregadas versiones de Firebase |
| `build.gradle.kts` | ✅ Agregado plugin Google Services |
| `app/build.gradle.kts` | ✅ Agregado plugin y dependencia de FCM |
| `StreamMessagingService.kt` | ✅ Archivo nuevo - Servicio FCM |
| `AndroidManifest.xml` | ✅ Registrado el servicio FCM |

---

**¡FCM está listo para ser configurado!** 🎉

