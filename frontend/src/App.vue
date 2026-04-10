<script setup>
import { ref, onMounted } from 'vue'
import { getQuickLinks } from './api'

const quickLinks = ref([])

const fetchQuickLinks = async () => {
  try {
    const res = await getQuickLinks()
    quickLinks.value = res.data?.data || []
  } catch (e) {
    console.error('Failed to fetch quick links:', e)
  }
}

onMounted(fetchQuickLinks)
</script>

<template>
  <aside v-if="quickLinks.length" class="global-quick-links">
    <div class="gql-wrapper">
      <a
        v-for="link in quickLinks"
        :key="link.id"
        :href="link.url"
        target="_blank"
        rel="noopener noreferrer"
        class="gql-item"
        :title="link.label"
      >
        <span class="gql-icon">{{ link.icon }}</span>
        <span class="gql-label">{{ link.label }}</span>
      </a>
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
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(16px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.fade-slide-enter-to,
.fade-slide-leave-from {
  opacity: 1;
  transform: translateY(0);
}

.global-quick-links {
  position: fixed;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 90;
}

.gql-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px 6px;
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: 12px;
  backdrop-filter: blur(14px);
  box-shadow: var(--shadow);
}

.gql-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 8px;
  color: var(--text);
  font-size: 13px;
  text-decoration: none;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.gql-item:hover {
  color: var(--accent);
  background: var(--accent-bg);
  box-shadow: var(--shadow-glow);
  text-shadow: none;
}

.gql-icon {
  font-size: 16px;
  width: 20px;
  text-align: center;
  flex-shrink: 0;
}

.gql-label {
  font-family: 'JetBrains Mono', monospace;
  font-size: 11px;
  letter-spacing: 0.3px;
}

@media (max-width: 1400px) {
  .gql-label { display: none; }
}

@media (max-width: 768px) {
  .global-quick-links { display: none; }
}
</style>
