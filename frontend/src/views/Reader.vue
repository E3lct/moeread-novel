<template>
  <div class="reader-page" :style="themeStyle" @click="handleTap">
    <!-- 顶部工具栏 -->
    <transition name="fade">
      <header v-show="showToolbar" class="reader-header" :style="headerStyle">
        <button @click.stop="goBack">
          <svg viewBox="0 0 24 24" width="22" height="22">
            <path d="M15 6L9 12L15 18" stroke="currentColor" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        <span class="reader-title">{{ bookTitle }}</span>
        <button @click.stop="showSettings = !showSettings">
          <svg viewBox="0 0 24 24" width="22" height="22">
            <circle cx="12" cy="12" r="3" fill="none" stroke="currentColor" stroke-width="2"/>
            <path d="M12 1v4M12 19v4M4.2 4.2l2.8 2.8M17 17l2.8 2.8M1 12h4M19 12h4M4.2 19.8l2.8-2.8M17 7l2.8-2.8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </button>
      </header>
    </transition>

    <!-- 阅读内容 -->
    <div class="reader-content" ref="contentRef" @scroll="onScroll">
      <div class="content-inner" :style="contentStyle">
        <h1 class="chapter-title">{{ chapter?.title || '加载中...' }}</h1>
        <div class="chapter-text" v-if="chapter?.content">
          <p
            v-for="(para, i) in paragraphs"
            :key="i"
            class="paragraph"
          >{{ para }}</p>
        </div>
        <div v-else class="loading-text">正在加载章节内容...</div>

        <!-- 章节导航 -->
        <div class="chapter-nav" v-if="chapter">
          <button
            class="nav-btn prev"
            :disabled="currentChapterIndex <= 0"
            @click.stop="goChapter(currentChapterIndex - 1)"
          >上一章</button>
          <button
            class="nav-btn next"
            :disabled="currentChapterIndex >= toc.length - 1"
            @click.stop="goChapter(currentChapterIndex + 1)"
          >下一章</button>
        </div>
      </div>
    </div>

    <!-- 底部工具栏 -->
    <transition name="fade">
      <footer v-show="showToolbar" class="reader-footer" :style="footerStyle">
        <button @click.stop="showToc = true">
          <svg viewBox="0 0 24 24" width="20" height="20">
            <line x1="4" y1="6" x2="20" y2="6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <line x1="4" y1="12" x2="20" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <line x1="4" y1="18" x2="20" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <span>目录</span>
        </button>
        <button @click.stop="prevChapter">
          <svg viewBox="0 0 24 24" width="20" height="20">
            <path d="M15 6L9 12L15 18" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>上一章</span>
        </button>
        <button @click.stop="nextChapter">
          <svg viewBox="0 0 24 24" width="20" height="20">
            <path d="M9 6L15 12L9 18" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>下一章</span>
        </button>
        <button @click.stop="enterMuck">
          <svg viewBox="0 0 24 24" width="20" height="20">
            <rect x="3" y="4" width="18" height="14" rx="2" fill="none" stroke="currentColor" stroke-width="2"/>
            <line x1="7" y1="8" x2="17" y2="8" stroke="currentColor" stroke-width="1.5"/>
            <line x1="7" y1="12" x2="13" y2="12" stroke="currentColor" stroke-width="1.5"/>
          </svg>
          <span>摸鱼</span>
        </button>
      </footer>
    </transition>

    <!-- 目录侧栏 -->
    <transition name="slide">
      <div v-if="showToc" class="toc-mask" @click="showToc = false">
        <aside class="toc-sidebar" @click.stop>
          <div class="toc-header">
            <h3>目录</h3>
            <span class="toc-count">{{ toc.length }} 章</span>
          </div>
          <div class="toc-list">
            <div
              v-for="(ch, i) in toc"
              :key="ch.id"
              class="toc-item"
              :class="{ active: i === currentChapterIndex }"
              @click="goChapter(i)"
            >
              <span class="toc-index">{{ i + 1 }}</span>
              <span class="toc-title">{{ ch.title }}</span>
            </div>
          </div>
        </aside>
      </div>
    </transition>

    <!-- 设置面板 -->
    <transition name="fade">
      <div v-if="showSettings" class="settings-panel" @click.stop>
        <h3>阅读设置</h3>

        <div class="setting-row">
          <span>字号</span>
          <div class="setting-controls">
            <button @click="readerStore.setFontSize(readerStore.fontSize - 1)">A-</button>
            <span class="setting-value">{{ readerStore.fontSize }}</span>
            <button @click="readerStore.setFontSize(readerStore.fontSize + 1)">A+</button>
          </div>
        </div>

        <div class="setting-row">
          <span>行距</span>
          <div class="setting-controls">
            <button @click="readerStore.setLineHeight(readerStore.lineHeight - 0.1)">紧凑</button>
            <span class="setting-value">{{ readerStore.lineHeight.toFixed(1) }}</span>
            <button @click="readerStore.setLineHeight(readerStore.lineHeight + 0.1)">宽松</button>
          </div>
        </div>

        <div class="setting-row">
          <span>主题</span>
          <div class="theme-row">
            <button
              v-for="(t, key) in readerStore.themes"
              :key="key"
              class="theme-swatch"
              :class="{ active: readerStore.theme === key }"
              :style="{ background: t.bg, color: t.text }"
              @click="readerStore.setTheme(key)"
            >{{ t.name }}</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useReaderStore } from '../stores/reader'
import { getToc, getChapter } from '../api/chapter'
import { getProgress, saveProgress } from '../api/progress'

const route = useRoute()
const router = useRouter()
const readerStore = useReaderStore()

const bookId = parseInt(route.params.bookId)
const bookTitle = ref('')
const toc = ref([])
const chapter = ref(null)
const currentChapterIndex = ref(0)
const showToolbar = ref(true)
const showToc = ref(false)
const showSettings = ref(false)
const contentRef = ref(null)

let scrollTimer = null
let saveTimer = null

const themeStyle = computed(() => {
  const t = readerStore.themes[readerStore.theme]
  return {
    '--reader-bg': t.bg,
    '--reader-text': t.text
  }
})

const headerStyle = computed(() => ({
  background: readerStore.themes[readerStore.theme].bg,
  color: readerStore.themes[readerStore.theme].text,
  borderBottom: `1px solid rgba(146, 64, 14, 0.1)`
}))

const footerStyle = computed(() => ({
  background: readerStore.themes[readerStore.theme].bg,
  color: readerStore.themes[readerStore.theme].text
}))

const contentStyle = computed(() => ({
  fontSize: readerStore.fontSize + 'px',
  lineHeight: readerStore.lineHeight,
  color: 'var(--reader-text)'
}))

const paragraphs = computed(() => {
  if (!chapter.value?.content) return []
  return chapter.value.content.split('\n').filter(p => p.trim())
})

function handleTap(e) {
  if (showToc.value || showSettings.value) {
    showToc.value = false
    showSettings.value = false
    return
  }
  // 点击中央区域翻页
  const rect = e.currentTarget.getBoundingClientRect()
  const y = e.clientY - rect.top
  const h = rect.height
  if (y < h * 0.3) {
    // 上方：上一页
    scrollBy(-rect.height * 0.8)
  } else if (y > h * 0.7) {
    // 下方：下一页
    scrollBy(rect.height * 0.8)
  } else {
    // 中央：切换工具栏
    showToolbar.value = !showToolbar.value
  }
}

function scrollBy(delta) {
  if (contentRef.value) {
    contentRef.value.scrollBy({ top: delta, behavior: 'smooth' })
  }
}

function onScroll() {
  // 防抖保存进度
  clearTimeout(saveTimer)
  saveTimer = setTimeout(() => {
    saveReadingProgress()
  }, 2000)
}

async function saveReadingProgress() {
  if (!contentRef.value || !chapter.value) return
  const el = contentRef.value
  const percent = el.scrollHeight > el.clientHeight
    ? Math.round(el.scrollTop / (el.scrollHeight - el.clientHeight) * 100)
    : 0
  try {
    await saveProgress(bookId, currentChapterIndex.value, percent)
  } catch (e) {}
}

function goBack() {
  saveReadingProgress()
  router.back()
}

function prevChapter() {
  if (currentChapterIndex.value > 0) goChapter(currentChapterIndex.value - 1)
}

function nextChapter() {
  if (currentChapterIndex.value < toc.value.length - 1) goChapter(currentChapterIndex.value + 1)
}

async function goChapter(index) {
  if (index < 0 || index >= toc.value.length) return
  showToc.value = false
  currentChapterIndex.value = index

  try {
    const res = await getChapter(toc.value[index].id)
    chapter.value = res.data
    bookTitle.value = res.data?.title || chapter.value?.title || ''
    // 滚动到顶部
    await nextTick()
    if (contentRef.value) contentRef.value.scrollTop = 0
    // 保存进度
    await saveProgress(bookId, index, 0)
  } catch (e) {}
}

function enterMuck() {
  saveReadingProgress()
  router.push(`/muck/${bookId}`)
}

function handleKeydown(e) {
  switch (e.key) {
    case 'ArrowLeft':
    case 'ArrowUp':
      e.preventDefault()
      scrollBy(-200)
      break
    case 'ArrowRight':
    case 'ArrowDown':
    case ' ':
      e.preventDefault()
      scrollBy(200)
      break
    case 'Escape':
      if (showToc.value) showToc.value = false
      else if (showSettings.value) showSettings.value = false
      else enterMuck()
      break
  }
}

onMounted(async () => {
  document.addEventListener('keydown', handleKeydown)

  try {
    // 加载目录
    const [tocRes, progressRes] = await Promise.all([
      getToc(bookId),
      getProgress(bookId)
    ])
    toc.value = tocRes.data || []

    // 恢复阅读进度
    let startIdx = 0
    if (progressRes.data?.chapterIndex != null) {
      startIdx = Math.min(progressRes.data.chapterIndex, toc.value.length - 1)
    }
    currentChapterIndex.value = startIdx

    // 加载章节内容
    if (toc.value.length > 0) {
      const chRes = await getChapter(toc.value[startIdx].id)
      chapter.value = chRes.data
      bookTitle.value = chRes.data?.title || ''

      // 恢复滚动位置
      await nextTick()
      requestAnimationFrame(() => {
        if (contentRef.value && progressRes.data?.scrollPercent) {
          const el = contentRef.value
          el.scrollTop = (el.scrollHeight - el.clientHeight) * progressRes.data.scrollPercent / 100
        }
      })
    }
  } catch (e) {}
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
  saveReadingProgress()
})
</script>

<style scoped>
.reader-page {
  height: 100vh;
  background: var(--reader-bg);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

/* 顶部工具栏 */
.reader-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  z-index: 50;
  backdrop-filter: blur(20px);
}

.reader-header button {
  color: inherit;
  cursor: pointer;
  display: flex;
}

.reader-title {
  font-size: 15px;
  font-weight: 600;
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 阅读内容 */
.reader-content {
  flex: 1;
  overflow-y: auto;
  padding: 60px 24px 80px;
  scroll-behavior: smooth;
}

.content-inner {
  max-width: 680px;
  margin: 0 auto;
}

.chapter-title {
  font-size: 22px;
  font-weight: 700;
  text-align: center;
  margin-bottom: 32px;
  color: var(--reader-text);
}

.paragraph {
  margin-bottom: 16px;
  text-indent: 2em;
  color: var(--reader-text);
}

.loading-text {
  text-align: center;
  color: var(--reader-text);
  opacity: 0.5;
  padding: 40px 0;
}

.chapter-nav {
  display: flex;
  gap: 12px;
  margin-top: 48px;
  padding-bottom: 20px;
}

.nav-btn {
  flex: 1;
  padding: 14px 0;
  text-align: center;
  border: 1.5px solid rgba(146, 64, 14, 0.15);
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  color: var(--reader-text);
  cursor: pointer;
  transition: opacity 0.2s;
}

.nav-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* 底部工具栏 */
.reader-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-around;
  z-index: 50;
  backdrop-filter: blur(20px);
  border-top: 1px solid rgba(146, 64, 14, 0.08);
}

.reader-footer button {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  color: inherit;
  font-size: 11px;
  cursor: pointer;
}

/* 目录侧栏 */
.toc-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 100;
}

.toc-sidebar {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 80%;
  max-width: 320px;
  background: #fff;
  display: flex;
  flex-direction: column;
}

.toc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 20px 16px;
  border-bottom: 1px solid var(--color-border);
}

.toc-header h3 {
  font-size: 17px;
  font-weight: 700;
  color: var(--color-text);
}

.toc-count {
  font-size: 13px;
  color: var(--color-text-hint);
}

.toc-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.toc-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  cursor: pointer;
  transition: background 0.15s;
}

.toc-item:active {
  background: var(--color-primary-lightest);
}

.toc-item.active {
  background: var(--color-primary-lightest);
}

.toc-item.active .toc-title {
  color: var(--color-primary-dark);
  font-weight: 600;
}

.toc-index {
  font-size: 12px;
  color: var(--color-text-hint);
  width: 28px;
  flex-shrink: 0;
}

.toc-title {
  font-size: 14px;
  color: var(--color-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 设置面板 */
.settings-panel {
  position: fixed;
  bottom: 60px;
  left: 16px;
  right: 16px;
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  z-index: 60;
  box-shadow: var(--shadow-strong);
}

.settings-panel h3 {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 16px;
}

.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.setting-row > span {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-secondary);
}

.setting-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.setting-controls button {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: var(--color-primary-lightest);
  color: var(--color-primary-dark);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.setting-controls button:active {
  background: var(--color-primary-lighter);
}

.setting-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  min-width: 30px;
  text-align: center;
}

.theme-row {
  display: flex;
  gap: 8px;
}

.theme-swatch {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 500;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.2s;
}

.theme-swatch.active {
  border-color: var(--color-primary);
}
</style>
