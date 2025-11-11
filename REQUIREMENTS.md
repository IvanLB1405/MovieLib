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

## Estado del Proyecto - Versión 1.0 (Completado)

### ✅ Requisitos Funcionales Implementados

**Librería Kotlin (`:movielib`):**
- ✅ **RF01:** Función de búsqueda de películas en API TMDb implementada
- ✅ **RF02:** Función de obtener detalles por ID implementada

**Aplicación Android (`:app`):**
- ✅ **RF03:** Búsqueda con SearchActivity, lista de resultados y navegación a detalles
- ✅ **RF04:** Vista detalle muestra sinopsis, portada, actores, año, géneros
- ✅ **RF05:** Añadir/eliminar películas, escribir reseñas, asignar puntuaciones
- ✅ **RF06:** Almacenamiento local con Room SQLite funcional
- ✅ **RF07:** Biblioteca personal con LibraryActivity implementada

**Funcionalidades Adicionales Implementadas:**
- ✅ Sección de reseñas en biblioteca personal
- ✅ Estadísticas de biblioteca (total, promedio rating, películas reseñadas)
- ✅ Caché automático de películas buscadas
- ✅ Interfaz de usuario completa con Material Design
- ✅ Paleta de colores MovieCritique (naranja pastel y negro)
- ✅ Icono de aplicación personalizado y minimalista

---

## Mejoras Planificadas para Versión 2.0

> **Nota:** La versión 1.0 cumple con todos los requisitos del PFC DAM 2º.
> Las siguientes mejoras están documentadas para futuras entregas profesionales.

### 🔐 Seguridad (Crítico)
Mejoras de seguridad planificadas para futuras versiones:
- **SEC-01:** Mover API key a BuildConfig (⚠️ CRÍTICO) - ✅ Implementado en v1.0
- **SEC-02:** Deshabilitar cleartext traffic en producción
- **SEC-03:** Logging solo en debug builds - ✅ Implementado en v1.0
- **SEC-04:** Activar ProGuard/R8 en release
- **SEC-05:** Configurar backup cifrado
- **SEC-06:** Certificate pinning para TMDb API

### 🏗️ Arquitectura (Alta Prioridad)
- **ARCH-01:** Implementar capa ViewModel (MVVM completo)
- **ARCH-02:** Inyección de dependencias con Hilt
- **ARCH-03:** Navigation Component
- **ARCH-04:** Repository con abstracción (interfaces)
- **ARCH-05:** UiState sealed classes

### ⚡ Performance (Media Prioridad)
- **PERF-01:** Room migrations (eliminar fallbackToDestructiveMigration)
- **PERF-02:** Paginación con Paging 3
- **PERF-03:** Configuración de caché de Glide
- **PERF-04:** WorkManager para sincronización

### 🧪 Testing (Alta Prioridad)
- **TEST-01:** Tests unitarios de Repository y DAOs
- **TEST-02:** Tests de integración con Room
- **TEST-03:** Tests de UI con Espresso

### 📝 Documentación (Media Prioridad)
- **DOC-01:** Completar KDoc en todas las clases públicas
- **DOC-02:** README de integración de librería
- **DOC-03:** Estandarizar comentarios a inglés
- **DOC-04:** Mover strings hardcodeados a resources
- **DOC-05:** Eliminar magic numbers

### 🔄 Refactoring (Baja Prioridad)
- **REF-01:** Nombres de paquetes más consistentes
- **REF-02:** Dividir Activities grandes en Fragments
- **REF-03:** Manejo de rotación de pantalla
- **REF-04:** Analytics y Crashlytics
- **REF-05:** CI/CD con GitHub Actions

---

## Checklist Pre-Entrega PFC

### Código
- [x] Todos los RF01-RF07 implementados
- [x] Aplicación compila sin errores
- [x] Arquitectura limpia con capas separadas
- [x] ViewBinding habilitado y en uso
- [x] Room database funcional
- [x] Retrofit integrado correctamente

### UI/UX
- [x] Todas las pantallas implementadas
- [x] Diseño coherente con Material Design
- [x] Navegación fluida entre pantallas
- [x] Iconografía personalizada
- [x] Paleta de colores definida

### Documentación
- [x] `REQUIREMENTS.md` con todos los RF
- [x] Comentarios KDoc en clases principales
- [x] `README.md` del proyecto completo
- [x] `DOCUMENTACION_TECNICA.md` exhaustiva (~60 páginas)
- [x] README.md de la librería completado

### Control de Versiones
- [x] Repositorio Git configurado
- [x] Commits descriptivos
- [x] Estructura de proyecto clara
- [ ] Tags de versión (v1.0 pendiente)

### Limpieza Final
- [ ] Eliminar código comentado
- [ ] Optimizar imports
- [ ] Ejecutar `./gradlew lint` y resolver warnings
- [ ] Formatear código con Kotlin Style Guide
- [ ] Remover logs de debug innecesarios

---

## Roadmap Futuro

### Versión 2.0 - Mejoras de Arquitectura (Post-PFC)
**Objetivos:**
- Implementar MVVM completo con ViewModels
- Añadir DI con Hilt
- Tests unitarios básicos (>50% coverage)
- Migrar API key a BuildConfig

**Duración estimada:** 2-3 semanas

### Versión 3.0 - Producción Ready
**Objetivos:**
- Todas las mejoras de seguridad implementadas
- Tests completos (>80% coverage)
- CI/CD configurado
- Publicación en Google Play (beta)
- Analytics y crash reporting

**Duración estimada:** 1-2 meses

### Versión 4.0 - Features Avanzados
**Posibles características:**
- Jetpack Compose (migración UI)
- Soporte offline completo
- Sincronización con cuenta de usuario
- Compartir reseñas en redes sociales
- Recomendaciones personalizadas con ML

---

## Notas de Desarrollo

### Decisiones Técnicas Tomadas (v1.0)

1. **ViewBinding vs DataBinding:** Se eligió ViewBinding por simplicidad
2. **Activities vs Fragments:** Activities para MVP, Fragments en v2.0
3. **Flow vs LiveData:** Flow para API moderna y mejor soporte de coroutines
4. **Singleton manual vs DI:** Manual para v1.0, Hilt en v2.0
5. **Paginación:** No implementada en v1.0 por simplicidad del MVP

### Lecciones Aprendidas

1. **API Key:** Nunca commitear keys en producción (⚠️ corregir en v2.0)
2. **Migrations:** `fallbackToDestructiveMigration()` solo para desarrollo
3. **Testing:** Tests desde el inicio reducen bugs
4. **Arquitectura:** MVVM desde el principio facilita escalabilidad
5. **Documentación:** KDoc es esencial para librerías reutilizables

### Deuda Técnica Identificada

Consultar `DOCUMENTACION_TECNICA.md` para análisis completo de mejoras futuras.

**Crítico:**
- API key hardcodeada (SEC-01)
- Logging en producción (SEC-03)
- Sin tests (TEST-01)

**Alta:**
- Falta ViewModel (ARCH-01)
- No hay DI (ARCH-02)
- Sin migrations (PERF-01)

**Media:**
- Sin paginación (PERF-02)
- Documentación incompleta (DOC-01)

---

**Fecha Documento:** 2025-01-04
**Última Actualización:** 2025-01-08 (Auditoría Técnica)
**Versión:** 1.0 (Completada)
**Próxima Versión:** 2.0 (Planificada)
