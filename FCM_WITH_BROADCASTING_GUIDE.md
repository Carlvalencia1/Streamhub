# 🔗 Integración de FCM con StreamBroadcastForegroundService

## Descripción

Este documento muestra cómo integrar Firebase Cloud Messaging (FCM) con tu servicio de broadcasting para:
- Iniciar transmisiones desde comandos remotos
- Detener transmisiones desde comandos remotos
- Actualizar información de la transmisión en tiempo real

---

## 1️⃣ Extender StreamMessagingService

Modifica `StreamMessagingService.kt` para inyectar el repositorio de broadcasting:

```kotlin
package com.valencia.streamhub.core.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.valencia.streamhub.MainActivity
import com.valencia.streamhub.R
import com.valencia.streamhub.core.services.StreamBroadcastForegroundService
import com.valencia.streamhub.features.broadcasting.domain.repositories.BroadcastingRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StreamMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var broadcastingRepository: BroadcastingRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Mensaje FCM recibido de: ${remoteMessage.from}")

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Datos del mensaje: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        remoteMessage.notification?.let {
            Log.d(TAG, "Notificación: Título=${it.title}, Cuerpo=${it.body}")
            handleNotification(
                title = it.title ?: DEFAULT_TITLE,
                message = it.body ?: DEFAULT_MESSAGE
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Token FCM refresheado: $token")
        sendTokenToServer(token)
    }

    private fun handleDataMessage(data: Map<String, String>) {
        when (data["type"]) {
            "broadcast_start" -> {
                handleBroadcastStart(data)
            }
            "broadcast_stop" -> {
                handleBroadcastStop(data)
            }
            "broadcast_update" -> {
                handleBroadcastUpdate(data)
            }
            else -> {
                Log.d(TAG, "Tipo de mensaje desconocido")
            }
        }
    }

    private fun handleBroadcastStart(data: Map<String, String>) {
        Log.d(TAG, "Iniciando transmisión desde FCM")
        val title = data["title"] ?: "Transmisión iniciada"
        val message = data["message"] ?: "Transmisión en vivo activa"
        
        // Iniciar el foreground service
        StreamBroadcastForegroundService.start(this, title, message)
        
        // Iniciar la transmisión en el repositorio (si es necesario)
        serviceScope.launch {
            runCatching {
                // broadcastingRepository.startBroadcasting()
                Log.d(TAG, "Transmisión iniciada desde FCM")
            }.onFailure {
                Log.e(TAG, "Error al iniciar transmisión", it)
            }
        }
    }

    private fun handleBroadcastStop(data: Map<String, String>) {
        Log.d(TAG, "Deteniendo transmisión desde FCM")
        
        // Detener el foreground service
        StreamBroadcastForegroundService.stop(this)
        
        serviceScope.launch {
            runCatching {
                broadcastingRepository.stopBroadcasting()
                Log.d(TAG, "Transmisión detenida desde FCM")
            }.onFailure {
                Log.e(TAG, "Error al detener transmisión", it)
            }
        }
    }

    private fun handleBroadcastUpdate(data: Map<String, String>) {
        val title = data["title"] ?: DEFAULT_TITLE
        val message = data["message"] ?: DEFAULT_MESSAGE
        Log.d(TAG, "Actualizando transmisión: $title - $message")
        
        // Actualizar la notificación del foreground service
        StreamBroadcastForegroundService.update(this, title, message)
    }

    private fun handleNotification(title: String, message: String) {
        sendNotification(title, message)
    }

    private fun sendNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FCM_CHANNEL_ID,
                FCM_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, FCM_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(FCM_NOTIFICATION_ID, notification)
    }

    private fun sendTokenToServer(token: String) {
        // TODO: Implementar el envío del token al servidor backend
        Log.d(TAG, "Token disponible para enviar al servidor: $token")
    }

    companion object {
        private const val TAG = "StreamMessagingService"
        private const val FCM_CHANNEL_ID = "streamhub_fcm_notifications"
        private const val FCM_CHANNEL_NAME = "Streamhub Notifications"
        private const val FCM_NOTIFICATION_ID = 4202
        private const val DEFAULT_TITLE = "Streamhub"
        private const val DEFAULT_MESSAGE = "Tienes un nuevo mensaje"
    }
}
```

---

## 2️⃣ Estructura de Mensajes para Broadcasting

### Iniciar Transmisión
```json
{
  "data": {
    "type": "broadcast_start",
    "title": "Mi Transmisión",
    "message": "Transmisión en vivo activa"
  }
}
```

### Detener Transmisión
```json
{
  "data": {
    "type": "broadcast_stop"
  }
}
```

### Actualizar Transmisión
```json
{
  "data": {
    "type": "broadcast_update",
    "title": "Mi Transmisión - Actualizada",
    "message": "1000 espectadores"
  }
}
```

---

## 3️⃣ Enviar Mensajes desde Firebase Console

1. Ve a **Cloud Messaging**
2. Haz clic en **Crear campaña**
3. Selecciona **FCM**
4. En la pestaña de **Notificación personalizada**, agrega los datos:

```
Título: Iniciar Transmisión
Cuerpo: Tu transmisión ha comenzado

Parámetros personalizados:
- Key: type | Value: broadcast_start
- Key: title | Value: Mi Transmisión
- Key: message | Value: Transmisión en vivo activa
```

---

## 4️⃣ Flujo de Ejecución

```
FCM Server
    ↓
  Envía mensaje
    ↓
StreamMessagingService.onMessageReceived()
    ↓
handleDataMessage()
    ↓
  Según tipo:
    - broadcast_start → StreamBroadcastForegroundService.start()
    - broadcast_stop  → StreamBroadcastForegroundService.stop()
    - broadcast_update → StreamBroadcastForegroundService.update()
    ↓
  Servicio iniciado/actualizado en foreground
    ↓
  Usuario ve notificación
```

---

## 5️⃣ Consideraciones de Seguridad

### ⚠️ Validar Origen del Mensaje

```kotlin
private fun isValidFCMMessage(remoteMessage: RemoteMessage): Boolean {
    // Verificar el origen del mensaje
    val senderId = remoteMessage.from
    val validSenderId = "123456789" // Tu Firebase Project Number
    
    return senderId == validSenderId
}
```

### 🔒 Proteger Datos Sensibles

```kotlin
private fun handleDataMessage(data: Map<String, String>) {
    // Validar que los datos no sean maliciosos
    val type = data["type"]?.takeIf { it.length < 50 } ?: return
    
    when (type) {
        // ... resto del código
    }
}
```

---

## 6️⃣ Debugging

### Verificar Logs de FCM

```bash
# En Android Studio
adb logcat | grep "StreamMessagingService"
```

### Logs importantes:
```kotlin
Log.d(TAG, "Token FCM: $token")  // Nuevo token
Log.d(TAG, "Mensaje FCM recibido")  // Mensaje recibido
Log.d(TAG, "Datos del mensaje: ${remoteMessage.data}")  // Datos
Log.e(TAG, "Error al iniciar transmisión", throwable)  // Errores
```

---

## 7️⃣ Testing en Emulador

Para probar en emulador:

1. Asegúrate de tener Google Play Services instalado
2. Abre una terminal y ejecuta:
   ```bash
   adb shell
   pm grant com.valencia.streamhub android.permission.POST_NOTIFICATIONS
   ```

3. Envía un mensaje de prueba desde Firebase Console
4. Verifica los logs en Logcat

---

## ✅ Checklist de Implementación

- [ ] `google-services.json` descargado y colocado en `app/`
- [ ] Gradle sincronizado
- [ ] `StreamMessagingService` creado
- [ ] Servicio registrado en `AndroidManifest.xml`
- [ ] Permiso `POST_NOTIFICATIONS` agregado
- [ ] Probado envío de mensajes desde Firebase Console
- [ ] Verificado que el token se genera correctamente
- [ ] Integración con `BroadcastingRepository` completada
- [ ] Validación de seguridad implementada

---

**¡Listo para usar FCM con Streamhub!** 🚀

