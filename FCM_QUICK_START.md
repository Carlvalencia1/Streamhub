# 🎯 INICIO RÁPIDO - FCM en Streamhub

---

## ⚡ En 3 Pasos

### Paso 1: Descargar google-services.json
```
1. Ve a: https://console.firebase.google.com/
2. Descarga: google-services.json
3. Coloca en: app/google-services.json
```

### Paso 2: Sincronizar
```
Android Studio: File > Sync Now
O Terminal: ./gradlew sync
```

### Paso 3: Compilar
```
Build > Make Project
O Terminal: ./gradlew build
```

---

## ✅ Ya Hecho (No necesitas hacer nada)

```
✅ Dependencias agregadas
✅ Plugin Google Services
✅ Servicio StreamMessagingService creado
✅ AndroidManifest actualizado
✅ Documentación completa
```

---

## 📱 Prueba Rápida

1. Instala la app en tu dispositivo
2. Ve a Firebase Console
3. Cloud Messaging > Enviar mensaje
4. Recibirás una notificación en el teléfono

---

## 📚 Documentación

| Documento | Para Quién |
|-----------|-----------|
| **FCM_STEP_BY_STEP.md** | Principiantes |
| **FCM_SUMMARY.md** | Resumen rápido |
| **FCM_CODE_EXAMPLES.md** | Código avanzado |
| **FCM_INTEGRATION_GUIDE.md** | Referencia técnica |

---

## 🎁 Tipos de Mensajes

```json
// Iniciar transmisión
{
  "type": "broadcast_start",
  "title": "Mi Transmisión",
  "message": "Transmisión en vivo"
}

// Detener transmisión
{
  "type": "broadcast_stop"
}

// Actualizar transmisión
{
  "type": "broadcast_update",
  "title": "Actualizado",
  "message": "500 espectadores"
}
```

---

## 🚨 Si Algo Falla

| Problema | Solución |
|----------|----------|
| Compilación falla | Sincroniza Gradle y limpia |
| No llegan notificaciones | Verifica permisos POST_NOTIFICATIONS |
| Error de plugin | Coloca google-services.json en app/ |

---

## 💡 Lo Que Está Pasando

```
Tu Servidor → Firebase Cloud Messaging → Streamhub App → Notificación
                     ↓
        Procesa según tipo de mensaje
            - broadcast_start
            - broadcast_stop
            - broadcast_update
```

---

## 🔑 Lo Más Importante

📥 **DESCARGAR `google-services.json` Y COLOCARLO EN `app/`**

Sin este archivo, Firebase no funcionará.

---

**¡Eso es todo! Tu app ahora recibe mensajes desde la nube.** 🚀

