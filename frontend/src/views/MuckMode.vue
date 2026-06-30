<template>
  <div class="muck-page" :class="{ 'muck-light': !isDark }">
    <!-- VS Code 顶部栏 -->
    <div class="vscode-titlebar">
      <div class="traffic-lights">
        <span class="light red"></span>
        <span class="light yellow"></span>
        <span class="light green"></span>
      </div>
      <div class="menu-items">
        <span>File</span>
        <span>Edit</span>
        <span>Selection</span>
        <span>View</span>
        <span>Go</span>
        <span>Run</span>
        <span>Terminal</span>
        <span>Help</span>
      </div>
      <div class="title-center">{{ fileName }}</div>
      <div class="title-right"></div>
    </div>

    <!-- VS Code 主体 -->
    <div class="vscode-body">
      <!-- 活动栏 -->
      <div class="activity-bar">
        <div class="activity-icon active">
          <svg viewBox="0 0 24 24" width="24" height="24">
            <path d="M3 3h7v7H3zM14 3h7v7h-7zM14 14h7v7h-7zM3 14h7v7H3z" fill="none" stroke="currentColor" stroke-width="1.5"/>
          </svg>
        </div>
        <div class="activity-icon">
          <svg viewBox="0 0 24 24" width="24" height="24">
            <circle cx="12" cy="12" r="9" fill="none" stroke="currentColor" stroke-width="1.5"/>
            <path d="M12 8v4l3 2" stroke="currentColor" stroke-width="1.5" fill="none" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="activity-icon">
          <svg viewBox="0 0 24 24" width="24" height="24">
            <path d="M21 6l-3-3-9 9-3-3-3 3 6 6z" fill="none" stroke="currentColor" stroke-width="1.5"/>
          </svg>
        </div>
      </div>

      <!-- 侧边栏 -->
      <div class="sidebar">
        <div class="sidebar-header">EXPLORER</div>
        <div class="file-tree">
          <div class="folder-item">
            <svg viewBox="0 0 16 16" width="14" height="14"><path d="M1 3h5l1 2h8v9H1z" fill="none" stroke="currentColor" stroke-width="1"/></svg>
            <span>src</span>
          </div>
          <div class="file-item active">
            <svg viewBox="0 0 16 16" width="14" height="14"><path d="M3 2h7l3 3v9H3z" fill="none" stroke="currentColor" stroke-width="1"/></svg>
            <span>{{ fileName }}</span>
          </div>
          <div class="file-item">
            <svg viewBox="0 0 16 16" width="14" height="14"><path d="M3 2h7l3 3v9H3z" fill="none" stroke="currentColor" stroke-width="1"/></svg>
            <span>utils.ts</span>
          </div>
          <div class="file-item">
            <svg viewBox="0 0 16 16" width="14" height="14"><path d="M3 2h7l3 3v9H3z" fill="none" stroke="currentColor" stroke-width="1"/></svg>
            <span>config.ts</span>
          </div>
        </div>
      </div>

      <!-- 编辑器区域 -->
      <div class="editor-area">
        <!-- 标签页 -->
        <div class="tab-bar">
          <div class="tab active">
            <span>{{ fileName }}</span>
            <svg viewBox="0 0 16 16" width="12" height="12"><line x1="4" y1="4" x2="12" y2="12" stroke="currentColor" stroke-width="1.5"/><line x1="12" y1="4" x2="4" y2="12" stroke="currentColor" stroke-width="1.5"/></svg>
          </div>
        </div>

        <!-- 代码内容 -->
        <div class="code-area" ref="codeArea" @scroll="onCodeScroll">
          <div class="code-content">
            <div
              v-for="(line, i) in codeLines"
              :key="i"
              class="code-line"
            >
              <span class="line-number">{{ i + 1 }}</span>
              <span class="line-content" v-html="line"></span>
            </div>
          </div>
        </div>

        <!-- 状态栏 -->
        <div class="status-bar">
          <div class="status-left">
            <span class="status-item">
              <svg viewBox="0 0 16 16" width="12" height="12"><circle cx="8" cy="8" r="6" fill="none" stroke="currentColor" stroke-width="1"/></svg>
              main
            </span>
            <span class="status-item">0 errors</span>
            <span class="status-item">0 warnings</span>
          </div>
          <div class="status-right">
            <span class="status-item" @click="toggleTheme">{{ isDark ? 'Dark' : 'Light' }}</span>
            <span class="status-item">UTF-8</span>
            <span class="status-item">TypeScript</span>
            <span class="status-item">Ln {{ currentLine }}, Col 1</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部提示 -->
    <div class="muck-hint">
      <span>Alt + &larr;/&rarr; 翻页 · ESC 退出</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getToc, getChapter } from '../api/chapter'
import { getProgress, saveProgress } from '../api/progress'

const route = useRoute()
const router = useRouter()

const bookId = parseInt(route.params.bookId)
const isDark = ref(true)
const toc = ref([])
const chapter = ref(null)
const currentChapterIndex = ref(0)
const codeArea = ref(null)
const currentLine = ref(1)

const fileName = computed(() => `main.ts`)

const codeLines = computed(() => {
  if (!chapter.value?.content) return ['// Loading...']
  const paragraphs = chapter.value.content.split('\n').filter(p => p.trim())

  const lines = []
  // 伪装代码头部
  lines.push('<span class="syn-import">import</span> { <span class="syn-var">UserService</span> } <span class="syn-import">from</span> <span class="syn-str">\'./services\'</span>;')
  lines.push('<span class="syn-import">import</span> { <span class="syn-var">Logger</span> } <span class="syn-import">from</span> <span class="syn-str">\'./utils\'</span>;')
  lines.push('')
  lines.push('<span class="syn-comment">/**</span>')
  lines.push('<span class="syn-comment"> * ' + (chapter.value.title || 'Chapter') + '</span>')
  lines.push('<span class="syn-comment"> */</span>')
  lines.push('<span class="syn-keyword">export</span> <span class="syn-keyword">class</span> <span class="syn-class">MainController</span> {')
  lines.push('  <span class="syn-keyword">private</span> <span class="syn-var">logger</span>: <span class="syn-type">Logger</span>;')
  lines.push('')

  paragraphs.forEach((para, i) => {
    // 将阅读内容伪装成注释
    const escaped = para.replace(/</g, '&lt;').replace(/>/g, '&gt;')
    if (i === 0) {
      lines.push(`  <span class="syn-comment">  // ${escaped}</span>`)
    } else if (i % 3 === 0) {
      lines.push(`  <span class="syn-comment">  /* ${escaped} */</span>`)
    } else if (i % 3 === 1) {
      lines.push(`  <span class="syn-comment">  // ${escaped}</span>`)
    } else {
      lines.push(`  <span class="syn-comment">  /** ${escaped} */</span>`)
    }

    // 偶尔插入假代码
    if (i % 5 === 4) {
      lines.push(`  <span class="syn-keyword">  if</span> (<span class="syn-var">data</span>.<span class="syn-prop">isValid</span>) {`)
      lines.push(`  <span class="syn-var">    this</span>.<span class="syn-prop">logger</span>.<span class="syn-fn">info</span>(<span class="syn-str">\'Processing...\'</span>);`)
      lines.push(`  }`)
    }
  })

  lines.push('')
  lines.push('  <span class="syn-comment">// End of file</span>')
  lines.push('}')

  return lines
})

function toggleTheme() {
  isDark.value = !isDark.value
}

function onCodeScroll() {
  if (!codeArea.value) return
  const lineHeight = 21
  currentLine.value = Math.floor(codeArea.value.scrollTop / lineHeight) + 1
}

function handleKeydown(e) {
  if (e.key === 'Escape') {
    e.preventDefault()
    saveProgressAndExit()
  } else if (e.altKey) {
    if (e.key === 'ArrowLeft') {
      e.preventDefault()
      prevChapter()
    } else if (e.key === 'ArrowRight') {
      e.preventDefault()
      nextChapter()
    }
  }
}

function prevChapter() {
  if (currentChapterIndex.value > 0) goChapter(currentChapterIndex.value - 1)
}

function nextChapter() {
  if (currentChapterIndex.value < toc.value.length - 1) goChapter(currentChapterIndex.value + 1)
}

async function goChapter(index) {
  if (index < 0 || index >= toc.value.length) return
  currentChapterIndex.value = index
  try {
    const res = await getChapter(toc.value[index].id)
    chapter.value = res.data
    await nextTick()
    if (codeArea.value) codeArea.value.scrollTop = 0
    await saveProgress(bookId, index, 0)
  } catch (e) {}
}

function saveProgressAndExit() {
  const percent = codeArea.value
    ? Math.round(codeArea.value.scrollTop / Math.max(codeArea.value.scrollHeight - codeArea.value.clientHeight, 1) * 100)
    : 0
  saveProgress(bookId, currentChapterIndex.value, percent).catch(() => {})
  router.replace(`/reader/${bookId}`)
}

onMounted(async () => {
  document.addEventListener('keydown', handleKeydown)
  try {
    const [tocRes, progressRes] = await Promise.all([
      getToc(bookId),
      getProgress(bookId)
    ])
    toc.value = tocRes.data || []
    let startIdx = 0
    if (progressRes.data?.chapterIndex != null) {
      startIdx = Math.min(progressRes.data.chapterIndex, toc.value.length - 1)
    }
    currentChapterIndex.value = startIdx
    if (toc.value.length > 0) {
      const chRes = await getChapter(toc.value[startIdx].id)
      chapter.value = chRes.data
    }
  } catch (e) {}
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
/* 暗色主题 (默认) */
.muck-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #1e1e1e;
  color: #d4d4d4;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  overflow: hidden;
}

/* 浅色主题 */
.muck-light {
  background: #ffffff;
  color: #333333;
}

.muck-light .vscode-titlebar {
  background: #dddddd;
  color: #333;
}

.muck-light .activity-bar {
  background: #2c2c2c;
}

.muck-light .sidebar {
  background: #f3f3f3;
  color: #333;
}

.muck-light .tab-bar {
  background: #dddddd;
}

.muck-light .tab.active {
  background: #ffffff;
  color: #333;
}

.muck-light .code-area {
  background: #ffffff;
}

.muck-light .line-number {
  color: #999;
}

.muck-light .syn-comment {
  color: #008000;
}

.muck-light .syn-keyword {
  color: #0000ff;
}

.muck-light .syn-str {
  color: #a31515;
}

.muck-light .status-bar {
  background: #007acc;
}

/* 标题栏 */
.vscode-titlebar {
  display: flex;
  align-items: center;
  height: 35px;
  background: #3c3c3c;
  color: #cccccc;
  padding: 0 12px;
  flex-shrink: 0;
  font-size: 12px;
  font-family: -apple-system, sans-serif;
}

.traffic-lights {
  display: flex;
  gap: 8px;
  margin-right: 20px;
}

.light {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.red { background: #ff5f57; }
.yellow { background: #febc2e; }
.green { background: #28c840; }

.menu-items {
  display: flex;
  gap: 16px;
}

.title-center {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  opacity: 0.8;
}

/* 主体 */
.vscode-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 活动栏 */
.activity-bar {
  width: 48px;
  background: #333333;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 8px;
  gap: 8px;
  flex-shrink: 0;
}

.activity-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #858585;
  cursor: pointer;
}

.activity-icon.active {
  color: #fff;
  border-left: 2px solid #fff;
}

/* 侧边栏 */
.sidebar {
  width: 200px;
  background: #252526;
  color: #cccccc;
  flex-shrink: 0;
  overflow-y: auto;
}

.sidebar-header {
  padding: 10px 16px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 1px;
  color: #bbbbbb;
}

.file-tree {
  padding: 0 8px;
}

.folder-item, .file-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  font-size: 13px;
  cursor: pointer;
  border-radius: 3px;
}

.file-item {
  padding-left: 24px;
}

.folder-item:hover, .file-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.file-item.active {
  background: rgba(14, 99, 156, 0.3);
}

/* 编辑器 */
.editor-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.tab-bar {
  display: flex;
  height: 35px;
  background: #252526;
  flex-shrink: 0;
}

.tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 14px;
  font-size: 13px;
  color: #969696;
  cursor: pointer;
  border-right: 1px solid #1e1e1e;
  font-family: -apple-system, sans-serif;
}

.tab.active {
  background: #1e1e1e;
  color: #fff;
}

/* 代码区域 */
.code-area {
  flex: 1;
  overflow-y: auto;
  background: #1e1e1e;
  padding: 8px 0;
}

.code-content {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 21px;
}

.code-line {
  display: flex;
  padding: 0 16px;
}

.line-number {
  width: 40px;
  text-align: right;
  padding-right: 16px;
  color: #858585;
  user-select: none;
  flex-shrink: 0;
}

.line-content {
  white-space: pre-wrap;
  word-break: break-all;
}

/* 语法高亮 */
.syn-comment {
  color: #6a9955;
}

.syn-keyword {
  color: #569cd6;
}

.syn-import {
  color: #c586c0;
}

.syn-str {
  color: #ce9178;
}

.syn-var {
  color: #9cdcfe;
}

.syn-type {
  color: #4ec9b0;
}

.syn-class {
  color: #4ec9b0;
}

.syn-prop {
  color: #9cdcfe;
}

.syn-fn {
  color: #dcdcaa;
}

/* 状态栏 */
.status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 22px;
  background: #007acc;
  color: #fff;
  font-size: 11px;
  padding: 0 8px;
  flex-shrink: 0;
  font-family: -apple-system, sans-serif;
}

.status-left, .status-right {
  display: flex;
  gap: 12px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}

/* 底部提示 */
.muck-hint {
  position: fixed;
  bottom: 30px;
  right: 20px;
  background: rgba(0, 0, 0, 0.7);
  color: rgba(255, 255, 255, 0.6);
  font-size: 11px;
  padding: 6px 12px;
  border-radius: 4px;
  z-index: 100;
  pointer-events: none;
  font-family: -apple-system, sans-serif;
}
</style>
