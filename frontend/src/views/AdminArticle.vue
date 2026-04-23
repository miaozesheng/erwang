<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { getArticle, createArticle, updateArticle, getCategories, getTags, uploadFile, resolveFileUrl } from '../api'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'
import Vditor from 'vditor'
import 'vditor/dist/index.css'
import { calculateExpandedEditorHeight } from '../utils/editorLayout'

const DRAFT_KEY = 'erwang_article_draft'
const IMPORT_EXCERPT_LIMIT = 160
const IMPORT_ACCEPT = '.md,.markdown,.txt,.xml,.docx,text/markdown,text/plain,text/xml,application/xml,application/vnd.openxmlformats-officedocument.wordprocessingml.document'
const EXPANDED_EDITOR_MIN_HEIGHT = 360
const EXPANDED_EDITOR_MOBILE_MIN_HEIGHT = 280
const EXPANDED_EDITOR_BOTTOM_GAP = 24
const EXPANDED_EDITOR_MOBILE_BOTTOM_GAP = 16

const normalizeImportedMarkdown = (markdown = '') => markdown
  .replace(/^\uFEFF/, '')
  .replace(/\r\n?/g, '\n')
  .trim()

const stripFrontMatter = (markdown = '') => markdown.replace(/^---\s*\n[\s\S]*?\n---\s*(?:\n|$)/, '')

const stripCodeBlocks = (markdown = '') => markdown.replace(/(^|\n)(```|~~~)[\s\S]*?\n\2(?=\n|$)/g, '$1')

const toPlainText = (input = '') => input
  .replace(/<!--([\s\S]*?)-->/g, ' ')
  .replace(/!\[[^\]]*\]\([^)]*\)/g, ' ')
  .replace(/\[([^\]]+)\]\([^)]*\)/g, '$1')
  .replace(/<[^>]+>/g, ' ')
  .replace(/`([^`]+)`/g, '$1')
  .replace(/^\s{0,3}>\s?/gm, '')
  .replace(/^\s{0,3}(?:[-*+]|\d+[.)])\s+/gm, '')
  .replace(/^\s{0,3}#{1,6}\s+/gm, '')
  .replace(/[*_~|]/g, ' ')
  .replace(/\\([\\`*_[\]{}()#+\-.!])/g, '$1')
  .replace(/\s+/g, ' ')
  .trim()

const clipText = (text = '', limit = IMPORT_EXCERPT_LIMIT) => {
  if (text.length <= limit) return text
  return `${text.slice(0, limit).trimEnd()}…`
}

const extractImportHints = (markdown = '') => {
  const normalizedMarkdown = normalizeImportedMarkdown(markdown)
  const body = stripFrontMatter(normalizedMarkdown).replace(/<!--([\s\S]*?)-->/g, '').trim()
  const searchable = stripCodeBlocks(body)
  const lines = searchable.split('\n').map((line) => line.trim())

  const headingLine = lines.find((line) => /^#\s+/.test(line))
  const suggestedTitle = headingLine
    ? toPlainText(headingLine.replace(/^#\s+/, ''))
    : lines
        .map((line) => toPlainText(line))
        .find((line) => line && !/^[-*_]{3,}$/.test(line) && !/^\|/.test(line) && line.length >= 2)

  const suggestedExcerpt = searchable
    .split(/\n\s*\n/)
    .map((block) => block.trim())
    .filter(Boolean)
    .map((block) => {
      const withoutLeadingHeading = block.replace(/^#{1,6}\s+.*(?:\n+|$)/, '').trim()
      return {
        raw: block,
        plain: toPlainText(withoutLeadingHeading || block)
      }
    })
    .find(({ raw, plain }) => {
      if (!plain) return false
      if (/^[-*_]{3,}$/.test(raw)) return false
      return plain !== suggestedTitle
    })?.plain || ''

  return {
    normalizedMarkdown,
    suggestedTitle: suggestedTitle || '',
    suggestedExcerpt: clipText(suggestedExcerpt)
  }
}

const getFileExtension = (fileName = '') => {
  const normalizedName = fileName.toLowerCase()
  const lastDotIndex = normalizedName.lastIndexOf('.')
  return lastDotIndex >= 0 ? normalizedName.slice(lastDotIndex + 1) : ''
}

const convertPlainTextToMarkdown = (text = '') => normalizeImportedMarkdown(text)

const convertXmlToMarkdown = (xmlText = '') => {
  const normalizedXml = normalizeImportedMarkdown(xmlText)
  if (!normalizedXml) return ''

  try {
    const parser = new DOMParser()
    const xmlDoc = parser.parseFromString(normalizedXml, 'application/xml')
    const hasParserError = xmlDoc.querySelector('parsererror')
    if (hasParserError) {
      return normalizedXml
    }

    const blocks = []
    const root = xmlDoc.documentElement
    const titleText = toPlainText(root?.querySelector(':scope > title')?.textContent || '')
    const summaryText = toPlainText(root?.querySelector(':scope > summary')?.textContent || '')

    if (titleText) {
      blocks.push(`# ${titleText}`)
    } else if (root?.nodeName) {
      blocks.push(`# ${root.nodeName}`)
    }

    if (summaryText) {
      blocks.push(summaryText)
    }

    const textBlocks = Array.from(root?.children || [])
      .map((node) => {
        const label = node.nodeName
        const value = toPlainText(node.textContent || '')
        if (!value || ['title', 'summary'].includes(label.toLowerCase())) return ''
        return `## ${label}\n\n${value}`
      })
      .filter(Boolean)

    if (textBlocks.length) {
      blocks.push(textBlocks.join('\n\n'))
    } else {
      const plainText = toPlainText(xmlDoc.documentElement?.textContent || '')
      if (plainText) {
        blocks.push(plainText)
      }
    }

    return normalizeImportedMarkdown(blocks.filter(Boolean).join('\n\n')) || normalizedXml
  } catch {
    return normalizedXml
  }
}

const convertDocxToMarkdown = async (file) => {
  const [{ default: TurndownService }, mammoth] = await Promise.all([
    import('turndown'),
    import('mammoth/mammoth.browser')
  ])

  const arrayBuffer = await file.arrayBuffer()
  const result = await mammoth.convertToHtml({ arrayBuffer })
  const html = (result.value || '').trim()

  if (!html) {
    const fallback = await mammoth.extractRawText({ arrayBuffer })
    return convertPlainTextToMarkdown(fallback.value || '')
  }

  const turndownService = new TurndownService({
    headingStyle: 'atx',
    codeBlockStyle: 'fenced'
  })
  turndownService.keep(['table'])
  return normalizeImportedMarkdown(turndownService.turndown(html))
}

const readImportedFile = async (file) => {
  const extension = getFileExtension(file.name)

  if (['md', 'markdown', 'txt'].includes(extension)) {
    return convertPlainTextToMarkdown(await file.text())
  }

  if (extension === 'xml') {
    return convertXmlToMarkdown(await file.text())
  }

  if (extension === 'docx') {
    return convertDocxToMarkdown(file)
  }

  throw new Error('unsupported-format')
}

const saveDraft = () => {
  if (isEdit.value) return
  localStorage.setItem(DRAFT_KEY, JSON.stringify({
    title: form.title,
    content: form.content,
    category: form.category,
    tags: form.tags,
    excerpt: form.excerpt,
    savedAt: Date.now()
  }))
}

const loadDraft = () => {
  if (isEdit.value) return false
  try {
    const raw = localStorage.getItem(DRAFT_KEY)
    if (!raw) return false
    const draft = JSON.parse(raw)
    if (Date.now() - draft.savedAt > 7 * 24 * 60 * 60 * 1000) {
      localStorage.removeItem(DRAFT_KEY)
      return false
    }
    form.title = draft.title || ''
    form.content = draft.content || ''
    form.category = draft.category || ''
    form.tags = draft.tags || []
    form.excerpt = draft.excerpt || ''
    return true
  } catch { return false }
}

const clearDraft = () => {
  localStorage.removeItem(DRAFT_KEY)
}

let draftTimer = null

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const loading = ref(false)
const saving = ref(false)

const form = reactive({
  title: '',
  content: '',
  category: '',
  tags: [],
  excerpt: ''
})

const contentStats = computed(() => {
  const content = form.content || ''
  const plainText = content
    .replace(/```[\s\S]*?```/g, ' ')
    .replace(/`[^`]*`/g, ' ')
    .replace(/!\[[^\]]*\]\([^)]*\)/g, ' ')
    .replace(/\[[^\]]*\]\([^)]*\)/g, ' ')
    .replace(/[#>*_~\-|]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
  const words = plainText ? plainText.split(' ').filter(Boolean).length : 0
  return {
    words,
    chars: content.length
  }
})

const categories = ref([])
const tags = ref([])
const fileInputRef = ref(null)
const editorWrapRef = ref(null)
const importing = ref(false)
const lastImportedFileName = ref('')
const isEditorExpanded = ref(false)
const expandedEditorHeight = ref(null)
let vditorInstance = null


const getExpandedEditorMinHeight = () => window.innerWidth <= 768
  ? EXPANDED_EDITOR_MOBILE_MIN_HEIGHT
  : EXPANDED_EDITOR_MIN_HEIGHT

const getExpandedEditorBottomGap = () => window.innerWidth <= 768
  ? EXPANDED_EDITOR_MOBILE_BOTTOM_GAP
  : EXPANDED_EDITOR_BOTTOM_GAP

const getSiteHeaderBottom = () => document.querySelector('[data-site-header]')?.getBoundingClientRect().bottom || 0

const updateExpandedEditorHeight = () => {
  if (!isEditorExpanded.value || !editorWrapRef.value) return

  expandedEditorHeight.value = calculateExpandedEditorHeight({
    viewportHeight: window.innerHeight,
    headerBottom: getSiteHeaderBottom(),
    editorTop: editorWrapRef.value.getBoundingClientRect().top,
    bottomGap: getExpandedEditorBottomGap(),
    minHeight: getExpandedEditorMinHeight()
  })

  editorWrapRef.value.style.setProperty('--editor-expanded-height', `${expandedEditorHeight.value}px`)
  editorWrapRef.value.style.height = `${expandedEditorHeight.value}px`
  editorWrapRef.value.style.width = '100%'
}

const expandEditor = async () => {
  isEditorExpanded.value = true
  await nextTick()
  updateExpandedEditorHeight()
}

const collapseExpandedEditor = () => {
  if (!isEditorExpanded.value) return
  isEditorExpanded.value = false
  expandedEditorHeight.value = null
  if (editorWrapRef.value) {
    editorWrapRef.value.style.removeProperty('--editor-expanded-height')
    editorWrapRef.value.style.height = '100%'
    editorWrapRef.value.style.width = '100%'
  }
}

const toggleEditorExpanded = async () => {
  if (isEditorExpanded.value) {
    collapseExpandedEditor()
    return
  }
  await expandEditor()
}

const handleWindowResize = () => {
  if (isEditorExpanded.value) {
    updateExpandedEditorHeight()
  }
}

const handleWindowKeydown = (event) => {
  if (event.key === 'Escape' && isEditorExpanded.value) {
    collapseExpandedEditor()
  }
}


const initVditor = () => {
  if (vditorInstance) {
    vditorInstance.destroy()
    vditorInstance = null
  }

  const toggleEditorMode = () => {
    if (!vditorInstance) return
    const modes = ['ir', 'wysiwyg', 'sv']
    const currentIdx = modes.indexOf(vditorInstance.getCurrentMode())
    const nextMode = modes[(currentIdx + 1) % modes.length]
    vditorInstance.setMode(nextMode)
    editorMode.value = nextMode
  }

  const editorMode = ref('ir') // 默认即时渲染模式

  vditorInstance = new Vditor('vditor-editor', {
    mode: 'ir', // 即时渲染模式
    lang: 'zh_CN',
    theme: 'light',
    height: '100%',
    placeholder: '开始编写 Markdown 内容...',
    value: form.content,
    cache: { enable: false },
    toolbar: [
      'headings', 'bold', 'italic', 'strike', '|',
      'line', 'quote', 'list', 'ordered-list', 'check', '|',
      'code', 'inline-code', 'table', 'link', 'upload', '|',
      'undo', 'redo', '|',
      'edit-mode', 'outline'
    ],
    input: (value) => {
      form.content = value
      saveDraft()
    },
    upload: {
      accept: 'image/*',
      multiple: false,
      handler: async (files) => {
        const file = files[0]
        if (!file) return null
        const maxSize = 10 * 1024 * 1024
        if (file.size > maxSize) {
          ElMessage.warning('图片大小不能超过 10MB')
          return null
        }
        try {
          const res = await uploadFile(file)
          const rawUrl = res.data?.data?.url || res.data?.url || ''
          const displayUrl = resolveFileUrl(rawUrl)
          if (displayUrl && vditorInstance) {
            vditorInstance.insertValue(`![image](${displayUrl})`)
          }
          ElMessage.success('图片上传成功')
        } catch (error) {
          console.error('Upload failed:', error)
          ElMessage.error('图片上传失败')
        }
        return null
      }
    },
    after: () => {
      if (form.content && vditorInstance) {
        vditorInstance.setValue(form.content)
      }
      const editorEl = document.getElementById('vditor-editor')
      if (editorEl) {
        editorEl.classList.add('vditor-cyber')
      }
    }
  })
}

const fetchData = async () => {
  loading.value = true
  try {
    const [catRes, tagRes] = await Promise.all([getCategories(), getTags()])
    categories.value = catRes.data.data || []
    tags.value = tagRes.data.data || []

    if (isEdit.value) {
      const res = await getArticle(route.params.id)
      const article = res.data.data || {}
      form.title = article.title || ''
      form.content = article.content || ''
      form.category = article.category || ''
      form.tags = article.tags || []
      form.excerpt = article.excerpt || article.summary || ''
    } else {
      const hasDraft = loadDraft()
      if (hasDraft) {
        ElMessage.info('已恢复上次未保存的草稿')
      }
    }
  } catch (e) {
    console.error('Failed to fetch data:', e)
  } finally {
    loading.value = false
    initVditor()
  }
}

const syncEditorContent = (content) => {
  form.content = content
  if (vditorInstance) {
    vditorInstance.setValue(content)
  }
}

const hasImportConflict = () => [form.title, form.content, form.excerpt].some((value) => value.trim())

const triggerImport = () => {
  if (loading.value || saving.value || importing.value) return
  fileInputRef.value?.click()
}

const handleImportChange = async (event) => {
  const input = event.target
  const file = input?.files?.[0]
  if (!file) return

  try {
    if (hasImportConflict()) {
      await ElMessageBox.confirm(
        '当前标题、摘要或正文里已经有内容。继续导入会替换正文，已有标题/摘要会保留，空白项才会自动补全。是否继续？',
        '确认导入 Markdown',
        {
          confirmButtonText: '继续导入',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    }

    importing.value = true
    const rawMarkdown = await readImportedFile(file)
    const { normalizedMarkdown, suggestedTitle, suggestedExcerpt } = extractImportHints(rawMarkdown)

    if (!normalizedMarkdown) {
      ElMessage.warning('这个文件里没有可导入的正文内容')
      return
    }

    syncEditorContent(normalizedMarkdown)

    const autofilledFields = []
    if (!form.title.trim() && suggestedTitle) {
      form.title = suggestedTitle
      autofilledFields.push('标题')
    }
    if (!form.excerpt.trim() && suggestedExcerpt) {
      form.excerpt = suggestedExcerpt
      autofilledFields.push('摘要')
    }

    lastImportedFileName.value = file.name
    saveDraft()

    ElMessage.success(
      autofilledFields.length
        ? `已导入 ${file.name}，并自动补全${autofilledFields.join('、')}`
        : `已导入 ${file.name}`
    )
  } catch (error) {
    if (error?.message === 'unsupported-format') {
      ElMessage.warning('目前支持导入 md、markdown、txt、xml、docx 文件')
      return
    }
    if (error !== 'cancel' && error !== 'close') {
      console.error('Failed to import markdown:', error)
      ElMessage.error('导入失败，请确认文件编码或内容后重试')
    }
  } finally {
    importing.value = false
    if (input) input.value = ''
  }
}

const handleSave = async () => {
  if (vditorInstance) {
    form.content = vditorInstance.getValue()
  }

  if (!form.title.trim()) {
    ElMessage.warning('请输入文章标题')
    return
  }
  if (!form.content.trim()) {
    ElMessage.warning('请输入文章内容')
    return
  }

  saving.value = true
  try {
    const data = {
      title: form.title,
      content: form.content,
      category: form.category,
      tags: form.tags,
      excerpt: form.excerpt
    }

    if (isEdit.value) {
      await updateArticle(route.params.id, data)
      ElMessage.success('更新成功')
    } else {
      await createArticle(data)
      ElMessage.success('创建成功')
      clearDraft()
    }

    router.push('/admin')
  } catch (e) {
    console.error('Failed to save:', e)
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    saving.value = false
  }
}

const handleCancel = () => {
  router.push('/admin')
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleWindowResize)
  window.addEventListener('keydown', handleWindowKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleWindowResize)
  window.removeEventListener('keydown', handleWindowKeydown)
  if (vditorInstance) {
    vditorInstance.destroy()
    vditorInstance = null
  }
  if (draftTimer) clearInterval(draftTimer)
})
</script>

<template>
  <div class="page-container">
    <Header />

    <main class="main-content">
      <section class="editor-shell">
        <div class="editor-header">
          <div class="editor-header-content">
            <button
              v-if="$route.path !== '/profile'"
              class="back-btn"
              @click="$router.push('/profile')"
            >
              ← 返回个人中心
            </button>
            <div class="editor-heading-copy">
              <p class="editor-eyebrow">内容编辑器</p>
              <h1 class="editor-title">{{ isEdit ? '编辑文章' : '新建文章' }}</h1>
               <p class="editor-subtitle">支持导入本地 md、txt、xml、docx 草稿；空白标题和摘要会按正文自动补全。</p>
             </div>
           </div>
           <div class="header-actions">
             <input
               ref="fileInputRef"
               class="import-input"
               type="file"
               :accept="IMPORT_ACCEPT"
               @change="handleImportChange"
             >
            <el-button class="import-btn" :loading="importing" :disabled="loading || saving" @click="triggerImport">
              <el-icon><Upload /></el-icon>
               导入正文文件
            </el-button>
            <el-button @click="handleCancel">取消</el-button>
            <el-button type="primary" :loading="saving" @click="handleSave">
              {{ isEdit ? '更新' : '发布' }}
            </el-button>
          </div>
       </div>

        <div v-loading="loading" class="editor-container">
          <el-form :model="form" label-position="top">
            <div class="section-label">// META</div>

            <div class="meta-row">
              <el-form-item label="标题" required class="meta-title">
                <el-input
                  v-model="form.title"
                  placeholder="请输入文章标题"
                  size="large"
                  maxlength="100"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="分类" class="meta-category">
                <el-select v-model="form.category" placeholder="选择或输入分类" style="width: 100%" filterable allow-create default-first-option>
                  <el-option
                    v-for="cat in categories"
                    :key="cat"
                    :label="cat"
                    :value="cat"
                  />
                </el-select>
              </el-form-item>
            </div>

            <div class="meta-row">
              <el-form-item label="标签" class="meta-tags">
                <el-select
                  v-model="form.tags"
                  multiple
                  filterable
                  allow-create
                  default-first-option
                  placeholder="选择或输入标签"
                  style="width: 100%"
                >
                  <el-option
                    v-for="tag in tags"
                    :key="tag"
                    :label="tag"
                    :value="tag"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="摘要" class="meta-excerpt">
                <el-input
                  v-model="form.excerpt"
                  placeholder="文章摘要（可选）"
                  maxlength="200"
                />
              </el-form-item>
            </div>

            <div class="editor-separator" aria-hidden="true"></div>
            <div class="content-heading-row">
              <div class="section-label">// CONTENT</div>
              <div class="content-heading-actions">
                <div class="import-status" :class="{ 'has-file': lastImportedFileName }">
                  <span class="import-status-label">导入</span>
                   <span>{{ lastImportedFileName || '支持 .md / .markdown / .txt / .xml / .docx' }}</span>
                </div>
                <button
                  type="button"
                  class="editor-expand-toggle"
                  :class="{ active: isEditorExpanded }"
                  :aria-pressed="isEditorExpanded ? 'true' : 'false'"
                  @click="toggleEditorExpanded"
                >
                  <span class="editor-expand-toggle__icon" aria-hidden="true">{{ isEditorExpanded ? '↙' : '↗' }}</span>
                  <span>{{ isEditorExpanded ? '收起编辑区' : '展开编辑区' }}</span>
                </button>
              </div>
            </div>

            <el-form-item label="正文" required>
              <div
                ref="editorWrapRef"
                id="vditor-editor"
                class="vditor-wrap"
                :class="{ 'is-expanded': isEditorExpanded }"
              ></div>
            </el-form-item>

            <div class="editor-stats">
              <span>WORDS {{ contentStats.words }}</span>
              <span>CHARS {{ contentStats.chars }}</span>
            </div>
          </el-form>
        </div>
      </section>
    </main>
    <Footer />
  </div>
</template>

<style scoped>
.page-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg);
}

.editor-page {
  background: var(--bg);
}

.main-content {
  --page-shell-max: var(--shell-wide, 1800px);
  flex: 1;
  width: 100%;
  max-width: 100%;
  margin: 0 auto;
  padding: 0 var(--page-gutter);
  box-sizing: border-box;
}

.editor-shell {
  position: relative;
  width: 100%;
  max-width: 100%;
  background: #ffffff;
  margin: 0 auto;
  padding: 32px clamp(24px, 5vw, 64px);
  box-sizing: border-box;
}

.editor-container {
  background: #ffffff;
  width: 100%;
  max-width: 100%;
}

.editor-header {
   display: flex;
   justify-content: space-between;
   align-items: center;
   margin-bottom: 24px;
   position: relative;
   z-index: 1;
 }

.back-btn {
   display: inline-flex;
   align-items: center;
   gap: 6px;
   padding: 8px 16px;
   border: 1px solid var(--border);
   border-radius: 6px;
   background: transparent;
   color: var(--text);
   font-size: 13px;
   cursor: pointer;
   transition: all 0.2s ease;
 }

.back-btn:hover {
   border-color: var(--accent);
   color: var(--accent);
   background: rgba(255, 255, 255, 0.96);
   transform: translateY(-1px);
 }

.editor-header-content {
   display: flex;
   flex-direction: column;
   align-items: flex-start;
   gap: var(--sp-3);
 }

.editor-heading-copy {
  display: flex;
  flex-direction: column;
  gap: var(--sp-1);
}

.editor-eyebrow {
  color: var(--accent);
  font-family: var(--mono);
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.editor-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-h);
  margin: 0;
}

.editor-subtitle {
  max-width: 52ch;
  color: var(--text-muted);
  font-size: 14px;
  line-height: 1.7;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  flex-wrap: wrap;
}

.header-actions .el-button--primary {
  background: var(--accent);
  border-color: var(--accent);
  box-shadow: none;
}

.import-input {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.import-btn {
  --el-button-bg-color: color-mix(in srgb, var(--accent-bg) 78%, white 22%);
  --el-button-border-color: color-mix(in srgb, var(--accent) 24%, var(--border-strong));
  --el-button-text-color: var(--accent);
  --el-button-hover-bg-color: color-mix(in srgb, var(--accent-bg) 92%, white 8%);
  --el-button-hover-border-color: color-mix(in srgb, var(--accent) 36%, var(--border-strong));
  --el-button-hover-text-color: var(--accent);
  --el-button-active-bg-color: color-mix(in srgb, var(--accent-bg) 70%, var(--bg-elevated));
}

.import-btn :deep(.el-icon) {
  margin-right: 2px;
}

.editor-container {
  position: relative;
  z-index: 1;
}

.section-label {
  margin: 4px 0 14px;
  color: var(--accent);
  font-family: var(--mono);
  font-size: 12px;
  letter-spacing: 1.2px;
  text-transform: uppercase;
  opacity: 0.88;
}

.editor-separator {
  margin: 8px 0 16px;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 240, 255, 0.2), transparent);
}

.content-heading-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--sp-4);
  margin-bottom: 6px;
}

.content-heading-row .section-label {
  margin: 0;
}

.content-heading-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--sp-3);
  flex-wrap: wrap;
}

.import-status {
  display: inline-flex;
  align-items: center;
  gap: var(--sp-2);
  min-height: 30px;
  padding: 0 var(--sp-3);
  border: 1px solid color-mix(in srgb, var(--border-strong) 90%, white 10%);
  border-radius: var(--radius-full);
  background: color-mix(in srgb, var(--bg-elevated) 74%, white 26%);
  color: var(--text-muted);
  font-size: 12px;
}

.import-status span:last-child {
  max-width: 28ch;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.editor-expand-toggle {
  display: inline-flex;
  align-items: center;
  gap: var(--sp-2);
  min-height: 32px;
  padding: 0 var(--sp-3);
  border: 1px solid color-mix(in srgb, var(--border-strong) 90%, white 10%);
  border-radius: var(--radius-full);
  background: color-mix(in srgb, var(--bg-elevated) 70%, white 30%);
  color: var(--text-h);
  font-family: var(--mono);
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.editor-expand-toggle:hover {
  border-color: color-mix(in srgb, var(--accent) 26%, var(--border-strong));
  color: var(--accent);
  background: color-mix(in srgb, var(--accent-bg) 54%, white 46%);
}

.editor-expand-toggle.active {
  border-color: color-mix(in srgb, var(--accent) 30%, var(--border-strong));
  color: var(--accent);
  background: color-mix(in srgb, var(--accent-bg) 72%, white 28%);
}

.editor-expand-toggle__icon {
  font-size: 13px;
  line-height: 1;
}

.import-status.has-file {
  color: var(--text-h);
  border-color: color-mix(in srgb, var(--accent) 22%, var(--border-strong));
  background: color-mix(in srgb, var(--accent-bg) 46%, white 54%);
}

.import-status-label {
  color: var(--accent);
  font-family: var(--mono);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.meta-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: clamp(14px, 2.4vw, 22px);
}

@media (max-width: 768px) {
  .meta-row {
    grid-template-columns: 1fr;
  }
}

:deep(.el-form-item__label) {
  color: var(--text-h);
  font-weight: 500;
  font-family: var(--mono);
  text-transform: uppercase;
  letter-spacing: 0.9px;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  background: #ffffff !important;
  border: 1px solid var(--border) !important;
  box-shadow: none;
}

:deep(.el-input__wrapper:hover),
:deep(.el-input__wrapper.is-focus) {
  border-color: var(--accent);
}

:deep(.el-input__inner),
:deep(.el-textarea__inner) {
  color: var(--text-h);
}

:deep(.el-select .el-input__wrapper) {
  background: #ffffff !important;
}

.vditor-wrap {
  --editor-default-height: min(85vh, calc(100vh - 260px));
  --editor-expanded-height: var(--editor-default-height);
  width: 100%;
  height: var(--editor-default-height);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.vditor-wrap.is-expanded {
  height: var(--editor-expanded-height);
}

.editor-stats {
  margin-top: 10px;
  display: flex;
  gap: 18px;
  color: rgba(114, 255, 220, 0.88);
  font-family: var(--mono);
  font-size: 12px;
  letter-spacing: 0.7px;
}

:deep(.vditor) {
  --panel-background-color: #ffffff !important;
  --toolbar-background-color: #f5f5f5 !important;
  --textarea-background-color: #ffffff !important;
  border: 1px solid var(--border) !important;
  border-radius: var(--radius-md) !important;
  display: flex !important;
  flex-direction: column !important;
  height: 100% !important;
  width: 100% !important;
  max-width: 100% !important;
}

:deep(.vditor-toolbar) {
  background: #f5f5f5 !important;
  border-bottom: 1px solid var(--border) !important;
  padding: 6px 8px !important;
  width: 100% !important;
}

:deep(.vditor-content) {
  width: 100% !important;
  flex: 1 !important;
}

:deep(.vditor-ir),
:deep(.vditor-sv),
:deep(.vditor-wysiwyg) {
  width: 100% !important;
}

:deep(.vditor-toolbar__item) {
  border-radius: 6px !important;
}

:deep(.vditor-toolbar__item:hover) {
  background: rgba(45, 90, 74, 0.08) !important;
}

:deep(.vditor-toolbar__item--current) {
  background: rgba(45, 90, 74, 0.12) !important;
  color: var(--accent) !important;
}

:deep(.vditor-toolbar svg) {
  color: #666666 !important;
  fill: #666666 !important;
}

:deep(.vditor-toolbar__item:hover svg),
:deep(.vditor-toolbar__item--current svg) {
  color: var(--accent) !important;
  fill: var(--accent) !important;
}

:deep(.vditor-ir .vditor-reset),
:deep(.vditor-sv .vditor-reset),
:deep(.vditor-wysiwyg .vditor-reset) {
  color: #333333 !important;
  font-family: var(--sans) !important;
  font-size: 15px !important;
  line-height: 1.8 !important;
  padding: 24px !important;
  width: 100% !important;
  max-width: 100% !important;
}

:deep(.vditor-ir .vditor-reset h1),
:deep(.vditor-ir .vditor-reset h2),
:deep(.vditor-ir .vditor-reset h3) {
  color: #1a1a1a !important;
  font-family: var(--heading) !important;
}

:deep(.vditor-ir .vditor-reset pre.vditor-reset) {
  background: #f5f5f5 !important;
  border: 1px solid var(--border) !important;
  border-radius: 8px !important;
}

:deep(.vditor-ir .vditor-reset code) {
  background: rgba(45, 90, 74, 0.08) !important;
  color: var(--accent) !important;
  border-radius: 4px !important;
  padding: 2px 6px !important;
  font-family: var(--mono) !important;
}

:deep(.vditor-ir .vditor-reset blockquote) {
  border-left: 4px solid var(--accent) !important;
  background: rgba(45, 90, 74, 0.05) !important;
  padding: 8px 16px !important;
}

:deep(.vditor-ir .vditor-reset a) {
  color: var(--accent) !important;
}

:deep(.vditor-ir .vditor-reset table td),
:deep(.vditor-ir .vditor-reset table th) {
  border-color: var(--border) !important;
}

:deep(.vditor-ir .vditor-reset table th) {
  background: #f5f5f5 !important;
}

:deep(.vditor-outline) {
  background: #fafafa !important;
  border-left: 1px solid var(--border) !important;
}

:deep(.vditor-hint),
:deep(.vditor-tip) {
  background: #ffffff !important;
  border: 1px solid var(--border) !important;
  color: #333333 !important;
}

:deep(.vditor-input) {
  background: #ffffff !important;
  color: #333333 !important;
  border-color: var(--border) !important;
}

@media (max-width: 768px) {
  .editor-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .content-heading-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .content-heading-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .import-status {
    width: 100%;
    justify-content: space-between;
  }

  .editor-expand-toggle {
    width: 100%;
    justify-content: center;
  }

  .import-status span:last-child {
    max-width: none;
  }

  .meta-row {
    grid-template-columns: 1fr;
  }

  .editor-shell {
    padding: 18px;
  }

  .main-content {
    padding: 24px 14px;
  }
}
</style>
