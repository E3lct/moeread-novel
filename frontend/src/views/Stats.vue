<template>
  <div class="page-container stats-page">
    <header class="stats-header">
      <h2>阅读统计</h2>
      <p class="year-label">{{ currentYear }} 年</p>
    </header>

    <!-- 概览带 -->
    <section class="overview-band">
      <div class="overview-item">
        <span class="overview-num">{{ formatNumber(overview.totalMinutes) }}</span>
        <span class="overview-label">总时长(分)</span>
      </div>
      <div class="overview-divider"></div>
      <div class="overview-item">
        <span class="overview-num">{{ overview.totalBooks }}</span>
        <span class="overview-label">书籍数</span>
      </div>
      <div class="overview-divider"></div>
      <div class="overview-item">
        <span class="overview-num">{{ overview.streakDays }}</span>
        <span class="overview-label">连续打卡</span>
      </div>
    </section>

    <!-- 年度热力图 -->
    <section class="section">
      <h3 class="section-title">年度热力图</h3>
      <div class="heatmap-wrap card">
        <!-- 月份标签 -->
        <div class="heatmap-months">
          <span
            v-for="m in 12"
            :key="m"
            class="month-label"
            :style="{ gridColumn: monthColStart(m) }"
          >{{ m }}月</span>
        </div>
        <!-- 热力图网格 -->
        <div class="heatmap-grid">
          <div class="weekday-labels">
            <span v-for="d in ['一','三','五','日']" :key="d" class="weekday-label">{{ d }}</span>
          </div>
          <div class="heatmap-cells" ref="heatmapRef">
            <div
              v-for="(day, i) in heatmapData"
              :key="i"
              class="heatmap-cell"
              :class="heatLevel(day.minutes)"
              :title="`${day.date} - ${day.minutes}分钟`"
            ></div>
          </div>
        </div>
        <!-- 图例 -->
        <div class="heatmap-legend">
          <span class="legend-text">少</span>
          <div class="legend-cells">
            <div class="heatmap-cell level-0"></div>
            <div class="heatmap-cell level-1"></div>
            <div class="heatmap-cell level-2"></div>
            <div class="heatmap-cell level-3"></div>
            <div class="heatmap-cell level-4"></div>
          </div>
          <span class="legend-text">多</span>
        </div>
      </div>
    </section>

    <!-- 阅读习惯 -->
    <section class="section">
      <h3 class="section-title">阅读习惯</h3>
      <div class="habit-wrap card">
        <div class="habit-chart">
          <div
            v-for="(val, i) in habitData"
            :key="i"
            class="habit-bar-wrap"
          >
            <div class="habit-bar-area">
              <div
                class="habit-bar"
                :style="{ height: barHeight(val) + '%' }"
              >
                <span class="habit-val" v-if="val > 0">{{ val }}</span>
              </div>
            </div>
            <span class="habit-label">{{ weekdayLabels[i] }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 已读完列表 -->
    <section class="section">
      <h3 class="section-title">已读完 ({{ finishedBooks.length }})</h3>
      <div class="finished-list">
        <div
          v-for="book in finishedBooks"
          :key="book.id"
          class="finished-item card"
          @click="openReader(book.id)"
        >
          <div class="finished-cover" :style="coverStyle(book)">
            <img v-if="book.coverImagePath" :src="coverUrl(book)" class="cover-img" />
            <span v-else class="cover-text">{{ book.title }}</span>
          </div>
          <div class="finished-info">
            <p class="finished-title">{{ book.title }}</p>
            <p class="finished-author">{{ book.author || '佚名' }}</p>
          </div>
          <svg class="finished-check" viewBox="0 0 24 24" width="20" height="20">
            <circle cx="12" cy="12" r="10" fill="#D97706" opacity="0.15"/>
            <path d="M8 12L11 15L16 9" stroke="#D97706" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div v-if="!finishedBooks.length" class="empty-inline">
          还没有读完的书
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHeatmap, getOverview, getHabit } from '../api/stats'
import { getBookList } from '../api/book'

const router = useRouter()

const currentYear = new Date().getFullYear()
const overview = ref({ totalMinutes: 0, totalBooks: 0, streakDays: 0 })
const heatmapData = ref([])
const habitData = ref([0, 0, 0, 0, 0, 0, 0])
const books = ref([])

const weekdayLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const maxHabit = computed(() => Math.max(...habitData.value, 1))

function barHeight(val) {
  return (val / maxHabit.value) * 100
}

function formatNumber(n) {
  if (n >= 10000) return (n / 10000).toFixed(1) + 'w'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k'
  return n
}

function heatLevel(minutes) {
  if (!minutes || minutes === 0) return 'level-0'
  if (minutes < 15) return 'level-1'
  if (minutes < 30) return 'level-2'
  if (minutes < 60) return 'level-3'
  return 'level-4'
}

function monthColStart(m) {
  const firstDay = new Date(currentYear, m - 1, 1)
  const dayOfYear = Math.floor((firstDay - new Date(currentYear, 0, 1)) / 86400000)
  const weekIndex = Math.floor((dayOfYear + (new Date(currentYear, 0, 1).getDay() === 0 ? 6 : new Date(currentYear, 0, 1).getDay() - 1)) / 7)
  return weekIndex + 1
}

const finishedBooks = computed(() => books.value.filter(b => b.status === 'finished'))

function coverStyle(book) {
  if (book.coverImagePath) return {}
  const colors = [
    'linear-gradient(135deg, #F59E0B, #D97706)',
    'linear-gradient(135deg, #FBBF24, #B45309)',
    'linear-gradient(135deg, #D97706, #92400E)'
  ]
  return { background: colors[(book.id || 0) % colors.length] }
}

function coverUrl(book) {
  if (!book.coverImagePath) return ''
  const path = book.coverImagePath
  if (path.startsWith('http')) return path
  return '/api' + path
}

function openReader(bookId) {
  router.push(`/reader/${bookId}`)
}

onMounted(async () => {
  try {
    const [heatmapRes, overviewRes, habitRes, bookRes] = await Promise.all([
      getHeatmap(currentYear),
      getOverview(),
      getHabit(),
      getBookList()
    ])

    heatmapData.value = heatmapRes.data || []
    overview.value = overviewRes.data || { totalMinutes: 0, totalBooks: 0, streakDays: 0 }
    habitData.value = habitRes.data || [0, 0, 0, 0, 0, 0, 0]
    books.value = bookRes.data || []
  } catch (e) {}
})
</script>

<style scoped>
.stats-page {
  padding: 16px 16px 0;
}

.stats-header {
  padding: 12px 4px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-header h2 {
  font-size: 24px;
  font-weight: 800;
  color: var(--color-text);
}

.year-label {
  font-size: 13px;
  color: var(--color-text-hint);
}

/* 概览带 */
.overview-band {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  border-radius: var(--radius-lg);
  padding: 22px 16px;
  margin-bottom: 24px;
  box-shadow: var(--shadow-medium);
}

.overview-item {
  flex: 1;
  text-align: center;
}

.overview-num {
  display: block;
  font-size: 26px;
  font-weight: 800;
  color: #fff;
  line-height: 1.2;
}

.overview-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.75);
  margin-top: 4px;
}

.overview-divider {
  width: 1px;
  height: 36px;
  background: rgba(255, 255, 255, 0.25);
}

/* 通用 section */
.section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 12px;
  padding: 0 4px;
}

/* 热力图 */
.heatmap-wrap {
  padding: 16px;
  overflow-x: auto;
}

.heatmap-months {
  display: grid;
  grid-template-columns: repeat(53, 1fr);
  margin-left: 28px;
  margin-bottom: 4px;
  gap: 2px;
}

.month-label {
  font-size: 10px;
  color: var(--color-text-hint);
}

.heatmap-grid {
  display: flex;
  gap: 4px;
}

.weekday-labels {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 1px 0;
  width: 24px;
}

.weekday-label {
  font-size: 10px;
  color: var(--color-text-hint);
  line-height: 11px;
  height: 11px;
}

.heatmap-cells {
  display: grid;
  grid-template-rows: repeat(7, 11px);
  grid-auto-flow: column;
  grid-auto-columns: 11px;
  gap: 2px;
}

.heatmap-cell {
  width: 11px;
  height: 11px;
  border-radius: 2px;
  background: #F3F0EA;
}

.heatmap-cell.level-0 {
  background: #F3F0EA;
}

.heatmap-cell.level-1 {
  background: #FEF3C7;
}

.heatmap-cell.level-2 {
  background: #FBBF24;
}

.heatmap-cell.level-3 {
  background: #F59E0B;
}

.heatmap-cell.level-4 {
  background: #D97706;
}

.heatmap-legend {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
  margin-top: 10px;
  padding-left: 28px;
}

.legend-text {
  font-size: 11px;
  color: var(--color-text-hint);
}

.legend-cells {
  display: flex;
  gap: 2px;
}

/* 阅读习惯 */
.habit-wrap {
  padding: 20px 16px;
}

.habit-chart {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  height: 140px;
  gap: 8px;
}

.habit-bar-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
}

.habit-bar-area {
  flex: 1;
  width: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding: 0 4px;
}

.habit-bar {
  width: 100%;
  max-width: 28px;
  min-height: 4px;
  background: linear-gradient(to top, var(--color-primary-dark), var(--color-primary));
  border-radius: 6px 6px 0 0;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  transition: height 0.5s ease;
  position: relative;
}

.habit-val {
  position: absolute;
  top: -20px;
  font-size: 11px;
  font-weight: 600;
  color: var(--color-text-secondary);
}

.habit-label {
  font-size: 11px;
  color: var(--color-text-hint);
  margin-top: 6px;
}

/* 已读完列表 */
.finished-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.finished-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 16px;
  cursor: pointer;
  transition: transform 0.15s;
}

.finished-item:active {
  transform: scale(0.98);
}

.finished-cover {
  width: 40px;
  height: 54px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-text {
  font-size: 9px;
  color: #fff;
  font-weight: 600;
  text-align: center;
  padding: 2px;
  line-height: 1.2;
}

.finished-info {
  flex: 1;
  min-width: 0;
}

.finished-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.finished-author {
  font-size: 12px;
  color: var(--color-text-hint);
  margin-top: 2px;
}

.finished-check {
  flex-shrink: 0;
}

.empty-inline {
  text-align: center;
  padding: 30px;
  color: var(--color-text-hint);
  font-size: 13px;
}
</style>
