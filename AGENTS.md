# AGENTS.md

## Repo Shape
- `frontend/` is the editable Vue 3 + Vite app. Main entry is `frontend/src/main.js`; routing lives in `frontend/src/router/index.js`.
- `backend/` is the Spring Boot 2.7 / Java 8 API (`backend/src/main/java/com/erwang/blog/BlogApplication.java`) using MyBatis-Plus + MySQL.
- Repo root `index.html`, `404.html`, and `assets/` are checked-in built static files. `.github/workflows/deploy.yml` publishes the repo root to GitHub Pages on `release-1.0`, while `frontend/dist/` is ignored. Do not patch root `assets/*.js` for app logic; edit `frontend/src/**` and only sync generated output to root intentionally.
- `deploy/` is the operational source of truth for ECS/Nginx/systemd deployment. `backend/sql/init.sql` bootstraps a clean database; incremental fixes belong in `backend/sql/patches/`.

## Commands
- Frontend dev: `cd frontend && npm install && npm run dev`
- Frontend verification: `cd frontend && npm run build`
- Backend local run: `cd backend && mvn spring-boot:run`
- Backend package verification: `cd backend && mvn -q -DskipTests package`
- There is no repo-wide task runner, no frontend lint/typecheck/test config, and no committed backend test sources right now. In practice, `npm run build` and `mvn -q -DskipTests package` are the normal verification steps.

## Integration Gotchas
- Frontend dev uses `frontend/.env.development`: `VITE_API_BASE=http://localhost:9090/api`, `VITE_FILE_BASE=http://localhost:9090`.
- Frontend prod uses `frontend/.env.production`: `VITE_API_BASE=/api`, `VITE_FILE_BASE=https://erwang.me`.
- Router uses `createWebHashHistory()` in `frontend/src/router/index.js`. Do not switch to history mode unless you also change static hosting and Nginx fallback behavior.
- Use `frontend/src/api/index.js` for API calls and file URLs. It owns the axios base URL, 401 redirect behavior, and `resolveFileUrl()`.
- Auth state depends on localStorage keys `token` and `userRole`; router guards and header/footer/admin UI all read those exact keys.
- Global quick links are loaded in `frontend/src/App.vue` from `/api/quick-links`; they are app-wide, not homepage-local.
- Backend responses are wrapped in `com.erwang.blog.common.Result<T>` with `code`, `message`, and `data`; frontend usually consumes `res.data?.data`.
- Local backend `dev` profile expects MySQL `blog` on `localhost:3306` with `root/root`. Production is env-driven from `deploy/erwang-backend.env.example`.
- Uploads go to `./uploads` in dev and `/www/server/erwang/uploads` in prod, and are served from `/api/files/{filename}`.

## Deploy Notes
- ECS deploy flow from repo docs: build `frontend/`, upload `frontend/dist/*` to `/www/wwwroot/erwang/`; package `backend/`, upload `backend/target/blog-backend-1.0.0.jar` to `/www/server/erwang/backend/app.jar`; restart `erwang`; smoke-test with `curl http://127.0.0.1:9090/api/articles`.
- Production service is `deploy/erwang.service`, which loads `/www/server/erwang/deploy/erwang-backend.env` and runs Java 8.
- Do not mix `systemctl restart erwang` with ad-hoc `nohup java -jar` in prod. The ops docs call out this exact cause of stale `9090` listeners and “new jar uploaded but old logic still running” failures.
