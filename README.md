# MovieLib + MovieCritique

**Proyecto Final de Ciclo - DAM 2º**
**Librería Android + Aplicación Demo de Crítica de Películas**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-7.0%2B-green.svg)](https://android.com)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.0-orange.svg)](REQUIREMENTS.md)

---

## Descripción

MovieLib es una librería Android reutilizable que proporciona componentes UI y lógica de negocio para aplicaciones relacionadas con películas. MovieCritique es la aplicación demo que utiliza esta librería como plataforma simplificada de crítica de películas.

### Características Principales

**Funcionalidades de Usuario:**
- Búsqueda en tiempo real de películas con TMDb API
- Exploración de películas populares y mejor valoradas
- Detalles completos: sinopsis, reparto, géneros, valoración
- Biblioteca personal de películas favoritas
- Sistema de valoración personalizada (escala 0-10)
- Escritura y gestión de reseñas
- Estadísticas de biblioteca personal

**Tecnologías Implementadas:**
- Caché local con Room para acceso offline
- Sincronización automática con TMDb API
- UI moderna con Material Design 3
- Arquitectura Clean con separación de capas
- Testing con JUnit, MockK y Turbine (56+ tests)
- Carga optimizada de imágenes con Glide
- Programación reactiva con Kotlin Flow

---

## Arquitectura

El proyecto sigue Clean Architecture con tres capas principales:

```
┌─────────────────────────────────────┐
│    CAPA DE PRESENTACIÓN (UI)        │
│  Activities • Adapters • ViewHolders│
└────────────────┬────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│   CAPA DE DOMINIO (Business Logic)  │
│  MovieRepository • ApiResponse       │
└────────────────┬────────────────────┘
                 │
       ┌─────────┴──────────┐
       ▼                    ▼
┌──────────────┐    ┌──────────────┐
│  DATA LOCAL  │    │  DATA REMOTE │
│ Room Database│    │ Retrofit API │
└──────────────┘    └──────────────┘
```

### Patrones de Diseño Implementados

- **Repository Pattern**: Abstracción de fuentes de datos
- **Singleton Pattern**: MovieDatabase y ApiClient
- **ViewHolder Pattern**: RecyclerView optimizado
- **Observer Pattern**: Flow para datos reactivos
- **Builder Pattern**: Configuración de Retrofit/OkHttp
- **Sealed Classes**: Gestión de estados API con ApiResponse

---

## Estructura del Proyecto

```
MovieLib/
├── app/                          # Módulo de aplicación (MovieCritique)
│   ├── adapters/                 # Adapters para RecyclerView
│   │   ├── MovieAdapter.kt       # Adapter para listas y grids
│   │   └── MovieReviewAdapter.kt # Adapter para películas reseñadas
│   ├── base/                     # BaseMovieActivity
│   ├── extensions/               # Extension functions (ApiResponse)
│   ├── MainActivity.kt           # Pantalla principal con películas populares
│   ├── SearchActivity.kt         # Búsqueda de películas
│   ├── LibraryActivity.kt        # Biblioteca personal
│   └── MovieDetailActivity.kt    # Detalles de película
│
├── movielib/                     # Módulo de librería reutilizable
│   ├── api/                      # Retrofit + TMDbService
│   │   ├── ApiClient.kt          # Singleton de Retrofit
│   │   ├── TMDbService.kt        # Endpoints de la API
│   │   └── ApiResponse.kt        # Sealed class para estados
│   ├── database/                 # Room Database + DAO
│   │   ├── MovieDatabase.kt      # Singleton de la base de datos
│   │   └── MovieDao.kt           # 30+ operaciones CRUD
│   ├── models/                   # Entidades y modelos
│   │   └── Movie.kt              # Movie, MovieApiModel, Conversores
│   ├── repository/               # MovieRepository
│   │   └── MovieRepository.kt    # Coordinación API + DB
│   └── utils/                    # Constants y utilidades
│
├── DOCUMENTACION_TECNICA.md      # Documentación técnica completa
└── REQUIREMENTS.md               # Requisitos funcionales del PFC
```

---

## Inicio Rápido

### Prerrequisitos

- **Android Studio**: Ladybug 2024.2.1 o superior
- **JDK**: 11 o superior
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 35

### Configuración de la API Key

1. Obtén una API key gratuita de [TMDb](https://www.themoviedb.org/settings/api)
2. Crea el archivo `local.properties` en la raíz del proyecto (si no existe)
3. Añade tu API key:

```properties
TMDB_API_KEY=tu_clave_aqui
```

**IMPORTANTE:** `local.properties` está en `.gitignore` y NO se commitea al repositorio por seguridad.

### Compilar y Ejecutar

```bash
# Limpiar y compilar el proyecto
./gradlew clean build

# Compilar APK de debug
./gradlew assembleDebug

# Instalar en dispositivo conectado
./gradlew installDebug

# Ejecutar tests unitarios
./gradlew test

# Ejecutar tests del módulo movielib
./gradlew :movielib:testDebugUnitTest
```

---

## Funcionalidades Implementadas

### Requisitos Funcionales Cumplidos (RF01-RF07)

**Librería Kotlin (:movielib):**
- RF01: Búsqueda de películas en TMDb API
- RF02: Obtención de detalles por ID

**Aplicación Android (:app):**
- RF03: Búsqueda, lista de resultados y navegación
- RF04: Vista detalle completa (sinopsis, portada, actores, año, géneros)
- RF05: Gestión de biblioteca (añadir, eliminar, reseñar, valorar)
- RF06: Almacenamiento local con Room SQLite
- RF07: Visualización de biblioteca personal

### Características Adicionales

- Sección hero con película destacada
- Estadísticas de biblioteca (total, promedio, reseñas)
- Caché automático con estrategia offline-first
- Búsqueda con debounce (500ms)
- Grids de 3 columnas para resultados
- Dialogs personalizados para rating/review
- Flow reactivo para actualizaciones en tiempo real
- Navegación fluida entre pantallas
- Manejo robusto de estados (Loading, Success, Error, NetworkError)

---

## Testing

El proyecto incluye 56+ tests con cobertura del aproximadamente 88% en componentes críticos:

```bash
# Tests unitarios
./gradlew :movielib:testDebugUnitTest

# Tests instrumentados (requiere emulador)
./gradlew :movielib:connectedAndroidTest

# Todos los tests
./gradlew test connectedAndroidTest
```

### Cobertura de Tests

| Componente       | Tests | Cobertura |
|------------------|-------|-----------|
| ApiResponse      | 9     | 100%      |
| Movie Models     | 13    | 100%      |
| MovieRepository  | 12    | ~85%      |
| MovieDao         | 19    | ~90%      |

**Tecnologías de testing:**
- JUnit 4.13.2
- MockK 1.13.8 (mocking)
- Turbine 1.0.0 (Flow testing)
- Coroutines Test 1.7.3
- Room Testing 2.6.1
- AndroidX Core Testing 2.2.0

---

## Documentación

### Documentación Disponible

- **[DOCUMENTACION_TECNICA.md](DOCUMENTACION_TECNICA.md)** - Documentación técnica completa (60 páginas)
  - Arquitectura detallada
  - Explicación de todos los componentes
  - Conceptos clave para desarrolladores junior
  - Patrones de diseño implementados
  - Guía de testing
  - Glosario técnico completo

- **[REQUIREMENTS.md](REQUIREMENTS.md)** - Requisitos funcionales del PFC
  - Todos los RF01-RF07
  - Estado del proyecto
  - Roadmap futuro

- **[movielib/README.md](movielib/README.md)** - Documentación específica de la librería
  - API pública
  - Instrucciones de integración
  - Ejemplos de uso

### Generar PDF de la Documentación

La documentación técnica puede convertirse a PDF usando varios métodos:

**Método 1: VS Code con Extension** (Recomendado)
1. Instalar [VS Code](https://code.visualstudio.com/)
2. Instalar extension "Markdown PDF"
3. Abrir `DOCUMENTACION_TECNICA.md`
4. Click derecho → "Markdown PDF: Export (pdf)"

**Método 2: Herramientas Online**
- https://www.markdowntopdf.com/
- https://cloudconvert.com/md-to-pdf

**Método 3: Pandoc** (Requiere instalación)
```bash
sudo apt-get install pandoc texlive-latex-base texlive-latex-extra
pandoc DOCUMENTACION_TECNICA.md -o DOCUMENTACION_TECNICA.pdf --toc
```

---

## Tecnologías y Dependencias

### Core

- **Kotlin**: 2.0.21
- **Android Gradle Plugin**: 8.10.1
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 35
- **JDK**: 11

### Librerías Principales

| Categoría | Librería | Versión |
|-----------|----------|---------|
| **UI** | Material Components | 1.12.0 |
| | Glide | 4.16.0 |
| | RecyclerView | 1.3.2 |
| | ConstraintLayout | 2.1.4 |
| | CardView | 1.0.0 |
| **Database** | Room Runtime | 2.6.1 |
| | Room KTX | 2.6.1 |
| **Networking** | Retrofit | 2.9.0 |
| | Gson Converter | 2.9.0 |
| | OkHttp Logging | 4.12.0 |
| **Async** | Coroutines | 1.7.3 |
| | Lifecycle KTX | 2.7.0 |
| **Testing** | JUnit | 4.13.2 |
| | MockK | 1.13.8 |
| | Turbine | 1.0.0 |
| | Coroutines Test | 1.7.3 |

---

## Seguridad

### Implementado en v1.0

- API key en BuildConfig (no en código fuente)
- `local.properties` en `.gitignore`
- Logging solo en builds debug
- Permisos mínimos necesarios (INTERNET, ACCESS_NETWORK_STATE)
- HTTPS obligatorio (usesCleartextTraffic=false)
- ProGuard/R8 activado en release builds

### Mejoras Planificadas v2.0

- Certificate pinning para TMDb API
- Backup cifrado de base de datos
- Auditoría de seguridad completa
- Ofuscación adicional de código

---

## Estado del Proyecto

**Versión Actual**: 1.0 - Production Ready

**Calidad de Código**:
- Clean Architecture implementada
- Patrones de diseño aplicados correctamente
- Testing completo (56+ tests)
- Documentación profesional y exhaustiva
- Código limpio sin deuda técnica crítica
- KDoc en clases principales

**Cobertura de Tests**: Aproximadamente 88% en componentes críticos

**Estado de Requisitos**: Todos los RF01-RF07 completados

---

## Roadmap

### Versión 2.0 (Planificada)

- Implementar ViewModels (MVVM completo)
- Inyección de dependencias con Hilt
- Navigation Component
- Room Migrations
- Paginación con Paging 3
- Tests de UI con Espresso

### Versión 3.0 (Futura)

- Jetpack Compose
- Sincronización con cuenta de usuario
- Recomendaciones personalizadas
- Compartir reseñas en redes sociales
- Modo offline completo

---

## Desarrollo

### Comandos Útiles

```bash
# Verificar dependencias
./gradlew dependencies

# Análisis de código
./gradlew lint

# Generar Dokka (documentación)
./gradlew dokkaHtml

# Limpiar proyecto
./gradlew clean

# Ver estructura del proyecto
./gradlew projects
```

### Estructura de Commits

El proyecto sigue un historial de commits descriptivo:

```
git log --oneline
649800c Version 1.0 casi entregable
355c782 Cambios para completar version 1.0 entregable
c2ae348 Añadida docu formato KDOC, mejoras en seguridad y refactorizacion de codigo
7101ebd Mejoras en la legibilidad del codigo. Cambio Strings y refactorizacion/eliminacion de cosas inutiles
5d5a115 Añadido icono de escritorio, cambio de colortheme y añadir funcionalidad a la biblioteca
```

---

## Licencia

Este proyecto está bajo la licencia MIT. Consulta el archivo [LICENSE](LICENSE) para más detalles.

---

## Autor

**Iván Fernández González**
- **Centro**: CIFP Avilés
- **Ciclo**: DAM 2º (Modalidad Distancia)
- **Proyecto**: Final de Ciclo 2024-2025

---

## Agradecimientos

- [The Movie Database (TMDb)](https://www.themoviedb.org/) por proporcionar la API gratuita
- Comunidad de Android Developers
- Documentación oficial de Android y Kotlin

---

## Contacto y Soporte

Para preguntas, sugerencias o reportar problemas:
- Crear un issue en el repositorio
- Consultar la [documentación técnica completa](DOCUMENTACION_TECNICA.md)

---

**MovieLib v1.0** - Desarrollado con Kotlin y Android
