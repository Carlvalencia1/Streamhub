# 📖 Guía de Uso - Feature de Usuarios

## 🚀 Cómo Funciona

### 1. **Flujo de Login**

```kotlin
// En LoginScreen.kt
fun login(email: String, password: String) {
    viewModel.login("user@mail.com", "password123")
    // El ViewModel ejecuta:
    // 1. loginUseCase(email, password)
    // 2. authRepository.login(email, password)
    // 3. authApiService.login(LoginRequest(...))
    // 4. Retorna AuthResult.Success(token) o AuthResult.Error(message)
}
```

### 2. **Flujo de Registro**

```kotlin
// En RegisterScreen.kt
fun register(username: String, email: String, password: String) {
    viewModel.register("username", "user@mail.com", "password123")
    // El ViewModel ejecuta:
    // 1. registerUseCase(username, email, password)
    // 2. authRepository.register(username, email, password)
    // 3. authApiService.register(RegisterRequest(...))
    // 4. Retorna AuthResult.Success(userId) o AuthResult.Error(message)
}
```

### 3. **Manejo del Estado**

```kotlin
// Observar cambios en el estado
val authState by viewModel.authState.collectAsStateWithLifecycle()

when {
    authState.isLoading -> {
        // Mostrar CircularProgressIndicator
        CircularProgressIndicator()
    }
    authState.error != null -> {
        // Mostrar mensaje de error
        snackbarHostState.showSnackbar(authState.error!!)
    }
    authState.isAuthenticated -> {
        // Token guardado en authState.token
        // Navegar a la pantalla principal
    }
}
```

---

## 🎨 Componentes Reutilizables

### EmailTextField
```kotlin
EmailTextField(
    value = email,
    onValueChange = { email = it },
    modifier = Modifier.fillMaxWidth()
)
// Validar email con regex (opcional):
// val isValidEmail = email.contains("@") && email.contains(".")
```

### PasswordTextField
```kotlin
PasswordTextField(
    value = password,
    onValueChange = { password = it },
    label = "Contraseña",
    modifier = Modifier.fillMaxWidth()
)
// El texto se oculta automáticamente con PasswordVisualTransformation
```

### UsernameTextField
```kotlin
UsernameTextField(
    value = username,
    onValueChange = { username = it },
    modifier = Modifier.fillMaxWidth()
)
```

---

## 🔌 Llamadas a API

### Login
```
POST http://localhost:8080/api/users/login
Content-Type: application/json

{
  "email": "user@mail.com",
  "password": "123456"
}

Response: { "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." }
```

### Register
```
POST http://localhost:8080/api/users/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@mail.com",
  "password": "securepass123"
}

Response: {
  "id": "user-1",
  "username": "john_doe",
  "email": "john@mail.com",
  "created_at": "2026-02-26T00:00:00Z",
  "updated_at": "2026-02-26T00:00:00Z"
}
```

---

## 🔐 Próximas Integraciones

### 1. **Guardar Token Persistentemente**

```kotlin
// En AuthRepositoryImpl.kt
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenStorage: TokenStorage  // DataStore o SharedPreferences
) : AuthRepository {
    override suspend fun login(email: String, password: String): AuthResult {
        try {
            val response = authApiService.login(LoginRequest(email, password))
            // Guardar token
            tokenStorage.saveToken(response.token)
            return AuthResult.Success(response.token)
        } catch (e: Exception) {
            return AuthResult.Error(e.message ?: "Error desconocido")
        }
    }
}
```

### 2. **Agregar Token a Peticiones Protegidas**

```kotlin
// En AuthInterceptor.kt
class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()
        val originalRequest = chain.request()

        return if (token != null && isProtectedEndpoint(originalRequest.url)) {
            val authenticatedRequest = originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            chain.proceed(authenticatedRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }

    private fun isProtectedEndpoint(url: okhttp3.HttpUrl): Boolean {
        return url.encodedPath.contains("/protected/") || 
               url.encodedPath.contains("/api/streams")
    }
}
```

### 3. **Usar Token en OkHttp**

```kotlin
// En NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @StreamhubRetrofit
    fun provideStreamhubRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://localhost:8080/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
```

---

## 🧪 Testing

### LoginScreen Test
```kotlin
@RunWith(AndroidUnit4::class)
class LoginScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLoginButtonEnabled_whenEmailAndPasswordNotEmpty() {
        composeTestRule.setContent {
            LoginScreen(navController = rememberNavController())
        }
        
        // Type email
        composeTestRule.onNodeWithText("Email").performTextInput("test@mail.com")
        // Type password
        composeTestRule.onNodeWithText("Contraseña").performTextInput("password123")
        // Check if button is enabled
        composeTestRule.onNodeWithText("Iniciar Sesión").assertIsEnabled()
    }
}
```

---

## 📋 Validaciones Implementadas

### En LoginScreen
- ✅ Email no vacío
- ✅ Contraseña no vacía
- ❌ Validación de email (opcional: agregar regex)

### En RegisterScreen
- ✅ Username no vacío
- ✅ Email no vacío
- ✅ Contraseña no vacía
- ✅ Confirmación de contraseña coincide
- ❌ Email único en backend (validar en servidor)
- ❌ Contraseña fuerte (longitud mínima, caracteres especiales)

---

## 🚨 Manejo de Errores

```kotlin
// Los errores se capturan automáticamente
when (val result = loginUseCase(email, password)) {
    is AuthResult.Success -> {
        // token: result.data
    }
    is AuthResult.Error -> {
        // message: result.message
        // Ej: "Invalid credentials" o "Network error"
    }
    AuthResult.Loading -> {
        // Mostrar loading UI
    }
}
```

---

## 🔄 Navegación

```kotlin
// Login exitoso → Home
navController.navigate(Screen.Home.route) {
    popUpTo(Screen.Login.route) { inclusive = true }
}

// Registrar nuevo usuario → Volver a Login
navController.popBackStack()

// Desde Home → Drawer abierto
navController.navigate(Screen.Posts.route) {
    popUpTo(Screen.Home.route)
}
```

---

## 📝 Notas Importantes

1. **Los tokens NO se guardan** en la implementación actual (solo en memoria)
   - Agregar DataStore o SharedPreferences para persistencia

2. **Sin refresh token** - Si el token expira, el usuario debe volver a login

3. **Sin logout** - Agregar botón de logout en el drawer

4. **Sin validación de email** - Agregar regex para validar formato

5. **Base URL hardcoded** - Considerar usar BuildConfig o constants

---

## 🎯 Resumen

La feature de usuarios está **completamente implementada** siguiendo Clean Architecture con:
- ✅ Login y Registro funcionales
- ✅ Validaciones en cliente
- ✅ Manejo de errores con Snackbars
- ✅ Estados de carga visuales
- ✅ Navegación condicional
- ✅ Inyección de dependencias con Hilt
- ✅ Integración con API en http://localhost:8080

Está lista para **testar y extender** con las integraciones de persistencia de token.

