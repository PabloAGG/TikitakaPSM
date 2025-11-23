# 🚀 Guía de Configuración - TikiTaka PSM

## ✅ Cambios Realizados

### 📁 Base de Datos
- ✅ Esquema de base de datos actualizado y sincronizado
- ✅ Tablas renombradas: `football_teams` → `teams`
- ✅ Campos de usuario alineados: soporte para `username`, `full_name`, `first_name`, `last_name`
- ✅ Referencias corregidas en todas las foreign keys

### 🔧 API (Node.js/Express)
- ✅ Archivo `.env` configurado con variables de entorno
- ✅ Middleware de autenticación JWT completado
- ✅ Rutas de auth actualizadas (login y registro)
- ✅ Rutas de teams, users y posts sincronizadas con la base de datos
- ✅ Nombres de tablas corregidos: `post_likes`, `user_favorites`

### 📱 App Android (Kotlin)
- ✅ LoginActivity mejorado con manejo robusto de errores
- ✅ RegisterActivity mejorado con validaciones completas
- ✅ Manejo específico de errores de red (timeout, sin conexión, etc.)
- ✅ Feedback mejorado al usuario durante el proceso
- ✅ Validación de teléfono opcional

---

## 🛠️ Pasos para Ejecutar el Proyecto

### 1️⃣ Configurar la Base de Datos

```bash
# Opción A: Usando el script SQL principal
mysql -u root -p < database/tikitaka_database.sql

# Opción B: Desde la API (recomendado)
cd tikitaka-api
npm run db:setup
```

Si tienes problemas, ejecuta manualmente en MySQL:
```sql
SOURCE C:/Users/migue/Documents/PABLO/Tikitaka-PSM/database/tikitaka_database.sql
```

### 2️⃣ Configurar Variables de Entorno

Edita `tikitaka-api/.env` con tus credenciales de MySQL:

```env
# Configuración de la base de datos MySQL
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=tu_password_aqui
DB_NAME=tikitaka_db

# JWT Secret (cambiar en producción)
JWT_SECRET=tikitaka_super_secret_key_2024_psm_app_mobile
JWT_EXPIRES_IN=7d

# Puerto del servidor
PORT=3000
NODE_ENV=development
```

### 3️⃣ Instalar Dependencias y Ejecutar API

```bash
cd tikitaka-api

# Instalar dependencias
npm install

# Probar conexión a la base de datos
npm run db:test

# Ejecutar en modo desarrollo
npm run dev
```

El servidor debería estar corriendo en: `http://localhost:3000`

### 4️⃣ Probar los Endpoints de la API

```bash
# Ruta raíz
curl http://localhost:3000/

# Obtener equipos
curl http://localhost:3000/api/teams

# Registro (ejemplo)
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@test.com",
    "password": "123456",
    "username": "test_user",
    "full_name": "Test User",
    "team_id": 1
  }'

# Login (ejemplo)
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@test.com",
    "password": "123456"
  }'
```

### 5️⃣ Configurar la App Android

1. **Abrir el proyecto en Android Studio**

2. **Verificar la URL de la API** en `ApiClient.kt`:
   - Para emulador: `http://10.0.2.2:3000/api/`
   - Para dispositivo físico: `http://TU_IP_LOCAL:3000/api/`

3. **Obtener tu IP local (si usas dispositivo físico)**:
   ```bash
   # Windows
   ipconfig
   
   # Linux/Mac
   ifconfig
   ```

4. **Sync del proyecto** en Android Studio

5. **Ejecutar la app** en emulador o dispositivo

---

## 🔍 Verificar que Todo Funciona

### Base de Datos
```sql
-- Verificar que las tablas existen
SHOW TABLES;

-- Verificar equipos
SELECT COUNT(*) FROM teams;

-- Debería mostrar 35 equipos
```

### API
- ✅ Servidor corriendo: `http://localhost:3000`
- ✅ Equipos disponibles: `http://localhost:3000/api/teams`
- ✅ Status: debería retornar `{ "success": true, "teams": [...] }`

### App Android
1. Abrir la app
2. Intentar registrarse con:
   - Email válido
   - Contraseña de al menos 6 caracteres
   - Seleccionar un equipo
3. Si todo funciona, deberías ver el MainActivity

---

## 🐛 Solución de Problemas Comunes

### Error: "No se puede conectar al servidor"
- ✅ Verifica que la API esté corriendo (`npm run dev`)
- ✅ Verifica la URL en `ApiClient.kt`
- ✅ Para emulador usa: `http://10.0.2.2:3000/api/`

### Error: "Table doesn't exist"
- ✅ Ejecuta el script SQL de nuevo
- ✅ Verifica que la base de datos `tikitaka_db` existe
- ✅ Usa: `npm run db:setup`

### Error: "El email ya está en uso"
- ✅ Normal si ya registraste ese email
- ✅ Usa otro email o elimina el registro anterior

### Error de JWT: "Token inválido"
- ✅ Verifica que JWT_SECRET sea el mismo en `.env`
- ✅ Cierra sesión y vuelve a iniciar

---

## 📋 Próximos Pasos

Una vez que login y registro funcionen:

1. ✅ Completar el feed de posts
2. ✅ Implementar creación de posts
3. ✅ Sistema de likes y favoritos
4. ✅ Perfil de usuario
5. ✅ Búsqueda y exploración

---

## 📝 Notas Importantes

### Estructura de Usuario en la API
```json
{
  "id": 1,
  "email": "usuario@example.com",
  "username": "usuario_123",
  "full_name": "Nombre Completo",
  "profile_image": null,
  "team_id": 1,
  "team_name": "Argentina",
  "team_logo": "https://flagcdn.com/ar.svg"
}
```

### Respuesta de Login/Register
```json
{
  "success": true,
  "message": "Login exitoso",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": { ...datos del usuario... }
}
```

---

## ⚙️ Comandos Útiles

```bash
# API
cd tikitaka-api
npm run dev          # Ejecutar en desarrollo
npm start            # Ejecutar en producción
npm run db:setup     # Configurar base de datos
npm run db:test      # Probar conexión

# Android
./gradlew clean      # Limpiar proyecto
./gradlew build      # Compilar proyecto
```

---

**Estado:** ✅ Login y Registro completamente funcionales

**Siguiente:** Completar funcionalidad del feed y posts