<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getQuickLinks } from './api'

const route = useRoute()
const quickLinks = ref([])
const viewportWidth = ref(typeof window === 'undefined' ? 1440 : window.innerWidth)

const authPaths = ['/login', '/register']

const isAuthRoute = computed(() => authPaths.includes(route.path))

const quickLinksMode = computed(() => {
  if (!quickLinks.value.length || isAuthRoute.value || viewportWidth.value < 960) {
    return 'hidden'
  }
  return viewportWidth.value < 1360 ? 'dock' : 'rail'
})

const showQuickLinks = computed(() => quickLinksMode.value !== 'hidden')

const handleResize = () => {
  viewportWidth.value = window.innerWidth
}

const fetchQuickLinks = async () => {
  try {
    const res = await getQuickLinks()
    quickLinks.value = res.data?.data || []
  } catch (e) {
    console.error('Failed to fetch quick links:', e)
  }
}

onMounted(() => {
  fetchQuickLinks()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<template>
  <aside v-if="showQuickLinks" class="global-quick-links" :class="`is-${quickLinksMode}`" aria-label="常用入口">
    <div class="gql-shell">
      <div class="gql-intro">
        <span class="gql-eyebrow">常去的地方</span>
        <p v-if="quickLinksMode === 'rail'" class="gql-note">顺手打开，不打断阅读节奏。</p>
      </div>

      <div class="gql-list">
        <a
          v-for="link in quickLinks"
          :key="link.id"
          :href="link.url"
          target="_blank"
          rel="noopener noreferrer"
          class="gql-item"
          :title="link.label"
        >
          <span class="gql-icon-wrap">
            <span class="gql-icon">{{ link.icon }}</span>
          </span>
          <span class="gql-label">{{ link.label }}</span>
        </a>
      </div>
    </div>
  </aside>

  <router-view v-slot="{ Component, route }">
    <Transition name="fade-slide" mode="out-in">
      <component :is="Component" :key="route.path" />
    </Transition>
  </router-view>
</template>

<style>
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition:
    opacity var(--duration-normal) var(--ease-out),
    transform var(--duration-slow) var(--ease-emphasis);
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(14px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.fade-slide-enter-to,
.fade-slide-leave-from {
  opacity: 1;
  transform: translateY(0);
}

.global-quick-links {
  position: fixed;
  z-index: 90;
  pointer-events: none;
}

.global-quick-links.is-rail {
  right: clamp(16px, 2vw, 28px);
  bottom: clamp(18px, 2.6vw, 30px);
}

.global-quick-links.is-dock {
  left: 50%;
  bottom: clamp(14px, 2vw, 22px);
  width: min(calc(100vw - 32px), 820px);
  transform: translateX(-50%);
}

.gql-shell {
  pointer-events: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 14px;
  background: color-mix(in srgb, var(--bg) 90%, white 10%);
  border: 1px solid color-mix(in srgb, var(--border-strong) 78%, white 22%);
  border-radius: 24px;
  box-shadow: 0 12px 30px rgba(109, 92, 72, 0.08);
}

.gql-intro {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 0 2px;
}

.gql-eyebrow {
  font-size: 10px;
  line-height: 1;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--text-muted);
}

.gql-note {
  max-width: 22ch;
  color: var(--text-muted);
  font-size: 12px;
  line-height: 1.55;
}

.gql-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.gql-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 16px;
  border: 1px solid transparent;
  color: var(--text);
  font-size: 13px;
  text-decoration: none;
  background: rgba(255, 255, 255, 0.78);
  transition:
    transform var(--duration-fast) var(--ease-out),
    color var(--duration-normal) var(--ease-out),
    background-color var(--duration-normal) var(--ease-out),
    border-color var(--duration-normal) var(--ease-out),
    box-shadow var(--duration-normal) var(--ease-out);
  white-space: nowrap;
}

.gql-item:hover {
  color: var(--accent);
  background: rgba(255, 255, 255, 0.96);
  border-color: color-mix(in srgb, var(--accent) 18%, var(--border-strong));
  box-shadow: var(--shadow-sm);
  transform: translateY(-1px);
}

.gql-icon-wrap {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  display: inline-grid;
  place-items: center;
  border-radius: 999px;
  background: color-mix(in srgb, var(--accent) 10%, white 90%);
}

.gql-icon {
  font-size: 15px;
  line-height: 1;
}

.gql-label {
  font-family: var(--heading);
  font-size: 12px;
  letter-spacing: 0.04em;
}

@media (max-width: 1360px) {
  .global-quick-links.is-dock .gql-shell {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    gap: 14px;
  }

  .global-quick-links.is-dock .gql-intro {
    flex-shrink: 0;
    justify-content: center;
    min-width: 88px;
  }

  .global-quick-links.is-dock .gql-list {
    flex: 1;
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: flex-end;
  }

  .global-quick-links.is-dock .gql-item {
    min-height: 42px;
  }
}

@media (max-width: 1120px) {
  .global-quick-links.is-dock .gql-shell {
    align-items: flex-start;
  }

  .global-quick-links.is-dock .gql-list {
    justify-content: flex-start;
  }
}

@media (prefers-reduced-motion: reduce) {
  .fade-slide-enter-active,
  .fade-slide-leave-active,
  .gql-item {
    transition-duration: 0.01ms !important;
  }
}
</style>
