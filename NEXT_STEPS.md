# 🎯 Primeros Pasos en Android Studio

## ✅ Todo está listo. Ahora solo necesitas:

---

## Paso 1️⃣: Sincronizar Gradle

### En Android Studio:

1. Si ves un **banner amarillo** en la parte superior que dice "Gradle changes detected" → haz clic en **"Sync Now"**

2. Si no lo ves:
   - Ve al menú: **File** → **Sync Now**
   - O presiona: **Ctrl + Shift + A** (Windows) y escribe "Sync"

### En Terminal:
```bash
cd C:\Users\daniv\AndroidStudioProjects\Streamhub
./gradlew sync
```

### ⏳ Espera hasta que veas: `Sync successful`

---

## Paso 2️⃣: Compilar el Proyecto

### En Android Studio:

1. Ve a: **Build** → **Make Project**
   - O presiona: **Ctrl + F9**

2. Espera a que termine la compilación

### En Terminal:
```bash
./gradlew build
```

### ✅ Deberías ver: `BUILD SUCCESSFUL`

---

## Paso 3️⃣: Instalar en Dispositivo/Emulador

### Opción A: Desde Android Studio

1. Asegúrate de tener un dispositivo/emulador conectado
2. Ve a: **Run** → **Run 'app'**
   - O presiona: **Shift + F10**

3. Selecciona tu dispositivo

### Opción B: Desde Terminal

```bash
./gradlew installDebug
```

---

## Paso 4️⃣: Probar FCM

### En Firebase Console:

1. Ve a: https://console.firebase.google.com/
2. Selecciona tu proyecto: **streamhub-64704**
3. En el menú izquierdo: **Cloud Messaging** (o **Messaging**)
4. Haz clic en: **Enviar tu primer mensaje**

### Completa el formulario:

```
📌 Título: "Hola desde Firebase"
📌 Cuerpo: "FCM está funcionando!"
📌 Destino: Selecciona tu app
📌 Usuarios: Todos (o tu dispositivo específico)
```

### Haz clic en: **Enviar**

---

## ✨ Resultado

Deberías ver una **notificación en tu dispositivo** 📱

---

## 🔍 Ver Logs de FCM

### En Android Studio:

1. Abre: **View** → **Tool Windows** → **Logcat**
   - O presiona: **Alt + 6**

2. Busca logs como:
   ```
   StreamMessagingService: Token FCM refresheado: xyz...
   StreamMessagingService: Mensaje FCM recibido
   ```

3. Si ves estos logs, ¡FCM está funcionando! ✅

---

## 📊 Estructura de Carpetas Verificar

```
Streamhub/
├── app/
│   ├── google-services.json              ← ✅ DEBE ESTAR AQUÍ
│   ├── build.gradle.kts                  ← ✅ VERIFICADO
│   └── src/main/
│       ├── java/com/valencia/streamhub/
│       │   └── core/services/
│       │       └── StreamMessagingService.kt  ← ✅ NUEVO
│       └── AndroidManifest.xml           ← ✅ ACTUALIZADO
├── gradle/
│   └── libs.versions.toml                ← ✅ ACTUALIZADO
└── build.gradle.kts                      ← ✅ ACTUALIZADO
```

---

## 🛠️ Si Hay Errores

### Error: "google-services.json no encontrado"

```
✅ Solución:
1. Verifica que app/google-services.json existe
2. Sincroniza: File > Sync Now
3. Limpia: Build > Clean Project
4. Reconstruye: Build > Make Project
```

### Error: "Plugin google-services no encontrado"

```
✅ Solución:
1. Sincroniza nuevamente
2. Invalida cachés: File > Invalidate Caches > Invalidate and Restart
3. Reinicia Android Studio
```

### Error de compilación

```
✅ Solución:
1. Limpia: Build > Clean Project
2. Reconstruye: Build > Make Project
3. Si persiste: Elimina la carpeta build/ y reinicia
```

---

## 📱 Pruebas Adicionales

### Probar con Datos Personalizados

En Firebase Console, usa los **Parámetros personalizados**:

```
Tipo: broadcast_start
Parámetro 1:
  Key: title
  Value: Mi Transmisión

Parámetro 2:
  Key: message
  Value: Transmisión activa
```

---

## ✅ Checklist Final

- [ ] Sincronizado Gradle sin errores
- [ ] Proyecto compilado exitosamente
- [ ] App instalada en dispositivo/emulador
- [ ] Token FCM visible en Logcat
- [ ] Mensaje de prueba enviado desde Firebase
- [ ] Notificación recibida en dispositivo
- [ ] Logs muestran "StreamMessagingService"

---

## 🎉 ¡Listo!

Cuando hayas seguido estos 4 pasos, FCM estará completamente funcionando en tu app.

**Ahora puedes:**
- Enviar notificaciones desde Firebase Console
- Controlar transmisiones remotamente
- Recibir actualizaciones en tiempo real

---

## 📞 Resumen Rápido

```
1. File > Sync Now
2. Build > Make Project
3. Run > Run 'app'
4. Firebase Console > Enviar mensaje
5. ¡Recibe la notificación! 📱
```

---

**¡A Sinronizar! 🚀**

