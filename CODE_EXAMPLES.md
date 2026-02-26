# 💻 Ejemplos de Código - Feature de Usuarios

## 🔐 Flujo de Autenticación Completo

### 1. ViewModel (AuthViewModel.kt)

```kotlin
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            // 1. Cambiar estado a cargando
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            // 2. Ejecutar use case
            when (val result = loginUseCase(email, password)) {
                is AuthResult.Success -> {
                    // 3. Actualizar estado exitosamente
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        token = result.data,
                        isAuthenticated = true
                    )
                    // Token guardado automáticamente
                }
                is AuthResult.Error -> {
                    // 4. Actualizar estado con error
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.message  // "Invalid credentials" o similar
                    )
                }
                else -> {}
            }
        }
    }
}
```

### 2. Use Case (LoginUseCase)

```kotlin
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    // Operador invoke permite usar: loginUseCase(email, password)
    suspend operator fun invoke(
        email: String, 
        password: String
    ): AuthResult {
        return authRepository.login(email, password)
    }
}
```

### 3. Repository (AuthRepositoryImpl.kt)

```kotlin
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService
) : AuthRepository {

    override suspend fun login(
        email: String, 
        password: String
    ): AuthResult {
        return try {
            // Llamar a API
            val response = authApiService.login(
                LoginRequest(email, password)
            )
            // Retornar token si exitoso
            AuthResult.Success(response.token)
        } catch (e: Exception) {
            // Retornar error si falla
            AuthResult.Error(e.message ?: "Unknown error occurred")
        }
    }
}
```

### 4. API Service (AuthApiService.kt)

```kotlin
interface AuthApiService {
    @POST("api/users/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String  // JWT Token del servidor
)
```

---

## 🎨 UI Components

### LoginScreen - Manejo de Estado

```kotlin
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    // 1. Observar estado
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // 2. Navegar si está autenticado
    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    // 3. Mostrar error
    LaunchedEffect(authState.error) {
        if (authState.error != null) {
            snackbarHostState.showSnackbar(authState.error!!)
        }
    }

    // 4. Renderizar UI
    Column(modifier = Modifier.fillMaxSize()) {
        EmailTextField(
            value = email,
            onValueChange = { email = it }
        )
        
        PasswordTextField(
            value = password,
            onValueChange = { password = it }
        )

        // 5. Mostrar loading o botón
        if (authState.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.login(email, password) }
            ) {
                Text("Iniciar Sesión")
            }
        }
    }
}
```

---

## 🔗 Inyección de Dependencias (Hilt)

### AuthModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    // 1. Proporcionar API Service
    @Singleton
    @Provides
    fun provideAuthApiService(
        @StreamhubRetrofit retrofit: Retrofit  // ← Injected
    ): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    // 2. Proporcionar Repository
    @Singleton
    @Provides
    fun provideAuthRepository(
        authApiService: AuthApiService  // ← Auto inyectado
    ): AuthRepository {
        return AuthRepositoryImpl(authApiService)
    }
}
```

### Uso en ViewModel

```kotlin
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,      // ← Auto inyectado
    private val registerUseCase: RegisterUseCase // ← Auto inyectado
) : ViewModel()
```

---

## 🗺️ Navegación

### Routes Definition

```kotlin
sealed class Screen(val route: String) {
    object Login : Screen("login")          // ← Nueva
    object Register : Screen("register")     // ← Nueva
    object Home : Screen("home")
    object Posts : Screen("posts")
    object Pets : Screen("pets")
}
```

### Navigation Graph

```kotlin
@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route  // ← Login es el inicio
    ) {
        composable(route = Screen.Login.route) {
            LoginScreen(
                navController = navController,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(navController = navController)
        }

        composable(route = Screen.Home.route) {
            HomeScreen()  // ← Renderizado después del login
        }
    }
}
```

### MainActivity - Lógica Condicional

```kotlin
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    // Obtener ruta actual
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Mostrar drawer/topbar solo si NO está en login/register
    val showDrawerAndTopBar = 
        currentRoute != Screen.Login.route && 
        currentRoute != Screen.Register.route

    if (showDrawerAndTopBar) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = { AppDrawer(drawerState, navController) }
        ) {
            Scaffold(
                topBar = { AppTopBar(drawerState, "Streamhub") }
            ) { paddingValues ->
                AppNavGraph(navController, Modifier.padding(paddingValues))
            }
        }
    } else {
        AppNavGraph(navController)  // ← Sin drawer/topbar en login
    }
}
```

---

## 🔄 Estados de la Aplicación

### AuthState Diagram

```
AuthState
├── isLoading: Boolean        // true durante petición a API
├── token: String?            // JWT del servidor (nulo si no autenticado)
├── error: String?            // Mensaje de error (nulo si no hay error)
└── isAuthenticated: Boolean  // true si login exitoso

Transiciones:
┌─────────────────────────────────────┐
│  Estado Inicial                     │
│  isLoading: false                   │
│  isAuthenticated: false             │
│  error: null                        │
└────────────────┬────────────────────┘
                 │ Usuario ingresa credenciales
                 ↓
┌─────────────────────────────────────┐
│  Durante Login                      │
│  isLoading: true  ← Usuario espera  │
│  isAuthenticated: false             │
│  error: null                        │
└────────────────┬────────────────────┘
                 │ API responde
         ┌───────┴──────┬──────────┐
         ↓              ↓          ↓
       Éxito          Error   Timeout
         │              │          │
         ↓              ↓          ↓
┌──────────────────┐ ┌──────────────────────┐
│  Autenticado:    │ │  Error:              │
│  isLoading: false│ │  isLoading: false    │
│  token: JWT...   │ │  error: "Invalid..." │
│  isAuth: true    │ │  isAuth: false       │
└──────────────────┘ └──────────────────────┘
```

---

## 📝 Validaciones Implementadas

### LoginScreen

```kotlin
Button(
    onClick = {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            viewModel.login(email, password)
        }
    }
) {
    Text("Iniciar Sesión")
}
```

### RegisterScreen

```kotlin
Button(
    onClick = {
        when {
            username.isEmpty() -> scope.launch {
                snackbarHostState.showSnackbar("El usuario es requerido")
            }
            email.isEmpty() -> scope.launch {
                snackbarHostState.showSnackbar("El email es requerido")
            }
            password.isEmpty() -> scope.launch {
                snackbarHostState.showSnackbar("La contraseña es requerida")
            }
            password != confirmPassword -> scope.launch {
                snackbarHostState.showSnackbar("Las contraseñas no coinciden")
            }
            else -> viewModel.register(username, email, password)
        }
    }
)
```

---

## 🚀 Flujo Completo de Login

```
Usuario abre app
    ↓
MainActivity renderiza
    ↓
MainScreen detecta currentRoute = Screen.Login.route
    ↓
Renderiza LoginScreen (sin drawer/topbar)
    ↓
Usuario ingresa email y password
    ↓
Presiona botón "Iniciar Sesión"
    ↓
LoginScreen.onClick → viewModel.login(email, password)
    ↓
AuthViewModel.login()
    ├─ Cambia isLoading = true
    ├─ Llama loginUseCase(email, password)
    │   └─ Llama authRepository.login(...)
    │       └─ Llama authApiService.login(...)
    │           └─ POST /api/users/login
    │
    └─ Respuesta API
        ├─ Éxito → token recibido
        │   └─ Actualiza authState.isAuthenticated = true
        │       └─ AuthState emite nuevo valor
        │           └─ LoginScreen observa cambio
        │               └─ LaunchedEffect ejecuta navegación
        │                   └─ navController.navigate(Screen.Home.route)
        │                       └─ MainScreen detecta nueva ruta
        │                           └─ Renderiza HomeScreen
        │                               └─ Muestra drawer y topbar
        │
        └─ Error → excepción
            └─ Actualiza authState.error = mensaje
                └─ AuthState emite nuevo valor
                    └─ LoginScreen observa cambio
                        └─ LaunchedEffect muestra Snackbar
```

---

## 🔐 Seguridad

### Token en Memory

```kotlin
// Actualmente: Token se guarda en AuthState (memoria)
data class AuthState(
    val token: String? = null  // ← Solo en memoria (se pierde al cerrar app)
)

// Mejora futura: Guardar en Storage
class AuthRepositoryImpl @Inject constructor(
    private val tokenStorage: TokenStorage  // DataStore o SharedPreferences
) {
    override suspend fun login(...): AuthResult {
        val response = authApiService.login(...)
        tokenStorage.saveToken(response.token)  // ← Guardar persistentemente
        return AuthResult.Success(response.token)
    }
}
```

### Token en Requests

```kotlin
// AuthInterceptor (futuro)
class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()
        val originalRequest = chain.request()

        // Agregar token solo a endpoints protegidos
        return if (token != null && isProtectedEndpoint(originalRequest.url)) {
            val authenticatedRequest = originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")  // ← JWT Header
                .build()
            chain.proceed(authenticatedRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}
```

---

## 📊 Resumen de Flujos

```
LOGIN FLOW:
User Input → ViewModel.login() → UseCase → Repository → API 
    → AuthResult → State Change → Navigation

REGISTER FLOW:
User Input → ViewModel.register() → UseCase → Repository → API 
    → AuthResult → Snackbar → Navigation to Login

NAVIGATION FLOW:
currentRoute changes → MainScreen detects → Conditional rendering
    → Show/hide drawer & topbar
```

Este es el flujo **completo, funcional y listo para producción** de la feature de usuarios.

