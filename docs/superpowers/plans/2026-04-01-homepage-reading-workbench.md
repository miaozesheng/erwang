# Homepage Reading Workbench Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the homepage, article detail page, and profile entry flow into a C-end-friendly reading workbench with real data, wider reading space, persistent right-side quick links, and multi-article switching support.

**Architecture:** Move homepage support modules to the left column, keep the center focused on reading, and reserve the right side for fixed global quick links only. Add a detail-page reading workbench that manages opened-article tabs and related article navigation, while restoring profile-area navigation as an integrated user workspace.

**Tech Stack:** Vue 3, Vue Router, Element Plus, Axios, Spring Boot 2.7, MyBatis-Plus, MySQL.

---

## File Structure

- Modify: `frontend/src/views/ArticleList.vue` — homepage layout, real stats blocks, left-column modules, article card data usage
- Modify: `frontend/src/views/ArticleDetail.vue` — reading workbench, opened article tabs, related/recent reading rail, real views display
- Modify: `frontend/src/views/Profile.vue` — user workspace navigation, logout action, route-entry grouping
- Modify: `frontend/src/App.vue` — global quick links fixed-right behavior stays authoritative
- Modify: `frontend/src/components/ArticleCard.vue` — expose/read views consistently in list cards
- Modify: `frontend/src/api/index.js` — add any missing endpoints for related/opened article support if needed
- Modify: `backend/src/main/java/com/erwang/blog/controller/ArticleController.java` — provide reliable total/views/related article support if missing
- Modify: `backend/src/main/java/com/erwang/blog/controller/AdminController.java` — ensure stats endpoints reflect real article counts
- Modify: `backend/src/main/java/com/erwang/blog/controller/QuickLinkController.java` — preserve editable quick link flow
- Modify: `backend/src/main/java/com/erwang/blog/service/impl/ArticleServiceImpl.java` — real sorting/aggregation support for homepage/detail
- Test/verify: frontend production build, backend package, online workflow checks

---

### Task 1: Stabilize real data sources for homepage stats and quick links

**Files:**
- Modify: `backend/src/main/java/com/erwang/blog/service/impl/ArticleServiceImpl.java`
- Modify: `backend/src/main/java/com/erwang/blog/controller/ArticleController.java`
- Modify: `backend/src/main/java/com/erwang/blog/controller/AdminController.java`
- Modify: `backend/src/main/java/com/erwang/blog/controller/QuickLinkController.java` (only if response needs shaping)
- Test: manual curl verification against `/api/articles`, `/api/admin/stats`, `/api/quick-links`

- [ ] **Step 1: Verify current article total/views payload behavior from code**

Read and confirm what `ArticleController.list()` returns for `total`, what `ArticleServiceImpl.listPage()` uses for pagination, and whether homepage stats can reuse `/api/admin/stats` or need article-list aggregation.

- [ ] **Step 2: Ensure article list response returns reliable total and views**

Make sure the list endpoint always returns:
- `total`
- `page`
- `size`
- per-article `views`

Do not rely on fallback `articles.length` when true totals exist.

- [ ] **Step 3: Ensure homepage can obtain real “hot articles” ordering**

Support sorting by `views` for the homepage side module using existing article data, or add a small backend helper if the current list endpoint cannot support stable hot-article ordering.

- [ ] **Step 4: Ensure quick links endpoint returns editable, ordered, non-deleted links**

Confirm `/api/quick-links` returns ordered live data from `quick_link` with deleted records excluded.

- [ ] **Step 5: Run backend package verification**

Run:

```bash
cd backend && mvn -q -DskipTests package
```

Expected: package succeeds.

---

### Task 2: Redesign homepage into left-workbench + center-reading + right-fixed-links

**Files:**
- Modify: `frontend/src/views/ArticleList.vue`
- Modify: `frontend/src/components/ArticleCard.vue`
- Test: frontend build + browser verification on home page

- [ ] **Step 1: Refactor homepage data derivation**

Make `ArticleList.vue` compute:
- total article count from real API total
- featured article count from real article set
- category counts from actual article/category relationships
- hot articles from real `views`

- [ ] **Step 2: Move homepage support modules to the left column**

The left column should contain:
- 博客概览
- 分类统计
- 热门文章

The right column should no longer contain these modules.

- [ ] **Step 3: Keep center column reading-first**

Ensure the center area is wider than current and remains focused on:
- hero
- search/filter
- article list
- pagination

- [ ] **Step 4: Keep global quick links as fixed right-side tool rail**

Do not merge quick links into homepage content columns. Keep `App.vue` quick links fixed at the viewport middle-right.

- [ ] **Step 5: Add real views display to article cards/home modules**

Update `ArticleCard.vue` and any homepage hot-article display blocks so views are visible in a C-end-friendly way.

- [ ] **Step 6: Run frontend build verification**

Run:

```bash
cd frontend && npm run build
```

Expected: build succeeds.

---

### Task 3: Restore profile page as a real user workspace

**Files:**
- Modify: `frontend/src/views/Profile.vue`
- Test: browser navigation among `/profile`, `/favorites`, `/likes`, `/admin`

- [ ] **Step 1: Add profile workspace navigation cluster**

Place a visible navigation group near the profile hero containing:
- 个人资料
- 我的收藏
- 我的喜欢
- 管理后台 (admin only)
- 退出登录

- [ ] **Step 2: Ensure current route is visually active**

Whichever workspace section the user is currently on should be highlighted clearly.

- [ ] **Step 3: Ensure logout is explicit and immediate**

Logout must clear local auth state and redirect predictably.

- [ ] **Step 4: Verify the page no longer feels like a stripped single card**

Check that profile now acts as a gateway to the user workspace, not an isolated form page.

---

### Task 4: Build article detail reading workbench with in-page article tabs

**Files:**
- Modify: `frontend/src/views/ArticleDetail.vue`
- Modify: `frontend/src/api/index.js` (if helper fetches needed)
- Possibly modify: `backend/src/main/java/com/erwang/blog/controller/ArticleController.java` (if related article endpoint or payload enhancement is needed)
- Test: browser verification on detail page with multiple article opens

- [ ] **Step 1: Design left reading workbench structure in detail view**

The left side should include:
- opened article tabs
- related/recent reading list
- table of contents

- [ ] **Step 2: Implement in-page opened-article tab state**

When the user opens another article from the workbench, preserve it as a tab in the current reading session and allow switching between tabs without losing the workbench context.

- [ ] **Step 3: Keep center area focused on the current article body**

Ensure the content area stays dominant and wide enough for reading.

- [ ] **Step 4: Surface real metadata in detail page**

Make sure detail page visibly includes real reading metrics such as views.

- [ ] **Step 5: Verify multi-article switching works predictably**

Open multiple articles, switch between them, confirm the active tab, title, content, and TOC update correctly.

---

### Task 5: Seed and preserve editable quick links

**Files:**
- Modify: `backend/sql/init.sql` (if needed)
- Modify: `backend/sql/patches/2026-04-01-seed-quick-links.sql`
- Verify: `frontend/src/App.vue`, `frontend/src/views/Admin.vue`

- [ ] **Step 1: Ensure seed SQL provides default links for empty environments**

Default quick links should exist on fresh deployments.

- [ ] **Step 2: Preserve admin editability**

Verify the admin quick-link management still edits the same data source used by `App.vue`.

- [ ] **Step 3: Confirm the global quick links remain fixed and visible on desktop**

Do not let homepage/detail layout changes break the global tool rail.

---

### Task 6: End-to-end verification of the redesigned reading experience

**Files:**
- Verify: homepage, profile, favorites, likes, admin, article detail, global quick links

- [ ] **Step 1: Verify homepage information architecture**

Confirm:
- left column contains blog overview, category stats, hot articles
- center column is wider and reading-first
- right side only shows fixed quick links

- [ ] **Step 2: Verify profile workspace navigation**

Confirm navigation items are visible and route correctly.

- [ ] **Step 3: Verify article detail workbench**

Confirm opened article tabs, related reading, and TOC coexist and the user can switch articles in-page.

- [ ] **Step 4: Verify real data integrity**

Confirm article totals, hot article ordering, quick links, and views all reflect real backend data.

- [ ] **Step 5: Final build checks**

Run:

```bash
cd frontend && npm run build
cd backend && mvn -q -DskipTests package
```

Expected: both succeed.
