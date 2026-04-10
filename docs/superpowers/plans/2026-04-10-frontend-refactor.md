# 博客前端页面重构实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将博客全部页面从当前 neon cyber 美学重构为 light-brand 个人博客风格，提升阅读体验和视觉精致度

**Architecture:** 基于现有 Vue 3 + Vite + Element Plus 技术栈，重构采用渐进式，每个页面独立重构但保持设计一致性

**Tech Stack:** Vue 3, Vite, Element Plus, CSS Variables

---

## 重构范围与设计方向

### 设计原则（来自 .impeccable.md）
- Reading first: 优先排版、层次、扫描体验
- Personal, not dashboard: 不像控制面板
- Fewer containers, stronger structure: 减少嵌套卡片
- Expressive restraint: 通过语气和细节保持个性
- Mobile is primary: 必须适配移动端

### 设计方向
- 色调：温暖中性调，避免 neon cyber 风格
- 排版：更强的字体层次，更舒适的阅读体验
- 布局：减少卡片嵌套，用间距和节奏构建结构
- 交互：更克制的动画，专注于关键状态变化

---

## Task 1: 重构全局样式变量 (style.css)

**Files:**
- Modify: `frontend/src/style.css`

- [ ] **Step 1: 重新定义配色方案**

替换现有的 neon cyber 配色为温暖中性调：

```css
:root {
  /* 基础色：温暖中性调 */
  --bg: #faf9f7;
  --bg-elevated: #f5f3ef;
  --bg-surface: rgba(245, 243, 239, 0.8);
  --bg-grid-line: rgba(45, 90, 74, 0.04);
  --border: #e5e2dc;
  --border-accent: rgba(45, 90, 74, 0.2);
  --code-bg: #f0ede8;
  --card-bg: #ffffff;
  --social-bg: #f8f6f3;

  /* 文字：柔和层次 */
  --text: #5a5a5a;
  --text-h: #1a1a1a;
  --text-dim: rgba(90, 90, 90, 0.5);

  /* 品牌色：不再使用 neon cyan */
  --accent: #2d5a4a;
  --accent-hover: #3d7a64;
  --accent-bg: rgba(45, 90, 74, 0.08);
  --accent-border: rgba(45, 90, 74, 0.3);
  --accent-glow: rgba(45, 90, 74, 0.15);
  --accent-secondary: #6a9a7a;

  /* 阴影：更柔和 */
  --shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  --shadow-lg: 0 4px 24px rgba(0, 0, 0, 0.08);
  --shadow-glow: 0 2px 12px rgba(45, 90, 74, 0.1);

  /* 暗色模式 */
  color-scheme: light;
}
```

- [ ] **Step 2: 修改 body 背景**

```css
body {
  background:
    radial-gradient(circle at 15% 5%, rgba(45, 90, 74, 0.05), transparent 50%),
    radial-gradient(circle at 85% 90%, rgba(106, 154, 122, 0.04), transparent 55%),
    linear-gradient(160deg, #f8f6f3 0%, #faf9f7 42%, #f5f3ef 100%);
}
```

- [ ] **Step 3: 修改 body::before 网格背景**

```css
body::before {
  background-image:
    linear-gradient(var(--bg-grid-line) 1px, transparent 1px),
    linear-gradient(90deg, var(--bg-grid-line) 1px, transparent 1px);
  background-size: 40px 40px;
  mask-image: radial-gradient(circle at 50% 20%, black 10%, transparent 70%);
  opacity: 0.5;
}
```

- [ ] **Step 4: 修改 selection 颜色**

```css
::selection {
  background: rgba(45, 90, 74, 0.2);
  color: #1a1a1a;
}
```

- [ ] **Step 5: 简化 Element Plus 组件覆盖样式**

移除 neon glow 效果，使用更简洁的样式：

```css
.el-button--primary {
  --el-button-bg-color: var(--accent);
  --el-button-border-color: var(--accent);
  --el-button-hover-bg-color: var(--accent-hover);
  --el-button-hover-border-color: var(--accent-hover);
  --el-button-text-color: #ffffff;
}

.el-input__wrapper {
  background: #ffffff !important;
  border: 1px solid var(--border) !important;
}

.el-input__wrapper.is-focus {
  border-color: var(--accent) !important;
  box-shadow: 0 0 0 2px rgba(45, 90, 74, 0.1) !important;
}

.el-input__inner {
  color: var(--text-h) !important;
}

.el-textarea__inner {
  background: #ffffff !important;
  border: 1px solid var(--border) !important;
  color: var(--text-h) !important;
}

.el-tag {
  --el-tag-bg-color: var(--accent-bg);
  --el-tag-border-color: var(--accent-border);
  --el-tag-text-color: var(--accent);
}
```

- [ ] **Step 6: 移除 neon 风格的下拉菜单样式**

替换为简洁的 light 风格：

```css
.el-select-dropdown {
  background: #ffffff !important;
  border: 1px solid var(--border) !important;
  box-shadow: var(--shadow-lg) !important;
}

.el-select-dropdown__item {
  color: var(--text) !important;
}

.el-select-dropdown__item.is-hovering {
  background: var(--accent-bg) !important;
  color: var(--accent) !important;
}

.el-select-dropdown__item.is-selected {
  color: var(--accent) !important;
  font-weight: 600 !important;
}

.el-dialog {
  --el-dialog-bg-color: #ffffff;
  border: 1px solid var(--border);
  box-shadow: var(--shadow-lg);
}
```

- [ ] **Step 7: 修改消息提示样式**

```css
.el-message {
  background: #ffffff !important;
  border: 1px solid var(--border) !important;
  box-shadow: var(--shadow-lg) !important;
}

.el-message .el-message__content {
  color: var(--text-h) !important;
}
```

- [ ] **Step 8: 提交**

---

## Task 2: 重构 Header 组件

**Files:**
- Modify: `frontend/src/components/Header.vue`

- [ ] **Step 1: 读取现有 Header.vue 样式部分**

提取所有 `<style scoped>` 中的样式定义

- [ ] **Step 2: 重构 Header 样式**

```css
.site-header {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border);
  padding: 12px 0;
}

.nav-link {
  color: var(--text);
  background: transparent;
  border: none;
}

.nav-link:hover,
.nav-link.active {
  color: var(--accent);
  background: var(--accent-bg);
}

.login-btn {
  background: var(--accent);
  color: #ffffff;
  border: none;
}

.login-btn:hover {
  background: var(--accent-hover);
}
```

- [ ] **Step 3: 移除 neon glow 效果**

删除 logo hover 的 drop-shadow glow 效果

- [ ] **Step 4: 提交**

---

## Task 3: 重构 ArticleList (首页)

**Files:**
- Modify: `frontend/src/views/ArticleList.vue`

- [ ] **Step 1: 读取现有 ArticleList.vue 样式部分**

- [ ] **Step 2: 重构 Hero Section**

移除 neon 渐变，使用简洁的白色卡片：

```css
.hero-section {
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
}
```

- [ ] **Step 3: 重构侧边栏**

```css
.sidebar-panel {
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
}

.sidebar-pill {
  border: 1px solid var(--border);
  background: transparent;
  color: var(--text);
}

.sidebar-pill:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.sidebar-pill.active {
  background: var(--accent-bg);
  border-color: var(--accent);
  color: var(--accent);
}
```

- [ ] **Step 4: 重构时间线样式**

移除 neon dot 效果，使用简洁的圆点：

```css
.timeline-dot {
  background: var(--accent);
  box-shadow: none;
}

.timeline-row:hover .timeline-dot {
  transform: scale(1.1);
}
```

- [ ] **Step 5: 提交**

---

## Task 4: 重构 ArticleDetail (文章详情)

**Files:**
- Modify: `frontend/src/views/ArticleDetail.vue`

- [ ] **Step 1: 读取现有样式**

- [ ] **Step 2: 重构文章容器**

```css
.article-container {
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  padding: 40px;
}
```

- [ ] **Step 3: 重构标题样式**

```css
.article-title {
  color: var(--text-h);
  font-weight: 700;
}
```

- [ ] **Step 4: 提交**

---

## Task 5: 重构 About 页面

**Files:**
- Modify: `frontend/src/views/About.vue`

- [ ] **Step 1: 读取现有样式**

- [ ] **Step 2: 重构 About 页面样式**

使用更舒适的阅读布局，减少装饰元素

- [ ] **Step 3: 提交**

---

## Task 6: 重构 Login/Register 页面

**Files:**
- Modify: `frontend/src/views/Login.vue`
- Modify: `frontend/src/views/Register.vue`

- [ ] **Step 1: 读取现有样式**

- [ ] **Step 2: 重构登录注册容器**

```css
.auth-container {
  background: #ffffff;
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
}

.auth-card {
  background: transparent;
}
```

- [ ] **Step 3: 提交**

---

## Task 7: 重构 Profile (个人中心)

**Files:**
- Modify: `frontend/src/views/Profile.vue`

- [ ] **Step 1: 读取现有样式**

- [ ] **Step 2: 重构 Profile 样式**

- [ ] **Step 3: 提交**

---

## Task 8: 重构 MyCollection (收藏/喜欢)

**Files:**
- Modify: `frontend/src/views/MyCollection.vue`

- [ ] **Step 1: 读取现有样式**

- [ ] **Step 2: 重构样式**

- [ ] **Step 3: 提交**

---

## Task 9: 重构 Admin (管理后台)

**Files:**
- Modify: `frontend/src/views/Admin.vue`

- [ ] **Step 1: 读取现有样式**

- [ ] **Step 2: 重构管理后台样式**

- [ ] **Step 3: 提交**

---

## Task 10: 重构 AdminArticle (文章编辑)

**Files:**
- Modify: `frontend/src/views/AdminArticle.vue`

- [ ] **Step 1: 读取现有样式**

- [ ] **Step 2: 重构编辑器样式**

- [ ] **Step 3: 提交**

---

## Task 11: 重构 GithubTrending 页面

**Files:**
- Modify: `frontend/src/views/GithubTrending.vue`

- [ ] **Step 1: 读取现有样式**

- [ ] **Step 2: 重构 GitHub 页面样式**

- [ ] **Step 3: 提交**

---

## Task 12: 重构 NotFound 页面

**Files:**
- Modify: `frontend/src/views/NotFound.vue`

- [ ] **Step 1: 读取现有样式**

- [ ] **Step 2: 重构 404 样式**

- [ ] **Step 3: 提交**

---

## 执行方式

**Plan complete and saved to `docs/superpowers/plans/2026-04-10-frontend-refactor.md`. 两个执行选项：**

**1. Subagent-Driven (推荐)** - 每个任务由独立子代理执行，任务间进行审查，快速迭代

**2. Inline Execution** - 在当前会话中批量执行，带审查检查点

**你想选择哪种方式？**