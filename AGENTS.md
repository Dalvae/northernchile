# AGENTS.md - Guidelines for agentic coding agents

## Build/Lint/Test Commands
- Backend build: `docker compose exec backend mvn clean package -DskipTests`
- Backend test: `docker compose exec backend mvn test`
- Backend single test: `docker compose exec backend mvn -Dtest=ClassNameTest#methodName test`
- Backend compile check: `docker compose exec backend mvn compile test-compile`
- Frontend dev: `cd frontend && pnpm dev`
- Frontend build: `cd frontend && pnpm build`
- Frontend typecheck: `cd frontend && pnpm typecheck` (only when asked)
- Frontend lint: `cd frontend && pnpm lint .`
- Frontend test: `cd frontend && pnpm test` (Vitest); single: `pnpm vitest path/to/test.ts`
- Frontend E2E: `cd frontend && pnpm test:e2e` (Playwright)
- API client regen: `cd frontend && pnpm generate-api-client` (after OpenAPI changes)

## Code Style
- Frontend: Nuxt 3 + Composition API, `<script setup>`, strict TS (no implicit any)
- Backend: Spring Boot 3, packages com.northernchile.api.*, constructor injection
- Imports: Group/sort like existing; explicit, no wildcards/unused
- Formatting: 2-space indent, LF endings, 1TBS braces, no trailing commas
- Colors: Semantic only (primary/secondary/error/etc from app.config.ts); no hardcoded RGB/hex
- i18n: Locales es-CL/en-US/pt-BR for user text; no hardcoded strings
- Errors: Backend ResponseEntity/handlers; Frontend Nuxt UI toasts + typed results
- Security: No secret logs; JWT roles; owner_id filters (PARTNER_ADMIN)
- Patterns: Reuse composables/stores/utils; Tour vs TourSchedule (CLAUDE.md); UTable `cell` config
- Match nearby files/CLAUDE.md/TODO.md; no CI/Docker changes unless asked

## Git Policy
- NEVER run `git push` - user handles all pushes manually
- Commits are allowed; pushing is strictly forbidden
