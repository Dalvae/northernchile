# AGENTS.md - Guidelines for agentic coding agents

## Build/Lint/Test Commands
- Backend: `cd backend; mvn clean package; mvn test`
- Backend single test: `mvn -Dtest=ClassNameTest#methodName test` (run from backend/)
- Frontend dev: `cd frontend; pnpm dev`
- Frontend build: `cd frontend; pnpm build`
- Frontend typecheck: `cd frontend; pnpm typecheck` (only when asked)
- Frontend lint: `cd frontend; pnpm lint .`
- Frontend test: `cd frontend; pnpm test` (Vitest); single: `pnpm vitest path/to/test.ts`

## Code Style
- Frontend: Nuxt 3 + Composition API, &lt;script setup&gt;, strict TS (no implicit any)
- Backend: Spring Boot 3, packages com.northernchile.api.*, constructor injection
- Imports: Group/sort like existing; explicit, no wildcards/unused
- Formatting: .editorconfig/ESLint (1TBS, no trailing commas); no comments unless asked
- Colors: Semantic only (app.config.ts, THEMING.md); no hardcoded RGB/hex
- i18n: Locales es-CL/en-US/pt-BR for user text; no hardcoded strings
- Errors: Backend ResponseEntity/handlers; Frontend Nuxt UI toasts + typed results
- Security: No secret logs; JWT roles; owner_id filters (PARTNER_ADMIN)
- Patterns: Reuse composables/stores/utils; Tour vs TourSchedule (CLAUDE.md); UTable `cell` config
- Match nearby files/CLAUDE.md/TODO.md; no CI/Docker changes unless asked