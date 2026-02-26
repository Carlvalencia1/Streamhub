# Integración de Feature de Usuarios en Streamhub

## 📋 Resumen de la Implementación

Se ha implementado una completa feature de autenticación (Login y Registro) siguiendo **Clean Architecture** y usando:
- **Jetpack Compose** para la UI
- **Hilt** para inyección de dependencias
- **Retrofit** para llamadas HTTP
- **StateFlow** para gestión reactiva del estado
- **Navigation Compose** para navegación entre pantallas

---

## 🎯 Endpoints Base URL

**Base URL**: `http://localhost:8080`

### Endpoints implementados:
```
POST /api/users/login
  Request: { "email": "user@mail.com", "password": "123456" }
  Response: { "token": "JWT_AQUI" }

POST /api/users/register
  Request: { "username": "user", "email": "user@mail.com", "password": "123456" }
  Response: { "id": "user-1", "username": "user", "email": "user@mail.com", "created_at": "2026-02-26T00:00:00Z" }
```

---

## 📂 Estructura de Carpetas Creada

```
features/users/
├── data/
│   ├── datasources/remote/
│   │   ├── AuthApiService.kt       (Interfaz Retrofit)
│   │   └── AuthInterceptor.kt      (Interceptor para Bearer token)
│   ├── repositories/
│   │   └── AuthRepositoryImpl.kt    (Implementación)
│   └── di/
│       └── AuthModule.kt           (Módulo Hilt)
├── domain/
│   ├── entities/
│   │   └── AuthResult.kt           (Sealed class: Success/Error/Loading)
│   ├── repositories/
│   │   └── AuthRepository.kt       (Interfaz)
│   └── usecases/
│       └── AuthUseCases.kt         (LoginUseCase, RegisterUseCase)
└── presentation/
    ├── components/
    │   └── TextFields.kt           (EmailTextField, PasswordTextField, UsernameTextField)
    ├── screens/
    │   ├── LoginScreen.kt          (Pantalla de login)
    │   └── RegisterScreen.kt       (Pantalla de registro)
    └── viewmodels/
        └── AuthViewModel.kt        (Gestión de estado)
```

---

## 🔄 Flujo de Navegación

```
Login Screen (inicio)
    ↓
    ├─ Si login exitoso → Home Screen (acceso a drawer y topbar)
    │
    └─ Si registra → Register Screen → vuelve a Login
```

La pantalla de Login es el punto de entrada de la aplicación. El drawer y topbar solo aparecen después de autenticarse.

---

## 🎨 Características de las Pantallas

### LoginScreen
- Campo email con validación de formato
- Campo contraseña con visualización oculta
- Botón "Iniciar Sesión" con estado de carga
- Enlace a "¿No tienes cuenta? Regístrate"
- Mensajes de error con Snackbar
- Validación en cliente antes de enviar

### RegisterScreen
- Campo username
- Campo email
- Campo contraseña
- Campo confirmar contraseña
- Validaciones:
  - Todos los campos requeridos
  - Coincidencia de contraseñas
  - Formato de email válido
- Botón atrás para volver a login
- Mensajes de error informativos

---

## 🧪 Estado de Autenticación (AuthState)

```kotlin
data class AuthState(
    val isLoading: Boolean = false,           // Indica si está haciendo una petición
    val token: String? = null,                // JWT del servidor
    val error: String? = null,                // Mensaje de error
    val isAuthenticated: Boolean = false      // Si el usuario está autenticado
)
```

---

## 🔒 Actualización del Core

### NetworkModule
Se agregó un nuevo Retrofit para Streamhub API:
```kotlin
@StreamhubRetrofit
fun provideStreamhubRetrofit(): Retrofit {
    return Retrofit.Builder()
        .baseUrl("http://localhost:8080/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
```

### Qualifiers
Se agregó un nuevo qualifier:
```kotlin
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class StreamhubRetrofit
```

### Navigation Routes (Screen.kt)
Se agregaron dos nuevas rutas:
```kotlin
object Login : Screen("login")      // Nueva - punto de entrada
object Register : Screen("register") // Nueva - registro de usuarios
```

---

## 🚀 Próximas Implementaciones Recomendadas

1. **Persistencia de token**
   - Guardar JWT en SharedPreferences o DataStore
   - Restaurar sesión al iniciar la app

2. **AuthInterceptor**
   - Agregar el AuthInterceptor al cliente HTTP para incluir automáticamente el token en peticiones protegidas

3. **Refresh Token**
   - Implementar mechanism para renovar el token cuando expire

4. **Logout**
   - Agregar funcionalidad de cerrar sesión
   - Limpiar token del estado

5. **Validaciones mejoradas**
   - Validación en tiempo real mientras escribes
   - Feedback visual más completo

6. **Manejo de errores específicos**
   - Diferenciar entre errores de red y errores de autenticación
   - Mostrar mensajes más descriptivos

---

## 📝 Notas Técnicas

- Los ViewModels usan `viewModelScope` para cancelar coroutinas al destruir la pantalla
- Se usa `collectAsStateWithLifecycle()` para recopilar estados de forma segura
- Los Snackbars se manejan con `SnackbarHostState` en composables
- La navegación se maneja con `NavController` para transiciones suaves
- Se usa `hiltViewModel()` para obtener ViewModels con inyección automática

---

## ✅ Estados Validados

Todos los archivos han sido validados y no contienen errores de compilación. Los warnings relativos a "never used" son normales en módulos Hilt que se inyectan automáticamente.

