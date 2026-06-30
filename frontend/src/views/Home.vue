<template>
  <div class="page-container home-page">
    <!-- 顶部 -->
    <header class="home-header">
      <div>
        <p class="greeting">{{ greetingText }}</p>
        <h2 class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '读者' }}</h2>
      </div>
      <button class="import-btn" @click="router.push('/import')">
        <svg viewBox="0 0 24 24" width="20" height="20">
          <path d="M12 2L12 16M5 9L12 2L19 9" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M4 20L20 20" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <span>导入</span>
      </button>
    </header>

    <!-- 今日阅读环 -->
    <section class="today-ring card">
      <div class="ring-bg-area">
        <svg viewBox="0 0 140 140" width="120" height="120">
          <circle cx="70" cy="70" r="60" fill="none" stroke="rgba(255,255,255,0.2)" stroke-width="8"/>
          <circle
            cx="70" cy="70" r="60" fill="none" stroke="#fff" stroke-width="8"
            stroke-linecap="round"
            :stroke-dasharray="ringCircumference"
            :stroke-dashoffset="ringOffset"
            :transform="'rotate(-90 70 70)'"
            style="transition: stroke-dashoffset 0.8s ease;"
          />
        </svg>
        <div class="ring-center">
          <span class="ring-minutes">{{ todayMinutes }}</span>
          <span class="ring-label">分钟</span>
        </div>
      </div>
      <div class="ring-info">
        <p class="ring-title">今日阅读</p>
        <p class="ring-goal">目标 {{ dailyGoal }} 分钟</p>
        <p class="ring-percent">{{ Math.round(todayMinutes / dailyGoal * 100) }}% 完成</p>
      </div>
    </section>

    <!-- 最近在读 -->
    <section class="section" v-if="recentBooks.length">
      <div class="section-header">
        <h3>最近在读</h3>
        <router-link to="/bookshelf" class="see-all">全部</router-link>
      </div>
      <div class="scroll-row">
        <div
          v-for="book in recentBooks"
          :key="book.id"
          class="scroll-item"
          @click="openReader(book.id)"
        >
          <BookCard :book="book" />
        </div>
      </div>
    </section>

    <!-- 喜爱书籍 -->
    <section class="section" v-if="favoriteBooks.length">
      <div class="section-header">
        <h3>喜爱书籍</h3>
        <router-link to="/bookshelf" class="see-all">全部</router-link>
      </div>
      <div class="scroll-row">
        <div
          v-for="book in favoriteBooks"
          :key="book.id"
          class="scroll-item"
          @click="openReader(book.id)"
        >
          <BookCard :book="book" />
        </div>
      </div>
    </section>

    <!-- 推荐位 -->
    <section class="section">
      <div class="section-header">
        <h3>为你推荐</h3>
      </div>
      <div class="recommend-card card" @click="router.push('/bookshelf')">
        <div class="recommend-icon">
          <svg viewBox="0 0 32 32" width="32" height="32">
            <path d="M16 4L19.5 12.5L28 13L21.5 18.5L24 27L16 22L8 27L10.5 18.5L4 13L12.5 12.5Z" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="recommend-text">
          <p class="recommend-title">导入你的第一本书</p>
          <p class="recommend-desc">支持 TXT 单文件或 ZIP 批量导入，自动识别章节</p>
        </div>
        <svg class="recommend-arrow" viewBox="0 0 24 24" width="20" height="20">
          <path d="M9 6L15 12L9 18" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
    </section>

    <!-- 空状态 -->
    <div v-if="!loading && !recentBooks.length && !favoriteBooks.length" class="empty-state">
      <p>书架空空如也，快去导入书籍吧</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getBookList } from '../api/book'
import { getHeatmap, getOverview } from '../api/stats'
import BookCard from '../components/BookCard.vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const books = ref([])
const todayMinutes = ref(0)
const dailyGoal = ref(30)

const ringCircumference = 2 * Math.PI * 60
const ringOffset = computed(() => ringCircumference * (1 - Math.min(todayMinutes.value / dailyGoal.value, 1)))

const greetingText = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const recentBooks = computed(() => {
  return books.value
    .filter(b => b.readProgress != null && b.readProgress > 0)
    .sort((a, b) => (b.readProgress || 0) - (a.readProgress || 0))
    .slice(0, 6)
})

const favoriteBooks = computed(() => {
  return books.value.filter(b => b.isFavorite).slice(0, 6)
})

function openReader(bookId) {
  router.push(`/reader/${bookId}`)
}

onMounted(async () => {
  try {
    // 并行请求
    const [bookRes, heatmapRes, overviewRes] = await Promise.all([
      getBookList(),
      getHeatmap(new Date().getFullYear()),
      getOverview()
    ])
    books.value = bookRes.data || []

    // 今日阅读分钟
    const today = new Date().toISOString().slice(0, 10)
    const todayPoint = (heatmapRes.data || []).find(p => p.date === today)
    todayMinutes.value = todayPoint ? todayPoint.minutes : 0

    // 从 profile 获取目标
    if (userStore.userInfo?.dailyGoal) {
      dailyGoal.value = userStore.userInfo.dailyGoal
    }
  } catch (e) {
    // 错误已处理
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.home-page {
  padding: 16px 16px 0;
}

/* 头部 */
.home-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 4px 20px;
}

.greeting {
  font-size: 13px;
  color: var(--color-text-hint);
}

.username {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text);
  margin-top: 2px;
}

.import-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #fff;
  border-radius: 20px;
  box-shadow: var(--shadow-soft);
  font-size: 13px;
  font-weight: 600;
  color: var(--color-primary-dark);
  cursor: pointer;
  transition: transform 0.15s;
}

.import-btn:active {
  transform: scale(0.95);
}

/* 今日阅读环 */
.today-ring {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px 28px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  margin-bottom: 24px;
}

.ring-bg-area {
  position: relative;
  width: 120px;
  height: 120px;
  flex-shrink: 0;
}

.ring-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.ring-minutes {
  display: block;
  font-size: 28px;
  font-weight: 800;
  color: #fff;
  line-height: 1;
}

.ring-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.ring-info {
  flex: 1;
}

.ring-title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.ring-goal {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.75);
  margin-top: 6px;
}

.ring-percent {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 4px;
}

/* 通用 section */
.section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding: 0 4px;
}

.section-header h3 {
  font-size: 17px;
  font-weight: 700;
  color: var(--color-text);
}

.see-all {
  font-size: 13px;
  color: var(--color-text-hint);
}

/* 横向滚动行 */
.scroll-row {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding: 2px 4px 8px;
  scroll-snap-type: x mandatory;
}

.scroll-item {
  flex-shrink: 0;
  width: 108px;
  scroll-snap-align: start;
}

/* 推荐位 */
.recommend-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  cursor: pointer;
  transition: transform 0.15s;
}

.recommend-card:active {
  transform: scale(0.98);
}

.recommend-icon {
  width: 48px;
  height: 48px;
  background: var(--color-primary-lightest);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary-dark);
  flex-shrink: 0;
}

.recommend-text {
  flex: 1;
}

.recommend-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
}

.recommend-desc {
  font-size: 12px;
  color: var(--color-text-hint);
  margin-top: 4px;
}

.recommend-arrow {
  color: var(--color-text-hint);
  flex-shrink: 0;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 40px 0;
  color: var(--color-text-hint);
  font-size: 14px;
}
</style>
