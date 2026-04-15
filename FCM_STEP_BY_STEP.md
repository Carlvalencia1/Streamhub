# 🎓 Guía Paso a Paso - Configuración Final de FCM

## 📌 Lo que ya hemos hecho

✅ Agregamos las dependencias de Firebase  
✅ Creamos el servicio `StreamMessagingService.kt`  
✅ Registramos el servicio en `AndroidManifest.xml`  
✅ Agregamos el plugin de Google Services  

**Ahora solo falta el último paso crucial** ⬇️

---

## 🚀 Paso 1: Descargar `google-services.json`

### 1.1 Abre Firebase Console

1. Ve a: https://console.firebase.google.com/
2. Inicia sesión con tu cuenta Google

### 1.2 Selecciona o Crea un Proyecto

![Firebase Console]
- Si tienes un proyecto existente, selecciónalo
- Si no, haz clic en "Crear proyecto"

### 1.3 Descarga el Archivo de Configuración

1. En el panel izquierdo, ve a **Configuración del proyecto** (icono de engranaje)
2. En la pestaña **General**, desplázate hacia abajo
3. Busca la sección **"Tus aplicaciones"**
4. Si aún no existe, haz clic en **iOS** para agregar tu app
5. Busca Android en la lista
6. Haz clic en el icono de descargar JSON

### Alternativa rápida:
1. Desde la página de inicio de Firebase
2. Haz clic en tu proyecto
3. Ve a **Configuración** → **Tus aplicaciones**
4. Selecciona tu app Android
5. Descarga `google-services.json`

---

## 📁 Paso 2: Colocar `google-services.json`

### 2.1 Ubica el Archivo Descargado

El archivo debe estar en tu carpeta de Descargas. Se verá así:
```
Descargas/
└── google-services.json
```

### 2.2 Copia el Archivo a tu Proyecto

**Opción A: Desde el Explorador de Archivos**

1. Abre el Explorador de Archivos de Windows
2. Navega a:
   ```
   C:\Users\daniv\AndroidStudioProjects\Streamhub\app\
   ```
3. Pega `google-services.json` aquí

**Opción B: Desde Android Studio**

1. En Android Studio, abre la vista **Project**
2. Haz clic derecho en la carpeta **app**
3. Selecciona **Reveal in File Explorer** (o similar)
4. Copia `google-services.json` a esa carpeta

### ✅ Estructura correcta:
```
Streamhub/
└── app/
    ├── build.gradle.kts
    ├── google-services.json      ← DEBE ESTAR AQUÍ
    ├── proguard-rules.pro
    └── src/
```

---

## 🔄 Paso 3: Sincronizar Gradle

Una vez que `google-services.json` esté en su lugar:

### 3.1 Sincronizar en Android Studio

1. En Android Studio, ve a: **File** → **Sync Now**

O espera a que Android Studio detect el cambio automáticamente.

### 3.2 Sincronizar desde Terminal

En la terminal de tu proyecto, ejecuta:
```bash
./gradlew sync
```

### ✅ Verifica que no haya errores:
- Busca la salida: `BUILD SUCCESSFUL`

---

## 🏗️ Paso 4: Compilar el Proyecto

### 4.1 Limpia y Compila

En la terminal:
```bash
./gradlew clean build
```

O en Android Studio:
- **Build** → **Clean Project**
- **Build** → **Make Project**

### ✅ Resultado esperado:
```
BUILD SUCCESSFUL in XXs
```

---

## 📱 Paso 5: Probar en Dispositivo/Emulador

### 5.1 Instala la App

```bash
./gradlew installDebug
```

O en Android Studio:
- **Run** → **Run 'app'** (o presiona Shift + F10)

### 5.2 Otorga Permisos

1. En el dispositivo, abre **Configuración**
2. Ve a **Aplicaciones** → **Streamhub**
3. **Permisos** → **Notificaciones** → **Permitir**

### 5.3 Verifica en Logcat

En Android Studio, abre **Logcat** (View → Tool Windows → Logcat)

Busca logs como:
```
D/StreamMessagingService: Token FCM refresheado: abc123xyz789...
```

---

## 📤 Paso 6: Envía un Mensaje de Prueba

### 6.1 Desde Firebase Console

1. Ve a https://console.firebase.google.com/
2. Selecciona tu proyecto
3. En el menú izquierdo: **Cloud Messaging** → **Crear campaña**
4. O simplemente: **Cloud Messaging** → **Enviar tu primer mensaje**

### 6.2 Completa los Detalles

```
Título: "Hola desde Firebase"
Cuerpo: "Este es un mensaje de prueba"
```

### 6.3 Selecciona el Destino

- **Aplicaciones**: Selecciona "Streamhub"
- **Usuarios**: Todos los usuarios

### 6.4 Envía

Haz clic en **Enviar**

### ✅ Resultado:
Deberías ver una notificación en tu dispositivo 📲

---

## 🎯 Paso 7: Envía un Mensaje con Comandos

### 7.1 Mensaje de Inicio de Transmisión

En Firebase Console, crea un nuevo mensaje:

**Notificación (mostrada al usuario):**
- Título: "Transmisión iniciada"
- Cuerpo: "Tu transmisión ha comenzado"

**Parámetros personalizados:**
```
type: broadcast_start
title: Mi Transmisión
message: Transmisión en vivo activa
```

### 7.2 Resultado Esperado:

1. ✅ Notificación aparece
2. ✅ Se ejecuta `StreamMessagingService.onMessageReceived()`
3. ✅ Se procesa el tipo "broadcast_start"
4. ✅ Se inicia `StreamBroadcastForegroundService`
5. ✅ Aparece el servicio en primer plano

---

## 🔧 Verificación de Configuración

### Checklist Final

- [ ] `google-services.json` descargado
- [ ] `google-services.json` colocado en `app/`
- [ ] Gradle sincronizado sin errores
- [ ] Proyecto compilado exitosamente
- [ ] App instalada en dispositivo/emulador
- [ ] Permiso `POST_NOTIFICATIONS` otorgado
- [ ] Token FCM aparece en Logcat
- [ ] Mensaje de prueba recibido
- [ ] Notificación aparece en dispositivo

---

## 🐛 Solución de Problemas

### ❌ Error: "google-services.json" not found

**Solución:**
1. Verifica que el archivo esté en `app/google-services.json`
2. Reinicia Android Studio
3. Sincroniza Gradle

### ❌ Error: Plugin "com.google.gms.google-services" not found

**Solución:**
1. Limpia el proyecto: `./gradlew clean`
2. Sincroniza: `./gradlew sync`
3. Reconstruye: `./gradlew build`

### ❌ No se generan tokens FCM

**Solución:**
1. Verifica que Google Play Services esté instalado en el emulador
2. Verifica la conexión a internet
3. Revisa los permisos en Logcat

### ❌ Las notificaciones no aparecen

**Solución:**
1. Verifica el permiso `POST_NOTIFICATIONS`
2. Verifica que la app no esté silenciada
3. Revisa que el canal de notificación esté configurado

### ❌ Error de compilación con Firebase

**Solución:**
1. Limpiar todo: `./gradlew clean`
2. Invalida cachés: **File** → **Invalidate Caches** → **OK**
3. Reinicia Android Studio

---

## 📚 Archivos Modificados - Resumen

| Archivo | Cambio |
|---------|--------|
| `gradle/libs.versions.toml` | Versiones de Firebase |
| `build.gradle.kts` | Plugin Google Services |
| `app/build.gradle.kts` | Plugin + Dependencia FCM |
| `StreamMessagingService.kt` | Servicio FCM (Nuevo) |
| `AndroidManifest.xml` | Registro del servicio |
| `google-services.json` | Configuración Firebase (Falta descargar) |

---

## 🎉 ¡Listo!

Después de seguir estos pasos:

✅ Firebase Cloud Messaging está completamente integrado  
✅ Puedes recibir notificaciones remotas  
✅ Puedes controlar transmisiones desde Firebase Console  
✅ La app es escalable y segura  

---

## 📞 Recursos Útiles

- 📖 [Firebase Messaging Docs](https://firebase.google.com/docs/cloud-messaging/android/client)
- 📖 [Notificaciones en Android](https://developer.android.com/develop/ui/views/notifications)
- 📖 [Google Play Services Setup](https://developers.google.com/android/guides/setup)

---

**¡Congratulations! 🎊 Tu aplicación ahora es capaz de recibir mensajes desde la nube!**

