<template>
  <div class="import-page">
    <div class="page-header import-header">
      <div>
        <div class="page-title">导入中心</div>
        <div class="page-subtitle">本地文件、开放书源和个人目录统一管理</div>
      </div>
      <router-link to="/bookshelf" class="btn btn-ghost">返回书架</router-link>
    </div>

    <div class="import-workspace">
      <aside class="import-rail">
        <button class="rail-item" :class="{ active: mode === 'local' }" @click="mode = 'local'">
          <span class="rail-kicker">文件</span>
          <span class="rail-title">本地导入</span>
          <span class="rail-meta">TXT / ZIP</span>
        </button>
        <button class="rail-item" :class="{ active: mode === 'source' }" @click="mode = 'source'">
          <span class="rail-kicker">书源</span>
          <span class="rail-title">开放书源</span>
          <span class="rail-meta">{{ enabledSources.length }} 个已启用</span>
        </button>
      </aside>

      <section class="import-panel" v-if="mode === 'local'">
        <div class="panel-head">
          <div>
            <h2>本地文件</h2>
            <p>选择 TXT 单本或 ZIP 批量文件</p>
          </div>
          <div class="format-switch">
            <button :class="{ active: format === 'txt' }" @click="format = 'txt'">TXT</button>
            <button :class="{ active: format === 'zip' }" @click="format = 'zip'">ZIP</button>
          </div>
        </div>

        <div
          class="upload-zone"
          :class="{ dragover: isDragover }"
          @click="triggerFileInput"
          @dragover.prevent="isDragover = true"
          @dragleave.prevent="isDragover = false"
          @drop.prevent="handleDrop"
        >
          <svg class="upload-mark" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <path d="M17 8l-5-5-5 5"/>
            <path d="M12 3v12"/>
          </svg>
          <div class="upload-title">{{ selectedFile ? selectedFile.name : '选择文件' }}</div>
          <div class="upload-meta">{{ selectedFile ? formatFileSize(selectedFile.size) : format.toUpperCase() }}</div>
          <input
            ref="fileInput"
            class="upload-input"
            type="file"
            :accept="format === 'txt' ? '.txt' : '.zip'"
            @change="handleFileSelect"
          />
        </div>

        <div class="action-row">
          <button class="btn btn-primary" :disabled="!selectedFile || importing" @click="doImport">
            {{ importing ? '导入中' : '开始导入' }}
          </button>
          <button class="btn btn-ghost" v-if="selectedFile" @click="selectedFile = null">清除</button>
        </div>
      </section>

      <section class="import-panel" v-else>
        <div class="panel-head">
          <div>
            <h2>开放书源</h2>
            <p>选择书源后，从公开文本地址导入</p>
          </div>
          <button class="btn btn-ghost" @click="reloadSources">刷新</button>
        </div>

        <div class="source-grid">
          <button
            v-for="preset in presets"
            :key="preset.sourceKey"
            class="source-preset"
            @click="addPreset(preset.sourceKey)"
          >
            <span class="source-name">{{ preset.name }}</span>
            <span class="source-desc">{{ preset.description }}</span>
            <span class="source-lang">{{ preset.language }}</span>
          </button>
        </div>

        <div class="source-form">
          <div class="form-grid">
            <label>
              <span>选择书源</span>
              <select class="input" v-model.number="sourceImport.sourceId">
                <option :value="0">请选择</option>
                <option v-for="source in enabledSources" :key="source.sourceId" :value="source.sourceId">
                  {{ source.name }}
                </option>
              </select>
            </label>
            <label>
              <span>书名</span>
              <input class="input" v-model="sourceImport.title" placeholder="留空则从地址推断" />
            </label>
            <label>
              <span>作者</span>
              <input class="input" v-model="sourceImport.author" placeholder="未知作者" />
            </label>
            <label class="wide">
              <span>公开 TXT 地址</span>
              <input class="input" v-model="sourceImport.contentUrl" placeholder="https://example.com/book.txt" />
            </label>
          </div>
          <div class="action-row">
            <button class="btn btn-primary" :disabled="sourceImporting" @click="doSourceImport">
              {{ sourceImporting ? '导入中' : '从书源导入' }}
            </button>
          </div>
        </div>

        <div class="custom-source">
          <div class="custom-title">自定义书源</div>
          <div class="custom-row">
            <input class="input" v-model="customSource.name" placeholder="名称" />
            <input class="input" v-model="customSource.baseUrl" placeholder="目录地址" />
            <button class="btn btn-ghost" @click="addCustom">添加</button>
          </div>
        </div>
      </section>
    </div>

    <section class="source-list" v-if="sources.length">
      <div class="source-list-head">
        <span>我的书源</span>
        <span>{{ sources.length }} 个</span>
      </div>
      <div class="source-list-body">
        <div class="source-row" v-for="source in sources" :key="source.sourceId">
          <div>
            <div class="source-row-name">{{ source.name }}</div>
            <div class="source-row-url">{{ source.baseUrl || source.sourceType }}</div>
          </div>
          <label class="source-toggle">
            <input type="checkbox" :checked="Number(source.enabled) === 1" @change="toggleSource(source, $event.target.checked)" />
            <span></span>
          </label>
        </div>
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
  addPresetSource,
  getSourceList,
  getSourcePresets,
  importFromSource,
  updateSourceEnabled
} from '../api/source'

const mode = ref('local')
const format = ref('txt')
const selectedFile = ref(null)
const isDragover = ref(false)
const importing = ref(false)
const sourceImporting = ref(false)
const importResult = ref('')
const importError = ref('')
const fileInput = ref(null)
const presets = ref([])
const sources = ref([])

const sourceImport = reactive({
  sourceId: 0,
  title: '',
  author: '',
  contentUrl: ''
})

const customSource = reactive({
  name: '',
  baseUrl: ''
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
  setTimeout(() => { importError.value = '' }, 4200)
}

async function doImport() {
  if (!selectedFile.value) return
  importing.value = true
  try {
    const res = format.value === 'txt'
      ? await importTxt(selectedFile.value)
      : await importZip(selectedFile.value)
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
  const [presetRes, sourceRes] = await Promise.all([getSourcePresets(), getSourceList()])
  presets.value = presetRes.data || []
  sources.value = sourceRes.data || []
  if (!sourceImport.sourceId && enabledSources.value.length) {
    sourceImport.sourceId = enabledSources.value[0].sourceId
  }
}

async function addPreset(sourceKey) {
  try {
    await addPresetSource(sourceKey)
    await reloadSources()
    showResult('书源已添加')
  } catch (e) {
    showError(e.message || '添加失败')
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
      sourceKey: 'custom-' + Date.now(),
      sourceType: 'custom',
      language: 'custom',
      enabled: 1
    })
    customSource.name = ''
    customSource.baseUrl = ''
    await reloadSources()
    showResult('自定义书源已添加')
  } catch (e) {
    showError(e.message || '添加失败')
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

async function doSourceImport() {
  if (!sourceImport.sourceId) {
    showError('请选择书源')
    return
  }
  if (!sourceImport.contentUrl.trim()) {
    showError('请输入公开 TXT 地址')
    return
  }
  sourceImporting.value = true
  try {
    const res = await importFromSource({
      sourceId: sourceImport.sourceId,
      title: sourceImport.title,
      author: sourceImport.author,
      contentUrl: sourceImport.contentUrl
    })
    showResult(`《${res.data.title}》已导入`)
    sourceImport.title = ''
    sourceImport.author = ''
    sourceImport.contentUrl = ''
  } catch (e) {
    showError(e.message || '书源导入失败')
  } finally {
    sourceImporting.value = false
  }
}

onMounted(reloadSources)
</script>

<style scoped>
.import-page {
  min-height: calc(100vh - var(--nav-height));
  padding-bottom: 48px;
}

.import-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 18px;
}

.import-workspace {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 18px;
  align-items: stretch;
}

.import-rail {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.rail-item {
  border: 1px solid var(--color-border-neutral);
  background: rgba(255, 255, 255, 0.72);
  border-radius: 8px;
  padding: 16px;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s, transform 0.15s;
  font-family: inherit;
}

.rail-item:hover {
  transform: translateY(-1px);
  border-color: var(--color-primary-light);
}

.rail-item.active {
  background: #fff;
  border-color: var(--color-primary);
  box-shadow: 0 8px 22px rgba(61, 46, 26, 0.08);
}

.rail-kicker,
.rail-meta {
  display: block;
  font-size: 11px;
  color: var(--color-text-tertiary);
}

.rail-title {
  display: block;
  margin: 6px 0 4px;
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text);
}

.import-panel,
.source-list {
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid var(--color-border-light);
  border-radius: 10px;
  padding: 22px;
  box-shadow: 0 10px 28px rgba(61, 46, 26, 0.06);
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 18px;
}

.panel-head h2 {
  font-size: 20px;
  line-height: 1.2;
  margin: 0 0 4px;
}

.panel-head p {
  color: var(--color-text-tertiary);
  font-size: 13px;
}

.format-switch {
  display: inline-flex;
  padding: 3px;
  border: 1px solid var(--color-border-neutral);
  border-radius: 8px;
  background: #fff;
}

.format-switch button {
  border: 0;
  border-radius: 6px;
  padding: 7px 16px;
  background: transparent;
  color: var(--color-text-secondary);
  cursor: pointer;
  font-family: inherit;
}

.format-switch button.active {
  background: var(--color-primary-pale);
  color: var(--color-primary-darker);
  font-weight: 700;
}

.upload-zone {
  min-height: 260px;
  border: 1.5px dashed var(--color-border);
  border-radius: 10px;
  background: linear-gradient(145deg, #fff, #fffbeb);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
}

.upload-zone.dragover,
.upload-zone:hover {
  border-color: var(--color-primary);
  background: #fff7df;
}

.upload-mark {
  width: 38px;
  height: 38px;
  color: var(--color-primary-dark);
}

.upload-title {
  max-width: 70%;
  font-size: 18px;
  font-weight: 700;
  text-align: center;
  overflow-wrap: anywhere;
}

.upload-meta {
  color: var(--color-text-tertiary);
  font-size: 12px;
}

.upload-input {
  display: none;
}

.action-row {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
}

.source-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 18px;
}

.source-preset {
  min-height: 120px;
  border: 1px solid var(--color-border-neutral);
  border-radius: 8px;
  background: #fff;
  padding: 14px;
  text-align: left;
  cursor: pointer;
  font-family: inherit;
  transition: border-color 0.15s, transform 0.15s;
}

.source-preset:hover {
  border-color: var(--color-primary);
  transform: translateY(-1px);
}

.source-name,
.source-desc,
.source-lang {
  display: block;
}

.source-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 8px;
}

.source-desc {
  min-height: 38px;
  font-size: 12px;
  line-height: 1.55;
  color: var(--color-text-secondary);
}

.source-lang {
  margin-top: 10px;
  width: fit-content;
  padding: 2px 8px;
  border-radius: 999px;
  background: #eef4f2;
  color: #37695e;
  font-size: 11px;
}

.source-form,
.custom-source {
  border-top: 1px solid var(--color-divider);
  padding-top: 18px;
  margin-top: 18px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.form-grid label,
.custom-title {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 12px;
  font-weight: 700;
  color: var(--color-text-secondary);
}

.form-grid .wide {
  grid-column: 1 / -1;
}

.custom-row {
  display: grid;
  grid-template-columns: 180px 1fr auto;
  gap: 10px;
  margin-top: 10px;
}

.source-list {
  margin-top: 18px;
  padding: 0;
  overflow: hidden;
}

.source-list-head,
.source-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.source-list-head {
  padding: 14px 18px;
  border-bottom: 1px solid var(--color-divider);
  font-weight: 700;
}

.source-list-body {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.source-row {
  padding: 14px 18px;
  border-bottom: 1px solid var(--color-divider);
}

.source-row:nth-child(odd) {
  border-right: 1px solid var(--color-divider);
}

.source-row-name {
  font-weight: 700;
  color: var(--color-text);
}

.source-row-url {
  max-width: 360px;
  margin-top: 3px;
  color: var(--color-text-tertiary);
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.source-toggle {
  position: relative;
  width: 40px;
  height: 22px;
  flex-shrink: 0;
}

.source-toggle input {
  opacity: 0;
}

.source-toggle span {
  position: absolute;
  inset: 0;
  border-radius: 999px;
  background: #d1d5db;
  cursor: pointer;
  transition: background 0.15s;
}

.source-toggle span::after {
  content: '';
  position: absolute;
  top: 3px;
  left: 3px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #fff;
  transition: transform 0.15s;
}

.source-toggle input:checked + span {
  background: var(--color-primary);
}

.source-toggle input:checked + span::after {
  transform: translateX(18px);
}

.toast {
  position: fixed;
  right: 28px;
  bottom: 28px;
  padding: 11px 16px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 32px rgba(0,0,0,0.15);
  z-index: 1200;
  font-weight: 700;
}

.toast.success {
  color: #047857;
}

.toast.error {
  color: #dc2626;
}

@media (max-width: 860px) {
  .import-workspace,
  .source-list-body,
  .source-grid,
  .form-grid,
  .custom-row {
    grid-template-columns: 1fr;
  }

  .source-row:nth-child(odd) {
    border-right: 0;
  }
}
</style>
