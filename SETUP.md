# Northern Chile - Guía de Configuración

Esta guía te ayudará a configurar el proyecto para desarrollo, demos y producción.

## 📋 Requisitos Previos

- Docker y Docker Compose instalados
- Node.js 20+ y pnpm (para el frontend)
- Java 21+ y Maven (opcional, si no usas Docker)

## 🚀 Inicio Rápido

### 1. Clonar el Repositorio

```bash
git clone <repository-url>
cd northernchile
```

### 2. Configurar Variables de Entorno

Copia el archivo de ejemplo y personalízalo:

```bash
cp .env.example .env
```

Edita el archivo `.env` y configura las variables necesarias (ver secciones abajo).

### 3. Iniciar los Servicios

```bash
# Inicia la base de datos y el backend
docker-compose up

# En otra terminal, inicia el frontend
cd frontend
pnpm install
pnpm dev
```

El frontend estará disponible en `http://localhost:3000` y el backend en `http://localhost:8080`.

---

## 👥 Configuración de Usuarios Administradores

### Método Recomendado: Multi-Admin Config

Usa la variable `ADMIN_USERS_CONFIG` para crear múltiples usuarios administradores de una sola vez:

```bash
# Formato: email:password:role;email2:password2:role2;...
ADMIN_USERS_CONFIG=alex@northernchile.cl:SecurePass123!:ROLE_SUPER_ADMIN;diego@northernchile.cl:SecurePass456!:ROLE_SUPER_ADMIN;david@northernchile.cl:SecurePass789!:ROLE_PARTNER_ADMIN
```

**Roles disponibles:**
- `ROLE_SUPER_ADMIN` - Acceso completo al sistema
- `ROLE_PARTNER_ADMIN` - Puede gestionar solo sus propios tours
- `ROLE_CLIENT` - Usuario cliente estándar

**Importante:**
- Los usuarios solo se crean si no existen previamente (idempotente)
- Las contraseñas se cifran automáticamente con BCrypt
- Nunca commits el archivo `.env` con contraseñas reales al repositorio

### Método Legacy (Deprecated)

Aún puedes usar las variables individuales, pero se recomienda migrar a `ADMIN_USERS_CONFIG`:

```bash
ADMIN_EMAIL=admin@northernchile.cl
ADMIN_FULL_NAME=Administrador Principal
ADMIN_PASSWORD=Admin123!secure
```

---

## 🌱 Datos Sintéticos (Seeding)

Para poblar la base de datos con datos de prueba (ideal para demos y MVPs):

### Habilitar Seeding

En tu archivo `.env`:

```bash
SEED_DATA=true
```

### ¿Qué datos se crean?

El seeding automáticamente crea:

**Tours de Ejemplo:**
- Tour Astronómico (ASTRONOMICAL)
- Valle de la Luna (REGULAR)
- Astrofotografía Nocturna (SPECIAL)
- Géiseres del Tatio (REGULAR)

**Características:**
- Traducciones en español, inglés y portugués
- Horarios generados para los próximos 30 días
- Simulación de fases lunares (tours astronómicos evitan luna llena)
- Diferentes horarios según categoría (tours nocturnos a las 20:00, regulares a las 15:00)
- Tours asignados al primer administrador encontrado en la base de datos

### Comportamiento del Seeding

- **Idempotente**: Solo crea datos si la base de datos está vacía (no hay tours)
- **Seguro**: No sobrescribe datos existentes
- **Configurable**: Desactiva con `SEED_DATA=false`

### Desactivar Seeding

Para producción o cuando ya tengas datos reales:

```bash
SEED_DATA=false
```

---

## 🔐 Gestión de Contraseñas (Admin)

Los SUPER_ADMIN pueden restablecer contraseñas de otros usuarios desde el panel de administración:

1. Ve a **Admin → Gestión de Usuarios**
2. Haz clic en **Editar** en el usuario deseado
3. En el modal, expande la sección **Restablecer Contraseña**
4. Ingresa la nueva contraseña (mínimo 8 caracteres)
5. Haz clic en **Restablecer Contraseña**

**Seguridad:**
- Esta acción queda registrada en el log de auditoría
- Solo accesible para usuarios con rol `ROLE_SUPER_ADMIN`
- Requiere confirmación antes de ejecutarse

---

## 🌍 Variables de Entorno Completas

### Base de Datos

```bash
POSTGRES_USER=user
POSTGRES_PASSWORD=password
SPRING_DATASOURCE_URL=jdbc:postgresql://database:5432/northernchile_db
```

### Administradores

```bash
# Nuevo método (recomendado)
ADMIN_USERS_CONFIG=email1:pass1:role1;email2:pass2:role2

# Método legacy
ADMIN_EMAIL=admin@northernchile.cl
ADMIN_FULL_NAME=Administrador Principal
ADMIN_PASSWORD=Admin123!secure
```

### Seeding

```bash
SEED_DATA=true  # o false para producción
```

### Frontend

```bash
NUXT_PUBLIC_API_BASE_URL=http://localhost:8080/api
NUXT_PUBLIC_BASE_URL=http://localhost:3000
```

### APIs Externas (Opcional)

```bash
GOOGLE_CLIENT_ID=TU_ID_DE_GOOGLE
GOOGLE_CLIENT_SECRET=TU_SECRETO_DE_GOOGLE
WEATHER_API_KEY=TU_API_KEY_DE_OPENWEATHERMAP
```

### AWS S3 (Para imágenes)

```bash
AWS_ACCESS_KEY_ID=TU_ACCESS_KEY_DE_AWS
AWS_SECRET_ACCESS_KEY=TU_SECRET_KEY_DE_AWS
AWS_REGION=sa-east-1
AWS_S3_BUCKET_NAME=northern-chile-assets
```

---

## 📦 Despliegue para Demo/MVP

Si vas a mostrar el proyecto a un cliente:

1. **Habilita el seeding:**
   ```bash
   SEED_DATA=true
   ```

2. **Configura múltiples admins** para demostrar diferentes roles:
   ```bash
   ADMIN_USERS_CONFIG=demo@northernchile.cl:Demo123!:ROLE_SUPER_ADMIN;partner@example.com:Partner123!:ROLE_PARTNER_ADMIN
   ```

3. **Reinicia los servicios** para aplicar los cambios:
   ```bash
   docker-compose down
   docker-compose up --build
   ```

4. **Verifica** que los datos se crearon correctamente revisando los logs:
   ```bash
   docker-compose logs backend | grep "INICIALIZACIÓN"
   ```

Deberías ver mensajes como:
```
✓ Usuario admin creado: demo@northernchile.cl con rol ROLE_SUPER_ADMIN
✓ Tour creado: Tour Astronómico (ASTRONOMICAL)
✓ 25 horarios generados para Tour Astronómico
```

---

## 🔒 Seguridad en Producción

**NUNCA subas el archivo `.env` al repositorio.** Usa `.env.example` como plantilla.

### Checklist de Seguridad:

- [ ] Cambia todas las contraseñas por defecto
- [ ] Usa contraseñas fuertes (mínimo 12 caracteres, mezcla de mayúsculas, minúsculas, números y símbolos)
- [ ] Configura `SEED_DATA=false` en producción
- [ ] Usa variables de entorno del sistema o un gestor de secretos (AWS Secrets Manager, etc.)
- [ ] Revisa el log de auditoría regularmente

---

## 🛠️ Troubleshooting

### Los usuarios admin no se crean

Verifica los logs del backend:
```bash
docker-compose logs backend | grep "admin"
```

Posibles causas:
- Variable `ADMIN_USERS_CONFIG` vacía o mal formateada
- Los usuarios ya existen en la base de datos

### El seeding no funciona

Verifica:
1. `SEED_DATA=true` en `.env`
2. La base de datos está vacía (no hay tours)
3. Existe al menos un usuario administrador

Para forzar un seeding limpio:
```bash
docker-compose down -v  # Elimina volúmenes (CUIDADO: borra todos los datos)
docker-compose up
```

### Error de conexión a la base de datos

Espera unos segundos más. PostgreSQL tarda en iniciarse. Si persiste:
```bash
docker-compose logs database
```

---

## 📚 Más Información

- Ver `CLAUDE.md` para guías de desarrollo específicas
- Ver `TODO.md` para funcionalidades pendientes
- API Docs disponibles en `http://localhost:8080/swagger-ui.html` cuando el backend está corriendo
