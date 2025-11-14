# Guía de Logs - Northern Chile Backend

Esta guía explica cómo ver y gestionar los logs del backend de forma fácil durante el desarrollo.

## 📋 Formas de Ver los Logs

### 1. Ver logs en tiempo real (Recomendado para desarrollo)

```bash
# Ver logs del backend en tiempo real (similar a tail -f)
docker-compose logs -f backend

# Ver logs de todos los servicios
docker-compose logs -f

# Ver últimas 100 líneas y seguir
docker-compose logs -f --tail=100 backend
```

### 2. Ver logs guardados en archivo

Los logs se guardan automáticamente en un volumen de Docker. Para verlos:

```bash
# Ver el archivo de logs completo
docker-compose exec backend cat /app/logs/application.log

# Ver últimas 50 líneas
docker-compose exec backend tail -n 50 /app/logs/application.log

# Seguir el archivo de logs en tiempo real
docker-compose exec backend tail -f /app/logs/application.log

# Buscar errores específicos
docker-compose exec backend grep "ERROR" /app/logs/application.log

# Buscar por palabra clave (ej: "AlertController")
docker-compose exec backend grep -i "AlertController" /app/logs/application.log
```

### 3. Filtrar logs por nivel

```bash
# Solo ver ERRORES
docker-compose logs backend 2>&1 | grep "ERROR"

# Solo ver WARNINGS y ERRORES
docker-compose logs backend 2>&1 | grep -E "WARN|ERROR"

# Ver logs de DEBUG level (para tu código en com.northernchile)
docker-compose logs backend 2>&1 | grep "DEBUG"
```

### 4. Ver stacktraces completos

Cuando ocurre un error 500, ahora en modo desarrollo verás:

**En la respuesta de la API:**
```json
{
  "timestamp": "2025-01-15T10:30:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Error message actual aquí",
  "path": "/api/admin/alerts/count",
  "exceptionType": "java.lang.NullPointerException",
  "stackTrace": "java.lang.NullPointerException: ...\n  at com.northernchile..."
}
```

**En los logs del contenedor:**
```bash
docker-compose logs backend 2>&1 | grep -A 30 "ERROR"
```

## 🔧 Comandos Útiles Adicionales

### Limpiar logs antiguos

```bash
# Rotar logs manualmente (vaciar el archivo actual)
docker-compose exec backend sh -c "echo '' > /app/logs/application.log"
```

### Copiar logs a tu máquina local

```bash
# Copiar el archivo de logs completo a tu máquina
docker cp $(docker-compose ps -q backend):/app/logs/application.log ./application.log

# Luego puedes abrirlo con tu editor favorito
code ./application.log  # VS Code
cat ./application.log   # Terminal
```

### Ver logs con formato bonito

```bash
# Usar jq para formatear logs JSON (si tu aplicación logea JSON)
docker-compose logs backend --no-log-prefix | jq

# Colorear output de logs
docker-compose logs backend 2>&1 | ccze -A
```

### Ver información del sistema

```bash
# Ver stats de uso de CPU/Memoria del backend
docker stats northernchile-backend-1

# Ver procesos corriendo dentro del contenedor
docker-compose exec backend ps aux
```

## 📊 Configuración de Logs

Los logs están configurados en `backend/src/main/resources/application.properties`:

- **Nivel de log para tu código:** `DEBUG` (paquete `com.northernchile`)
- **Nivel de log general:** `INFO`
- **SQL queries:** `DEBUG` (activo en desarrollo)
- **Tamaño máximo por archivo:** 10MB
- **Historial:** 30 archivos
- **Tamaño total máximo:** 1GB

### Cambiar nivel de logs temporalmente

Puedes cambiar el nivel de log sin reiniciar el servidor usando Spring Boot Actuator:

```bash
# Cambiar nivel de log a TRACE para debugging intenso
curl -X POST http://localhost:8080/actuator/loggers/com.northernchile \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "TRACE"}'

# Volver a DEBUG
curl -X POST http://localhost:8080/actuator/loggers/com.northernchile \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'
```

## 🐛 Debugging de Errores Específicos

### Error del AlertController mencionado:

```bash
# Ver todos los logs relacionados con alerts
docker-compose logs backend 2>&1 | grep -i "alert"

# Ver stacktrace completo del error
docker-compose logs backend 2>&1 | grep -B 5 -A 30 "AlertController"
```

### Verificar conexión a base de datos:

```bash
docker-compose logs backend 2>&1 | grep -i "datasource"
```

### Ver todas las requests HTTP:

```bash
docker-compose logs backend 2>&1 | grep "HTTP"
```

## 💡 Tips

1. **Usa `-f` para seguir logs en tiempo real** mientras desarrollas
2. **Usa `grep` para filtrar** logs relevantes
3. **El archivo `application.log` persiste** en el volumen de Docker incluso si reinicias
4. **En producción**, los stacktraces NO se envían al frontend (solo en desarrollo)
5. **Usa `docker-compose down -v`** para limpiar todos los volúmenes (¡incluyendo logs!)

## 🔍 Troubleshooting

Si no ves logs:

```bash
# Verificar que el contenedor está corriendo
docker-compose ps

# Verificar que el volumen existe
docker volume ls | grep backend_logs

# Verificar logs del sistema Docker
docker-compose logs backend --tail=50
```
