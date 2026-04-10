<script setup>
import { computed } from 'vue'

const props = defineProps({
  size: {
    type: Number,
    default: 40
  },
  decorative: {
    type: Boolean,
    default: true
  }
})

const brandStyle = computed(() => ({
  '--brand-size': `${props.size}px`,
  '--brand-icon-size': `${Math.round(props.size * 0.8)}px`
}))
</script>

<template>
  <span class="brand-mark" :style="brandStyle" :aria-hidden="decorative ? 'true' : undefined">
    <svg class="brand-mark-svg" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect x="2" y="2" width="28" height="28" rx="9" fill="#fcfaf6" stroke="rgba(45, 90, 74, 0.22)" stroke-width="1.6" />
      <path d="M9 11.5h14M9 16h10M9 20.5h6" stroke="var(--accent)" stroke-width="1.8" stroke-linecap="round" />
      <circle cx="23.5" cy="20.5" r="3" fill="rgba(45, 90, 74, 0.16)" stroke="var(--accent)" stroke-width="1.4" />
    </svg>
  </span>
</template>

<style scoped>
.brand-mark {
  --brand-size: 40px;
  --brand-icon-size: 32px;
  width: var(--brand-size);
  height: var(--brand-size);
  flex-shrink: 0;
  display: inline-grid;
  place-items: center;
  border-radius: calc(var(--brand-size) * 0.35);
  background: rgba(255, 255, 255, 0.82);
  box-shadow: inset 0 0 0 1px rgba(45, 90, 74, 0.06);
}

.brand-mark-svg {
  width: var(--brand-icon-size);
  height: var(--brand-icon-size);
}
</style>
