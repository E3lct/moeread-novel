<template>
  <div class="page-container bookshelf-page">
    <!-- 顶部搜索 -->
    <header class="shelf-header">
      <h2 class="shelf-title">书架</h2>
      <div class="search-box">
        <svg viewBox="0 0 24 24" width="18" height="18" class="search-icon">
          <circle cx="11" cy="11" r="7" fill="none" stroke="currentColor" stroke-width="2"/>
          <line x1="16.5" y1="16.5" x2="21" y2="21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索书名..."
          @input="onSearch"
        />
        <button v-if="keyword" class="clear-btn" @click="keyword = ''; onSearch()">
          <svg viewBox="0 0 24 24" width="16" height="16">
            <circle cx="12" cy="12" r="10" fill="currentColor" opacity="0.15"/>
            <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </button>
      </div>
    </header>

    <!-- 视图切换 + 标签筛选 -->
    <div class="filter-bar">
      <div class="view-toggle">
        <button :class="{ active: viewMode === 'grid' }" @click="viewMode = 'grid'">网格</button>
        <button :class="{ active: viewMode === 'group' }" @click="viewMode = 'group'">分组</button>
      </div>
    </div>

    <!-- 标签栏 -->
    <div class="tag-bar">
      <button
        class="tag-chip"
        :class="{ active: activeTag === 'all' }"
        @click="activeTag = 'all'"
      >全部</button>
      <button
        v-for="tag in systemTags"
        :key="tag.value"
        class="tag-chip"
        :class="{ active: activeTag === tag.value }"
        @click="activeTag = tag.value"
      >{{ tag.label }}</button>
    </div>

    <!-- 网格视图 -->
    <div v-if="viewMode === 'grid'" class="book-grid">
      <div
        v-for="book in filteredBooks"
        :key="book.id"
        class="grid-item"
        @click="openReader(book)"
        @contextmenu.prevent="openEdit(book)"
      >
        <BookCard :book="book" />
        <button class="edit-btn" @click.stop="openEdit(book)">
          <svg viewBox="0 0 24 24" width="14" height="14">
            <path d="M12 20h9M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- 分组视图 -->
    <div v-else class="group-view">
      <div v-for="group in groupedBooks" :key="group.label" class="group-section" v-show="group.books.length">
        <h3 class="group-title">{{ group.label }} ({{ group.books.length }})</h3>
        <div class="book-grid">
          <div
            v-for="book in group.books"
            :key="book.id"
            class="grid-item"
            @click="openReader(book)"
          >
            <BookCard :book="book" />
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && filteredBooks.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg viewBox="0 0 64 64" width="64" height="64">
          <rect x="12" y="10" width="40" height="48" rx="4" fill="none" stroke="#FDE68A" stroke-width="2.5"/>
          <line x1="22" y1="22" x2="42" y2="22" stroke="#FDE68A" stroke-width="2" stroke-linecap="round"/>
          <line x1="22" y1="32" x2="42" y2="32" stroke="#FDE68A" stroke-width="2" stroke-linecap="round"/>
          <line x1="22" y1="42" x2="36" y2="42" stroke="#FDE68A" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </div>
      <p class="empty-text">还没有书籍</p>
      <button class="btn-primary empty-btn" @click="router.push('/import')">去导入</button>
    </div>

    <!-- 编辑弹窗 -->
    <transition name="fade">
      <div v-if="editDialog" class="modal-mask" @click="editDialog = false">
        <div class="modal-content" @click.stop>
          <h3 class="modal-title">编辑书籍</h3>

          <!-- 封面上传 -->
          <div class="cover-upload" @click="triggerCoverUpload">
            <img v-if="editingBook.coverImagePath" :src="coverUrl(editingBook)" class="upload-preview" />
            <div v-else class="upload-placeholder">
              <svg viewBox="0 0 24 24" width="28" height="28">
                <rect x="3" y="3" width="18" height="18" rx="2" fill="none" stroke="currentColor" stroke-width="2"/>
                <circle cx="8.5" cy="8.5" r="1.5" fill="currentColor"/>
                <path d="M21 15l-5-5L5 21" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span>上传封面</span>
            </div>
            <input ref="coverInput" type="file" accept="image/*" hidden @change="handleCoverUpload" />
          </div>

          <div class="input-group">
            <label>书名</label>
            <input v-model="editingBook.title" type="text" placeholder="书名" />
          </div>

          <div class="input-group">
            <label>作者</label>
            <input v-model="editingBook.author" type="text" placeholder="作者" />
          </div>

          <div class="input-group">
            <label>简介</label>
            <textarea v-model="editingBook.description" rows="3" placeholder="简介"></textarea>
          </div>

          <!-- 状态选择 -->
          <div class="input-group">
            <label>阅读状态</label>
            <div class="status-row">
              <button
                v-for="s in statusOptions"
                :key="s.value"
                class="status-chip"
                :class="{ active: editingBook.status === s.value }"
                @click="editingBook.status = s.value"
              >{{ s.label }}</button>
            </div>
          </div>

          <div class="modal-actions">
            <button class="btn-cancel" @click="editDialog = false">取消</button>
            <button class="btn-danger" @click="handleDelete">删除</button>
            <button class="btn-save" @click="handleSave">保存</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getBookList, searchBooks, updateBook, deleteBook, uploadCover } from '../api/book'
import BookCard from '../components/BookCard.vue'

const router = useRouter()

const loading = ref(true)
const books = ref([])
const keyword = ref('')
const viewMode = ref('grid')
const activeTag = ref('all')

const systemTags = [
  { label: '喜欢', value: 'favorite' },
  { label: '正在看', value: 'reading' },
  { label: '准备看', value: 'pending' },
  { label: '已看完', value: 'finished' }
]

const statusOptions = [
  { label: '准备看', value: 'pending' },
  { label: '正在看', value: 'reading' },
  { label: '已看完', value: 'finished' }
]

// 编辑弹窗
const editDialog = ref(false)
const editingBook = ref({})
const coverInput = ref(null)

const filteredBooks = computed(() => {
  let result = books.value
  if (activeTag.value === 'favorite') {
    result = result.filter(b => b.isFavorite)
  } else if (activeTag.value !== 'all') {
    result = result.filter(b => b.status === activeTag.value)
  }
  return result
})

const groupedBooks = computed(() => {
  const groups = [
    { label: '正在看', books: [] },
    { label: '准备看', books: [] },
    { label: '已看完', books: [] },
    { label: '其他', books: [] }
  ]
  books.value.forEach(b => {
    if (b.status === 'reading') groups[0].books.push(b)
    else if (b.status === 'pending') groups[1].books.push(b)
    else if (b.status === 'finished') groups[2].books.push(b)
    else groups[3].books.push(b)
  })
  return groups
})

function coverUrl(book) {
  if (!book.coverImagePath) return ''
  const path = book.coverImagePath
  if (path.startsWith('http')) return path
  return '/api' + path
}

function openReader(book) {
  router.push(`/reader/${book.id}`)
}

function openEdit(book) {
  editingBook.value = { ...book }
  editDialog.value = true
}

function triggerCoverUpload() {
  coverInput.value?.click()
}

async function handleCoverUpload(e) {
  const file = e.target.files[0]
  if (!file) return
  try {
    const res = await uploadCover(file)
    editingBook.value.coverImagePath = res.data.path
  } catch (err) {
    // 错误已处理
  }
  e.target.value = ''
}

async function handleSave() {
  try {
    await updateBook(editingBook.value.id, {
      title: editingBook.value.title,
      author: editingBook.value.author,
      description: editingBook.value.description,
      coverImagePath: editingBook.value.coverImagePath,
      status: editingBook.value.status,
      isFavorite: editingBook.value.isFavorite
    })
    // 更新本地数据
    const idx = books.value.findIndex(b => b.id === editingBook.value.id)
    if (idx > -1) books.value[idx] = { ...editingBook.value }
    editDialog.value = false
  } catch (err) {
    // 错误已处理
  }
}

async function handleDelete() {
  if (!confirm('确定删除这本书吗？')) return
  try {
    await deleteBook(editingBook.value.id)
    books.value = books.value.filter(b => b.id !== editingBook.value.id)
    editDialog.value = false
  } catch (err) {
    // 错误已处理
  }
}

let searchTimer = null
function onSearch() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(async () => {
    if (!keyword.value.trim()) {
      await loadBooks()
    } else {
      try {
        const res = await searchBooks(keyword.value.trim())
        books.value = res.data || []
      } catch (e) {}
    }
  }, 300)
}

async function loadBooks() {
  loading.value = true
  try {
    const res = await getBookList()
    books.value = res.data || []
  } catch (e) {} finally {
    loading.value = false
  }
}

onMounted(loadBooks)
</script>

<style scoped>
.bookshelf-page {
  padding: 16px 16px 0;
}

/* 头部 */
.shelf-header {
  padding: 12px 4px 16px;
}

.shelf-title {
  font-size: 24px;
  font-weight: 800;
  color: var(--color-text);
  margin-bottom: 14px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  border-radius: 12px;
  padding: 10px 14px;
  box-shadow: var(--shadow-soft);
}

.search-icon {
  color: var(--color-text-hint);
  flex-shrink: 0;
}

.search-box input {
  flex: 1;
  font-size: 14px;
  color: var(--color-text);
}

.clear-btn {
  color: var(--color-text-hint);
  display: flex;
  cursor: pointer;
}

/* 视图切换 */
.filter-bar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.view-toggle {
  display: flex;
  background: #fff;
  border-radius: 8px;
  padding: 3px;
  box-shadow: var(--shadow-soft);
}

.view-toggle button {
  padding: 6px 16px;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-hint);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.view-toggle button.active {
  background: var(--color-primary);
  color: #fff;
  font-weight: 600;
}

/* 标签栏 */
.tag-bar {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 12px;
}

.tag-chip {
  flex-shrink: 0;
  padding: 6px 16px;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary);
  background: #fff;
  border-radius: 20px;
  box-shadow: var(--shadow-soft);
  cursor: pointer;
  transition: all 0.2s;
}

.tag-chip.active {
  background: var(--color-primary);
  color: #fff;
  font-weight: 600;
}

/* 网格 */
.book-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  padding: 4px 4px 16px;
}

.grid-item {
  position: relative;
}

.edit-btn {
  position: absolute;
  top: 6px;
  left: 6px;
  width: 26px;
  height: 26px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(4px);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-secondary);
  cursor: pointer;
  z-index: 5;
  opacity: 0;
  transition: opacity 0.2s;
}

.grid-item:hover .edit-btn,
.grid-item:active .edit-btn {
  opacity: 1;
}

/* 分组视图 */
.group-section {
  margin-bottom: 20px;
}

.group-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-secondary);
  padding: 8px 4px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0;
}

.empty-icon {
  margin-bottom: 16px;
  opacity: 0.6;
}

.empty-text {
  color: var(--color-text-hint);
  font-size: 14px;
  margin-bottom: 20px;
}

.empty-btn {
  max-width: 200px;
}

/* 编辑弹窗 */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(61, 46, 26, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 200;
}

.modal-content {
  width: 100%;
  max-width: 480px;
  background: #fff;
  border-radius: 24px 24px 0 0;
  padding: 28px 24px calc(28px + env(safe-area-inset-bottom));
  max-height: 85vh;
  overflow-y: auto;
}

.modal-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 20px;
  text-align: center;
}

.cover-upload {
  width: 100px;
  height: 134px;
  margin: 0 auto 20px;
  border-radius: 10px;
  overflow: hidden;
  border: 2px dashed var(--color-border);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: var(--color-primary-lightest);
}

.upload-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  color: var(--color-text-hint);
  font-size: 12px;
}

.input-group {
  margin-bottom: 16px;
}

.input-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-secondary);
  margin-bottom: 6px;
}

.input-group input,
.input-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1.5px solid var(--color-border);
  border-radius: 8px;
  font-size: 14px;
  color: var(--color-text);
  background: var(--color-primary-lightest);
  resize: none;
}

.input-group input:focus,
.input-group textarea:focus {
  border-color: var(--color-primary);
  background: #fff;
}

.status-row {
  display: flex;
  gap: 8px;
}

.status-chip {
  flex: 1;
  padding: 8px 0;
  font-size: 13px;
  border: 1.5px solid var(--color-border);
  border-radius: 8px;
  color: var(--color-text-secondary);
  cursor: pointer;
  background: #fff;
  transition: all 0.2s;
}

.status-chip.active {
  background: var(--color-primary-lightest);
  border-color: var(--color-primary);
  color: var(--color-primary-darker);
  font-weight: 600;
}

.modal-actions {
  display: flex;
  gap: 10px;
  margin-top: 24px;
}

.btn-cancel, .btn-danger, .btn-save {
  flex: 1;
  padding: 12px 0;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}

.btn-cancel {
  background: var(--color-primary-lightest);
  color: var(--color-text-secondary);
}

.btn-danger {
  background: #FEE2E2;
  color: #DC2626;
}

.btn-save {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: #fff;
}

.btn-cancel:active, .btn-danger:active, .btn-save:active {
  opacity: 0.85;
}
</style>
