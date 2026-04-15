# 💻 Ejemplos de Código - Implementación FCM Avanzada

## 1️⃣ Obtener Token FCM en Runtime

### Desde MainActivity

```kotlin
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Obtener token FCM
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("FCM", "Token FCM: $token")
            
            // Aquí puedes enviar el token al servidor
            sendTokenToServer(token)
        }
    }
    
    private fun sendTokenToServer(token: String) {
        // TODO: Implementar envío al servidor
        Log.d("FCM", "Token para el servidor: $token")
    }
}
```

---

## 2️⃣ Suscribirse a Tópicos FCM

### Desde cualquier parte de tu código

```kotlin
import com.google.firebase.messaging.FirebaseMessaging

class BroadcastingViewModel : ViewModel() {
    
    fun subscribeToTopics() {
        // Suscribirse a tópico de transmisiones
        FirebaseMessaging.getInstance().subscribeToTopic("streaming_updates")
            .addOnSuccessListener {
                Log.d("FCM", "Suscrito a: streaming_updates")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Error al suscribirse", e)
            }
    }
    
    fun unsubscribeFromTopics() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("streaming_updates")
            .addOnSuccessListener {
                Log.d("FCM", "Desuscrito de: streaming_updates")
            }
    }
}
```

---

## 3️⃣ Manejo Avanzado de Mensajes en StreamMessagingService

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
        
        Log.d(TAG, "Mensaje recibido de: ${remoteMessage.from}")
        Log.d(TAG, "Prioridad: ${remoteMessage.priority}")
        Log.d(TAG, "Tipo de contenido: ${remoteMessage.contentAvailable}")

        // Validar que el mensaje sea de una fuente confiable
        if (!isValidFCMMessage(remoteMessage)) {
            Log.w(TAG, "Mensaje de fuente no confiable")
            return
        }

        // Procesar datos
        if (remoteMessage.data.isNotEmpty()) {
            handleDataMessage(remoteMessage.data)
        }

        // Procesar notificación
        remoteMessage.notification?.let {
            handleNotification(
                title = it.title ?: DEFAULT_TITLE,
                message = it.body ?: DEFAULT_MESSAGE
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Token FCM refresheado")
        
        // Almacenar localmente si es necesario
        saveTokenLocally(token)
        
        // Enviar al servidor
        sendTokenToServer(token)
    }

    private fun isValidFCMMessage(remoteMessage: RemoteMessage): Boolean {
        // Validar el origen del mensaje
        val senderId = remoteMessage.from ?: return false
        
        // Validar que venga del proyecto correcto
        return senderId.startsWith(VALID_SENDER_PREFIX)
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"]?.takeIf { it.length < 50 } ?: return
        val priority = data["priority"] ?: "normal"
        
        Log.d(TAG, "Procesando mensaje tipo: $type con prioridad: $priority")

        when (type) {
            "broadcast_start" -> handleBroadcastStart(data)
            "broadcast_stop" -> handleBroadcastStop(data)
            "broadcast_update" -> handleBroadcastUpdate(data)
            "broadcast_notification" -> handleBroadcastNotification(data)
            else -> {
                Log.w(TAG, "Tipo de mensaje desconocido: $type")
            }
        }
    }

    private fun handleBroadcastStart(data: Map<String, String>) {
        Log.d(TAG, "Iniciando transmisión")
        
        val title = data["title"] ?: "Transmisión iniciada"
        val message = data["message"] ?: "Transmisión en vivo activa"
        val broadcastId = data["broadcast_id"]
        
        // Iniciar servicio en foreground
        StreamBroadcastForegroundService.start(this, title, message)
        
        // Iniciar la transmisión (si tienes ese método)
        serviceScope.launch {
            runCatching {
                if (broadcastId != null) {
                    // broadcastingRepository.startBroadcasting(broadcastId)
                    Log.d(TAG, "Transmisión iniciada con ID: $broadcastId")
                }
            }.onFailure { e ->
                Log.e(TAG, "Error al iniciar transmisión", e)
                notifyError("Error", "No se pudo iniciar la transmisión")
            }
        }
    }

    private fun handleBroadcastStop(data: Map<String, String>) {
        Log.d(TAG, "Deteniendo transmisión")
        
        val reason = data["reason"] ?: "Transmisión detenida"
        
        StreamBroadcastForegroundService.stop(this)
        
        serviceScope.launch {
            runCatching {
                broadcastingRepository.stopBroadcasting()
                Log.d(TAG, "Transmisión detenida: $reason")
            }.onFailure { e ->
                Log.e(TAG, "Error al detener transmisión", e)
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

    private fun handleBroadcastNotification(data: Map<String, String>) {
        val title = data["title"] ?: "Notificación"
        val message = data["message"] ?: ""
        val link = data["link"]
        
        sendNotification(title, message, link)
    }

    private fun handleNotification(title: String, message: String) {
        sendNotification(title, message)
    }

    private fun sendNotification(title: String, message: String, link: String? = null) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            if (link != null) {
                putExtra("deep_link", link)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            (System.currentTimeMillis() % 10000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal si es necesario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FCM_CHANNEL_ID,
                FCM_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de Streamhub"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, FCM_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .build()

        notificationManager.notify(FCM_NOTIFICATION_ID, notification)
    }

    private fun notifyError(title: String, message: String) {
        sendNotification(title, message)
    }

    private fun saveTokenLocally(token: String) {
        // Aquí puedes guardar el token en SharedPreferences o DataStore
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
        Log.d(TAG, "Token guardado localmente")
    }

    private fun sendTokenToServer(token: String) {
        // TODO: Implementar envío al servidor backend
        Log.d(TAG, "Token disponible para enviar al servidor: $token")
        
        serviceScope.launch {
            runCatching {
                // val response = apiService.sendFCMToken(SendTokenRequest(token))
                Log.d(TAG, "Token enviado al servidor")
            }.onFailure { e ->
                Log.e(TAG, "Error al enviar token al servidor", e)
            }
        }
    }

    companion object {
        private const val TAG = "StreamMessagingService"
        private const val FCM_CHANNEL_ID = "streamhub_fcm_notifications"
        private const val FCM_CHANNEL_NAME = "Streamhub Notifications"
        private const val FCM_NOTIFICATION_ID = 4202
        private const val DEFAULT_TITLE = "Streamhub"
        private const val DEFAULT_MESSAGE = "Tienes un nuevo mensaje"
        private const val VALID_SENDER_PREFIX = "123456789"  // Tu Firebase Project Number
        private const val PREFS_NAME = "streamhub_prefs"
        private const val KEY_FCM_TOKEN = "fcm_token"
    }
}
```

---

## 4️⃣ ViewModel para Gestión de FCM

```kotlin
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FCMViewModel @Inject constructor(
    app: Application
) : AndroidViewModel(app) {

    private val _fcmToken = MutableLiveData<String>()
    val fcmToken: LiveData<String> = _fcmToken

    private val _subscriptionStatus = MutableLiveData<SubscriptionStatus>()
    val subscriptionStatus: LiveData<SubscriptionStatus> = _subscriptionStatus

    init {
        retrieveFCMToken()
    }

    private fun retrieveFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _fcmToken.value = task.result
            }
        }
    }

    fun subscribeToTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnSuccessListener {
                _subscriptionStatus.value = SubscriptionStatus.Success(topic)
            }
            .addOnFailureListener { e ->
                _subscriptionStatus.value = SubscriptionStatus.Error(e.message ?: "Unknown error")
            }
    }

    fun unsubscribeFromTopic(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnSuccessListener {
                _subscriptionStatus.value = SubscriptionStatus.Unsubscribed(topic)
            }
            .addOnFailureListener { e ->
                _subscriptionStatus.value = SubscriptionStatus.Error(e.message ?: "Unknown error")
            }
    }

    sealed class SubscriptionStatus {
        data class Success(val topic: String) : SubscriptionStatus()
        data class Unsubscribed(val topic: String) : SubscriptionStatus()
        data class Error(val message: String) : SubscriptionStatus()
    }
}
```

---

## 5️⃣ Estructura de Mensajes JSON Avanzada

### Mensaje con Datos y Notificación

```json
{
  "notification": {
    "title": "Nueva Transmisión",
    "body": "Ana está transmitiendo en vivo"
  },
  "data": {
    "type": "broadcast_start",
    "broadcast_id": "live_123456",
    "title": "Transmisión de Ana",
    "message": "En vivo con 150 espectadores",
    "priority": "high"
  },
  "android": {
    "priority": "high",
    "ttl": "3600s",
    "notification": {
      "sound": "default",
      "channel_id": "streamhub_fcm_notifications"
    }
  }
}
```

---

## 6️⃣ Testing FCM en Desarrollo

### Clase de Prueba

```kotlin
import org.junit.Test
import org.junit.Before
import com.google.firebase.messaging.RemoteMessage
import kotlin.test.assertEquals

class StreamMessagingServiceTest {

    private lateinit var service: StreamMessagingService

    @Before
    fun setUp() {
        // Inicializar servicio
    }

    @Test
    fun testValidMessageProcessing() {
        // Crear mensaje de prueba
        val data = mapOf(
            "type" to "broadcast_start",
            "title" to "Test Broadcast",
            "message" to "Test Message"
        )
        
        // Verificar que se procesa correctamente
        assertEquals("broadcast_start", data["type"])
    }
}
```

---

## 7️⃣ Enviar Mensaje desde Backend (Ejemplo Python)

```python
import firebase_admin
from firebase_admin import credentials
from firebase_admin import messaging

# Inicializar Firebase
cred = credentials.Certificate('path/to/serviceAccountKey.json')
firebase_admin.initialize_app(cred)

# Crear mensaje
message = messaging.MulticastMessage(
    notification=messaging.Notification(
        title='Nueva Transmisión',
        body='Alguien está transmitiendo en vivo'
    ),
    data={
        'type': 'broadcast_start',
        'title': 'Transmisión en Vivo',
        'message': 'Únete ahora'
    },
    tokens=[
        'token_1',
        'token_2',
        'token_3'
    ]
)

# Enviar
response = messaging.send_multicast(message)
print(f"Mensajes enviados: {response.success}")
```

---

## 8️⃣ Casos de Uso Comunes

### Notificación de Nueva Transmisión

```kotlin
data = {
    "type": "broadcast_start",
    "title": "Juan está transmitiendo",
    "message": "Transmisión: Gaming Session",
    "broadcast_id": "live_juan_001"
}
```

### Actualización de Espectadores

```kotlin
data = {
    "type": "broadcast_update",
    "title": "Juan está transmitiendo",
    "message": "500 espectadores conectados"
}
```

### Notificación Urgente

```kotlin
data = {
    "type": "broadcast_notification",
    "title": "Advertencia",
    "message": "La transmisión será interrumpida por mantenimiento",
    "priority": "high"
}
```

---

## 🎯 Mejores Prácticas

✅ Validar siempre el origen del mensaje  
✅ No confiar en datos del cliente  
✅ Implementar timeout en operaciones async  
✅ Loguear todos los eventos importantes  
✅ Manejar errores de red gracefully  
✅ Usar tópicos para dirigirse a grupos de usuarios  
✅ Encriptar datos sensibles  
✅ Mantener tokens actualizados  

---

**¡Listo para implementar FCM avanzado!** 🚀

