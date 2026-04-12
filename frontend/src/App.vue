<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getQuickLinks, createQuickLink, updateQuickLink, deleteQuickLink } from './api'
import { ElMessageBox, ElMessage } from 'element-plus'

const route = useRoute()
const quickLinks = ref([])
const viewportWidth = ref(typeof window === 'undefined' ? 1440 : window.innerWidth)
const isEditing = ref(false)
const editedLink = ref(null)

const authPaths = ['/login', '/register']

const isAuthRoute = computed(() => authPaths.includes(route.path))
const isAdmin = computed(() => localStorage.getItem('userRole') === 'ROLE_ADMIN')

const quickLinksMode = computed(() => {
   if (!quickLinks.value.length || isAuthRoute.value) {
     return 'hidden'
   }
   return 'dock' // Always centered dock mode
 })

const showQuickLinks = computed(() => quickLinksMode.value !== 'hidden')
const canEdit = computed(() => isAdmin.value && !isAuthRoute.value)

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

const startEdit = (link) => {
   editedLink.value = { ...link }
   isEditing.value = true
}

const saveEdit = async (link) => {
   try {
     if (link.id) {
       await updateQuickLink(link.id, link)
       ElMessage.success('更新成功')
     } else {
       await createQuickLink(link)
       ElMessage.success('添加成功')
     }
     isEditing.value = false
     editedLink.value = null
     await fetchQuickLinks()
   } catch (e) {
     console.error('Failed to save quick link:', e)
     ElMessage.error('保存失败')
   }
}

const cancelEdit = () => {
   isEditing.value = false
   editedLink.value = null
}

const deleteLink = async (id) => {
   try {
     await ElMessageBox.confirm('确定要删除此常用链接吗？', '提示', {
       confirmButtonText: '确定',
       cancelButtonText: '取消',
       type: 'warning'
     })
     await deleteQuickLink(id)
     ElMessage.success('删除成功')
     await fetchQuickLinks()
   } catch (e) {
     if (e !== 'cancel') {
       console.error('Failed to delete quick link:', e)
       ElMessage.error('删除失败')
     }
   }
}

const addNewLink = () => {
   editedLink.value = { id: null, icon: '🔗', label: '新链接', url: 'https://', sort: 0 }
   isEditing.value = true
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
         <div
           v-for="link in quickLinks"
           :key="link.id"
           :class="{ 'is-editing': isEditing && editedLink.value?.id === link.id }"
         >
           <div v-if="!isEditing || editedLink.value?.id !== link.id" class="gql-item-view">
             <a
               :href="link.url"
               target="_blank"
               rel="noopener noreferrer"
               class="gql-item"
               :title="link.label"
               @click.stop
             >
               <span class="gql-icon-wrap">
                 <span class="gql-icon">{{ link.icon }}</span>
               </span>
               <span class="gql-label">{{ link.label }}</span>
             </a>
             <div v-if="canEdit" class="gql-item-actions">
               <button
                 class="gql-edit-btn"
                 @click="startEdit(link)"
                 title="编辑"
               >
                 ✏️
               </button>
               <button
                 v-if="link.id"
                 class="gql-delete-btn"
                 @click="deleteLink(link.id)"
                 title="删除"
               >
                 🗑️
               </button>
             </div>
           </div>
           <div v-else class="gql-item-edit">
             <div class="edit-fields">
               <div class="edit-field">
                 <label>图标</label>
                 <input
                   v-model="editedLink.icon"
                   placeholder="例如：🔗"
                   class="edit-input"
                 />
               </div>
               <div class="edit-field">
                 <label>名称</label>
                 <input
                   v-model="editedLink.label"
                   placeholder="链接名称"
                   class="edit-input"
                 />
               </div>
               <div class="edit-field">
                 <label>链接</label>
                 <input
                   v-model="editedLink.url"
                   placeholder="https://"
                   class="edit-input"
                 />
               </div>
               <div class="edit-field">
                 <label>排序</label>
                 <input
                   type="number"
                   v-model="editedLink.sort"
                   placeholder="0"
                   class="edit-input"
                 />
               </div>
             </div>
             <div class="edit-actions">
               <button
                 type="button"
                 class="edit-btn cancel"
                 @click="cancelEdit"
               >
                 取消
               </button>
               <button
                 type="button"
                 class="edit-btn save"
                 @click="saveEdit(editedLink.value)"
               >
                 保存
               </button>
             </div>
           </div>
         </div>
         <div v-if="canEdit && !isEditing" class="add-link-btn" @click="addNewLink">
           + 添加链接
         </div>
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

/* Edit modes */
.gql-item-view {
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
   cursor: pointer;
 }

.gql-item-view:hover {
   color: var(--accent);
   background: rgba(255, 255, 255, 0.96);
   border-color: color-mix(in srgb, var(--accent) 18%, var(--border-strong));
   box-shadow: var(--shadow-sm);
   transform: translateY(-1px);
 }

.gql-item-actions {
   display: flex;
   gap: 6px;
   margin-left: auto;
 }

.gql-edit-btn,
.gql-delete-btn {
   width: 28px;
   height: 28px;
   display: inline-flex;
   align-items: center;
   justify-content: center;
   border-radius: 999px;
   background: transparent;
   color: var(--text-muted);
   font-size: 12px;
   cursor: pointer;
   transition: all 0.2s ease;
 }

.gql-edit-btn:hover {
   color: var(--accent);
   background: rgba(255, 255, 255, 0.9);
   border-color: color-mix(in srgb, var(--accent) 18%, var(--border-strong));
 }

.gql-delete-btn:hover {
   color: #f56c6c;
   background: rgba(255, 255, 255, 0.9);
   border-color: color-mix(in srgb, #f56c6c 18%, var(--border-strong));
 }

.gql-item-edit {
   background: rgba(255, 255, 255, 0.9);
   border-radius: 16px;
   padding: 16px;
   display: flex;
   flex-direction: column;
   gap: 12px;
 }

.edit-fields {
   display: grid;
   grid-template-columns: repeat(2, 1fr);
   gap: 12px;
 }

.edit-field {
   display: flex;
   flex-direction: column;
   gap: 4px;
 }

.edit-field label {
   font-size: 12px;
   color: var(--text-dim);
 }

.edit-input {
   padding: 8px 12px;
   border: 1px solid var(--border);
   border-radius: 8px;
   background: var(--card-bg);
   color: var(--text);
   font-size: 13px;
 }

.edit-input:focus {
   border-color: var(--accent);
   outline: none;
 }

.edit-actions {
   display: flex;
   gap: 12px;
   justify-content: flex-end;
   margin-top: 4px;
 }

.edit-btn {
   padding: 8px 16px;
   border-radius: 6px;
   font-size: 13px;
   cursor: pointer;
   border: 1px solid var(--border);
   background: transparent;
   color: var(--text);
   transition: all 0.2s ease;
 }

.edit-btn.cancel {
   border-color: var(--border);
   color: var(--text-dim);
 }

.edit-btn.cancel:hover {
   background: rgba(0, 0, 0, 0.02);
 }

.edit-btn.save {
   border-color: var(--accent);
   color: var(--accent);
   background: var(--accent-bg);
 }

.edit-btn.save:hover {
   background: var(--accent);
   color: white;
 }

.add-link-btn {
   padding: 12px 20px;
   border: 1px solid var(--border);
   border-radius: 16px;
   background: rgba(255, 255, 255, 0.78);
   color: var(--text);
   font-size: 13px;
   cursor: pointer;
   text-align: center;
   transition: all 0.2s ease;
 }

.add-link-btn:hover {
   background: rgba(255, 255, 255, 0.96);
   border-color: color-mix(in srgb, var(--accent) 18%, var(--border-strong));
   color: var(--accent);
   transform: translateY(-1px);
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

   .global-quick-links.is-dock .gql-item-view,
   .global-quick-links.is-dock .gql-item-edit {
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
