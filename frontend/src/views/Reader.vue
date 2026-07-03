<template>
  <div class="reader-root" :class="`theme-${theme}`" :style="readerStyle">
    <!-- 顶部栏 -->
    <div class="reader-topbar">
      <button class="reader-btn" @click="$router.back()">
        <svg viewBox="0 0 24 24" fill="currentColor"><path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/></svg>
        返回
      </button>

      <button class="toc-toggle-btn" @click="tocCollapsed = !tocCollapsed">
        <span class="toc-toggle-icon">{{ tocCollapsed ? '>' : '<' }}</span>
      </button>

      <div class="reader-topbar-title">{{ epubMeta.title || book.title }}<span v-if="!isEpubBook"> - {{ currentChapter.title }}</span></div>

      <button
        class="reader-mode-btn"
        :class="{ active: pageMode === 'paged' }"
        @click="pageMode = pageMode === 'paged' ? 'scroll' : 'paged'"
      >
        {{ pageMode === 'paged' ? '翻页' : '滚动' }}
      </button>

      <router-link :to="`/muck/${bookId}`" class="reader-stealth-btn">
        摸鱼模式
      </router-link>
    </div>

    <!-- 主体 -->
    <div class="reader-body">
      <!-- 侧边目录 -->
      <div class="reader-toc" :class="{ collapsed: tocCollapsed }">
        <div class="toc-scroll" ref="tocScroll">
          <div class="toc-header">目录 ({{ chapters.length }})</div>
          <div class="toc-list">
            <div
              v-for="(ch, i) in chapters"
              :key="ch.id"
              class="toc-item"
              :class="{ active: i === currentChapterIndex }"
              @click="goToChapter(i)"
            >
              {{ ch.title }}
            </div>
          </div>
        </div>
      </div>

      <!-- 正文 -->
      <div class="reader-content-wrap" :class="{ 'paged-mode': pageMode === 'paged', 'epub-mode': isEpubBook }" ref="contentWrap">
        <div class="reader-content">
          <div v-if="isEpubBook" class="epub-reader-shell">
            <div class="epub-frame" :class="{ loading: epubLoading, 'scroll-frame': pageMode === 'scroll' }">
              <div ref="epubViewer" class="epub-viewer"></div>
              <button v-if="pageMode === 'paged'" class="epub-hit epub-hit-left" @click="prevPage" aria-label="上一页"></button>
              <button v-if="pageMode === 'paged'" class="epub-hit epub-hit-right" @click="nextPage" aria-label="下一页"></button>
              <div v-if="epubLoading && !epubHasVisibleFrame" class="epub-loading">EPUB 加载中...</div>
              <div v-if="epubError" class="epub-error">{{ epubError }}</div>
            </div>
            <div class="epub-reader-footer">
              <span>{{ epubMeta.title || book.title || '未命名书籍' }}</span>
              <span>{{ epubMeta.creator || book.author || '未知作者' }}</span>
              <span>{{ epubLocationLabel }}</span>
            </div>
          </div>

          <template v-else>
          <div class="reader-state" v-if="loadingChapter">章节加载中...</div>
          <div class="reader-state error" v-else-if="chapterError">
            <div>{{ chapterError }}</div>
            <button class="reader-nav-btn" @click="loadChapter(currentChapterIndex)">重试</button>
          </div>
          <div v-else-if="!chapters.length" class="reader-state error">这本书还没有可阅读章节，请重新导入或检查书源内容。</div>
          <h2 v-else class="reader-chapter-title">{{ currentChapter.title || '未命名章节' }}</h2>
          <div
            v-if="!loadingChapter && !chapterError && pageMode === 'paged'"
            class="ebook-stage"
            :class="pageTurnClass"
          >
            <button class="page-hit page-hit-left" :disabled="!canPrevPage" @click="prevPage" aria-label="上一页"></button>
            <div class="ebook-spread">
              <article
                v-for="(page, side) in visiblePages"
                :key="`${currentChapterIndex}-${pageIndex}-${side}`"
                class="ebook-page"
                :class="{ empty: !page }"
              >
                <div class="ebook-page-head" v-if="page">
                  <span>{{ currentChapter.title || '章节' }}</span>
                  <span>{{ pageIndex + side + 1 }} / {{ pagedPages.length || 1 }}</span>
                </div>
                <div class="ebook-page-body" v-if="page">
                  <template v-for="(block, i) in page.blocks" :key="i">
                    <p v-if="block.type === 'text'">{{ block.text }}</p>
                    <div v-else class="ebook-html-block" v-html="block.html"></div>
                  </template>
                </div>
              </article>
            </div>
            <button class="page-hit page-hit-right" :disabled="!canNextPage" @click="nextPage" aria-label="下一页"></button>
          </div>

          <div class="reader-text" v-else-if="!loadingChapter && !chapterError && !isHtmlChapter">
            <p v-for="(para, i) in visibleParagraphs" :key="i">{{ para }}</p>
            <button
              v-if="paragraphs.length > paragraphLimit"
              class="load-more"
              @click="paragraphLimit += paragraphStep"
            >
              继续显示本章内容（{{ paragraphLimit }} / {{ paragraphs.length }}）
            </button>
          </div>
          <div class="reader-text reader-html" v-else-if="!loadingChapter && !chapterError" v-html="currentChapter.content"></div>

          <!-- 翻页栏 -->
          <div class="reader-nav">
            <button
              class="reader-nav-btn"
              :disabled="currentChapterIndex === 0"
              @click="goToChapter(currentChapterIndex - 1)"
            >上一章</button>
            <span class="reader-nav-info">{{ currentChapterIndex + 1 }} / {{ chapters.length }}</span>
            <button
              class="reader-nav-btn"
              :disabled="currentChapterIndex === chapters.length - 1"
              @click="goToChapter(currentChapterIndex + 1)"
            >下一章</button>
          </div>
          <div class="reader-shortcut-hint">{{ pageMode === 'paged' ? '左右键翻页，点页面边缘也可以翻页' : '键盘左右键切换章节' }}</div>
          </template>
        </div>
      </div>
    </div>

    <!-- 设置浮窗按钮 -->
    <button class="reader-settings-fab" @click="settingsOpen = !settingsOpen">
      <svg viewBox="0 0 24 24" fill="currentColor"><path d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/></svg>
    </button>

    <!-- 设置面板 -->
    <div class="reader-settings-panel" :class="{ open: settingsOpen }">
      <div class="settings-group">
        <div class="settings-label">版式</div>
        <div class="layout-segment">
          <button :class="{ active: pageMode === 'scroll' }" @click="pageMode = 'scroll'">滚动</button>
          <button :class="{ active: pageMode === 'paged' }" @click="pageMode = 'paged'">翻页</button>
        </div>
      </div>
      <div class="settings-group">
        <div class="settings-label">字号</div>
        <div class="settings-controls">
          <button class="settings-step-btn" @click="fontSize = Math.max(14, fontSize - 1)">-</button>
          <span class="settings-value">{{ fontSize }}px</span>
          <button class="settings-step-btn" @click="fontSize = Math.min(28, fontSize + 1)">+</button>
        </div>
      </div>
      <div class="settings-group">
        <div class="settings-label">行距</div>
        <div class="settings-controls">
          <button class="settings-step-btn" @click="lineHeight = Math.max(1.4, lineHeight - 0.1)">-</button>
          <span class="settings-value">{{ lineHeight.toFixed(1) }}</span>
          <button class="settings-step-btn" @click="lineHeight = Math.min(3, lineHeight + 0.1)">+</button>
        </div>
      </div>
      <div class="settings-group">
        <div class="settings-label">主题</div>
        <div class="theme-swatches">
          <button class="theme-swatch" :style="{ background: '#FAF8F3' }" :class="{ selected: theme === 'default' }" @click="theme = 'default'" title="默认"></button>
          <button class="theme-swatch" :style="{ background: '#1A1714' }" :class="{ selected: theme === 'night' }" @click="theme = 'night'" title="夜间"></button>
          <button class="theme-swatch" :style="{ background: '#F0E6D2' }" :class="{ selected: theme === 'paper' }" @click="theme = 'paper'" title="护眼"></button>
          <button class="theme-swatch" :style="{ background: '#E8EDE0' }" :class="{ selected: theme === 'green' }" @click="theme = 'green'" title="绿色"></button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ePub from 'epubjs'
import { getBookshelf } from '../api/book'
import { getChapterList, getChapterContent } from '../api/chapter'
import { getProgress, saveProgress } from '../api/progress'
import { useReaderStore } from '../stores/reader'
import { epubReaderUrl, fastEpubSource } from '../utils/epubCache'

const route = useRoute()
const router = useRouter()
const readerStore = useReaderStore()

const bookId = parseInt(route.params.bookId)
const book = ref({ title: '' })
const chapters = ref([])
const currentChapterIndex = ref(0)
const currentChapter = ref({ title: '', content: '' })
const tocCollapsed = ref(false)
const settingsOpen = ref(false)
const contentWrap = ref(null)
const tocScroll = ref(null)
const epubViewer = ref(null)
const loadingChapter = ref(false)
const chapterError = ref('')
const epubLoading = ref(false)
const epubError = ref('')
const epubHasVisibleFrame = ref(false)
const epubMeta = ref({ title: '', creator: '' })
const epubLocationLabel = ref('')
const paragraphLimit = ref(520)
const paragraphStep = 520
let loadSeq = 0
let epubBook = null
let epubRendition = null
let epubInitSeq = 0
let savedChapterIndex = 0
let epubSavedCfi = ''
let resizeObserver = null
let epubUserNavSeq = 0
let epubDisplaySeq = 0

const fontSize = ref(readerStore.fontSize || 18)
const lineHeight = ref(readerStore.lineHeight || 1.9)
const theme = ref(readerStore.theme || 'default')
const savedPageMode = localStorage.getItem('readerPageMode')
const pageMode = ref(savedPageMode === 'spread' ? 'paged' : (savedPageMode || 'scroll'))
const pageIndex = ref(0)
const pageTurnClass = ref('')
let pageTurnTimer = 0

const readerStyle = computed(() => ({
  '--reader-font-size': fontSize.value + 'px',
  '--reader-line-height': lineHeight.value
}))

const paragraphs = computed(() => {
  if (!currentChapter.value.content) return []
  return currentChapter.value.content.split('\n').filter(p => p.trim())
})

const visibleParagraphs = computed(() => paragraphs.value.slice(0, paragraphLimit.value))
const isEpubBook = computed(() => book.value?.sourceType === 'epub' && !!book.value?.epubUrl)
const epubFileUrl = computed(() => {
  return epubReaderUrl(book.value)
})
const isHtmlChapter = computed(() => /<\s*(p|div|section|article|img|h[1-6]|br)\b/i.test(currentChapter.value.content || ''))
const readingBlocks = computed(() => buildReadingBlocks(currentChapter.value.content || '', isHtmlChapter.value))
const pagedPages = computed(() => paginateBlocks(readingBlocks.value))
const visiblePages = computed(() => [pagedPages.value[pageIndex.value], pagedPages.value[pageIndex.value + 1]])
const canPrevPage = computed(() => pageIndex.value > 0 || currentChapterIndex.value > 0)
const canNextPage = computed(() => pageIndex.value + 2 < pagedPages.value.length || currentChapterIndex.value < chapters.value.length - 1)
const epubProgressKey = computed(() => `reader:epub:cfi:${bookId}`)

async function loadChapter(index) {
  if (index < 0 || index >= chapters.value.length) return
  const seq = ++loadSeq
  currentChapterIndex.value = index
  scrollTocToCurrent()
  const ch = chapters.value[index]
  loadingChapter.value = true
  chapterError.value = ''
  paragraphLimit.value = paragraphStep
  currentChapter.value = { ...ch, content: '' }
  try {
    const res = await getChapterContent(ch.id)
    if (seq !== loadSeq) return
    currentChapter.value = res.data || ch
    pageIndex.value = 0
    await nextTick()
    if (contentWrap.value) {
      contentWrap.value.scrollTop = 0
      contentWrap.value.scrollLeft = 0
    }
    scrollTocToCurrent()
    saveCurrentProgress()
  } catch (e) {
    console.error('加载章节失败:', e)
    if (seq === loadSeq) {
      chapterError.value = e.message || '章节加载失败，请稍后重试'
    }
  } finally {
    if (seq === loadSeq) loadingChapter.value = false
  }
}

function goToChapter(index) {
  if (isEpubBook.value) {
    goToEpubChapter(index)
    return
  }
  loadChapter(index)
}

function destroyEpubReader() {
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
  if (epubRendition) {
    try { epubRendition.destroy() } catch (e) {}
    epubRendition = null
  }
  if (epubBook) {
    try { epubBook.destroy() } catch (e) {}
    epubBook = null
  }
}

function applyEpubTheme() {
  if (!epubRendition) return
  const color = theme.value === 'night' ? '#C8BFA8' : '#3D2E1A'
  epubRendition.themes.default({
    body: {
      color,
      background: 'transparent',
      'font-size': `${fontSize.value}px !important`,
      'line-height': `${lineHeight.value} !important`,
      'font-family': 'system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
      margin: '0 !important',
      padding: '0 !important'
    },
    p: {
      'text-indent': '2em',
      'margin-top': '0',
      'margin-bottom': '1em'
    },
    img: {
      'max-width': '100%',
      'max-height': '92%',
      'object-fit': 'contain'
    },
    html: { background: 'transparent' }
  })
  epubRendition.themes.select('default')
}

async function initEpubReader() {
  if (!isEpubBook.value || !epubViewer.value || !epubFileUrl.value) return
  const seq = ++epubInitSeq
  epubLoading.value = true
  epubError.value = ''
  epubHasVisibleFrame.value = false
  epubLocationLabel.value = ''
  destroyEpubReader()
  try {
    await nextTick()
    await waitForStableFrame()
    if (!epubViewer.value || seq !== epubInitSeq) return
    epubViewer.value.innerHTML = ''
    const epubSource = await withTimeout(fastEpubSource(book.value), 15000, 'EPUB 文件读取超时，请重新导入或检查文件')
    if (seq !== epubInitSeq) return
    epubBook = ePub(epubSource)
    await withTimeout(epubBook.ready, 15000, 'EPUB 文件解析超时，请重新导入或检查文件')
    if (seq !== epubInitSeq) return
    await hydrateEpubMetadataAndToc(seq)
    if (seq !== epubInitSeq) return
    createEpubRendition()
    await displayEpubInitialTarget(getInitialEpubTarget())
    if (seq !== epubInitSeq) return
    scrollTocToCurrent()
  } catch (e) {
    console.error('EPUB 加载失败:', e)
    epubError.value = e?.message || 'EPUB 加载失败，请重新导入文件'
  } finally {
    if (seq === epubInitSeq) epubLoading.value = false
  }
}

async function hydrateEpubMetadataAndToc(seq) {
  try {
    if (seq !== epubInitSeq) return
    const metadata = epubBook.package?.metadata || {}
    epubMeta.value = {
      title: metadata.title || book.value.title || '',
      creator: metadata.creator || book.value.author || ''
    }
    const toc = epubBook.navigation?.toc || []
    if (toc.length) {
      chapters.value = flattenEpubToc(toc)
    } else {
      chapters.value = buildSpineChapters()
    }
    if (chapters.value.length) {
      const maxIndex = Math.max(chapters.value.length - 1, 0)
      currentChapterIndex.value = Math.min(Math.max(savedChapterIndex, 0), maxIndex)
    }
  } catch (e) {
    console.warn('EPUB 元数据读取失败，使用 spine 目录:', e)
    chapters.value = buildSpineChapters()
  }
}

function createEpubRendition(options = {}) {
  if (!epubBook || !epubViewer.value) return
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
  if (epubRendition) {
    try { epubRendition.destroy() } catch (e) {}
    epubRendition = null
  }
  epubViewer.value.innerHTML = ''
  epubHasVisibleFrame.value = false
  const size = measureEpubViewer()
  epubRendition = epubBook.renderTo(epubViewer.value, {
    width: size.width,
    height: size.height,
    flow: pageMode.value === 'paged' ? 'paginated' : 'scrolled-doc',
    spread: pageMode.value === 'paged' ? (options.forceSingle ? 'none' : 'auto') : 'none',
    manager: 'default',
    allowScriptedContent: false
  })
  bindEpubResize()
  applyEpubTheme()
  epubRendition.on('relocated', (location) => {
    if (epubDisplaySeq > 0 && location?.start == null) return
    const current = location?.start?.displayed
    epubLocationLabel.value = current ? `${current.page} / ${current.total}` : ''
    epubSavedCfi = location?.start?.cfi || ''
    if (epubSavedCfi) localStorage.setItem(epubProgressKey.value, epubSavedCfi)
    updateEpubChapterIndex(location)
    saveCurrentProgress()
  })
}

async function displayEpubInitialTarget(chapterTarget) {
  const hasCfi = epubSavedCfi && /^epubcfi\(/i.test(epubSavedCfi)
  if (!hasCfi) {
    epubSavedCfi = ''
    localStorage.removeItem(epubProgressKey.value)
    await displayWithFallback(chapterTarget)
    return
  }
  try {
    await waitForEpubDisplay(epubSavedCfi, 7000, 'EPUB 位置恢复超时')
  } catch (displayError) {
    console.warn('EPUB CFI 恢复失败，回退到章节:', displayError)
    epubSavedCfi = ''
    localStorage.removeItem(epubProgressKey.value)
    createEpubRendition()
    await displayWithFallback(chapterTarget)
  }
}

function getInitialEpubTarget() {
  if (!chapters.value.length) return undefined
  const index = Math.min(Math.max(savedChapterIndex, 0), chapters.value.length - 1)
  return chapters.value[index]?.href || epubBook?.spine?.spineItems?.[index]?.href
}

async function displayWithFallback(target) {
  try {
    await waitForEpubDisplay(target, 10000, 'EPUB 目录位置加载超时')
  } catch (targetError) {
    console.warn('EPUB 指定位置加载失败，尝试打开首页:', targetError)
    createEpubRendition()
    try {
      await waitForEpubDisplay(undefined, 10000, 'EPUB 加载超时，请重新导入或检查文件')
    } catch (homeError) {
      if (pageMode.value !== 'paged') throw homeError
      console.warn('EPUB 双页排版失败，降级为单页分页:', homeError)
      createEpubRendition({ forceSingle: true })
      await waitForEpubDisplay(target || undefined, 10000, 'EPUB 加载超时，请切换滚动模式或重新导入文件')
    }
  }
}

function waitForEpubDisplay(target, ms, message) {
  if (!epubRendition) return Promise.reject(new Error('EPUB 阅读器未初始化'))
  const displaySeq = ++epubDisplaySeq
  let settled = false
  let timer = 0
  let frameTimer = 0
  let offRendered = null
  let offRelocated = null
  const cleanup = () => {
    window.clearTimeout(timer)
    window.clearTimeout(frameTimer)
    try {
      if (offRendered) epubRendition.off('rendered', offRendered)
      if (offRelocated) epubRendition.off('relocated', offRelocated)
    } catch (e) {}
  }
  return new Promise((resolve, reject) => {
    const done = () => {
      if (displaySeq !== epubDisplaySeq) return
      if (settled) return
      epubHasVisibleFrame.value = true
      settled = true
      cleanup()
      resolve()
    }
    const fail = (error) => {
      if (displaySeq !== epubDisplaySeq) return
      if (settled) return
      settled = true
      cleanup()
      reject(error instanceof Error ? error : new Error(String(error || message)))
    }
    offRendered = done
    offRelocated = done
    epubRendition.on('rendered', offRendered)
    epubRendition.on('relocated', offRelocated)
    const probeFrame = () => {
      if (settled || displaySeq !== epubDisplaySeq) return
      if (epubViewer.value?.querySelector('iframe, .epub-container, [ref="epubjs-view"]')) {
        epubHasVisibleFrame.value = true
        done()
        return
      }
      frameTimer = window.setTimeout(probeFrame, 120)
    }
    frameTimer = window.setTimeout(probeFrame, 180)
    timer = window.setTimeout(() => fail(new Error(message)), ms)
    try {
      const displayPromise = target ? epubRendition.display(target) : epubRendition.display()
      Promise.resolve(displayPromise).then(done).catch(fail)
    } catch (error) {
      fail(error)
    }
  })
}

function waitForStableFrame() {
  return new Promise(resolve => {
    requestAnimationFrame(() => requestAnimationFrame(resolve))
  })
}

function measureEpubViewer() {
  const rect = epubViewer.value?.getBoundingClientRect()
  return {
    width: Math.max(320, Math.floor(rect?.width || 0)),
    height: Math.max(420, Math.floor(rect?.height || 0))
  }
}

function bindEpubResize() {
  if (!epubViewer.value || !window.ResizeObserver) return
  resizeObserver = new ResizeObserver(() => {
    if (!epubRendition) return
    const size = measureEpubViewer()
    epubRendition.resize(size.width, size.height)
  })
  resizeObserver.observe(epubViewer.value)
}

function flattenEpubToc(items, depth = 0) {
  const result = []
  for (const item of items || []) {
    if (item?.href) {
      result.push({
        id: item.id || item.href,
        title: `${depth ? '　'.repeat(depth) : ''}${item.label || item.title || '未命名章节'}`,
        href: item.href
      })
    }
    result.push(...flattenEpubToc(item?.subitems || item?.children || [], depth + 1))
  }
  return result
}

function buildSpineChapters() {
  const spineItems = epubBook?.spine?.spineItems || []
  return spineItems.map((item, index) => ({
    id: item.idref || item.href || index,
    title: item.label || item.title || `第 ${index + 1} 节`,
    href: item.href
  }))
}

function withTimeout(promise, ms, message) {
  let timer = 0
  return Promise.race([
    Promise.resolve(promise).finally(() => window.clearTimeout(timer)),
    new Promise((_, reject) => {
      timer = window.setTimeout(() => reject(new Error(message)), ms)
    })
  ])
}

async function goToEpubChapter(index) {
  if (!epubRendition || index < 0 || index >= chapters.value.length) return
  epubUserNavSeq += 1
  currentChapterIndex.value = index
  pageIndex.value = 0
  epubLoading.value = false
  epubError.value = ''
  try {
    const target = chapters.value[index].href || epubBook?.spine?.spineItems?.[index]?.href
    epubSavedCfi = ''
    localStorage.removeItem(epubProgressKey.value)
    await waitForEpubDisplay(target, 10000, '章节跳转超时，请重新打开本书')
    scrollTocToCurrent()
    saveCurrentProgress()
  } catch (e) {
    epubError.value = e?.message || '章节跳转失败'
  } finally {
    epubLoading.value = false
  }
}

function updateEpubChapterIndex(location) {
  if (!chapters.value.length) return
  const href = location?.start?.href || location?.start?.cfi || ''
  const cleanHref = href.split('#')[0]
  let matchedIndex = -1
  for (let i = 0; i < chapters.value.length; i++) {
    const chapterHref = (chapters.value[i].href || '').split('#')[0]
    if (chapterHref && (cleanHref.endsWith(chapterHref) || chapterHref.endsWith(cleanHref))) {
      matchedIndex = i
      break
    }
  }
  const spineIndex = Number.isInteger(location?.start?.index) ? location.start.index : -1
  if (matchedIndex < 0 && spineIndex >= 0 && spineIndex < chapters.value.length) matchedIndex = spineIndex
  if (matchedIndex >= 0) currentChapterIndex.value = matchedIndex
}

function triggerPageTurn(direction) {
  window.clearTimeout(pageTurnTimer)
  pageTurnClass.value = direction === 'next' ? 'turn-next' : 'turn-prev'
  pageTurnTimer = window.setTimeout(() => {
    pageTurnClass.value = ''
  }, 420)
}

async function nextPage() {
  if (isEpubBook.value && epubRendition) {
    triggerPageTurn('next')
    await epubRendition.next()
    return
  }
  if (pageMode.value !== 'paged') {
    if (currentChapterIndex.value < chapters.value.length - 1) goToChapter(currentChapterIndex.value + 1)
    return
  }
  if (pageIndex.value + 2 < pagedPages.value.length) {
    triggerPageTurn('next')
    pageIndex.value += 2
    saveCurrentProgress()
    return
  }
  if (currentChapterIndex.value < chapters.value.length - 1) {
    triggerPageTurn('next')
    await loadChapter(currentChapterIndex.value + 1)
  }
}

async function prevPage() {
  if (isEpubBook.value && epubRendition) {
    triggerPageTurn('prev')
    await epubRendition.prev()
    return
  }
  if (pageMode.value !== 'paged') {
    if (currentChapterIndex.value > 0) goToChapter(currentChapterIndex.value - 1)
    return
  }
  if (pageIndex.value > 0) {
    triggerPageTurn('prev')
    pageIndex.value = Math.max(0, pageIndex.value - 2)
    saveCurrentProgress()
    return
  }
  if (currentChapterIndex.value > 0) {
    triggerPageTurn('prev')
    await loadChapter(currentChapterIndex.value - 1)
    await nextTick()
    pageIndex.value = Math.max(0, Math.floor((pagedPages.value.length - 1) / 2) * 2)
  }
}

function buildReadingBlocks(content, htmlMode) {
  if (!content) return []
  if (!htmlMode) {
    return content.split('\n')
      .map(text => text.trim())
      .filter(Boolean)
      .map(text => ({ type: 'text', text, weight: text.length }))
  }
  const doc = new DOMParser().parseFromString(`<main>${content}</main>`, 'text/html')
  const nodes = Array.from(doc.body.firstElementChild?.children || [])
  return nodes.map(node => {
    const tag = node.tagName?.toLowerCase()
    const html = node.outerHTML
    const text = node.textContent?.trim() || ''
    const hasImage = tag === 'img' || node.querySelector?.('img')
    return {
      type: 'html',
      html,
      weight: hasImage ? 1200 : Math.max(text.length, 80)
    }
  }).filter(block => block.html)
}

function paginateBlocks(blocks) {
  const pages = []
  const limit = pageMode.value === 'paged' ? 1050 : 1600
  let current = []
  let size = 0
  for (const block of blocks) {
    const blockSize = block.weight || 120
    if (current.length && size + blockSize > limit) {
      pages.push({ blocks: current })
      current = []
      size = 0
    }
    current.push(block)
    size += blockSize
    if (blockSize >= limit * 0.9) {
      pages.push({ blocks: current })
      current = []
      size = 0
    }
  }
  if (current.length) pages.push({ blocks: current })
  return pages.length ? pages : [{ blocks: [] }]
}

async function scrollTocToCurrent() {
  await nextTick()
  if (!tocScroll.value || tocCollapsed.value) return
  const active = tocScroll.value.querySelector('.toc-item.active')
  if (!active) return
  active.scrollIntoView({ block: 'center', inline: 'nearest' })
}

async function saveCurrentProgress() {
  try {
    await saveProgress(bookId, {
      chapterIndex: currentChapterIndex.value,
      scrollPosition: 0
    })
  } catch (e) {
    console.error('保存进度失败:', e)
  }
}

function handleKeydown(e) {
  if (isEpubBook.value && (e.key === 'ArrowLeft' || e.key === 'PageUp')) {
    e.preventDefault()
    prevPage()
    return
  }
  if (isEpubBook.value && (e.key === 'ArrowRight' || e.key === 'PageDown')) {
    e.preventDefault()
    nextPage()
    return
  }
  if (e.key === 'ArrowLeft') {
    if (pageMode.value === 'paged') {
      e.preventDefault()
      prevPage()
    } else if (currentChapterIndex.value > 0) {
      goToChapter(currentChapterIndex.value - 1)
    }
  } else if (e.key === 'ArrowRight') {
    if (pageMode.value === 'paged') {
      e.preventDefault()
      nextPage()
    } else if (currentChapterIndex.value < chapters.value.length - 1) {
      goToChapter(currentChapterIndex.value + 1)
    }
  } else if (e.key === 'Escape') {
    settingsOpen.value = false
  } else if (e.key === 'PageUp' && pageMode.value === 'paged') {
    e.preventDefault()
    prevPage()
  } else if (e.key === 'PageDown' && pageMode.value === 'paged') {
    e.preventDefault()
    nextPage()
  }
}

watch(fontSize, (v) => {
  readerStore.setFontSize(v)
  applyEpubTheme()
})
watch(lineHeight, (v) => {
  readerStore.setLineHeight(v)
  applyEpubTheme()
})
watch(theme, (v) => {
  readerStore.setTheme(v)
  applyEpubTheme()
})
watch(pageMode, async (v) => {
  localStorage.setItem('readerPageMode', v)
  if (v === 'paged') tocCollapsed.value = true
  await nextTick()
  if (contentWrap.value) {
    contentWrap.value.scrollTop = 0
    contentWrap.value.scrollLeft = 0
  }
  pageIndex.value = 0
  if (isEpubBook.value) await initEpubReader()
})
watch(pagedPages, () => {
  if (pageIndex.value >= pagedPages.value.length) {
    pageIndex.value = Math.max(0, Math.floor((pagedPages.value.length - 1) / 2) * 2)
  }
})
watch(currentChapterIndex, () => scrollTocToCurrent())
watch(tocCollapsed, (collapsed) => {
  if (!collapsed) scrollTocToCurrent()
})

onMounted(async () => {
  window.addEventListener('keydown', handleKeydown)
  try {
    const [bookRes, chRes] = await Promise.all([
      getBookshelf(),
      getChapterList(bookId)
    ])
    book.value = (bookRes.data || []).find(b => b.id === bookId) || { title: '未知' }
    chapters.value = chRes.data || []
    if (isEpubBook.value) {
      try {
        const progRes = await getProgress(bookId)
        if (progRes.data && progRes.data.chapterIndex != null) {
          savedChapterIndex = Math.max(progRes.data.chapterIndex, 0)
          currentChapterIndex.value = savedChapterIndex
        }
      } catch (e) {}
      epubSavedCfi = localStorage.getItem(epubProgressKey.value) || ''
      if (pageMode.value === 'paged') tocCollapsed.value = true
      await nextTick()
      await initEpubReader()
      return
    }

    // 恢复进度
    try {
      const progRes = await getProgress(bookId)
      if (progRes.data && progRes.data.chapterIndex != null) {
        const idx = Math.min(Math.max(progRes.data.chapterIndex, 0), chapters.value.length - 1)
        if (idx !== -1) {
          await loadChapter(idx)
          return
        }
      }
    } catch (e) {}

    if (chapters.value.length) await loadChapter(0)
  } catch (e) {
    console.error('加载阅读器失败:', e)
    chapterError.value = e.message || '加载阅读器失败'
  }
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
  window.clearTimeout(pageTurnTimer)
  destroyEpubReader()
  saveCurrentProgress()
})
</script>

<style scoped>
.reader-root {
    --reader-bg: #FAF8F3;
    --reader-text: #3D2E1A;
    --reader-font-size: 18px;
    --reader-line-height: 1.9;
    --reader-max-width: 720px;
    min-height: 100vh;
    background: var(--reader-bg);
    color: var(--reader-text);
    transition: background 0.2s, color 0.2s;
}

.reader-root.theme-night { --reader-bg: #1A1714; --reader-text: #C8BFA8; }
.reader-root.theme-paper { --reader-bg: #F0E6D2; --reader-text: #4A3820; }
.reader-root.theme-green { --reader-bg: #E8EDE0; --reader-text: #2F3D2A; }

/* 顶部栏 */
.reader-topbar {
    position: sticky;
    top: 0;
    z-index: 50;
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 24px;
    background: rgba(255, 255, 255, 0.72);
    backdrop-filter: blur(20px) saturate(180%);
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.reader-root.theme-night .reader-topbar {
    background: rgba(26, 23, 20, 0.82);
    border-bottom-color: rgba(255, 255, 255, 0.06);
}

.reader-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 5px;
    padding: 6px 12px;
    border: 1px solid transparent;
    background: transparent;
    color: var(--reader-text);
    border-radius: var(--radius-md);
    font-size: var(--font-size-sm);
    font-family: inherit;
    cursor: pointer;
    transition: background 0.12s, border-color 0.12s;
}

.reader-btn:hover { background: rgba(0, 0, 0, 0.05); }
.reader-root.theme-night .reader-btn:hover { background: rgba(255, 255, 255, 0.06); }
.reader-btn svg { width: 15px; height: 15px; fill: currentColor; }

.reader-topbar-title {
    flex: 1;
    text-align: center;
    font-size: var(--font-size-sm);
    font-weight: var(--font-medium);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    opacity: 0.85;
}

/* 目录切换按钮 */
.toc-toggle-btn {
    width: 34px;
    height: 30px;
    border: 1px solid rgba(0, 0, 0, 0.08);
    background: transparent;
    color: var(--reader-text);
    border-radius: var(--radius-sm);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0;
    font-family: -apple-system, "SF Mono", monospace;
    transition: background 0.12s, border-color 0.12s;
}

.toc-toggle-btn:hover {
    background: rgba(0, 0, 0, 0.05);
    border-color: rgba(0, 0, 0, 0.15);
}
.reader-root.theme-night .toc-toggle-btn:hover {
    background: rgba(255, 255, 255, 0.08);
    border-color: rgba(255, 255, 255, 0.15);
}

.toc-toggle-icon {
    font-size: 16px;
    font-weight: 300;
    line-height: 1;
}

/* 主体布局 */
.reader-body {
    display: flex;
    min-height: calc(100vh - 52px);
}

/* 侧边目录 */
.reader-toc {
    width: 260px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    position: sticky;
    top: 52px;
    height: calc(100vh - 52px);
    overflow-y: auto;
    background: rgba(0, 0, 0, 0.02);
    border-right: 1px solid rgba(0, 0, 0, 0.05);
    transition: width 0.25s cubic-bezier(.4,0,.2,1), opacity 0.2s;
    z-index: 10;
}

.reader-root.theme-night .reader-toc {
    background: rgba(255, 255, 255, 0.025);
    border-right-color: rgba(255, 255, 255, 0.05);
}

.reader-toc.collapsed {
    width: 0 !important;
    opacity: 0;
    border-right: 0;
    pointer-events: none;
}

.toc-scroll {
    overflow-y: auto;
    flex: 1;
}

.toc-header {
    padding: 16px 16px 10px;
    font-size: 11px;
    color: var(--reader-text);
    opacity: 0.55;
    font-weight: 600;
    letter-spacing: 0.04em;
    text-transform: uppercase;
    flex-shrink: 0;
}

.toc-list {
    list-style: none;
    padding: 0 8px 24px;
    margin: 0;
}

.toc-item {
    padding: 7px 11px;
    border-radius: var(--radius-sm);
    color: var(--reader-text);
    opacity: 0.7;
    cursor: pointer;
    font-size: var(--font-size-sm);
    line-height: 1.45;
    transition: background 0.1s, opacity 0.1s;
}

.toc-item:hover { background: rgba(0, 0, 0, 0.04); opacity: 1; }
.reader-root.theme-night .toc-item:hover { background: rgba(255, 255, 255, 0.04); }

.toc-item.active {
    background: var(--color-primary-pale);
    color: var(--color-primary-darker);
    opacity: 1;
    font-weight: 500;
}
.reader-root.theme-night .toc-item.active {
    background: rgba(217, 119, 6, 0.18);
    color: #FBBF24;
}

/* 正文区 */
.reader-content-wrap {
    flex: 1;
    overflow-y: auto;
    scroll-behavior: smooth;
}

.reader-content {
    max-width: var(--reader-max-width);
    margin: 0 auto;
    padding: 44px 32px 120px;
    font-size: var(--reader-font-size);
    line-height: var(--reader-line-height);
}

.reader-chapter-title {
    font-size: 1.35em;
    font-weight: var(--font-semibold);
    margin-bottom: 28px;
    text-align: center;
    color: var(--reader-text);
}

.reader-text p {
    margin: 0 0 1.3em;
    text-indent: 2em;
    word-break: break-word;
}

/* 翻页栏 */
.reader-nav {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 14px;
    margin-top: 56px;
    padding-top: 22px;
    border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.reader-root.theme-night .reader-nav { border-top-color: rgba(255, 255, 255, 0.06); }

.reader-nav-btn {
    padding: 9px 20px;
    border-radius: var(--radius-md);
    border: 1px solid rgba(0, 0, 0, 0.1);
    background: transparent;
    color: var(--reader-text);
    font-size: var(--font-size-sm);
    font-family: inherit;
    cursor: pointer;
    transition: background 0.12s, border-color 0.12s;
}

.reader-nav-btn:hover:not(:disabled) {
    background: rgba(0, 0, 0, 0.04);
    border-color: var(--color-primary);
}
.reader-root.theme-night .reader-nav-btn:hover:not(:disabled) {
    background: rgba(255, 255, 255, 0.05);
}

.reader-nav-btn:disabled { opacity: 0.3; cursor: not-allowed; }

.reader-nav-info { font-size: var(--font-size-xs); opacity: 0.55; }

.reader-shortcut-hint {
    text-align: center;
    font-size: 11px;
    color: var(--color-text-tertiary);
    opacity: 0.5;
    margin-top: 16px;
}

.reader-state {
    margin: 56px auto;
    padding: 24px;
    border-radius: var(--radius-lg);
    text-align: center;
    color: var(--reader-text);
    background: rgba(255, 255, 255, 0.48);
    border: 1px solid rgba(0, 0, 0, 0.05);
}

.reader-state.error {
    color: #B45309;
}

.load-more {
    display: block;
    margin: 30px auto 8px;
    padding: 10px 22px;
    border: 1px solid rgba(217, 119, 6, 0.28);
    border-radius: 999px;
    background: rgba(255, 251, 235, 0.82);
    color: #92400E;
    font: inherit;
    font-size: var(--font-size-sm);
    cursor: pointer;
}

.load-more:hover {
    background: rgba(254, 243, 199, 0.95);
}

/* 设置浮窗按钮 */
.reader-settings-fab {
    position: fixed;
    right: 28px;
    bottom: 28px;
    width: 46px;
    height: 46px;
    border-radius: 50%;
    background: var(--color-primary);
    color: #fff;
    border: 0;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 16px rgba(217, 119, 6, 0.35);
    z-index: 60;
    transition: transform 0.15s, background 0.15s, box-shadow 0.15s;
}

.reader-settings-fab:hover {
    background: var(--color-primary-dark);
    transform: scale(1.07);
    box-shadow: 0 6px 22px rgba(217, 119, 6, 0.42);
}

.reader-settings-fab svg { width: 21px; height: 21px; fill: currentColor; }

/* 设置面板 */
.reader-settings-panel {
    position: fixed;
    right: 28px;
    bottom: 86px;
    width: 272px;
    background: #fff;
    color: var(--color-text);
    border-radius: var(--radius-lg);
    box-shadow: 0 10px 34px rgba(0, 0, 0, 0.16);
    padding: 16px 18px;
    z-index: 60;
    opacity: 0;
    transform: translateY(10px) scale(0.95);
    pointer-events: none;
    transition: opacity 0.2s, transform 0.2s;
}

.reader-settings-panel.open {
    opacity: 1;
    transform: translateY(0) scale(1);
    pointer-events: auto;
}

.settings-group {
    padding: 8px 0;
    border-bottom: 1px solid var(--color-divider);
}

.settings-group:last-child { border-bottom: 0; }

.settings-label {
    font-size: 11px;
    color: var(--color-text-tertiary);
    margin-bottom: 8px;
    font-weight: 600;
}

.settings-controls {
    display: flex;
    align-items: center;
    gap: 8px;
}

.settings-step-btn {
    width: 30px;
    height: 28px;
    border: 1px solid var(--color-border-neutral);
    background: #fff;
    color: var(--color-text);
    border-radius: var(--radius-sm);
    cursor: pointer;
    font-size: 15px;
    padding: 0;
}

.settings-step-btn:hover { border-color: var(--color-primary); color: var(--color-primary-darker); }

.settings-value {
    flex: 1;
    text-align: center;
    font-size: var(--font-size-sm);
    font-weight: var(--font-medium);
    color: var(--color-text);
}

.theme-swatches { display: flex; gap: 8px; }

.theme-swatch {
    width: 26px;
    height: 26px;
    border-radius: 50%;
    border: 2px solid transparent;
    cursor: pointer;
    padding: 0;
    transition: transform 0.1s;
}
.theme-swatch:hover { transform: scale(1.08); }
.theme-swatch.selected { box-shadow: 0 0 0 2px #fff, 0 0 0 4px var(--color-text); }

/* 摸鱼入口 */
.reader-stealth-btn {
    font-size: var(--font-size-xs);
    font-weight: var(--font-medium);
    color: var(--color-text-secondary);
    padding: 5px 12px;
    border-radius: 14px;
    border: 1px solid var(--color-border);
    background: rgba(255, 255, 255, 0.6);
    transition: all 0.15s;
    white-space: nowrap;
    user-select: none;
}
.reader-stealth-btn:hover {
    background: var(--color-primary);
    color: #fff;
    border-color: var(--color-primary);
    box-shadow: 0 2px 8px rgba(217, 119, 6, 0.25);
}

.reader-mode-btn {
    border: 1px solid rgba(217, 119, 6, 0.24);
    border-radius: 999px;
    padding: 5px 12px;
    background: rgba(255, 251, 235, 0.62);
    color: var(--color-primary-darker);
    font-size: var(--font-size-xs);
    cursor: pointer;
    white-space: nowrap;
}

.reader-mode-btn.active {
    background: var(--color-primary);
    color: #fff;
}

.reader-html :deep(p),
.reader-html :deep(div) {
    margin: 0 0 1.3em;
    word-break: break-word;
}

.reader-html :deep(h1),
.reader-html :deep(h2),
.reader-html :deep(h3) {
    margin: 1.4em 0 0.8em;
    text-align: center;
    line-height: 1.35;
}

.reader-html :deep(img),
.reader-html :deep(.reader-illustration) {
    display: block;
    max-width: min(100%, 720px);
    max-height: 82vh;
    width: auto;
    height: auto;
    margin: 1.8em auto;
    object-fit: contain;
    border-radius: 8px;
}

.reader-content-wrap.epub-mode {
    overflow: hidden;
}

.reader-content-wrap.epub-mode:not(.paged-mode) {
    overflow-y: auto;
}

.reader-content-wrap.epub-mode .reader-content {
    max-width: none;
    width: 100%;
    min-height: calc(100vh - 52px);
    padding: clamp(10px, 1.4vw, 18px) clamp(14px, 2vw, 26px);
}

.epub-reader-shell {
    display: flex;
    flex-direction: column;
    min-height: calc(100vh - 74px);
    gap: 8px;
}

.epub-reader-footer {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 14px;
    min-height: 24px;
    padding: 0 10px;
    font-size: 11px;
    color: var(--reader-text);
    opacity: 0.48;
    white-space: nowrap;
    overflow: hidden;
}

.epub-reader-footer span {
    overflow: hidden;
    text-overflow: ellipsis;
}

.epub-frame {
    position: relative;
    flex: 1;
    min-height: calc(100vh - 112px);
    border-radius: 12px;
    overflow: hidden;
    background:
        linear-gradient(90deg, rgba(93, 65, 34, 0.08), transparent 7%, transparent 93%, rgba(93, 65, 34, 0.08)),
        rgba(255, 252, 245, 0.74);
    border: 1px solid rgba(91, 68, 40, 0.12);
    box-shadow: 0 24px 70px rgba(66, 48, 25, 0.16);
}

.epub-frame.scroll-frame {
    min-height: calc(100vh - 112px);
    border: 0;
    border-radius: 0;
    box-shadow: none;
    background: transparent;
}

.reader-root.theme-night .epub-frame {
    background: rgba(22, 20, 17, 0.82);
    border-color: rgba(255, 255, 255, 0.08);
    box-shadow: 0 24px 70px rgba(0, 0, 0, 0.34);
}

.reader-root.theme-night .epub-frame.scroll-frame {
    background: transparent;
    box-shadow: none;
}

.epub-frame::before {
    content: '';
    position: absolute;
    top: 0;
    bottom: 0;
    left: 50%;
    width: 24px;
    transform: translateX(-50%);
    background: linear-gradient(90deg, rgba(0,0,0,0.10), rgba(255,255,255,0.24), rgba(0,0,0,0.08));
    opacity: 0.36;
    pointer-events: none;
    z-index: 2;
}

.epub-frame.scroll-frame::before {
    display: none;
}

.epub-viewer {
    position: absolute;
    inset: clamp(18px, 2.2vw, 34px) clamp(20px, 3vw, 48px);
    overflow: hidden;
    contain: strict;
    min-width: 0;
}

.epub-frame.scroll-frame .epub-viewer {
    inset: 0 clamp(10px, 3vw, 54px);
    overflow: auto;
    contain: content;
}

.epub-viewer :deep(iframe) {
    display: block;
    width: 100% !important;
    height: 100% !important;
    max-width: 100% !important;
    max-height: 100% !important;
    border: 0 !important;
    overflow: hidden !important;
}

.epub-frame.scroll-frame .epub-viewer :deep(iframe) {
    height: auto !important;
    min-height: 100% !important;
}

.epub-hit {
    position: absolute;
    top: 0;
    bottom: 0;
    width: 18%;
    z-index: 4;
    border: 0;
    background: transparent;
    cursor: pointer;
}

.epub-hit-left { left: 0; }
.epub-hit-right { right: 0; }

.epub-loading,
.epub-error {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 5;
    background: rgba(250, 248, 243, 0.82);
    color: var(--reader-text);
}

.epub-error {
    color: #B45309;
}

.reader-content-wrap.paged-mode {
    overflow: hidden;
}

.reader-content-wrap.paged-mode .reader-content {
    max-width: none;
    min-height: calc(100vh - 52px);
    padding: 18px clamp(18px, 2.4vw, 42px) 28px;
}

.reader-content-wrap.paged-mode .reader-chapter-title {
    margin-bottom: 18px;
}

.ebook-stage {
    position: relative;
    max-width: 1480px;
    margin: 0 auto;
    perspective: 1800px;
}

.ebook-spread {
    position: relative;
    display: grid;
    grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
    min-height: calc(100vh - 148px);
    background: linear-gradient(90deg, rgba(72, 55, 35, 0.08), transparent 2.6%, transparent 97.4%, rgba(72, 55, 35, 0.08));
    border-radius: 10px;
    box-shadow: 0 22px 58px rgba(53, 43, 31, 0.18);
    transform-style: preserve-3d;
}

.ebook-spread::before {
    content: '';
    position: absolute;
    top: 0;
    bottom: 0;
    left: 50%;
    width: 28px;
    transform: translateX(-50%);
    background: linear-gradient(90deg, rgba(0,0,0,0.12), rgba(255,255,255,0.28), rgba(0,0,0,0.10));
    opacity: 0.42;
    z-index: 2;
    pointer-events: none;
}

.ebook-page {
    position: relative;
    min-height: calc(100vh - 148px);
    padding: 28px 34px 34px;
    overflow: hidden;
    background:
        linear-gradient(90deg, rgba(0,0,0,0.035), transparent 8%),
        var(--reader-bg);
    border: 1px solid rgba(72, 55, 35, 0.08);
}

.ebook-page:first-child {
    border-radius: 10px 0 0 10px;
    transform-origin: right center;
}

.ebook-page:last-child {
    border-radius: 0 10px 10px 0;
    transform-origin: left center;
    background:
        linear-gradient(270deg, rgba(0,0,0,0.035), transparent 8%),
        var(--reader-bg);
}

.ebook-page.empty {
    opacity: 0.58;
}

.ebook-page-head {
    display: flex;
    justify-content: space-between;
    gap: 18px;
    margin-bottom: 18px;
    font-size: 11px;
    color: var(--reader-text);
    opacity: 0.46;
    white-space: nowrap;
}

.ebook-page-body {
    height: calc(100vh - 240px);
    overflow: hidden;
    font-size: var(--reader-font-size);
    line-height: var(--reader-line-height);
}

.ebook-page-body p,
.ebook-html-block :deep(p),
.ebook-html-block :deep(div) {
    margin: 0 0 1.1em;
    text-indent: 2em;
    word-break: break-word;
}

.ebook-html-block :deep(h1),
.ebook-html-block :deep(h2),
.ebook-html-block :deep(h3) {
    margin: 0.8em 0 0.8em;
    text-align: center;
    line-height: 1.35;
}

.ebook-html-block :deep(img) {
    display: block;
    max-width: 100%;
    max-height: calc(100vh - 280px);
    width: auto;
    height: auto;
    margin: 1em auto;
    object-fit: contain;
    border-radius: 8px;
}

.ebook-stage.turn-next .ebook-page:last-child {
    animation: pageTurnNext 0.42s cubic-bezier(.2,.72,.25,1);
}

.ebook-stage.turn-prev .ebook-page:first-child {
    animation: pageTurnPrev 0.42s cubic-bezier(.2,.72,.25,1);
}

.page-hit {
    position: absolute;
    top: 0;
    bottom: 0;
    width: 18%;
    z-index: 5;
    border: 0;
    padding: 0;
    background: transparent;
    cursor: pointer;
}

.page-hit:disabled {
    cursor: default;
}

.page-hit-left { left: 0; }
.page-hit-right { right: 0; }

@keyframes pageTurnNext {
    0% { transform: rotateY(0deg); filter: brightness(1); }
    48% { transform: rotateY(-18deg); filter: brightness(0.96); }
    100% { transform: rotateY(0deg); filter: brightness(1); }
}

@keyframes pageTurnPrev {
    0% { transform: rotateY(0deg); filter: brightness(1); }
    48% { transform: rotateY(18deg); filter: brightness(0.96); }
    100% { transform: rotateY(0deg); filter: brightness(1); }
}

.layout-segment {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 4px;
    padding: 4px;
    border-radius: 999px;
    background: #f3eadc;
}

.layout-segment button {
    border: 0;
    border-radius: 999px;
    padding: 7px 10px;
    background: transparent;
    color: var(--color-text-secondary);
    cursor: pointer;
}

.layout-segment button.active {
    background: #fff;
    color: var(--color-primary-darker);
    font-weight: 700;
}

@media (max-width: 900px) {
    .reader-content-wrap.paged-mode {
        overflow-y: auto;
    }
    .reader-content-wrap.paged-mode .reader-content {
        width: auto;
        padding: 34px 22px 92px;
    }
    .ebook-spread {
        grid-template-columns: 1fr;
        min-height: 70vh;
    }
    .ebook-spread::before,
    .ebook-page:nth-child(2) {
        display: none;
    }
    .ebook-page:first-child {
        border-radius: 10px;
    }
    .ebook-page-body {
        height: auto;
        min-height: 56vh;
        max-height: none;
        overflow: visible;
    }
}
</style>
