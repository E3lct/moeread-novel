<template>
  <div class="book-card" @click="$emit('click')">
    <!-- 封面 -->
    <div class="book-cover" :style="coverStyle">
      <!-- 有封面上传图片 -->
      <img
        v-if="book.coverImagePath"
        :src="coverUrl"
        class="cover-img"
        alt=""
      />
      <!-- 无封面：琥珀渐变 + 白字书名 -->
      <div v-else class="cover-fallback">
        <span class="cover-title">{{ book.title }}</span>
      </div>

      <!-- 喜爱标记 -->
      <svg
        v-if="book.isFavorite"
        class="favorite-icon"
        viewBox="0 0 24 24"
        width="18"
        height="18"
      >
        <path
          d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
          fill="#D97706"
          opacity="0.9"
        />
      </svg>

      <!-- 进度环 (hover显示) -->
      <div class="progress-overlay" v-if="book.readProgress != null && book.readProgress > 0">
        <ProgressRing :percent="book.readProgress" :size="56" :showText="true" />
      </div>

      <!-- 底部毛玻璃遮盖层 -->
      <div class="cover-overlay">
        <div class="overlay-title">{{ book.title }}</div>
        <div class="overlay-meta" v-if="book.author">{{ book.author }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import ProgressRing from './ProgressRing.vue'

const props = defineProps({
  book: { type: Object, required: true }
})

defineEmits(['click'])

const coverStyle = computed(() => {
  if (props.book.coverImagePath) return {}
  const colors = [
    'linear-gradient(135deg, #F59E0B, #D97706)',
    'linear-gradient(135deg, #FBBF24, #B45309)',
    'linear-gradient(135deg, #D97706, #92400E)',
    'linear-gradient(135deg, #FEF3C7, #F59E0B)'
  ]
  const idx = (props.book.id || 0) % colors.length
  return { background: colors[idx] }
})

const coverUrl = computed(() => {
  if (!props.book.coverImagePath) return ''
  const path = props.book.coverImagePath
  if (path.startsWith('http')) return path
  return '/api' + path
})
</script>

<style scoped>
.book-card {
  width: 100%;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.book-card:active {
  transform: scale(0.96);
}

.book-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 3 / 4;
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-medium);
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.cover-title {
  color: #fff;
  font-size: 17px;
  font-weight: 700;
  text-align: center;
  line-height: 1.4;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
  word-break: break-all;
}

.favorite-icon {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 3;
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.2));
}

.progress-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.15);
  opacity: 0;
  transition: opacity 0.25s ease;
  z-index: 2;
}

.book-card:hover .progress-overlay,
.book-card:active .progress-overlay {
  opacity: 1;
}

.cover-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 28px 12px 10px;
  background: linear-gradient(
    to top,
    rgba(61, 46, 26, 0.75) 0%,
    rgba(61, 46, 26, 0.35) 60%,
    transparent 100%
  );
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  z-index: 1;
}

.overlay-title {
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.overlay-meta {
  color: rgba(255, 255, 255, 0.8);
  font-size: 11px;
  margin-top: 2px;
}
</style>
