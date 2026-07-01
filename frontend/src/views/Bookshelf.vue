<template>
  <div class="bookshelf-page">
    <!-- 页头 -->
    <div class="page-header">
      <div class="page-title">书架</div>
      <div class="page-subtitle">{{ books.length }} 本书</div>
    </div>

    <!-- 工具栏 -->
    <div class="bookshelf-toolbar">
      <!-- 搜索 -->
      <div class="bookshelf-search">
        <svg class="bookshelf-search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="搜索书名..."
        />
        <button v-if="searchQuery" class="bookshelf-search-clear" @click="searchQuery = ''">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </div>

      <!-- 右侧操作 -->
      <div class="bookshelf-toolbar-right">
        <!-- 视图切换 -->
        <div class="view-switcher">
          <button class="view-switcher-btn" :class="{ active: viewMode === 'grid' }" @click="viewMode = 'grid'" title="网格视图">
            <svg viewBox="0 0 24 24"><path fill="currentColor" d="M3 3h8v8H3V3m10 0h8v8h-8V3M3 13h8v8H3v-8m10 0h8v8h-8v-8z"/></svg>
          </button>
          <button class="view-switcher-btn" :class="{ active: viewMode === 'grouped' }" @click="viewMode = 'grouped'" title="分组视图">
            <svg viewBox="0 0 24 24"><path fill="currentColor" d="M3 3h18v4H3V3m0 7h18v4H3v-4m0 7h18v4H3v-4z"/></svg>
          </button>
        </div>

        <!-- 导入按钮 -->
        <router-link to="/import" class="bookshelf-import-btn">
          <svg viewBox="0 0 24 24"><path fill="currentColor" d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
          导入
        </router-link>
      </div>
    </div>

    <!-- 筛选标签 -->
    <div class="bookshelf-filters">
      <div
        class="filter-tab"
        :class="{ active: filter === 'all' }"
        @click="filter = 'all'"
      >
        全部 <span class="filter-count">{{ books.length }}</span>
      </div>
      <div
        class="filter-tab"
        :class="{ active: filter === 'reading' }"
        @click="filter = 'reading'"
      >
        正在看 <span class="filter-count">{{ readingCount }}</span>
      </div>
      <div
        class="filter-tab"
        :class="{ active: filter === 'prepare' }"
        @click="filter = 'prepare'"
      >
        准备看 <span class="filter-count">{{ prepareCount }}</span>
      </div>
      <div
        class="filter-tab"
        :class="{ active: filter === 'finished' }"
        @click="filter = 'finished'"
      >
        已看完 <span class="filter-count">{{ finishedCount }}</span>
      </div>
      <div
        class="filter-tab"
        :class="{ active: filter === 'favored' }"
        @click="filter = 'favored'"
      >
        喜欢 <span class="filter-count">{{ favoredCount }}</span>
      </div>
    </div>

    <!-- 网格视图 -->
    <div class="book-grid" v-if="viewMode === 'grid' && filteredBooks.length">
      <BookCard
        v-for="book in filteredBooks"
        :key="book.id"
        :book="book"
        @edit="openEditModal"
        @toggle-favorite="toggleFavorite"
      />
    </div>

    <!-- 分组视图 -->
    <div class="grouped-view" v-if="viewMode === 'grouped' && filteredBooks.length">
      <div class="tag-group" v-for="group in groupedBooks" :key="group.tag">
        <div class="tag-group-header">
          <span class="tag-group-name">{{ group.tag }}</span>
          <span class="tag-group-count">{{ group.books.length }} 本</span>
        </div>
        <div class="book-grid">
          <BookCard
            v-for="book in group.books"
            :key="book.id"
            :book="book"
            @edit="openEditModal"
            @toggle-favorite="toggleFavorite"
          />
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div class="empty-state" v-if="!filteredBooks.length">
      <div class="empty-state-icon">
        <svg viewBox="0 0 24 24" fill="currentColor"><path d="M18 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 4h5v8l-2.5-1.5L6 12V4z"/></svg>
      </div>
      <div class="empty-state-title" v-if="books.length">没有符合条件的书籍</div>
      <div class="empty-state-desc" v-if="books.length">试试其他筛选条件</div>
      <template v-if="!books.length">
        <div class="empty-state-title">书架还是空的</div>
        <div class="empty-state-desc">导入一本小说开始阅读吧</div>
        <router-link to="/import" class="btn btn-primary">导入书籍</router-link>
      </template>
    </div>

    <!-- 编辑弹窗 -->
    <div class="modal-overlay" :class="{ open: editModal.open }" @click.self="editModal.open = false">
      <div class="modal-card" v-if="editModal.book">
        <div class="modal-header">
          <span class="modal-title">编辑书籍</span>
          <button class="modal-close" @click="editModal.open = false">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/></svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <label class="form-label">书名</label>
            <input class="form-input" v-model="editModal.book.title" />
          </div>
          <div class="form-row">
            <label class="form-label">封面</label>
            <div class="cover-edit-area">
              <div class="cover-preview" :class="coverPreviewClass" :style="coverPreviewStyle">
                <span v-if="!editModal.book.coverImagePath" class="cover-preview-title">{{ editModal.book.title }}</span>
              </div>
              <div class="cover-edit-actions">
                <label class="btn-upfile">
                  上传封面
                  <input type="file" accept="image/*" @change="onCoverFileSelected" hidden />
                </label>
                <button class="btn-danger-ghost-sm" v-if="editModal.book.coverImagePath" @click="removeCover">移除封面</button>
              </div>
              <div class="cover-color-picker">
                <span class="cover-color-label">封面色</span>
                <button
                  v-for="c in COVER_COLORS"
                  :key="c"
                  class="cover-color-swatch"
                  :class="{ active: editModal.book.coverColor === c && !editModal.book.coverImagePath }"
                  :style="{ background: c }"
                  @click="selectCoverColor(c)"
                ></button>
              </div>
            </div>
          </div>
          <div class="form-row">
            <label class="form-label">阅读状态</label>
            <select class="form-input" v-model="editModal.book.status">
              <option value="prepare">准备看</option>
              <option value="reading">正在看</option>
              <option value="finished">已看完</option>
            </select>
          </div>
          <div class="form-row">
            <label class="form-label">标签</label>
            <div class="tag-editor">
              <div class="tag-list">
                <span class="tag-chip" v-for="(tag, i) in editModal.book.tags" :key="i">
                  {{ tag }}
                  <button class="tag-chip-remove" @click="editModal.book.tags.splice(i, 1)">x</button>
                </span>
              </div>
              <div class="tag-input-wrap">
                <input class="form-input tag-input" v-model="editModal.newTag" placeholder="输入标签名" @keyup.enter="addTag" />
                <button class="tag-add-btn" @click="addTag">添加</button>
              </div>
            </div>
          </div>
          <div class="form-row form-row-danger">
            <button class="btn btn-danger-ghost" @click="deleteBook">删除书籍</button>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-ghost" @click="editModal.open = false">取消</button>
          <button class="btn btn-primary" @click="saveEdit">保存</button>
        </div>
      </div>
    </div>

    <!-- 裁剪弹窗 -->
    <CropModal
      :show="cropModal.show"
      :imageSrc="cropModal.imageSrc"
      @close="cropModal.show = false"
      @crop="onCropComplete"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getBookshelf, updateBook, deleteBook as apiDeleteBook, uploadCover } from '../api/book'
import BookCard from '../components/BookCard.vue'
import CropModal from '../components/CropModal.vue'

const books = ref([])
const searchQuery = ref('')
const viewMode = ref('grid')
const filter = ref('all')

const editModal = ref({
  open: false,
  book: null,
  newTag: ''
})

const COVER_COLORS = ['#F59E0B', '#D97706', '#B45309', '#FBBF24', '#92400E', '#78350F']
const cropModal = ref({ show: false, imageSrc: '' })

const filteredBooks = computed(() => {
  let result = books.value
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    result = result.filter(b => b.title.toLowerCase().includes(q))
  }
  if (filter.value === 'reading') result = result.filter(b => b.status === 'reading')
  else if (filter.value === 'prepare') result = result.filter(b => b.status === 'prepare')
  else if (filter.value === 'finished') result = result.filter(b => b.status === 'finished')
  else if (filter.value === 'favored') result = result.filter(b => Number(b.isFavorite) === 1)
  return result
})

const readingCount = computed(() => books.value.filter(b => b.status === 'reading').length)
const prepareCount = computed(() => books.value.filter(b => b.status === 'prepare').length)
const finishedCount = computed(() => books.value.filter(b => b.status === 'finished').length)
const favoredCount = computed(() => books.value.filter(b => Number(b.isFavorite) === 1).length)

const groupedBooks = computed(() => {
  const groups = {}
  filteredBooks.value.forEach(book => {
    if (book.tags && book.tags.length) {
      book.tags.forEach(tag => {
        if (!groups[tag]) groups[tag] = []
        groups[tag].push(book)
      })
    } else {
      const key = '未分组'
      if (!groups[key]) groups[key] = []
      groups[key].push(book)
    }
  })
  return Object.entries(groups).map(([tag, books]) => ({ tag, books }))
})

const coverPreviewClass = computed(() => {
  if (!editModal.value.book) return ''
  if (editModal.value.book.coverImagePath) return ''
  if (editModal.value.book.coverColor) return ''
  return `cover-${(editModal.value.book.id % 6) + 1}`
})

const coverPreviewStyle = computed(() => {
  if (!editModal.value.book) return {}
  if (editModal.value.book.coverImagePath) {
    const url = editModal.value.book.coverImagePath.startsWith('http')
      ? editModal.value.book.coverImagePath
      : '/api' + editModal.value.book.coverImagePath
    return { background: `url(${url}) center/cover` }
  }
  if (editModal.value.book.coverColor) {
    return { background: editModal.value.book.coverColor }
  }
  return {}
})

function openEditModal(book) {
  editModal.value = {
    open: true,
    book: { ...book, tags: [...(book.tags || [])] },
    newTag: ''
  }
}

function onCoverFileSelected(e) {
  const file = e.target.files[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) {
    alert('图片大小不能超过5MB')
    return
  }
  const reader = new FileReader()
  reader.onload = (ev) => {
    cropModal.value = { show: true, imageSrc: ev.target.result }
  }
  reader.readAsDataURL(file)
  e.target.value = ''
}

async function onCropComplete(blob) {
  try {
    const res = await uploadCover(blob)
    editModal.value.book.coverImagePath = res.data.url
    editModal.value.book.coverColor = ''
  } catch (e) {
    alert('封面上传失败: ' + (e.message || ''))
  }
}

function selectCoverColor(color) {
  editModal.value.book.coverImagePath = ''
  editModal.value.book.coverColor = color
}

function removeCover() {
  editModal.value.book.coverImagePath = ''
  editModal.value.book.coverColor = ''
}

function addTag() {
  const tag = editModal.value.newTag.trim()
  if (tag && !editModal.value.book.tags.includes(tag)) {
    editModal.value.book.tags.push(tag)
  }
  editModal.value.newTag = ''
}

async function saveEdit() {
  try {
    await updateBook(editModal.value.book.id, {
      title: editModal.value.book.title,
      status: editModal.value.book.status,
      tags: editModal.value.book.tags,
      coverImagePath: editModal.value.book.coverImagePath || '',
      coverColor: editModal.value.book.coverColor || ''
    })
    const idx = books.value.findIndex(b => b.id === editModal.value.book.id)
    if (idx !== -1) {
      books.value[idx] = { ...books.value[idx], ...editModal.value.book }
    }
    editModal.value.open = false
  } catch (e) {
    alert('保存失败: ' + (e.message || ''))
  }
}

async function deleteBook() {
  if (!confirm('确定删除这本书吗？')) return
  try {
    await apiDeleteBook(editModal.value.book.id)
    books.value = books.value.filter(b => b.id !== editModal.value.book.id)
    editModal.value.open = false
  } catch (e) {
    alert('删除失败: ' + (e.message || ''))
  }
}

async function toggleFavorite(book) {
  try {
    const next = Number(book.isFavorite) === 1 ? 0 : 1
    await updateBook(book.id, { isFavorite: next })
    book.isFavorite = next
  } catch (e) {
    console.error('切换收藏失败:', e)
  }
}

onMounted(async () => {
  try {
    const res = await getBookshelf()
    books.value = res.data || []
  } catch (e) {
    console.error('加载书架失败:', e)
  }
})
</script>

<style scoped>
.bookshelf-page {
    min-height: calc(100vh - var(--nav-height));
    padding-bottom: 48px;
    max-width: var(--content-max-width);
    margin: 0 auto;
}

/* 工具栏 */
.bookshelf-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 20px;
    margin-bottom: 24px;
}

.bookshelf-search {
    flex: 1;
    max-width: 380px;
    position: relative;
}

.bookshelf-search input {
    width: 100%;
    padding: 8px 32px 8px 34px;
    border: 1px solid var(--color-border-neutral);
    border-radius: var(--radius-md);
    font-size: var(--font-size-md);
    color: var(--color-text);
    background: var(--color-bg-card);
    outline: none;
    font-family: inherit;
    transition: border-color 0.15s, box-shadow 0.15s, background 0.15s;
}

.bookshelf-search input::placeholder {
    color: var(--color-text-light);
}

.bookshelf-search input:focus {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.12);
    background: #fff;
}

.bookshelf-search-icon {
    position: absolute;
    left: 11px;
    top: 50%;
    transform: translateY(-50%);
    width: 14px;
    height: 14px;
    color: var(--color-text-tertiary);
    pointer-events: none;
    z-index: 1;
}

.bookshelf-search-clear {
    position: absolute;
    right: 6px;
    top: 50%;
    transform: translateY(-50%);
    width: 18px;
    height: 18px;
    border: 0;
    background: rgba(0, 0, 0, 0.08);
    border-radius: 50%;
    color: var(--color-text-tertiary);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    padding: 0;
    transition: background 0.15s, color 0.15s;
}

.bookshelf-search-clear:hover {
    background: rgba(0, 0, 0, 0.16);
    color: var(--color-text);
}

.bookshelf-search-clear svg {
    width: 10px;
    height: 10px;
}

.bookshelf-toolbar-right {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-shrink: 0;
}

.view-switcher {
    display: inline-flex;
    background: var(--color-bg-card);
    border: 1px solid var(--color-border-neutral);
    border-radius: var(--radius-md);
    padding: 2px;
}

.view-switcher-btn {
    width: 30px;
    height: 26px;
    border: 0;
    background: transparent;
    border-radius: 6px;
    color: var(--color-text-tertiary);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0;
    transition: background 0.12s, color 0.12s;
}

.view-switcher-btn svg {
    width: 14px;
    height: 14px;
    fill: currentColor;
}

.view-switcher-btn:hover {
    color: var(--color-text);
}

.view-switcher-btn.active {
    background: var(--color-primary-pale);
    color: var(--color-primary-darker);
}

.bookshelf-import-btn {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 7px 14px 7px 12px;
    background: var(--color-primary);
    color: #fff;
    border-radius: var(--radius-md);
    font-size: var(--font-size-md);
    font-weight: var(--font-medium);
    line-height: 1;
    white-space: nowrap;
    box-shadow: 0 1px 2px rgba(217, 119, 6, 0.18), inset 0 1px 0 rgba(255, 255, 255, 0.18);
    transition: background 0.15s, transform 0.08s, box-shadow 0.15s;
}

.bookshelf-import-btn:hover {
    background: var(--color-primary-dark);
    color: #fff;
}

.bookshelf-import-btn svg {
    width: 14px;
    height: 14px;
    fill: currentColor;
}

/* 筛选标签 */
.bookshelf-filters {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-bottom: 28px;
    padding-bottom: 14px;
    border-bottom: 1px solid var(--color-divider);
    flex-wrap: wrap;
}

.filter-tab {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 5px 12px;
    border-radius: var(--radius-pill);
    font-size: var(--font-size-base);
    color: var(--color-text-secondary);
    cursor: pointer;
    transition: background 0.1s;
    white-space: nowrap;
}

.filter-tab:hover {
    background: rgba(0, 0, 0, 0.04);
    color: var(--color-text);
}

.filter-tab.active {
    background: var(--color-primary-pale);
    color: var(--color-primary-darker);
    font-weight: var(--font-medium);
}

.filter-count {
    font-size: var(--font-size-xs);
    padding: 1px 6px;
    border-radius: var(--radius-pill);
    background: rgba(0, 0, 0, 0.06);
    color: var(--color-text-tertiary);
}

.filter-tab.active .filter-count {
    background: rgba(217, 119, 6, 0.15);
    color: var(--color-primary-darker);
}

/* 书籍网格 */
.book-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(178px, 1fr));
    gap: 22px;
}

/* 分组视图 */
.grouped-view {
    display: flex;
    flex-direction: column;
    gap: 36px;
}

.tag-group-header {
    display: flex;
    align-items: baseline;
    gap: 10px;
    margin-bottom: 14px;
    padding-bottom: 8px;
    border-bottom: 1px solid var(--color-divider);
}

.tag-group-name {
    font-size: var(--font-size-xl);
    font-weight: var(--font-semibold);
    color: var(--color-text);
}

.tag-group-count {
    font-size: var(--font-size-sm);
    color: var(--color-text-tertiary);
}

/* 空状态 */
.empty-state {
    text-align: center;
    padding: 80px 24px;
}

.empty-state-icon {
    width: 56px;
    height: 56px;
    margin: 0 auto 20px;
    background: var(--color-primary-pale);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--color-primary-darker);
}

.empty-state-icon svg {
    width: 26px;
    height: 26px;
}

.empty-state-title {
    font-size: var(--font-size-lg);
    font-weight: var(--font-medium);
    color: var(--color-text);
    margin-bottom: 8px;
}

.empty-state-desc {
    font-size: var(--font-size-base);
    color: var(--color-text-tertiary);
    margin-bottom: 20px;
}

/* 模态弹窗 */
.modal-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.28);
    backdrop-filter: blur(2px);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
    opacity: 0;
    pointer-events: none;
    transition: opacity 0.18s;
}

.modal-overlay.open {
    opacity: 1;
    pointer-events: auto;
}

.modal-card {
    width: 460px;
    max-width: calc(100vw - 32px);
    max-height: calc(100vh - 64px);
    overflow: auto;
    background: #fff;
    border-radius: var(--radius-xl);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
    transform: translateY(8px) scale(0.98);
    transition: transform 0.18s;
}

.modal-overlay.open .modal-card {
    transform: translateY(0) scale(1);
}

.modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 18px 22px 14px;
    border-bottom: 1px solid var(--color-divider);
}

.modal-title {
    font-size: var(--font-size-xl);
    font-weight: var(--font-semibold);
    color: var(--color-text);
}

.modal-close {
    width: 28px;
    height: 28px;
    border: 0;
    background: transparent;
    border-radius: 50%;
    color: var(--color-text-tertiary);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0;
    transition: background 0.12s, color 0.12s;
}

.modal-close:hover {
    background: rgba(0, 0, 0, 0.06);
    color: var(--color-text);
}

.modal-close svg {
    width: 14px;
    height: 14px;
    fill: currentColor;
}

.modal-body {
    padding: 18px 22px;
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.form-row {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.form-label {
    font-size: var(--font-size-sm);
    font-weight: var(--font-medium);
    color: var(--color-text-secondary);
}

.form-input {
    width: 100%;
    padding: 8px 10px;
    border: 1px solid var(--color-border-neutral);
    border-radius: var(--radius-md);
    font-size: var(--font-size-md);
    color: var(--color-text);
    background: #fff;
    outline: none;
    font-family: inherit;
    transition: border-color 0.15s, box-shadow 0.15s;
}

.form-input:focus {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.12);
}

/* 标签编辑器 */
.tag-editor {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.tag-list {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    min-height: 28px;
}

.tag-chip {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 3px 4px 3px 10px;
    background: var(--color-primary-pale);
    color: var(--color-primary-darker);
    border-radius: var(--radius-pill);
    font-size: var(--font-size-sm);
}

.tag-chip-remove {
    width: 18px;
    height: 18px;
    border: 0;
    background: transparent;
    color: inherit;
    cursor: pointer;
    border-radius: 50%;
    font-size: 14px;
    line-height: 1;
    padding: 0;
    opacity: 0.6;
    transition: opacity 0.12s, background 0.12s;
}

.tag-chip-remove:hover {
    opacity: 1;
    background: rgba(217, 119, 6, 0.18);
}

.tag-input-wrap {
    display: flex;
    gap: 8px;
}

.tag-input {
    flex: 1;
}

.tag-add-btn {
    padding: 8px 14px;
    border: 1px solid var(--color-border-neutral);
    background: #fff;
    color: var(--color-text);
    border-radius: var(--radius-md);
    font-size: var(--font-size-sm);
    font-family: inherit;
    cursor: pointer;
    transition: background 0.12s, border-color 0.12s;
}

.tag-add-btn:hover {
    background: var(--color-primary-pale);
    border-color: var(--color-primary);
    color: var(--color-primary-darker);
}

.form-row-danger {
    flex-direction: row;
    padding-top: 4px;
    border-top: 1px solid var(--color-divider);
    margin-top: 4px;
}

.modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    padding: 14px 22px 18px;
    border-top: 1px solid var(--color-divider);
}

/* 封面编辑 */
.cover-edit-area {
    display: flex;
    flex-direction: column;
    gap: 10px;
}
.cover-preview {
    width: 90px;
    height: 120px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 8px;
    overflow: hidden;
    box-shadow: 0 1px 4px rgba(0,0,0,0.1);
}
.cover-preview-title {
    color: #fff;
    font-size: 11px;
    font-weight: 500;
    text-align: center;
    line-height: 1.3;
    text-shadow: 0 1px 2px rgba(0,0,0,0.25);
    word-break: break-all;
}
.cover-edit-actions {
    display: flex;
    gap: 8px;
}
.cover-color-picker {
    display: flex;
    align-items: center;
    gap: 6px;
    flex-wrap: wrap;
}
.cover-color-label {
    font-size: var(--font-size-sm);
    color: var(--color-text-tertiary);
}
.cover-color-swatch {
    width: 22px;
    height: 22px;
    border-radius: 50%;
    border: 2px solid transparent;
    cursor: pointer;
    padding: 0;
    transition: border-color 0.15s, transform 0.1s;
}
.cover-color-swatch:hover {
    transform: scale(1.1);
}
.cover-color-swatch.active {
    border-color: var(--color-text);
}
.btn-upfile {
    display: inline-block;
    padding: 6px 14px;
    background: var(--color-primary-pale);
    color: var(--color-primary-darker);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-md);
    font-size: var(--font-size-sm);
    cursor: pointer;
    transition: background 0.15s;
    text-align: center;
    font-family: inherit;
}
.btn-upfile:hover {
    background: #FEF3C7;
    border-color: var(--color-primary);
}
.btn-danger-ghost-sm {
    padding: 6px 14px;
    background: transparent;
    color: #DC2626;
    border: 1px solid #FCA5A5;
    border-radius: var(--radius-md);
    font-size: var(--font-size-sm);
    cursor: pointer;
    transition: background 0.15s;
    font-family: inherit;
}
.btn-danger-ghost-sm:hover {
    background: #FEF2F2;
}
</style>
