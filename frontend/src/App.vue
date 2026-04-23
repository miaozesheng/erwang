<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getQuickLinks } from './api'

const route = useRoute()
const quickLinks = ref([])

const quickLinksVisiblePaths = new Set(['/', '/github', '/favorites', '/likes', '/profile'])
const authPaths = new Set(['/login', '/register'])

const isAuthRoute = computed(() => authPaths.has(route.path))
const showQuickLinks = computed(() => !isAuthRoute.value && quickLinksVisiblePaths.has(route.path) && quickLinks.value.length > 0)

const fetchQuickLinks = async () => {
  try {
    const res = await getQuickLinks()
    quickLinks.value = res.data?.data || []
  } catch (error) {
    console.error('Failed to fetch quick links:', error)
  }
}

onMounted(() => {
  fetchQuickLinks()
})
</script>

<template>
  <div class="app-shell" :class="{ 'has-quick-links': showQuickLinks }">
    <section v-if="showQuickLinks" class="global-quick-links" aria-label="常用入口">
      <div class="global-quick-links__inner">
        <div class="global-quick-links__intro">
          <span class="global-quick-links__eyebrow">常用入口</span>
          <p class="global-quick-links__note">常去的地方单独收纳，不挤占正文，也不打断阅读。</p>
        </div>

        <div class="global-quick-links__list" role="list">
          <a
            v-for="link in quickLinks"
            :key="link.id"
            :href="link.url"
            target="_blank"
            rel="noopener noreferrer"
            class="quick-link-chip"
            :title="link.label"
            role="listitem"
          >
            <span class="quick-link-chip__icon" aria-hidden="true">{{ link.icon }}</span>
            <span class="quick-link-chip__label">{{ link.label }}</span>
          </a>
        </div>
      </div>
    </section>

    <router-view v-slot="{ Component, route: currentRoute }">
      <Transition name="fade-slide" mode="out-in">
        <component :is="Component" :key="currentRoute.path" />
      </Transition>
    </router-view>
  </div>
</template>

<style>
.app-shell {
  min-height: 100vh;
}

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
  top: 50%;
  right: clamp(12px, 2vw, 24px);
  width: min(188px, calc(100vw - 24px));
  z-index: 60;
  z-index: 60;
  padding: 0;
  pointer-events: none;
  transform: translateY(-50%);
}

.global-quick-links__inner {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: stretch;
}

.global-quick-links__intro,
.global-quick-links__list {
  pointer-events: auto;
}

.global-quick-links__intro {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 14px 14px 12px;
  border: 1px solid color-mix(in srgb, var(--border-strong) 82%, white 18%);
  border-radius: 22px;
  background: color-mix(in srgb, var(--bg) 92%, white 8%);
  box-shadow: 0 10px 24px rgba(109, 92, 72, 0.06);
}

.global-quick-links__eyebrow {
  font-size: 10px;
  line-height: 1;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--text-muted);
}

.global-quick-links__note {
  margin: 0;
  color: var(--text-muted);
  font-size: 12px;
  line-height: 1.6;
}

.global-quick-links__list {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 10px;
}

.quick-link-chip {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 46px;
  width: 100%;
  box-sizing: border-box;
  padding: 0 16px;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--border-strong) 82%, white 18%);
  background: rgba(255, 255, 255, 0.9);
  color: var(--text);
  text-decoration: none;
  box-shadow: 0 8px 18px rgba(109, 92, 72, 0.05);
  transition:
    transform var(--duration-fast) var(--ease-out),
    border-color var(--duration-normal) var(--ease-out),
    color var(--duration-normal) var(--ease-out),
    background-color var(--duration-normal) var(--ease-out),
    box-shadow var(--duration-normal) var(--ease-out);
}

.quick-link-chip:hover {
  color: var(--accent);
  border-color: color-mix(in srgb, var(--accent) 24%, var(--border-strong));
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 10px 24px rgba(109, 92, 72, 0.08);
  transform: translateY(-1px);
}

.quick-link-chip__icon {
  display: inline-grid;
  place-items: center;
  width: 26px;
  height: 26px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--accent) 10%, white 90%);
  font-size: 14px;
  line-height: 1;
}

.quick-link-chip__label {
  max-width: 12ch;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: var(--heading);
  font-size: 12px;
  letter-spacing: 0.04em;
}

@media (min-width: 1025px) {
  .app-shell.has-quick-links .page-container > .main-content {
    max-width: min(
      var(--page-shell-max, var(--shell-default)),
      calc(100vw - var(--quick-links-reserve) - (var(--page-gutter) * 2) - 16px)
    );
    margin-right: clamp(176px, 14vw, 236px);
  }
}

@media (max-width: 1024px) {
  .global-quick-links {
    top: auto;
    right: 0;
    bottom: 0;
    left: 0;
    width: auto;
    padding: 0;
    transform: translateY(0);
    background: color-mix(in srgb, var(--bg) 92%, white 8%);
    box-shadow: 0 -4px 12px rgba(109, 92, 72, 0.06);
  }

  .global-quick-links__inner {
    padding: 10px clamp(14px, 2.8vw, 32px);
    gap: 10px;
  }

  .global-quick-links__intro {
    display: none;
  }

  .global-quick-links__list {
    flex-direction: row;
    flex-wrap: nowrap;
    overflow-x: auto;
    padding-bottom: 4px;
    scrollbar-width: none;
  }

  .global-quick-links__list::-webkit-scrollbar {
    display: none;
  }

  .quick-link-chip {
    flex: 0 0 auto;
    width: auto;
  }
}

@media (max-width: 640px) {
  .global-quick-links {
    top: auto;
    bottom: 0;
    left: 0;
    right: 0;
    width: 100%;
    padding: 8px 0;
    transform: translateY(0);
  }

  .global-quick-links__inner {
    padding: 0 12px;
  }

  .global-quick-links__intro {
    display: none;
  }

  .quick-link-chip {
    min-height: 40px;
    padding-inline: 12px;
  }

  .quick-link-chip__label {
    font-size: 11px;
  }

  /* 给正文内容留出底部空间 */
  .app-shell.has-quick-links .page-container {
    padding-bottom: 60px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .fade-slide-enter-active,
  .fade-slide-leave-active,
  .quick-link-chip {
    transition-duration: 0.01ms !important;
  }
}
</style>
