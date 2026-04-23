# Homepage Search Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the homepage search area in `frontend/src/views/ArticleList.vue` into an editorial-style discovery bar with a primary search input, category chip rail, contextual status row, and hidden-by-default time filtering.

**Architecture:** Keep the implementation inside `ArticleList.vue` to match current repo conventions, but simplify the interaction model by removing duplicate category entry points from the search bar, promoting category chips as the only visible category control, and moving time filtering behind a lightweight reveal. Preserve the existing backend query contract (`keyword`, `category`, `startDate`, `endDate`) while changing how those controls are surfaced and explained in the UI.

**Tech Stack:** Vue 3 `<script setup>`, Element Plus, scoped CSS in SFC, existing API helpers from `frontend/src/api/index.js`

---

## File Map

- **Modify:** `frontend/src/views/ArticleList.vue`
  - Owns homepage search state, fetch params, category chips, status text, empty state, and responsive layout.
- **Reference only:** `frontend/src/style.css`
  - Reuse spacing, shell, radius, transition, and color tokens already defined globally.
- **No backend changes**
  - Existing `getArticles()` query model already supports the needed filter params.

## Task 1: Simplify Search State Model

**Files:**
- Modify: `frontend/src/views/ArticleList.vue`

- [ ] **Step 1: Introduce explicit UI state for advanced filters and status formatting**

Add focused computed helpers near the existing search/filter state:

```js
const showAdvancedFilters = ref(false)

const activeFilterSummary = computed(() => {
  const parts = []

  if (selectedCategory.value) parts.push(selectedCategory.value)
  if (keyword.value.trim()) parts.push(keyword.value.trim())

  if (startDate.value && endDate.value) {
    parts.push(`${startDate.value} — ${endDate.value}`)
  }

  parts.push(`${total.value} 篇文章`)
  return parts.join(' · ')
})

const hasDateFilter = computed(() => Boolean(startDate.value && endDate.value))
```

- [ ] **Step 2: Keep the existing query contract untouched**

Do **not** change this API payload shape inside `fetchArticles()`:

```js
const res = await getArticles({
  page: currentPage.value,
  size: pageSize.value,
  keyword: keyword.value || undefined,
  category: selectedCategory.value || undefined,
  tag: selectedTag.value || undefined,
  startDate: startDate.value || undefined,
  endDate: endDate.value || undefined
})
```

Expected result: UI changes only; backend compatibility remains intact.

- [ ] **Step 3: Add a small toggle handler for advanced filters**

```js
const toggleAdvancedFilters = () => {
  showAdvancedFilters.value = !showAdvancedFilters.value
}
```

- [ ] **Step 4: Verify script section stays valid**

Run: `npm run build`

Expected: build completes successfully with no new Vue compile errors.

## Task 2: Replace the Existing Search Row with Editorial Structure

**Files:**
- Modify: `frontend/src/views/ArticleList.vue`

- [ ] **Step 1: Replace the current search bar markup**

Replace the current combined input/select/date/button block with this structure:

```vue
<section class="discovery-bar">
  <div class="discovery-search-row">
    <el-input
      v-model="keyword"
      class="discovery-search-input"
      placeholder="搜索文章"
      clearable
      @keyup.enter="handleSearch"
      @clear="handleSearch"
    >
      <template #prefix>
        <span class="search-icon">⌕</span>
      </template>
    </el-input>

    <el-button class="search-btn" @click="handleSearch">搜索</el-button>
  </div>

  <div class="discovery-chip-row" role="tablist" aria-label="文章分类">
    <button
      type="button"
      class="discovery-chip"
      :class="{ active: !selectedCategory }"
      @click="handleCategoryFilter('')"
    >
      全部
    </button>
    <button
      v-for="category in categories"
      :key="category"
      type="button"
      class="discovery-chip"
      :class="{ active: selectedCategory === category }"
      @click="handleCategoryFilter(category)"
    >
      {{ category }}
    </button>
  </div>

  <div class="discovery-status-row">
    <p class="discovery-status-copy">{{ activeFilterSummary }}</p>
    <div class="discovery-status-actions">
      <button type="button" class="status-link" @click="toggleAdvancedFilters">
        {{ showAdvancedFilters ? '收起筛选' : '更多筛选' }}
      </button>
      <button v-if="hasActiveFilters" type="button" class="status-link" @click="clearAllFilters">
        清除
      </button>
    </div>
  </div>

  <div v-if="showAdvancedFilters" class="advanced-filter-panel">
    <span class="advanced-filter-label">时间</span>
    <div class="advanced-filter-shortcuts">
      <button type="button" class="shortcut-chip">近 30 天</button>
      <button type="button" class="shortcut-chip">近半年</button>
    </div>
    <el-date-picker
      v-model="dateRange"
      type="daterange"
      class="advanced-date-picker"
      range-separator="—"
      start-placeholder="开始"
      end-placeholder="结束"
      value-format="YYYY-MM-DD"
      @change="handleDateChange"
    />
  </div>
</section>
```

- [ ] **Step 2: Remove the category `<el-select>` from the search area**

Delete the old duplicate control:

```vue
<el-select v-model="selectedCategory" class="search-filter" placeholder="分类" @change="handleSearch">
  <el-option label="全部分类" value="" />
  <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
</el-select>
```

Expected result: category is controlled only by the chip rail in the main discovery area.

- [ ] **Step 3: Keep sidebar category duplication from competing visually**

If the sidebar category block remains temporarily, do **not** show the same category interaction style as the main discovery chips. Prefer one of these minimal options during implementation:

```vue
<section class="sidebar-section sidebar-section--secondary">
  <h2 class="sidebar-title">标签</h2>
  ...
</section>
```

Or remove the sidebar category section entirely if the page still reads cleanly.

- [ ] **Step 4: Build after template replacement**

Run: `npm run build`

Expected: build passes and generated bundle includes updated `ArticleList` assets.

## Task 3: Add Advanced Time Filter Shortcuts

**Files:**
- Modify: `frontend/src/views/ArticleList.vue`

- [ ] **Step 1: Add shortcut handlers in script**

```js
const applyRelativeDateRange = (days) => {
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - days)

  const format = (date) => date.toISOString().slice(0, 10)

  startDate.value = format(start)
  endDate.value = format(end)
  dateRange.value = [startDate.value, endDate.value]
  currentPage.value = 1
  fetchArticles()
}
```

- [ ] **Step 2: Wire shortcuts into template**

Replace the placeholder shortcut buttons with real handlers:

```vue
<button type="button" class="shortcut-chip" @click="applyRelativeDateRange(30)">近 30 天</button>
<button type="button" class="shortcut-chip" @click="applyRelativeDateRange(180)">近半年</button>
```

- [ ] **Step 3: Ensure clear/reset also collapses advanced state when appropriate**

Update `clearAllFilters()` so it clears the time range and resets disclosure state:

```js
const clearAllFilters = () => {
  keyword.value = ''
  selectedCategory.value = ''
  selectedTag.value = ''
  startDate.value = ''
  endDate.value = ''
  showAdvancedFilters.value = false
  currentPage.value = 1
  fetchArticles()
}
```

- [ ] **Step 4: Verify date shortcut flow manually**

Run: `npm run dev`

Manual check:
- open homepage
- click `更多筛选`
- click `近 30 天`
- confirm result summary includes time range and list refreshes

Expected: no console errors, date shortcut updates results immediately.

## Task 4: Rewrite Search-Area Styling for Editorial Feel

**Files:**
- Modify: `frontend/src/views/ArticleList.vue`

- [ ] **Step 1: Remove old `.search-bar` and `.search-filter` styling block**

Delete or replace these selectors:

```css
.search-bar { ... }
.search-bar .el-input { ... }
.search-bar .search-filter { ... }
```

- [ ] **Step 2: Add discovery bar styles**

Add a new styling block like this:

```css
.discovery-bar {
  display: grid;
  gap: clamp(12px, 1.8vw, 18px);
  margin-bottom: clamp(18px, 2.4vw, 28px);
}

.discovery-search-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
}

.discovery-chip-row {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 2px;
}

.discovery-chip {
  border: 1px solid var(--border);
  border-radius: 999px;
  padding: 8px 14px;
  background: #fff;
  color: var(--text);
  white-space: nowrap;
  transition: transform var(--duration-fast) var(--ease-out), border-color var(--duration-fast) var(--ease-out), background-color var(--duration-fast) var(--ease-out);
}

.discovery-chip.active {
  color: var(--accent);
  border-color: var(--accent);
  background: var(--accent-bg);
}

.discovery-status-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}
```

- [ ] **Step 3: Add advanced panel and status link styles**

```css
.advanced-filter-panel {
  display: grid;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  background: #fff;
}

.status-link {
  border: none;
  background: transparent;
  color: var(--text-muted);
  padding: 0;
}

.status-link:hover {
  color: var(--accent);
}
```

- [ ] **Step 4: Preserve reduced-motion behavior**

Extend the existing reduced motion block:

```css
@media (prefers-reduced-motion: reduce) {
  .discovery-chip,
  .status-link {
    transition: none;
  }
}
```

## Task 5: Tune Mobile and Narrow Desktop Behavior

**Files:**
- Modify: `frontend/src/views/ArticleList.vue`

- [ ] **Step 1: Add responsive rules for the discovery bar**

Inside the existing mobile breakpoint, add:

```css
@media (max-width: 768px) {
  .discovery-search-row {
    grid-template-columns: 1fr;
  }

  .discovery-status-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .discovery-status-actions {
    width: 100%;
    display: flex;
    justify-content: space-between;
  }

  .advanced-date-picker {
    width: 100%;
  }
}
```

- [ ] **Step 2: Make the chip rail feel intentional on touch**

Add:

```css
.discovery-chip-row::-webkit-scrollbar {
  display: none;
}

.discovery-chip-row {
  scrollbar-width: none;
  -webkit-overflow-scrolling: touch;
}
```

- [ ] **Step 3: Verify desktop and mobile layouts manually**

Run: `npm run dev`

Manual check at these widths:
- 1440px: search row should read as editorial toolbar, not admin form
- 1024px: chips should still scan comfortably
- 390px: search button stacks, chips scroll horizontally, advanced filters remain usable

Expected: no overflow, no cramped multi-control row, no duplicate visible category controls fighting for attention.

## Task 6: Final Verification and Handoff

**Files:**
- Modify: `frontend/src/views/ArticleList.vue`

- [ ] **Step 1: Run production build**

Run: `npm run build`

Expected: successful Vite build.

- [ ] **Step 2: Check user-facing behaviors manually**

Manual checklist:
- keyword search works via Enter and button
- category chip click refreshes results
- `清除` resets keyword/category/date and hides advanced panel
- `更多筛选` reveals time controls without shifting the page awkwardly
- empty state copy reads naturally after search with no results

- [ ] **Step 3: Sanity-check visual hierarchy against the spec**

Confirm all of the following are true:

- the search input is visually dominant
- category is a chip rail, not a select dropdown
- time filter is hidden by default
- status row explains the active filters
- mobile layout adapts rather than merely wraps the old toolbar

- [ ] **Step 4: Prepare final review summary**

Summarize implementation in this structure:

```md
- What changed in `frontend/src/views/ArticleList.vue`
- How the duplicate category interaction was removed or weakened
- How advanced time filtering now works
- Build result
- Manual viewport checks performed
```

## Spec Coverage Check

- **主搜索框** → Tasks 1, 2, 4, 6
- **分类胶囊带** → Tasks 2, 4, 5
- **状态条** → Tasks 1, 2, 4, 6
- **时间筛选默认隐藏** → Tasks 1, 3, 4, 5
- **更克制高级的文案语气** → Tasks 2 and 6 manual copy review
- **去掉重复分类入口** → Task 2 and Task 5 verification
- **响应式适配** → Task 5
- **保留现有接口模型** → Task 1

## Risks and Controls

- **Risk:** sidebar category and main chips still feel duplicated
  - **Control:** remove sidebar category block or visually demote it during implementation review
- **Risk:** advanced time filter adds layout jump on mobile
  - **Control:** keep it inside a compact bordered panel directly under the status row
- **Risk:** summary text becomes noisy with long keywords or date ranges
  - **Control:** keep summary short and truncate with CSS if needed during implementation

## Completion Criteria

- Homepage search area reads as a consumer-facing discovery entry, not an admin filter bar
- Category selection is no longer duplicated in both select and chip form inside the main search area
- Time filtering is hidden by default and only appears on demand
- Build passes and viewport checks succeed
