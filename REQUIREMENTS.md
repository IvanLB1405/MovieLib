# Requisitos del Proyecto MovieLib + MovieCritique

**Proyecto Final de Ciclo - DAM 2º (Modalidad Distancia)**
**Alumno:** Iván Fernández González
**Centro:** CIFP Avilés

---

## Descripción General

Desarrollo de una **librería de componentes reutilizables** para Android (`:movielib`) y una **aplicación demo** (`:app` - MovieCritique) que demuestre su funcionalidad. Todo implementado en Kotlin.

### Objetivo Principal

Facilitar la integración de funcionalidades relacionadas con información de películas en aplicaciones Android mediante componentes UI y lógica de negocio reutilizables.

---

## Componentes de la Librería (`:movielib`)

### 1. Componente de Búsqueda de Películas
- **Descripción:** Elemento UI para ingresar términos de búsqueda
- **Funcionalidad:** Conecta con API externa (TMDb) para buscar películas
- **Tecnología:** Custom View/ViewGroup o Composable

### 2. Componente de Lista de Resultados
- **Descripción:** Componente UI para mostrar resultados de búsqueda
- **Información mostrada por elemento:**
  - Título de la película
  - Imagen de portada (poster)
  - Año de publicación
- **Tecnología:** RecyclerView + Adapter o LazyList

### 3. Componente de Vista Detalle de Película
- **Descripción:** Interfaz completa para mostrar información detallada
- **Información mostrada:**
  - Título
  - Sinopsis completa
  - Portada (poster)
  - Actores principales
  - Año de publicación
  - Género(s)
- **Tecnología:** Activity/Fragment o Screen

### 4. Funcionalidad de Biblioteca Personal
- **Descripción:** Lógica de negocio para gestión de colección personal
- **Operaciones:**
  - Añadir películas a biblioteca local
  - Eliminar películas de biblioteca
  - Escribir reseñas de usuario
  - Asignar puntuaciones (rating)
- **Almacenamiento:** SQLite local (Room)

---

## Aplicación Demo: MovieCritique (`:app`)

Aplicación nativa Android que sirve como **plataforma de crítica de películas** simplificada.

### Funcionalidades de la App

1. **Búsqueda de Películas**
   - Utilizar componente de búsqueda de la librería
   - Mostrar resultados en lista

2. **Navegación a Detalles**
   - Selección de película desde lista
   - Vista de detalle completa

3. **Gestión de Biblioteca Personal**
   - Añadir películas a colección local
   - Escribir reseñas personales
   - Asignar puntuaciones
   - Visualizar biblioteca completa

---

## Requisitos Funcionales (RF)

### Librería Kotlin (`:movielib`)

- **RF01:** Proporcionar función para realizar búsquedas de películas en API externa (TMDb)
- **RF02:** Proporcionar función para obtener detalles de película específica por ID desde API externa

### Aplicación Android Kotlin (`:app`)

- **RF03:** Permitir al usuario ingresar términos de búsqueda, mostrar lista de resultados y seleccionar película para ver detalles
- **RF04:** Mostrar en vista de detalle: sinopsis, portada, actores principales, año de publicación y género
- **RF05:** Permitir añadir película, escribir reseña, asignar puntuación o eliminar de biblioteca personal local
- **RF06:** Almacenar localmente en SQLite (Room): películas añadidas, reseñas, puntuaciones
- **RF07:** Permitir visualizar biblioteca personal de películas

---

## Requisitos No Funcionales

### Tecnologías Obligatorias

- **Lenguaje:** Kotlin (100%)
- **IDE:** Android Studio / IntelliJ IDEA
- **Base de Datos Local:** SQLite via Room
- **API Externa:** The Movie Database (TMDb)
- **Testing:** JUnit
- **Control de Versiones:** Git
- **Build System:** Gradle

### Arquitectura

- **Patrón:** Clean Architecture con capas separadas
- **Data Layer:** Repository pattern (ya implementado)
- **Persistencia:** Room Database
- **Networking:** Retrofit + OkHttp
- **UI:** ViewBinding (ya habilitado)
- **Async:** Kotlin Coroutines + Flow

### SDK Requirements

- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 35
- **Compile SDK:** 35

---

## Módulos Relacionados del Ciclo

- **Programación en Dispositivos Móviles:** Desarrollo Android nativo, componentes UI
- **Acceso a Datos:** Room, SQLite, API REST con Retrofit
- **Desarrollo de Interfaces:** Custom Views, RecyclerView, Material Design

---

## Estado Actual del Proyecto

### ✅ Implementado (Capa de Datos)

- Estructura multi-módulo (`:app` + `:movielib`)
- Integración completa con API TMDb
- Room Database con entidad Movie
- Repository pattern con caching
- ApiResponse sealed class para manejo de estados
- Modelos de datos y conversores
- Layouts XML para todas las pantallas

### ⏳ Pendiente (Componentes UI y Lógica)

- **Componentes de la Librería:**
  - [ ] Componente de búsqueda (SearchView customizado)
  - [ ] Componente de lista de resultados (RecyclerView + Adapter)
  - [ ] Componente de vista detalle (Activity/Fragment)
  - [ ] API pública de la librería para biblioteca personal

- **Aplicación Demo:**
  - [ ] Implementar MainActivity (pantalla principal)
  - [ ] Implementar SearchActivity (búsqueda)
  - [ ] Implementar MovieDetailActivity (detalles)
  - [ ] Implementar gestión de biblioteca personal
  - [ ] Testing con JUnit

---

## Próximos Pasos

1. **Definir API Pública de la Librería**
   - Interfaces y clases públicas que expondrá `:movielib`
   - Documentación con KDoc

2. **Implementar Componentes UI Reutilizables**
   - SearchMovieView
   - MovieListView/Adapter
   - MovieDetailView

3. **Completar Aplicación Demo**
   - Integrar componentes de la librería
   - Implementar navegación entre pantallas
   - Añadir funcionalidad de biblioteca personal

4. **Testing y Documentación**
   - Unit tests con JUnit
   - Documentación de uso de la librería
   - README con instrucciones de integración

---

## Entregables

1. **Código Fuente:**
   - Librería `:movielib` (Android Library Module)
   - Aplicación demo `:app` (Android Application)

2. **Documentación:**
   - README.md con instrucciones de uso
   - KDoc en código público de la librería
   - Este documento de requisitos

3. **Control de Versiones:**
   - Repositorio Git con historial de commits
   - Tags para versiones importantes

---

**Fecha Documento:** 2025-01-04
**Versión:** 1.0
