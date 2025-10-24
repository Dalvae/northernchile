# Stage 1: Construir la app Nuxt
FROM node:18-alpine AS builder
WORKDIR /app
COPY package.json pnpm-lock.yaml ./
# Usamos pnpm para instalar y construir
RUN npm install -g pnpm && pnpm install
COPY . .
RUN pnpm build

# Stage 2: Crear la imagen final para servir
FROM node:18-alpine
WORKDIR /app
# Copiamos solo el resultado de la construcción
COPY --from=builder /app/.output ./.output
EXPOSE 3000
# El comando para iniciar el servidor de Nuxt en producción
CMD ["node", ".output/server/index.mjs"]
