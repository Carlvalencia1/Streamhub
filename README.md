# Demo App 

Una aplicación de Android moderna construida con **Jetpack Compose** y **Clean Architecture**.

## 🏗️ Arquitectura
El proyecto sigue los principios de **Clean Architecture**, asegurando que la lógica de negocio sea independiente de la interfaz de usuario y de las fuentes de datos externas.

1.  **Presentation (UI):** Uso de ViewModels para un flujo de datos unidireccional (UDF).
2.  **Domain:** Contiene los Casos de Uso y las Entidades puras en Kotlin.
3.  **Data:** Implementación de Repositorios, Mappers para transformar DTOs a modelos de Dominio y configuración de Retrofit.

## 🛠️ Stack Tecnológico
* **Kotlin:** Lenguaje base.
* **Jetpack Compose:** Toolkit para UI declarativa.
* **Coroutines & Flow:** Gestión de concurrencia y estados reactivos.
* **Retrofit & Gson:** Consumo y parseo de APIs REST.
* **Coil:** Carga eficiente de imágenes asíncronas.
* **Architecture Components:** ViewModel, StateFlow y Lifecycle.

## 📂 Estructura del Proyecto
```text
features/feature01/
├── data/           # Repositories, DTOs (Data Transfer Objects) y Mappers
├── domain/         # Entidades de negocio y Use Cases
└── presentation/   # UI State, ViewModels y Composables (Screens/Components)
core/
├── ui/theme         
