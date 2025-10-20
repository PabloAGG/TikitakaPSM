# PATRONES DE DISEÑO - TIKITAKA PSM

## 1. **PATRÓN SINGLETON**

### **Implementación: AuthManager**
**Propósito**: Gestionar la sesión del usuario de forma única en toda la aplicación.

```kotlin
// AuthManager.kt
class AuthManager private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: AuthManager? = null
        
        fun getInstance(): AuthManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthManager().also { INSTANCE = it }
            }
        }
    }
    
    private var currentUser: User? = null
    private var authToken: String? = null
    
    fun login(user: User, token: String) {
        currentUser = user
        authToken = token
        saveToPreferences()
    }
    
    fun logout() {
        currentUser = null
        authToken = null
        clearPreferences()
    }
    
    fun isLoggedIn(): Boolean {
        return currentUser != null && authToken != null
    }
    
    fun getCurrentUser(): User? = currentUser
    fun getAuthToken(): String? = authToken
    
    private fun saveToPreferences() {
        // Guardar en SharedPreferences
    }
    
    private fun clearPreferences() {
        // Limpiar SharedPreferences
    }
}
```

**Uso en la aplicación**:
```kotlin
// En LoginActivity
AuthManager.getInstance().login(user, token)

// En MainActivity
if (!AuthManager.getInstance().isLoggedIn()) {
    // Redirigir al login
}

// En ProfileFragment
val currentUser = AuthManager.getInstance().getCurrentUser()
```

**Ventajas**:
- ✅ Una sola instancia de gestión de autenticación
- ✅ Acceso global controlado
- ✅ Evita problemas de sincronización de sesión

---

## 2.  **PATRÓN FACTORY METHOD**

### **Implementación: PostAdapterFactory**
**Propósito**: Crear diferentes tipos de adaptadores según el contexto de visualización.

```kotlin
// PostAdapterFactory.kt
abstract class PostAdapterFactory {
    abstract fun createAdapter(posts: List<Post>): RecyclerView.Adapter<*>
    
    companion object {
        fun getFactory(type: AdapterType): PostAdapterFactory {
            return when(type) {
                AdapterType.FEED -> FeedAdapterFactory()
                AdapterType.PROFILE -> ProfileAdapterFactory()
                AdapterType.FAVORITES -> FavoritesAdapterFactory()
                AdapterType.DRAFTS -> DraftsAdapterFactory()
            }
        }
    }
}

enum class AdapterType {
    FEED, PROFILE, FAVORITES, DRAFTS
}

// Implementaciones específicas
class FeedAdapterFactory : PostAdapterFactory() {
    override fun createAdapter(posts: List<Post>): RecyclerView.Adapter<*> {
        return PostsAdapter(posts) // Adaptador estándar
    }
}

class ProfileAdapterFactory : PostAdapterFactory() {
    override fun createAdapter(posts: List<Post>): RecyclerView.Adapter<*> {
        return ProfilePostsAdapter(posts) // Con opciones de editar/eliminar
    }
}

class FavoritesAdapterFactory : PostAdapterFactory() {
    override fun createAdapter(posts: List<Post>): RecyclerView.Adapter<*> {
        return FavoritesAdapter(posts) // Con opción de quitar de favoritos
    }
}

class DraftsAdapterFactory : PostAdapterFactory() {
    override fun createAdapter(posts: List<Post>): RecyclerView.Adapter<*> {
        return DraftsAdapter(posts) { post -> 
            // Callback para editar borrador
        }
    }
}
```

**Uso en la aplicación**:
```kotlin
// En FeedFragment
val factory = PostAdapterFactory.getFactory(AdapterType.FEED)
val adapter = factory.createAdapter(posts)
recyclerView.adapter = adapter

// En ProfileFragment
val factory = PostAdapterFactory.getFactory(AdapterType.PROFILE)
val adapter = factory.createAdapter(userPosts)
recyclerView.adapter = adapter
```

**Ventajas**:
- ✅ Código más mantenible y extensible
- ✅ Separación de responsabilidades
- ✅ Fácil agregar nuevos tipos de adaptadores

---

## 3.  **PATRÓN OBSERVER**

### **Implementación: PostEventManager**
**Propósito**: Notificar cambios en posts a múltiples componentes de la aplicación.

```kotlin
// PostEventManager.kt
interface PostObserver {
    fun onPostLiked(postId: Int, newLikeCount: Int)
    fun onPostFavorited(postId: Int, isFavorited: Boolean)
    fun onPostDeleted(postId: Int)
    fun onPostUpdated(post: Post)
}

class PostEventManager private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: PostEventManager? = null
        
        fun getInstance(): PostEventManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PostEventManager().also { INSTANCE = it }
            }
        }
    }
    
    private val observers = mutableListOf<PostObserver>()
    
    fun subscribe(observer: PostObserver) {
        if (!observers.contains(observer)) {
            observers.add(observer)
        }
    }
    
    fun unsubscribe(observer: PostObserver) {
        observers.remove(observer)
    }
    
    fun notifyPostLiked(postId: Int, newLikeCount: Int) {
        observers.forEach { it.onPostLiked(postId, newLikeCount) }
    }
    
    fun notifyPostFavorited(postId: Int, isFavorited: Boolean) {
        observers.forEach { it.onPostFavorited(postId, isFavorited) }
    }
    
    fun notifyPostDeleted(postId: Int) {
        observers.forEach { it.onPostDeleted(postId) }
    }
    
    fun notifyPostUpdated(post: Post) {
        observers.forEach { it.onPostUpdated(post) }
    }
}
```

**Implementación en Fragmentos**:
```kotlin
// FeedFragment.kt
class FeedFragment : Fragment(), PostObserver {
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Suscribirse a eventos
        PostEventManager.getInstance().subscribe(this)
        
        setupRecyclerView()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        // Desuscribirse para evitar memory leaks
        PostEventManager.getInstance().unsubscribe(this)
    }
    
    // Implementación de PostObserver
    override fun onPostLiked(postId: Int, newLikeCount: Int) {
        // Actualizar UI del post específico
        adapter.updatePostLikes(postId, newLikeCount)
    }
    
    override fun onPostFavorited(postId: Int, isFavorited: Boolean) {
        // Actualizar estado de favorito en UI
        adapter.updatePostFavoriteStatus(postId, isFavorited)
    }
    
    override fun onPostDeleted(postId: Int) {
        // Remover post de la lista
        adapter.removePost(postId)
    }
    
    override fun onPostUpdated(post: Post) {
        // Actualizar post completo
        adapter.updatePost(post)
    }
}
```

**Uso desde Adaptadores**:
```kotlin
// PostsAdapter.kt
class PostsAdapter(private var posts: List<Post>) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {
    
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        
        holder.likeButton.setOnClickListener {
            // Llamar API para dar like
            ApiManager.likePost(post.id) { success, newLikeCount ->
                if (success) {
                    // Notificar a todos los observadores
                    PostEventManager.getInstance().notifyPostLiked(post.id, newLikeCount)
                }
            }
        }
        
        holder.favoriteButton.setOnClickListener {
            // Llamar API para favorito
            ApiManager.toggleFavorite(post.id) { success, isFavorited ->
                if (success) {
                    // Notificar a todos los observadores
                    PostEventManager.getInstance().notifyPostFavorited(post.id, isFavorited)
                }
            }
        }
    }
}
```

**Ventajas**:
- ✅ Sincronización automática entre pantallas
- ✅ Bajo acoplamiento entre componentes
- ✅ Fácil mantenimiento y extensión
- ✅ Actualizaciones en tiempo real

---

## 🎯 **RESUMEN DE PATRONES IMPLEMENTADOS**

| Patrón | Clase Principal | Propósito | Beneficio |
|--------|----------------|-----------|-----------|
| **Singleton** | `AuthManager` | Gestión única de sesión | Consistencia global |
| **Factory Method** | `PostAdapterFactory` | Creación de adaptadores | Flexibilidad y extensibilidad |
| **Observer** | `PostEventManager` | Comunicación entre componentes | Sincronización automática |

Estos patrones trabajan juntos para crear una arquitectura robusta, mantenible y escalable para la aplicación.