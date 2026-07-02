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

      <div class="reader-topbar-title">{{ book.title }} - {{ currentChapter.title }}</div>

      <router-link :to="`/muck/${bookId}`" class="reader-stealth-btn">
        摸鱼模式
      </router-link>
    </div>

    <!-- 主体 -->
    <div class="reader-body">
      <!-- 侧边目录 -->
      <div class="reader-toc" :class="{ collapsed: tocCollapsed }">
        <div class="toc-scroll">
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
      <div class="reader-content-wrap" ref="contentWrap">
        <div class="reader-content">
          <div class="reader-state" v-if="loadingChapter">章节加载中...</div>
          <div class="reader-state error" v-else-if="chapterError">
            <div>{{ chapterError }}</div>
            <button class="reader-nav-btn" @click="loadChapter(currentChapterIndex)">重试</button>
          </div>
          <div v-else-if="!chapters.length" class="reader-state error">这本书还没有可阅读章节，请重新导入或检查书源内容。</div>
          <h2 v-else class="reader-chapter-title">{{ currentChapter.title || '未命名章节' }}</h2>
          <div class="reader-text" v-if="!loadingChapter && !chapterError">
            <p v-for="(para, i) in visibleParagraphs" :key="i">{{ para }}</p>
            <button
              v-if="paragraphs.length > paragraphLimit"
              class="load-more"
              @click="paragraphLimit += paragraphStep"
            >
              继续显示本章内容（{{ paragraphLimit }} / {{ paragraphs.length }}）
            </button>
          </div>

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
          <div class="reader-shortcut-hint">键盘左右键翻页</div>
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
import { getBookshelf } from '../api/book'
import { getChapterList, getChapterContent } from '../api/chapter'
import { getProgress, saveProgress } from '../api/progress'
import { useReaderStore } from '../stores/reader'

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
const loadingChapter = ref(false)
const chapterError = ref('')
const paragraphLimit = ref(520)
const paragraphStep = 520
let loadSeq = 0

const fontSize = ref(readerStore.fontSize || 18)
const lineHeight = ref(readerStore.lineHeight || 1.9)
const theme = ref(readerStore.theme || 'default')

const readerStyle = computed(() => ({
  '--reader-font-size': fontSize.value + 'px',
  '--reader-line-height': lineHeight.value
}))

const paragraphs = computed(() => {
  if (!currentChapter.value.content) return []
  return currentChapter.value.content.split('\n').filter(p => p.trim())
})

const visibleParagraphs = computed(() => paragraphs.value.slice(0, paragraphLimit.value))

async function loadChapter(index) {
  if (index < 0 || index >= chapters.value.length) return
  const seq = ++loadSeq
  currentChapterIndex.value = index
  const ch = chapters.value[index]
  loadingChapter.value = true
  chapterError.value = ''
  paragraphLimit.value = paragraphStep
  currentChapter.value = { ...ch, content: '' }
  try {
    const res = await getChapterContent(ch.id)
    if (seq !== loadSeq) return
    currentChapter.value = res.data || ch
    await nextTick()
    if (contentWrap.value) contentWrap.value.scrollTop = 0
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
  loadChapter(index)
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
  if (e.key === 'ArrowLeft') {
    if (currentChapterIndex.value > 0) goToChapter(currentChapterIndex.value - 1)
  } else if (e.key === 'ArrowRight') {
    if (currentChapterIndex.value < chapters.value.length - 1) goToChapter(currentChapterIndex.value + 1)
  } else if (e.key === 'Escape') {
    settingsOpen.value = false
  }
}

watch(fontSize, (v) => readerStore.setFontSize(v))
watch(lineHeight, (v) => readerStore.setLineHeight(v))
watch(theme, (v) => readerStore.setTheme(v))

onMounted(async () => {
  window.addEventListener('keydown', handleKeydown)
  try {
    const [bookRes, chRes] = await Promise.all([
      getBookshelf(),
      getChapterList(bookId)
    ])
    book.value = (bookRes.data || []).find(b => b.id === bookId) || { title: '未知' }
    chapters.value = chRes.data || []

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
    width: 36px !important;
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
</style>
