# Requisitos del Proyecto MovieLib + MovieCritique

**Proyecto Final de Ciclo - DAM 2º (Modalidad Distancia)**
**Alumno:** Iván Fernández González
**Centro:** CIFP Avilés
**Versión:** 1.0
**Fecha Última Actualización:** 2025-11-11

---

## Descripción General

Desarrollo de una **librería de componentes reutilizables** para Android (:movielib) y una **aplicación demo** (:app - MovieCritique) que demuestre su funcionalidad. Todo implementado en Kotlin siguiendo principios de Clean Architecture.

### Objetivo Principal

Facilitar la integración de funcionalidades relacionadas con información de películas en aplicaciones Android mediante componentes UI y lógica de negocio reutilizables, con almacenamiento local y sincronización con API externa.

---

## Componentes de la Librería (:movielib)

### 1. Componente de Búsqueda de Películas
- **Descripción:** Funcionalidad para realizar búsquedas de películas
- **Implementación:** SearchActivity con debounce de 500ms
- **Tecnología:** Kotlin Coroutines + Flow
- **Estado:** Implementado

### 2. Componente de Lista de Resultados
- **Descripción:** Visualización de resultados de búsqueda y listas de películas
- **Implementación:** MovieAdapter con RecyclerView
- **Información mostrada:**
  - Título de la película
  - Imagen de portada (poster)
  - Año de publicación
  - Valoración media
- **Tecnología:** RecyclerView con ViewBinding y DiffUtil
- **Layouts soportados:** Horizontal y Grid (3 columnas)
- **Estado:** Implementado

### 3. Componente de Vista Detalle de Película
- **Descripción:** Interfaz completa para mostrar información detallada
- **Implementación:** MovieDetailActivity
- **Información mostrada:**
  - Título
  - Sinopsis completa
  - Portada y backdrop
  - Actores principales (máximo 5)
  - Año de publicación
  - Género(s)
  - Valoración TMDb
  - Valoración personal (si está en biblioteca)
- **Tecnología:** Activity con ViewBinding
- **Estado:** Implementado

### 4. Funcionalidad de Biblioteca Personal
- **Descripción:** Lógica de negocio para gestión de colección personal
- **Implementación:** MovieRepository + MovieDao
- **Operaciones:**
  - Añadir películas a biblioteca local
  - Eliminar películas de biblioteca
  - Escribir reseñas de usuario
  - Asignar puntuaciones (escala 0-10)
  - Visualizar estadísticas de biblioteca
- **Almacenamiento:** SQLite con Room
- **Estado:** Implementado

---

## Aplicación Demo: MovieCritique (:app)

Aplicación nativa Android que sirve como **plataforma de crítica de películas** simplificada.

### Funcionalidades de la App

1. **Pantalla Principal (MainActivity)**
   - Película destacada en portada con información ampliada
   - Lista horizontal de películas populares
   - Lista horizontal de películas mejor valoradas
   - Sección de biblioteca personal (si hay contenido)
   - Actualización automática con Flow

2. **Búsqueda de Películas (SearchActivity)**
   - Campo de búsqueda con debounce
   - Grid de resultados (3 columnas)
   - Estados: vacío, cargando, sin resultados, éxito
   - Navegación a detalles

3. **Detalles de Película (MovieDetailActivity)**
   - Vista completa de información
   - Botones de añadir/eliminar de biblioteca
   - Dialog de valoración (0-5 estrellas)
   - Dialog de reseña (texto libre)
   - Preservación de datos de usuario

4. **Gestión de Biblioteca Personal (LibraryActivity)**
   - Estadísticas: total, promedio, reseñas
   - Grid de películas en biblioteca
   - Sección separada para películas reseñadas
   - Navegación a detalles

---

## Requisitos Funcionales (RF)

### Librería Kotlin (:movielib)

**RF01:** Proporcionar función para realizar búsquedas de películas en API externa (TMDb)
- **Estado:** COMPLETADO
- **Implementación:** TMDbService.searchMovies() + MovieRepository.searchMovies()
- **Características:** Retorna Flow con estados (Loading, Success, Error, NetworkError)

**RF02:** Proporcionar función para obtener detalles de película específica por ID desde API externa
- **Estado:** COMPLETADO
- **Implementación:** TMDbService.getMovieDetails() + MovieRepository.getMovieDetails()
- **Características:** Incluye créditos (cast), caché automático, preservación de datos de usuario

### Aplicación Android Kotlin (:app)

**RF03:** Permitir al usuario ingresar términos de búsqueda, mostrar lista de resultados y seleccionar película para ver detalles
- **Estado:** COMPLETADO
- **Implementación:** SearchActivity con grid layout
- **Características:** Debounce de 500ms, cancelación de búsquedas previas, navegación directa

**RF04:** Mostrar en vista de detalle: sinopsis, portada, actores principales, año de publicación y género
- **Estado:** COMPLETADO
- **Implementación:** MovieDetailActivity
- **Características:** Backdrop + poster, máximo 5 actores, géneros formateados, year extraído de releaseDate

**RF05:** Permitir añadir película, escribir reseña, asignar puntuación o eliminar de biblioteca personal local
- **Estado:** COMPLETADO
- **Implementación:** MovieDetailActivity con dialogs personalizados
- **Características:** Rating 0-5 estrellas (guardado 0-10), reseña opcional, confirmación de eliminación

**RF06:** Almacenar localmente en SQLite (Room): películas añadidas, reseñas, puntuaciones
- **Estado:** COMPLETADO
- **Implementación:** MovieDatabase (Room) con MovieDao
- **Características:** 30+ operaciones, Flow para actualizaciones reactivas, caché automático

**RF07:** Permitir visualizar biblioteca personal de películas
- **Estado:** COMPLETADO
- **Implementación:** LibraryActivity
- **Características:** Estadísticas, grid layout, sección de reseñas, navegación a detalles

---

## Requisitos No Funcionales

### Tecnologías Implementadas

- **Lenguaje:** Kotlin 2.0.21 (100%)
- **IDE:** Android Studio Ladybug 2024.2.1
- **Base de Datos Local:** SQLite via Room 2.6.1
- **API Externa:** The Movie Database (TMDb) API v3
- **Testing:** JUnit 4.13.2, MockK 1.13.8, Turbine 1.0.0
- **Control de Versiones:** Git
- **Build System:** Gradle 8.10.1

### Arquitectura Implementada

- **Patrón:** Clean Architecture con tres capas
  - Presentación: Activities + Adapters
  - Dominio: Repository + ApiResponse
  - Datos: Room Database + Retrofit API
- **Patrones de Diseño:**
  - Repository Pattern
  - Singleton Pattern (Database, ApiClient)
  - Sealed Classes (ApiResponse)
  - ViewHolder Pattern
  - Observer Pattern (Flow)
- **Async:** Kotlin Coroutines 1.7.3 + Flow
- **UI:** ViewBinding habilitado
- **Networking:** Retrofit 2.9.0 + OkHttp 4.12.0

### SDK Requirements

- **Min SDK:** 24 (Android 7.0 Nougat)
- **Target SDK:** 35
- **Compile SDK:** 35
- **JDK:** 11

---

## Módulos Relacionados del Ciclo

- **Programación en Dispositivos Móviles**
- **Desarrollo de Interfaces**
- **Acceso a Datos**

---

## Estado del Proyecto - Versión 1.0

### Componentes Implementados

**Arquitectura y Estructura:**
- Estructura multi-módulo (:app + :movielib)
- Separación de capas (UI, Domain, Data)
- Patrones de diseño aplicados correctamente

**Capa de Datos:**
- Integración completa con TMDb API (5 endpoints)
- Room Database con entidad Movie
- MovieDao con 30+ operaciones
- Repository pattern con caché automático
- ApiResponse sealed class para gestión de estados
- Modelos de datos con conversores

**Capa de Presentación:**
- MainActivity: Pantalla principal con hero y listas
- SearchActivity: Búsqueda con grid layout
- MovieDetailActivity: Detalles completos con dialogs
- LibraryActivity: Biblioteca con estadísticas
- MovieAdapter: Soporte horizontal y grid
- MovieReviewAdapter: Películas reseñadas

**Testing:**
- MovieTest: 13 tests (100% cobertura de modelos)
- ApiResponseTest: 9 tests (100% cobertura)
- MovieRepositoryTest: 12 tests (~85% cobertura)
- MovieDaoTest: 19 tests (~90% cobertura)
- Total: 56+ tests con ~88% cobertura en componentes críticos

**Seguridad:**
- API key en BuildConfig (no hardcodeada)
- local.properties en .gitignore
- HTTPS obligatorio (usesCleartextTraffic=false)

**Documentación:**
- README.md
- REQUIREMENTS.md
- DOCUMENTACION_TECNICA.pdf
- KDoc en clases principales
- Comentarios de código

### Características Adicionales Implementadas

- Película destacada en portada con información ampliada (sinopsis y valoración)
- Listas de películas populares y top-rated
- Estadísticas de biblioteca (total, promedio, películas con reseña)
- Caché automático con estrategia offline-first
- Búsqueda con debounce (500ms) para optimización
- Grids de 3 columnas para mejor visualización
- Dialogs personalizados para rating y review
- Flow reactivo para actualizaciones en tiempo real
- Preservación de datos de usuario al actualizar desde API
- Manejo robusto de estados y errores
- Navegación fluida entre pantallas

---

## Decisiones Técnicas Documentadas

### ViewBinding vs DataBinding
**Decisión:** ViewBinding
**Razón:** Más ligero, sin procesamiento en build time

### Activities vs Fragments
**Decisión:** Activities
**Razón:** Simplicidad en navegación

### Flow vs LiveData
**Decisión:** Flow
**Razón:** API de coroutines, mejor integración con Room y Retrofit, más flexible

### Singleton manual vs DI
**Decisión:** Singleton manual (getInstance())
**Razón:** Simplicidad y suficiente para el alcance del proyecto

### Migrations de Room
**Decisión:** fallbackToDestructiveMigration()
**Razón:** Aceptable para el alcance del proyecto

---

## Entregables

### 1. Código Fuente
- Librería :movielib (Android Library Module)
- Aplicación demo :app (Android Application)
- Estructura multi-módulo con Gradle

### 2. Documentación
- README.md: Instrucciones de instalación y uso
- REQUIREMENTS.md: Este documento con requisitos completos
- DOCUMENTACION_TECNICA.md: Documentación exhaustiva
- KDoc: En clases y métodos públicos de la librería
- movielib/README.md: Guía de integración de la librería

### 3. Testing
- 56+ tests automatizados
- Cobertura ~88% en componentes críticos
- Tests unitarios y de integración

### 4. Control de Versiones
- Repositorio Git con historial completo
- Commits descriptivos y organizados
- .gitignore configurado correctamente

---

## Métricas del Proyecto

**Líneas de Código:**
- Total: ~3,500 líneas
- Kotlin: 100%
- Tests: ~1,200 líneas

**Archivos:**
- Clases Kotlin: 25+
- Layouts XML: 15+
- Tests: 4 archivos

**Cobertura de Tests:**
- Modelos: 100%
- API Layer: ~90%
- Repository: ~85%
- Database: ~90%
- Total Crítico: ~88%


---

## Contacto y Soporte del Proyecto

**Desarrollador:** Iván Fernández González
**Centro Educativo:** CIFP Avilés
**Ciclo Formativo:** Desarrollo de Aplicaciones Multiplataforma (DAM 2º)
**Modalidad:** Distancia
**Año Académico:** 2024-2025

Para consultas sobre el proyecto:
- Revisar DOCUMENTACION_TECNICA.md
- Consultar README.md
- Crear issue en el repositorio

---

**Documento de Requisitos v1.0** - MovieLib Project
**Fecha de Entrega:** 2025-11-11
**Estado:** COMPLETADO


