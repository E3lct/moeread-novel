<template>
  <div class="page-container import-page">
    <header class="import-header">
      <button class="back-btn" @click="router.back()">
        <svg viewBox="0 0 24 24" width="22" height="22">
          <path d="M15 6L9 12L15 18" stroke="currentColor" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      <h2>导入书籍</h2>
    </header>

    <!-- 导入方式 -->
    <div class="import-cards">
      <!-- TXT 导入 -->
      <div class="import-card card" @click="triggerUpload('txt')">
        <div class="card-icon txt-icon">
          <svg viewBox="0 0 32 32" width="32" height="32">
            <path d="M8 4h12l6 6v18a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
            <path d="M20 4v6h6" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
            <text x="16" y="22" text-anchor="middle" fill="currentColor" font-size="7" font-weight="bold">TXT</text>
          </svg>
        </div>
        <div class="card-info">
          <h3>TXT 单文件导入</h3>
          <p>自动识别章节，支持中文编码</p>
        </div>
      </div>

      <!-- ZIP 批量导入 -->
      <div class="import-card card" @click="triggerUpload('zip')">
        <div class="card-icon zip-icon">
          <svg viewBox="0 0 32 32" width="32" height="32">
            <path d="M8 4h12l6 6v18a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
            <path d="M20 4v6h6" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
            <rect x="14" y="12" width="4" height="4" fill="currentColor"/>
            <rect x="14" y="18" width="4" height="4" fill="currentColor"/>
          </svg>
        </div>
        <div class="card-info">
          <h3>ZIP 批量导入</h3>
          <p>打包多个 TXT，一次导入</p>
        </div>
      </div>
    </div>

    <!-- 拖拽区域 -->
    <div
      class="drop-zone"
      :class="{ dragging: isDragging }"
      @dragover.prevent="isDragging = true"
      @dragleave.prevent="isDragging = false"
      @drop.prevent="handleDrop"
    >
      <svg viewBox="0 0 48 48" width="48" height="48">
        <path d="M24 6L24 32M14 22L24 6L34 22" stroke="currentColor" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
        <path d="M8 36L8 42L40 42L40 36" stroke="currentColor" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
      <p class="drop-text">将文件拖到此处</p>
      <p class="drop-hint">支持 .txt 和 .zip 格式</p>
    </div>

    <!-- 导入进度 -->
    <div v-if="importing" class="progress-section">
      <p class="progress-text">{{ progressText }}</p>
      <div class="progress-bar">
        <div class="progress-fill"></div>
      </div>
    </div>

    <!-- 导入结果 -->
    <div v-if="importResult" class="result-section">
      <p class="result-text" v-if="importResult.success">
        导入成功：{{ importResult.message }}
      </p>
      <p class="result-text error" v-else>
        导入失败：{{ importResult.message }}
      </p>
      <button v-if="importResult.success" class="btn-primary result-btn" @click="router.push('/bookshelf')">
        去书架查看
      </button>
    </div>

    <input ref="fileInput" type="file" :accept="acceptType" hidden @change="handleFileSelect" />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { importTxt, importZip } from '../api/book'

const router = useRouter()

const fileInput = ref(null)
const importType = ref('txt')
const importing = ref(false)
const progressText = ref('')
const importResult = ref(null)
const isDragging = ref(false)

const acceptType = computed(() => {
  if (importType.value === 'zip') return '.zip'
  return '.txt'
})

function triggerUpload(type) {
  importType.value = type
  importResult.value = null
  fileInput.value?.click()
}

async function handleFileSelect(e) {
  const file = e.target.files[0]
  if (file) await doImport(file)
  e.target.value = ''
}

function handleDrop(e) {
  isDragging.value = false
  const file = e.dataTransfer.files[0]
  if (!file) return

  if (file.name.endsWith('.zip')) {
    importType.value = 'zip'
  } else if (file.name.endsWith('.txt')) {
    importType.value = 'txt'
  } else {
    alert('不支持的文件格式')
    return
  }

  importResult.value = null
  doImport(file)
}

async function doImport(file) {
  importing.value = true
  progressText.value = '正在导入...'
  importResult.value = null

  try {
    let res
    if (importType.value === 'zip') {
      res = await importZip(file)
      importResult.value = {
        success: true,
        message: `成功导入 ${res.data?.length || 0} 本书`
      }
    } else {
      res = await importTxt(file)
      importResult.value = {
        success: true,
        message: res.data?.title || file.name
      }
    }
  } catch (err) {
    importResult.value = {
      success: false,
      message: err.response?.data?.msg || '未知错误'
    }
  } finally {
    importing.value = false
  }
}
</script>

<style scoped>
.import-page {
  padding: 16px 16px 0;
}

.import-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 4px 20px;
}

.back-btn {
  display: flex;
  color: var(--color-text);
  cursor: pointer;
}

.import-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text);
}

.import-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.import-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  cursor: pointer;
  transition: transform 0.15s;
}

.import-card:active {
  transform: scale(0.98);
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.txt-icon {
  background: var(--color-primary-lightest);
  color: var(--color-primary-dark);
}

.zip-icon {
  background: #FEF3C7;
  color: var(--color-primary-darker);
}

.card-info h3 {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
}

.card-info p {
  font-size: 12px;
  color: var(--color-text-hint);
  margin-top: 4px;
}

/* 拖拽区域 */
.drop-zone {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  border: 2px dashed var(--color-border);
  border-radius: var(--radius-lg);
  color: var(--color-text-hint);
  transition: all 0.2s;
  background: var(--color-card);
}

.drop-zone.dragging {
  border-color: var(--color-primary);
  background: var(--color-primary-lightest);
  color: var(--color-primary-dark);
}

.drop-text {
  font-size: 14px;
  font-weight: 600;
  margin-top: 12px;
}

.drop-hint {
  font-size: 12px;
  margin-top: 4px;
}

/* 进度 */
.progress-section {
  margin-top: 20px;
  text-align: center;
}

.progress-text {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
}

.progress-bar {
  width: 100%;
  height: 4px;
  background: var(--color-primary-lighter);
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--color-primary), var(--color-primary-dark));
  border-radius: 2px;
  animation: indeterminate 1.5s ease-in-out infinite;
}

@keyframes indeterminate {
  0% { width: 0; margin-left: 0; }
  50% { width: 60%; margin-left: 20%; }
  100% { width: 0; margin-left: 100%; }
}

/* 结果 */
.result-section {
  margin-top: 20px;
  text-align: center;
}

.result-text {
  font-size: 14px;
  color: var(--color-primary-dark);
  font-weight: 600;
  margin-bottom: 16px;
}

.result-text.error {
  color: #DC2626;
}

.result-btn {
  max-width: 200px;
  margin: 0 auto;
}
</style>
