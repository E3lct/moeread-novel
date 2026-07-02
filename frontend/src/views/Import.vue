<template>
  <div class="source-page">
    <header class="source-hero">
      <div>
        <span class="eyebrow">Moeread Library</span>
        <h1>导入与书源</h1>
        <p>从本地文件、书源搜索和 JSON 书源包中，把书放进你的书架。</p>
      </div>
      <router-link class="ghost-link" to="/bookshelf">回到书架</router-link>
    </header>

    <div class="mode-tabs">
      <button :class="{ active: mode === 'store' }" @click="mode = 'store'">书源书城</button>
      <button :class="{ active: mode === 'local' }" @click="mode = 'local'">本地导入</button>
      <button :class="{ active: mode === 'sources' }" @click="mode = 'sources'">书源管理</button>
    </div>

    <section v-if="mode === 'store'" class="glass-panel store-panel">
      <div class="search-strip">
        <select v-model.number="searchSourceId">
          <option :value="0">全部启用书源</option>
          <option v-for="source in enabledSources" :key="source.sourceId" :value="source.sourceId">
            {{ source.name }}
          </option>
        </select>
        <input v-model="keyword" placeholder="搜索书名或作者" @keyup.enter="searchBooks" />
        <button class="primary-action" :disabled="searching" @click="searchBooks">
          {{ searching ? '搜索中' : '搜索' }}
        </button>
      </div>

      <div class="source-empty" v-if="!enabledSources.length">
        还没有启用书源，先去“书源管理”导入或添加一个。
      </div>

      <div class="result-grid" v-if="searchResults.length">
        <article class="result-card" v-for="book in searchResults" :key="book.sourceId + book.contentUrl">
          <div class="result-cover" :style="book.coverUrl ? { backgroundImage: `url(${book.coverUrl})` } : {}">
            <span v-if="!book.coverUrl">{{ book.title.slice(0, 6) }}</span>
          </div>
          <div class="result-info">
            <span class="source-badge">{{ book.sourceName }}</span>
            <h3>{{ book.title }}</h3>
            <p class="result-author">{{ book.author || '未知作者' }}</p>
            <p class="result-desc">{{ book.description || '这个书源没有提供简介。' }}</p>
          </div>
          <button class="download-btn" @click="downloadSourceBook(book)">下载</button>
        </article>
      </div>
    </section>

    <section v-if="mode === 'local'" class="glass-panel local-panel">
      <div class="local-head">
        <div>
          <h2>本地文件</h2>
          <p>支持 TXT 单本和 ZIP 批量导入。</p>
        </div>
        <div class="format-pill">
          <button :class="{ active: format === 'txt' }" @click="format = 'txt'">TXT</button>
          <button :class="{ active: format === 'zip' }" @click="format = 'zip'">ZIP</button>
        </div>
      </div>

      <div
        class="drop-zone"
        :class="{ dragover: isDragover }"
        @click="triggerFileInput"
        @dragover.prevent="isDragover = true"
        @dragleave.prevent="isDragover = false"
        @drop.prevent="handleDrop"
      >
        <div class="drop-title">{{ selectedFile ? selectedFile.name : '点击或拖入文件' }}</div>
        <div class="drop-meta">{{ selectedFile ? formatFileSize(selectedFile.size) : `${format.toUpperCase()} 文件` }}</div>
        <input ref="fileInput" type="file" :accept="format === 'txt' ? '.txt' : '.zip'" @change="handleFileSelect" hidden />
      </div>

      <div class="panel-actions">
        <button class="primary-action" :disabled="!selectedFile || importing" @click="doImport">
          {{ importing ? '导入中' : '开始导入' }}
        </button>
        <button class="soft-action" v-if="selectedFile" @click="selectedFile = null">清除</button>
      </div>
    </section>

    <section v-if="mode === 'sources'" class="sources-layout">
      <div class="glass-panel source-editor">
        <h2>订阅与添加书源</h2>
        <div class="subscription-box">
          <div class="subscription-head">
            <strong>可信订阅入口</strong>
            <span>导入前请确认书源合法性和可用性</span>
          </div>
          <div class="subscription-list">
            <button
              v-for="sub in subscriptions"
              :key="sub.url"
              class="subscription-card"
              @click="subscribe(sub.url)"
            >
              <strong>{{ sub.name }}</strong>
              <span>{{ sub.note }}</span>
            </button>
          </div>
          <div class="subscribe-line">
            <input v-model="subscribeUrl" placeholder="粘贴 JSON 书源订阅地址" />
            <button class="soft-action" @click="subscribe(subscribeUrl)">订阅</button>
          </div>
        </div>
        <div class="editor-grid">
          <input v-model="customSource.name" placeholder="书源名称" />
          <input v-model="customSource.baseUrl" placeholder="基础地址，可选" />
          <input class="wide" v-model="customSource.searchUrl" placeholder="搜索 URL，例如 https://api.example.com/search?q={keyword}" />
          <input class="wide" v-model="customSource.contentUrlTemplate" placeholder="下载模板，可选，例如 https://api.example.com/book/{id}.txt" />
        </div>
        <div class="panel-actions">
          <button class="primary-action" @click="addCustom">添加书源</button>
          <label class="soft-action file-action">
            导入书源 JSON
            <input type="file" accept=".json,application/json" @change="importSourceJson" hidden />
          </label>
        </div>
        <pre class="source-format">[
  {
    "name": "示例书源",
    "sourceType": "json_api",
    "baseUrl": "https://example.com",
    "searchUrl": "https://example.com/search?q={keyword}",
    "contentUrlTemplate": "https://example.com/books/{id}.txt"
  }
]</pre>
      </div>

      <div class="glass-panel source-list">
        <div class="source-list-head">
          <h2>我的书源</h2>
          <button class="soft-action" @click="reloadSources">刷新</button>
        </div>
        <div class="source-row" v-for="source in sources" :key="source.sourceId">
          <div>
            <strong>{{ source.name }}</strong>
            <span>{{ source.searchUrl || source.baseUrl || source.sourceType }}</span>
          </div>
          <label class="switch">
            <input type="checkbox" :checked="Number(source.enabled) === 1" @change="toggleSource(source, $event.target.checked)" />
            <i></i>
          </label>
        </div>
        <div class="source-empty" v-if="!sources.length">暂无书源</div>
      </div>
    </section>

    <div class="toast success" v-if="importResult">{{ importResult }}</div>
    <div class="toast error" v-if="importError">{{ importError }}</div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { importTxt, importZip } from '../api/book'
import {
  addCustomSource,
  addSourceBatch,
  getSourceSubscriptions,
  getSourceList,
  importFromSource,
  searchSourceBooks,
  subscribeSource,
  updateSourceEnabled
} from '../api/source'

const mode = ref('store')
const format = ref('txt')
const selectedFile = ref(null)
const isDragover = ref(false)
const importing = ref(false)
const searching = ref(false)
const importResult = ref('')
const importError = ref('')
const fileInput = ref(null)
const sources = ref([])
const subscriptions = ref([])
const keyword = ref('')
const searchSourceId = ref(0)
const searchResults = ref([])
const subscribeUrl = ref('')

const customSource = reactive({
  name: '',
  baseUrl: '',
  searchUrl: '',
  contentUrlTemplate: ''
})

const enabledSources = computed(() => sources.value.filter(s => Number(s.enabled) === 1))

function triggerFileInput() {
  fileInput.value?.click()
}

function handleFileSelect(e) {
  const file = e.target.files[0]
  if (file) selectedFile.value = file
}

function handleDrop(e) {
  isDragover.value = false
  const file = e.dataTransfer.files[0]
  if (file) selectedFile.value = file
}

function formatFileSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

function showResult(message) {
  importResult.value = message
  importError.value = ''
  setTimeout(() => { importResult.value = '' }, 3200)
}

function showError(message) {
  importError.value = message
  importResult.value = ''
  setTimeout(() => { importError.value = '' }, 4600)
}

async function doImport() {
  if (!selectedFile.value) return
  importing.value = true
  try {
    const res = format.value === 'txt' ? await importTxt(selectedFile.value) : await importZip(selectedFile.value)
    const count = Array.isArray(res.data) ? res.data.length : 1
    showResult(`导入成功，新增 ${count} 本书`)
    selectedFile.value = null
    if (fileInput.value) fileInput.value.value = ''
  } catch (e) {
    showError(e.message || '导入失败')
  } finally {
    importing.value = false
  }
}

async function reloadSources() {
  const res = await getSourceList()
  sources.value = res.data || []
}

async function reloadSubscriptions() {
  const res = await getSourceSubscriptions()
  subscriptions.value = res.data || []
}

async function subscribe(url) {
  if (!url || !url.trim()) {
    showError('请输入订阅地址')
    return
  }
  try {
    const res = await subscribeSource(url.trim())
    await reloadSources()
    subscribeUrl.value = ''
    showResult(`已订阅 ${res.data?.length || 0} 个书源`)
  } catch (e) {
    showError(e.message || '订阅失败')
  }
}

async function addCustom() {
  if (!customSource.name.trim()) {
    showError('请输入书源名称')
    return
  }
  try {
    await addCustomSource({
      name: customSource.name,
      baseUrl: customSource.baseUrl,
      searchUrl: customSource.searchUrl,
      contentUrlTemplate: customSource.contentUrlTemplate,
      sourceKey: 'custom-' + Date.now(),
      sourceType: 'json_api',
      language: 'custom',
      enabled: 1
    })
    Object.assign(customSource, { name: '', baseUrl: '', searchUrl: '', contentUrlTemplate: '' })
    await reloadSources()
    showResult('书源已添加')
  } catch (e) {
    showError(e.message || '添加失败')
  }
}

async function importSourceJson(e) {
  const file = e.target.files[0]
  if (!file) return
  try {
    const text = await file.text()
    const data = JSON.parse(text)
    const list = Array.isArray(data) ? data : (data.sources || [])
    if (!Array.isArray(list) || !list.length) throw new Error('书源 JSON 为空')
    await addSourceBatch(list.map((item, i) => ({
      name: item.name || `书源 ${i + 1}`,
      sourceKey: item.sourceKey || `json-${Date.now()}-${i}`,
      sourceType: item.sourceType || 'json_api',
      baseUrl: item.baseUrl || '',
      searchUrl: item.searchUrl || '',
      contentUrlTemplate: item.contentUrlTemplate || '',
      description: item.description || '',
      language: item.language || 'custom',
      enabled: item.enabled ?? 1
    })))
    await reloadSources()
    showResult(`已导入 ${list.length} 个书源`)
  } catch (err) {
    showError(err.message || '书源 JSON 解析失败')
  } finally {
    e.target.value = ''
  }
}

async function toggleSource(source, enabled) {
  try {
    await updateSourceEnabled(source.sourceId, enabled)
    source.enabled = enabled ? 1 : 0
  } catch (e) {
    showError(e.message || '更新失败')
  }
}

async function searchBooks() {
  if (!keyword.value.trim()) {
    showError('请输入关键词')
    return
  }
  searching.value = true
  try {
    const res = await searchSourceBooks(keyword.value.trim(), searchSourceId.value)
    searchResults.value = res.data || []
    if (!searchResults.value.length) showError('没有搜到可下载结果')
  } catch (e) {
    showError(e.message || '搜索失败')
  } finally {
    searching.value = false
  }
}

async function downloadSourceBook(book) {
  try {
    const res = await importFromSource({
      sourceId: book.sourceId,
      title: book.title,
      author: book.author,
      contentUrl: book.contentUrl,
      coverUrl: book.coverUrl,
      description: book.description
    })
    showResult(`《${res.data.title}》已下载到书架`)
  } catch (e) {
    showError(e.message || '下载失败')
  }
}

onMounted(async () => {
  await Promise.all([reloadSources(), reloadSubscriptions()])
})
</script>

<style scoped>
.source-page {
  min-height: calc(100vh - 90px);
}

.source-hero {
  min-height: 180px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  padding: 34px;
  border-radius: 28px;
  background:
    linear-gradient(120deg, rgba(44, 59, 54, 0.92), rgba(112, 85, 54, 0.82)),
    radial-gradient(circle at 20% 10%, rgba(255,255,255,0.26), transparent 32%);
  color: #fff;
  box-shadow: 0 24px 80px rgba(48, 42, 33, 0.18);
}

.eyebrow {
  display: block;
  margin-bottom: 8px;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  opacity: 0.72;
}

.source-hero h1 {
  font-size: 40px;
  line-height: 1.05;
  margin: 0 0 10px;
  letter-spacing: 0;
}

.source-hero p {
  margin: 0;
  color: rgba(255,255,255,0.78);
}

.ghost-link,
.soft-action,
.primary-action {
  border: 0;
  border-radius: 999px;
  padding: 10px 18px;
  font: inherit;
  cursor: pointer;
  white-space: nowrap;
}

.ghost-link {
  color: #fff;
  background: rgba(255,255,255,0.16);
  border: 1px solid rgba(255,255,255,0.26);
}

.primary-action {
  color: #fff;
  background: #2f5d50;
  box-shadow: 0 10px 24px rgba(47, 93, 80, 0.22);
}

.primary-action:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.soft-action {
  color: #38564f;
  background: rgba(47, 93, 80, 0.10);
}

.mode-tabs {
  display: flex;
  gap: 8px;
  margin: 20px 0;
  padding: 6px;
  width: fit-content;
  border-radius: 999px;
  background: rgba(255,255,255,0.72);
  border: 1px solid rgba(226, 219, 204, 0.9);
}

.mode-tabs button {
  border: 0;
  border-radius: 999px;
  padding: 9px 18px;
  background: transparent;
  color: #70665a;
  cursor: pointer;
  font: inherit;
}

.mode-tabs button.active {
  background: #2f5d50;
  color: #fff;
}

.glass-panel {
  border: 1px solid rgba(226, 219, 204, 0.92);
  border-radius: 24px;
  background: rgba(255,255,255,0.76);
  box-shadow: 0 20px 60px rgba(48, 42, 33, 0.10);
  backdrop-filter: blur(18px);
}

.store-panel,
.local-panel,
.source-editor,
.source-list {
  padding: 24px;
}

.search-strip {
  display: grid;
  grid-template-columns: 210px minmax(0, 1fr) auto;
  gap: 10px;
  margin-bottom: 22px;
}

.search-strip input,
.search-strip select,
.editor-grid input,
.subscribe-line input {
  width: 100%;
  border: 1px solid #ddd4c4;
  border-radius: 16px;
  padding: 12px 14px;
  background: rgba(255,255,255,0.86);
  color: #332a20;
  font: inherit;
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 14px;
}

.result-card {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr);
  gap: 14px;
  position: relative;
  padding: 14px;
  border-radius: 18px;
  background: #fffdf8;
  border: 1px solid #eee4d3;
}

.result-cover {
  width: 72px;
  height: 102px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px;
  background: linear-gradient(145deg, #44675d, #b58a53);
  background-size: cover;
  background-position: center;
  color: #fff;
  font-size: 13px;
  text-align: center;
}

.source-badge {
  display: inline-block;
  margin-bottom: 5px;
  color: #2f5d50;
  font-size: 11px;
  font-weight: 700;
}

.result-info h3 {
  margin: 0 0 4px;
  font-size: 17px;
  color: #2f271f;
}

.result-author,
.result-desc {
  margin: 0;
  color: #756b5f;
  font-size: 12px;
  line-height: 1.55;
}

.result-desc {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.download-btn {
  position: absolute;
  right: 12px;
  bottom: 12px;
  border: 0;
  border-radius: 999px;
  padding: 6px 12px;
  background: #efe6d5;
  color: #3f594f;
  cursor: pointer;
}

.local-head,
.source-list-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.local-head h2,
.source-editor h2,
.source-list h2 {
  margin: 0 0 5px;
}

.local-head p {
  margin: 0;
  color: #7d7366;
}

.format-pill {
  display: flex;
  padding: 4px;
  border-radius: 999px;
  background: #f1eadf;
}

.format-pill button {
  border: 0;
  border-radius: 999px;
  padding: 8px 16px;
  background: transparent;
  cursor: pointer;
}

.format-pill button.active {
  background: #fff;
  color: #2f5d50;
}

.drop-zone {
  min-height: 240px;
  border: 1.5px dashed #d8cbb8;
  border-radius: 22px;
  background: linear-gradient(135deg, #fffaf1, #f4f2ea);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.drop-zone.dragover {
  border-color: #2f5d50;
}

.drop-title {
  font-size: 20px;
  font-weight: 800;
}

.drop-meta {
  margin-top: 6px;
  color: #7f7364;
}

.panel-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
}

.sources-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 18px;
}

.editor-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.subscription-box {
  margin-bottom: 18px;
  padding: 14px;
  border-radius: 18px;
  background: rgba(255, 250, 241, 0.78);
  border: 1px solid #eee4d3;
}

.subscription-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  color: #3f362b;
}

.subscription-head span {
  color: #8a7b68;
  font-size: 12px;
}

.subscription-list {
  display: grid;
  gap: 10px;
  margin-bottom: 12px;
}

.subscription-card {
  border: 1px solid #eadcc8;
  border-radius: 14px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.72);
  text-align: left;
  cursor: pointer;
  font: inherit;
}

.subscription-card strong,
.subscription-card span {
  display: block;
}

.subscription-card span {
  margin-top: 4px;
  color: #7b6e60;
  font-size: 12px;
  line-height: 1.5;
}

.subscribe-line {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
}

.editor-grid .wide {
  grid-column: 1 / -1;
}

.file-action {
  display: inline-flex;
  align-items: center;
}

.source-format {
  margin: 18px 0 0;
  padding: 14px;
  border-radius: 16px;
  background: #26231e;
  color: #f3e9d9;
  overflow-x: auto;
  font-size: 12px;
}

.source-row {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  padding: 14px 0;
  border-top: 1px solid #eee4d3;
}

.source-row strong,
.source-row span {
  display: block;
}

.source-row span {
  margin-top: 3px;
  color: #84796d;
  font-size: 12px;
  overflow-wrap: anywhere;
}

.switch {
  position: relative;
  width: 44px;
  height: 24px;
  flex-shrink: 0;
}

.switch input {
  opacity: 0;
}

.switch i {
  position: absolute;
  inset: 0;
  border-radius: 999px;
  background: #d8d1c7;
}

.switch i::after {
  content: '';
  position: absolute;
  width: 18px;
  height: 18px;
  top: 3px;
  left: 3px;
  border-radius: 50%;
  background: #fff;
  transition: transform 0.15s;
}

.switch input:checked + i {
  background: #2f5d50;
}

.switch input:checked + i::after {
  transform: translateX(20px);
}

.source-empty {
  padding: 22px;
  color: #7d7366;
  text-align: center;
}

.toast {
  position: fixed;
  right: 28px;
  bottom: 28px;
  padding: 11px 16px;
  border-radius: 999px;
  background: #fff;
  box-shadow: 0 12px 32px rgba(0,0,0,0.15);
  z-index: 1200;
  font-weight: 700;
}

.toast.success { color: #047857; }
.toast.error { color: #dc2626; }

@media (max-width: 900px) {
  .source-hero,
  .local-head,
  .source-list-head {
    flex-direction: column;
    align-items: stretch;
  }
  .search-strip,
  .sources-layout,
  .editor-grid,
  .subscribe-line {
    grid-template-columns: 1fr;
  }
  .source-hero h1 {
    font-size: 32px;
  }
}
</style>
