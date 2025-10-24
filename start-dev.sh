#!/bin/bash

# Colores para la salida
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${GREEN}Iniciando servicios de backend y base de datos con Docker...${NC}"

# Iniciar Docker Compose en segundo plano (-d para 'detached')
docker-compose up -d

echo -e "\n${GREEN}Servicios de fondo iniciados.${NC}"
echo -e "${BLUE}Puedes ver sus logs con: docker-compose logs -f${NC}"

echo -e "\n${GREEN}Iniciando servidor de desarrollo del frontend...${NC}"
echo -e "${BLUE}Navega a http://localhost:3000 cuando esté listo.${NC}"

# Navegar a la carpeta del frontend y ejecutar el servidor de desarrollo
cd frontend
pnpm run dev
