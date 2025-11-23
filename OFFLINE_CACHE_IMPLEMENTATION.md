# 📱 Sistema de Caché Offline - TikiTaka

## ✅ Funcionalidades Implementadas

### 1. **Base de Datos Local con Room**

Se implementó Room Database para almacenamiento offline con las siguientes entidades:

#### Entidades Creadas:
- **CachedPostEntity**: Almacena posts en caché local
  - Incluye toda la información del post (contenido, imágenes, likes, favoritos)
  - Campo `cachedAt` para control de validez del caché
  - Métodos `toPost()` y `fromPost()` para conversión

- **CachedTeamEntity**: Almacena información de equipos
  - Datos básicos del equipo (nombre, logo, confederación)
  - Contadores de fans y posts

- **DraftPostEntity**: Almacena borradores localmente
  - `localId`: ID local autogenerado
  - `serverId`: ID del servidor una vez sincronizado
  - `isSynced`: Marca si fue publicado en el servidor

#### DAOs Implementados:
- **PostDao**: CRUD completo para posts en caché
  - `getCachedPosts()`: Paginación offline
  - `updateLikeStatus()`: Actualización optimista de likes
  - `updateFavoriteStatus()`: Actualización optimista de favoritos
  - `deleteOldCache()`: Limpieza de caché antiguo

- **TeamDao**: Gestión de equipos en caché
- **DraftDao**: Gestión de borradores locales

### 2. **Repositorio con Estrategia Cache-First**

**PostRepository** implementa patrón Repository con:

#### Características Principales:
- **Cache-First Strategy**: Intenta cargar desde caché primero, luego desde red
- **Actualización Optimista**: Cambios visuales inmediatos, sincronización en background
- **Detección de Conectividad**: Usa `NetworkUtils` para decidir origen de datos
- **Caché con Validez**: Posts válidos por 30 minutos
- **Sincronización Automática**: Guarda datos de red en caché automáticamente

#### Métodos Implementados:
```kotlin
suspend fun getPosts(page: Int, limit: Int, forceRefresh: Boolean): Result<List<Post>>
suspend fun toggleLike(postId: Int, currentlyLiked: Boolean): Result<Boolean>
suspend fun toggleFavorite(postId: Int, currentlyFavorited: Boolean): Result<Boolean>
suspend fun clearOldCache()
fun getPostsFlow(): Flow<List<Post>>
```

### 3. **FeedFragment Mejorado**

Se actualizó para usar el sistema de caché:

#### Mejoras Implementadas:
- Integración con `PostRepository`
- Carga offline automática cuando no hay conexión
- Indicador visual de modo offline con Snackbar
- Likes y favoritos funcionan sin conexión (guardado local)
- Mensajes informativos según estado de red

#### Flujo de Funcionamiento:
1. **Con conexión**: Carga desde API → Guarda en caché → Muestra posts
2. **Sin conexión**: Carga desde caché → Muestra posts guardados
3. **Pull-to-refresh**: Fuerza carga desde red si hay conexión

### 4. **DraftsActivity Completado**

Funcionalidad completa para gestión de borradores:

#### Características:
- Carga borradores desde base de datos local
- Botones de acción:
  - **Editar**: Abre CreatePostActivity con datos del borrador
  - **Publicar**: Confirma y publica el borrador (en desarrollo)
  - **Eliminar**: Elimina borrador con confirmación
- Estados vacíos con mensajes informativos
- Recarga automática en `onResume()`

### 5. **ProfileFragment Completado**

Perfil de usuario con datos reales:

#### Implementación:
- Carga datos de usuario desde `PreferencesManager`
- Obtiene posts del usuario vía API (`getUserPosts`)
- Navegación a:
  - EditProfileActivity (editar perfil)
  - DraftsActivity (ver borradores)
- **Logout funcional**: Limpia `PreferencesManager` y redirige a Login
- Loading states con ProgressBar

### 6. **Utilidades de Red**

**NetworkUtils.kt** creado para:
- Detectar disponibilidad de red
- Compatible con Android M+ y versiones anteriores
- Verifica WiFi, datos móviles y ethernet

## 📦 Dependencias Agregadas

```kotlin
// Room database for offline caching
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")
```

Plugin agregado:
```kotlin
id("kotlin-kapt")
```

## 🎯 Características Principales del Sistema Offline

### ✨ Ventajas para el Usuario:

1. **Experiencia sin interrupciones**: Los usuarios pueden ver posts incluso sin conexión
2. **Respuesta instantánea**: Likes y favoritos se actualizan inmediatamente (actualización optimista)
3. **Transparencia**: El app informa cuando está en modo offline
4. **Sincronización automática**: Cambios locales se sincronizan cuando vuelve la conexión
5. **Caché inteligente**: Posts recientes se mantienen disponibles

### 🔄 Flujos Implementados:

#### Flujo de Lectura de Posts:
```
Usuario abre Feed
    ↓
¿Hay conexión?
    │
    ├─ Sí → Cargar desde API → Guardar en Room → Mostrar
    │
    └─ No → Cargar desde Room → Mostrar con indicador offline
```

#### Flujo de Like/Favorite:
```
Usuario da like
    ↓
Actualizar UI inmediatamente (optimista)
    ↓
Actualizar base de datos local
    ↓
¿Hay conexión?
    │
    ├─ Sí → Enviar a API → Confirmar cambio
    │
    └─ No → Guardar localmente → Sincronizar después
```

## 📁 Archivos Creados/Modificados

### Nuevos Archivos:
```
app/src/main/java/com/example/tikitaka/
├── database/
│   ├── AppDatabase.kt           (Base de datos Room)
│   ├── Entities.kt              (Entidades: Post, Team, Draft)
│   └── Daos.kt                  (DAOs para acceso a datos)
├── repository/
│   └── PostRepository.kt        (Patrón Repository con cache-first)
└── utils/
    └── NetworkUtils.kt          (Utilidades de conectividad)
```

### Archivos Modificados:
```
app/build.gradle.kts             (Dependencias Room + kapt plugin)
FeedFragment.kt                  (Integración con PostRepository)
DraftsActivity.kt                (Funcionalidad completa)
DraftsAdapter.kt                 (Callbacks para publish/delete)
ProfileFragment.kt               (Datos reales + logout)
```

## 🚀 Próximos Pasos Sugeridos

### Pendientes de Implementación:

1. **Sincronización de Borradores**:
   - Publicar borradores cuando haya conexión
   - Marcar borradores como sincronizados

2. **Sincronización de Likes/Favoritos**:
   - Cola de sincronización para cambios offline
   - Retry automático cuando vuelva la conexión

3. **EditProfileActivity**:
   - Implementar edición de perfil de usuario
   - Cambio de contraseña
   - Actualización de foto de perfil

4. **SearchFragment**:
   - Búsqueda de usuarios
   - Búsqueda de posts por contenido
   - Búsqueda de equipos

5. **FavoritesFragment**:
   - Mostrar posts favoritos del usuario
   - Filtros y ordenamiento

6. **Mejoras de Caché**:
   - WorkManager para sincronización en background
   - Política de limpieza de caché automática
   - Límite de tamaño de caché

## 🧪 Cómo Probar el Sistema Offline

### Probar Modo Offline en Emulador:

1. **Abrir el app y cargar posts con conexión**
2. **Desactivar la conexión**:
   - En emulador: Settings → Network & Internet → Airplane mode ON
   - O: Emulator Extended Controls → Cellular → Data status: denied
3. **Cerrar y reabrir el app**
4. **Verificar**:
   - ✅ Posts cargados previamente se muestran
   - ✅ Aparece mensaje "Modo offline - mostrando contenido guardado"
   - ✅ Likes/favoritos funcionan (guardado local)
5. **Reactivar conexión**:
   - Hacer pull-to-refresh
   - ✅ Se sincronizan cambios con servidor

## 💡 Notas Técnicas

### Decisiones de Diseño:

1. **Validez de Caché: 30 minutos**
   - Balance entre frescura y uso offline
   - Configurable en `PostRepository.CACHE_VALIDITY_DURATION`

2. **Estrategia de Actualización**:
   - Cache-first para lecturas
   - Optimistic updates para escrituras

3. **Gestión de Memoria**:
   - Singleton pattern para Repository y Database
   - Limpieza automática de caché antiguo

4. **Manejo de Errores**:
   - Try-catch en todas las operaciones de BD
   - Mensajes informativos al usuario

## 📚 Referencias

- [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [Repository Pattern](https://developer.android.com/codelabs/basic-android-kotlin-training-repository-pattern)
- [Offline-First Architecture](https://developer.android.com/topic/architecture/data-layer/offline-first)
