<template>
  <div class="book-card" @click="goReader">
    <div class="book-cover" :class="coverClass" :style="coverStyle">
      <!-- 封面标题 (有自定义封面图时不显示文字) -->
      <div class="book-cover-title" v-if="!book.coverImagePath">{{ book.title }}</div>

      <!-- 进度环 -->
      <ProgressRing :percent="book.readProgress || 0" />

      <!-- 喜欢标记 -->
      <button
        v-if="Number(book.isFavorite) === 1"
        class="book-favorite"
        @click.stop="$emit('toggle-favorite', book)"
      >
        <svg viewBox="0 0 24 24"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
      </button>

      <!-- 编辑按钮 -->
      <button class="book-edit-btn" @click.stop="$emit('edit', book)">
        <svg viewBox="0 0 24 24"><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/></svg>
      </button>

      <!-- 毛玻璃底部信息层 -->
      <div class="book-cover-overlay">
        <div class="book-overlay-title">{{ book.title }}</div>
        <div class="book-overlay-meta">
          <span v-if="book.chapterCount">{{ book.chapterCount }} 章</span>
          <span v-if="book.readProgress">{{ book.readProgress }}%</span>
        </div>
        <div class="book-overlay-tags" v-if="book.tags && book.tags.length">
          <span v-for="tag in book.tags" :key="tag" class="book-tag-mini">{{ tag }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import ProgressRing from './ProgressRing.vue'

const props = defineProps({
  book: { type: Object, required: true }
})

defineEmits(['edit', 'toggle-favorite'])

const router = useRouter()

const coverClass = computed(() => {
  if (props.book.coverImagePath) return ''
  if (props.book.coverColor) return ''
  return `cover-${(props.book.id % 6) + 1}`
})

const coverStyle = computed(() => {
  if (props.book.coverImagePath) {
    const url = props.book.coverImagePath.startsWith('http')
      ? props.book.coverImagePath
      : '/api' + props.book.coverImagePath
    return { background: `url(${url}) center/cover` }
  }
  if (props.book.coverColor) {
    return { background: props.book.coverColor }
  }
  return {}
})

function goReader() {
  router.push(`/reader/${props.book.id}`)
}
</script>

<style scoped>
.book-card {
    border-radius: var(--radius-lg);
    overflow: hidden;
    cursor: pointer;
    transition: transform 0.2s, box-shadow 0.2s;
    background: transparent;
    border: none;
}

.book-card:hover {
    transform: translateY(-4px) scale(1.02);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
}

.book-cover {
    width: 100%;
    aspect-ratio: 3 / 4;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    align-items: center;
    color: #fff;
    font-size: 17px;
    font-weight: 600;
    text-align: center;
    padding: 24px 14px 0;
    position: relative;
    overflow: hidden;
    background-size: cover;
    background-position: center;
}

.book-cover-title {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1;
    text-shadow: 0 1px 4px rgba(0, 0, 0, 0.25);
    line-height: 1.4;
    word-break: break-word;
    padding: 8px;
    opacity: 1;
    transition: opacity 0.2s;
}

.book-card:hover .book-cover-title {
    opacity: 0;
}

.book-card:hover .book-progress-ring {
    opacity: 1;
    visibility: visible;
    transition: opacity 0.2s ease, visibility 0s 0s;
}

/* 毛玻璃底部信息层 */
.book-cover-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 48%;
    padding: 28px 12px 10px;
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    gap: 4px;
    z-index: 2;
    pointer-events: none;
    backdrop-filter: blur(16px) saturate(180%);
    -webkit-backdrop-filter: blur(16px) saturate(180%);
    background: rgba(255, 255, 255, 0.18);
    border-top: 1px solid rgba(255, 255, 255, 0.22);
}

.book-overlay-title {
    font-size: 13px;
    font-weight: 600;
    color: #fff;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.book-overlay-meta {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: 11px;
    color: rgba(255, 255, 255, 0.78);
}

.book-overlay-tags {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-top: 4px;
    flex-wrap: wrap;
}

.book-tag-mini {
    display: inline-block;
    padding: 1px 7px;
    border-radius: var(--radius-pill);
    background: rgba(255, 255, 255, 0.28);
    color: #fff;
    font-size: 10px;
    line-height: 1.5;
    backdrop-filter: blur(4px);
}

/* 喜欢标记 */
.book-favorite {
    position: absolute;
    top: 8px;
    right: 8px;
    width: 28px;
    height: 28px;
    background: rgba(255, 255, 255, 0.85);
    border: none;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    backdrop-filter: blur(6px);
    z-index: 10;
    cursor: pointer;
    padding: 0;
    transition: transform 0.15s, background 0.15s;
}
.book-favorite:hover {
    transform: scale(1.12);
    background: rgba(255, 255, 255, 0.95);
}
.book-favorite svg {
    width: 14px;
    height: 14px;
    fill: var(--color-primary-dark);
}

/* 编辑按钮 */
.book-edit-btn {
    position: absolute;
    top: 8px;
    left: 8px;
    width: 26px;
    height: 26px;
    border: 0;
    background: rgba(255, 255, 255, 0.85);
    border-radius: 50%;
    color: var(--color-text-secondary);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    padding: 0;
    opacity: 0;
    transform: scale(0.8);
    transition: opacity 0.15s, transform 0.15s, color 0.15s, background 0.15s;
    z-index: 10;
}

.book-edit-btn svg {
    width: 13px;
    height: 13px;
    fill: currentColor;
}

.book-card:hover .book-edit-btn {
    opacity: 1;
    transform: scale(1);
}

.book-edit-btn:hover {
    background: #fff;
    color: var(--color-primary-darker);
}
</style>
