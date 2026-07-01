<template>
  <div class="stats-page">
    <div class="stats-content">
      <!-- 页头 -->
      <div class="page-header">
        <div class="page-title">阅读统计</div>
        <div class="page-subtitle">坚持阅读，每天都有进步</div>
      </div>

      <!-- 概览带 -->
      <div class="overview-band">
        <div class="ov-item">
          <div class="ov-icon">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"/></svg>
          </div>
          <div>
            <span class="ov-num">{{ overview.totalMinutesText || '0分钟' }}</span>
          </div>
          <div class="ov-label">总阅读时长</div>
        </div>
        <div class="ov-divider"></div>
        <div class="ov-item">
          <div class="ov-icon">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M18 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 4h5v8l-2.5-1.5L6 12V4z"/></svg>
          </div>
          <div>
            <span class="ov-num">{{ overview.totalBooks || 0 }}</span>
            <span class="ov-unit">本</span>
          </div>
          <div class="ov-label">书籍总数</div>
        </div>
        <div class="ov-divider"></div>
        <div class="ov-item">
          <div class="ov-icon">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M16 6l2.29 2.29-4.88 4.88-4-4L2 16.59 3.41 18l6-6 4 4 6.3-6.29L22 12V6z"/></svg>
          </div>
          <div>
            <span class="ov-num">{{ overview.streak || 0 }}</span>
            <span class="ov-unit">天</span>
          </div>
          <div class="ov-label">连续打卡</div>
        </div>
      </div>

      <!-- 热力图 + 阅读习惯 -->
      <div class="heatmap-row">
        <!-- 热力图 -->
        <div class="heatmap-card">
          <div class="sec-head">
            <span class="sec-title">年度热力图</span>
            <span class="sec-hint">{{ currentYear }}年</span>
          </div>
          <div class="heatmap-scroll">
            <div class="heatmap-inner" :style="{ width: heatmapWidth + 'px' }">
              <!-- 月份标签 -->
              <div class="hm-months-row">
                <div class="hm-dow-spacer" :style="{ width: '24px' }"></div>
                <div class="hm-months" :style="{ position: 'relative', height: '16px' }">
                  <span
                    v-for="m in months"
                    :key="m.month"
                    class="hm-month"
                    :style="{ left: m.offset + 'px' }"
                  >{{ m.label }}</span>
                </div>
              </div>
              <!-- 主体 -->
              <div class="hm-body-row">
                <!-- 星期 -->
                <div class="hm-dow-col">
                  <div class="hm-dow-label"></div>
                  <div class="hm-dow-label">一</div>
                  <div class="hm-dow-label"></div>
                  <div class="hm-dow-label">三</div>
                  <div class="hm-dow-label"></div>
                  <div class="hm-dow-label">五</div>
                  <div class="hm-dow-label"></div>
                </div>
                <!-- 周列 -->
                <div class="hm-weeks">
                  <div class="hm-week" v-for="(week, wi) in heatmapWeeks" :key="wi">
                    <div
                      v-for="(day, di) in week"
                      :key="di"
                      class="heat-cell"
                      :class="day ? `heat-${heatLevel(day.minutes)}` : 'heat-empty'"
                      :title="day ? `${day.date}: ${day.minutes}分钟` : ''"
                    ></div>
                  </div>
                </div>
              </div>
              <!-- 图例 -->
              <div class="hm-legend">
                <span>少</span>
                <div class="heat-cell heat-0"></div>
                <div class="heat-cell heat-1"></div>
                <div class="heat-cell heat-2"></div>
                <div class="heat-cell heat-3"></div>
                <div class="heat-cell heat-4"></div>
                <span>多</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 阅读习惯 -->
        <div class="habit-card">
          <div class="habit-title">阅读习惯</div>
          <div class="habit-stats">
            <div class="habit-stat">
              <span class="habit-stat-num">{{ habit.avgPerDay || 0 }}</span>
              <span class="habit-stat-label">日均(分)</span>
            </div>
            <div class="habit-stat">
              <span class="habit-stat-num">{{ habit.bestDay || '—' }}</span>
              <span class="habit-stat-label">最活跃</span>
            </div>
          </div>
          <div class="habit-bars">
            <div class="habit-bar-row" v-for="(d, i) in habit.weekly || []" :key="i">
              <span class="habit-bar-label">{{ d.label }}</span>
              <div class="habit-bar-track">
                <div class="habit-bar-fill" :style="{ width: (d.percent || 0) + '%' }"></div>
              </div>
              <span class="habit-bar-val">{{ d.minutes || 0 }}m</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 已读完列表 -->
      <div class="stats-section" v-if="finishedBooks.length">
        <div class="sec-head">
          <span class="sec-title">已读完</span>
          <span class="sec-hint">{{ finishedBooks.length }} 本</span>
        </div>
        <div class="fin-grid">
          <div class="fin-card" v-for="book in finishedBooks" :key="book.id" @click="$router.push(`/reader/${book.id}`)">
            <div class="fin-cover" :class="finCoverClass(book)" :style="finCoverStyle(book)">
              <span class="fin-cover-title" v-if="!book.coverImagePath">{{ book.title }}</span>
            </div>
            <div class="fin-info">
              <div class="fin-name">{{ book.title }}</div>
              <div class="fin-meta">{{ book.author || '未知' }}</div>
            </div>
            <div class="fin-check">
              <svg width="20" height="20" viewBox="0 0 24 24"><path fill="#059669" d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/></svg>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getStatsOverview, getHeatmap, getWeeklyHabit } from '../api/stats'
import { getBookshelf } from '../api/book'

const overview = ref({})
const heatmapData = ref([])
const habit = ref({})
const books = ref([])

const currentYear = new Date().getFullYear()

const finishedBooks = computed(() => books.value.filter(b => b.status === 'finished'))

// 热力图数据映射 (date string -> data point)
const heatmapMap = computed(() => {
  const map = {}
  heatmapData.value.forEach(d => {
    map[d.date] = d
  })
  return map
})

// 热力图按周列布局 - 从1月1日所在周的周一开始，生成全年所有周
const heatmapWeeks = computed(() => {
  const year = currentYear
  const map = heatmapMap.value

  // 找到1月1日所在周的周一
  const jan1 = new Date(year, 0, 1)
  const jan1Dow = (jan1.getDay() + 6) % 7 // 周一=0, 周日=6
  const firstMonday = new Date(jan1)
  firstMonday.setDate(jan1.getDate() - jan1Dow)

  // 生成所有周直到12月31日之后
  const weeks = []
  const cursor = new Date(firstMonday)
  const dec31 = new Date(year, 11, 31)
  while (cursor <= dec31) {
    const week = []
    for (let i = 0; i < 7; i++) {
      const d = new Date(cursor)
      d.setDate(cursor.getDate() + i)
      // 本地日期格式 YYYY-MM-DD
      const dateStr = d.getFullYear() + '-' +
        String(d.getMonth() + 1).padStart(2, '0') + '-' +
        String(d.getDate()).padStart(2, '0')
      if (d.getFullYear() !== year) {
        week.push(null) // 年份外的日期不显示
      } else if (map[dateStr]) {
        week.push(map[dateStr])
      } else {
        week.push({ date: dateStr, minutes: 0 }) // 无数据但属于该年，显示灰色
      }
    }
    weeks.push(week)
    cursor.setDate(cursor.getDate() + 7)
  }
  return weeks
})

const heatmapWidth = computed(() => heatmapWeeks.value.length * 14 + 24)

const months = computed(() => {
  const result = []
  let lastMonth = -1
  let lastOffset = -100
  heatmapWeeks.value.forEach((week, wi) => {
    for (let i = 0; i < 7; i++) {
      if (week[i]) {
        const m = new Date(week[i].date).getMonth()
        if (m !== lastMonth) {
          const offset = wi * 14
          // 间距小于3列的跳过避免重叠
          if (offset - lastOffset >= 42 || lastMonth === -1) {
            lastMonth = m
            lastOffset = offset
            result.push({ month: m, label: `${m + 1}月`, offset })
          } else {
            lastMonth = m
            lastOffset = offset
          }
        }
        break
      }
    }
  })
  return result
})

function heatLevel(minutes) {
  if (!minutes || minutes === 0) return 0
  if (minutes < 15) return 1
  if (minutes < 30) return 2
  if (minutes < 60) return 3
  return 4
}

// 格式化阅读时长
function formatMinutes(mins) {
  if (!mins) return '0分钟'
  const h = Math.floor(mins / 60)
  const m = mins % 60
  if (h > 0 && m > 0) return `${h}小时${m}分`
  if (h > 0) return `${h}小时`
  return `${m}分钟`
}

// 已读书籍封面样式
function finCoverStyle(book) {
  if (book.coverImagePath) {
    const url = book.coverImagePath.startsWith('http') ? book.coverImagePath : '/api' + book.coverImagePath
    return { background: `url(${url}) center/cover` }
  }
  if (book.coverColor) {
    return { background: book.coverColor }
  }
  return {}
}
function finCoverClass(book) {
  if (book.coverImagePath || book.coverColor) return ''
  return `cover-${(book.id % 6) + 1}`
}

onMounted(async () => {
  try {
    const [ovRes, hmRes, habRes, bookRes] = await Promise.all([
      getStatsOverview(),
      getHeatmap(currentYear),
      getWeeklyHabit(),
      getBookshelf()
    ])
    // 概览数据：后端返回streakDays，前端用streak
    const ov = ovRes.data || {}
    overview.value = {
      ...ov,
      streak: ov.streakDays || 0,
      totalMinutesText: formatMinutes(ov.totalMinutes)
    }
    heatmapData.value = hmRes.data || []

    // 阅读习惯：后端返回int[7](0=周一,6=周日)，需转换为对象格式
    const rawHabit = Array.isArray(habRes.data) ? habRes.data : [0, 0, 0, 0, 0, 0, 0]
    const dayLabels = ['一', '二', '三', '四', '五', '六', '日']
    const maxVal = Math.max(...rawHabit, 1)
    const total = rawHabit.reduce((a, b) => a + b, 0)
    const activeDays = rawHabit.filter(v => v > 0).length
    const bestIdx = rawHabit.indexOf(Math.max(...rawHabit))
    habit.value = {
      avgPerDay: activeDays > 0 ? Math.round(total / activeDays) : 0,
      bestDay: Math.max(...rawHabit) > 0 ? dayLabels[bestIdx] : '—',
      weekly: rawHabit.map((minutes, i) => ({
        label: dayLabels[i],
        minutes: minutes,
        percent: maxVal > 0 ? Math.round(minutes * 100 / maxVal) : 0
      }))
    }

    books.value = bookRes.data || []
  } catch (e) {
    console.error('加载统计数据失败:', e)
  }
})
</script>

<style scoped>
.stats-page {
    min-height: calc(100vh - var(--nav-height));
    padding-bottom: 48px;
}
.stats-content {
    max-width: var(--content-max-width);
    margin: 0 auto;
    padding: 0;
}

/* 概览带 */
.overview-band {
    display: flex;
    align-items: center;
    background: var(--color-bg-card);
    border: 1px solid var(--color-border-light);
    border-radius: 8px;
    padding: 24px 0;
    margin-bottom: 32px;
    box-shadow: 0 1px 2px rgba(0,0,0,0.04);
}
.ov-item {
    flex: 1;
    text-align: center;
}
.ov-divider {
    width: 1px;
    height: 48px;
    background: var(--color-border-light);
    flex-shrink: 0;
}
.ov-icon {
    width: 36px;
    height: 36px;
    margin: 0 auto 8px;
    border-radius: 10px;
    background: #FEF3C7;
    color: #D97706;
    display: flex;
    align-items: center;
    justify-content: center;
}
.ov-icon svg { width: 18px; height: 18px; }
.ov-num {
    font-size: 24px;
    font-weight: 700;
    color: var(--color-text);
    line-height: 1.1;
}
.ov-unit {
    font-size: 13px;
    font-weight: 500;
    color: #92400E;
    margin-left: 1px;
}
.ov-label {
    font-size: 12px;
    color: var(--color-text-tertiary);
    margin-top: 4px;
}

/* 区块 */
.stats-section { margin-bottom: 32px; }
.sec-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
}
.sec-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--color-text);
}
.sec-hint {
    font-size: 12px;
    color: var(--color-text-tertiary);
}

/* 热力图 + 阅读习惯 */
.heatmap-row {
    display: flex;
    gap: 16px;
    align-items: flex-start;
    margin-bottom: 32px;
}
.heatmap-card {
    flex: 1;
    min-width: 0;
    background: var(--color-bg-card);
    border: 1px solid var(--color-border-light);
    border-radius: 8px;
    padding: 18px 20px 12px;
    box-shadow: 0 1px 2px rgba(0,0,0,0.04);
    overflow: hidden;
}

.habit-card {
    width: 260px;
    flex-shrink: 0;
    background: var(--color-bg-card);
    border: 1px solid var(--color-border-light);
    border-radius: 8px;
    padding: 18px 20px;
    box-shadow: 0 1px 2px rgba(0,0,0,0.04);
}
.habit-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--color-text);
    margin-bottom: 14px;
}
.habit-stats {
    display: flex;
    gap: 12px;
    margin-bottom: 18px;
}
.habit-stat {
    flex: 1;
    text-align: center;
    background: #FFFBEB;
    border-radius: 10px;
    padding: 10px 6px;
}
.habit-stat-num {
    display: block;
    font-size: 18px;
    font-weight: 700;
    color: #D97706;
    line-height: 1.2;
}
.habit-stat-label {
    display: block;
    font-size: 11px;
    color: #A16207;
    margin-top: 2px;
}
.habit-bars {
    display: flex;
    flex-direction: column;
    gap: 7px;
}
.habit-bar-row {
    display: flex;
    align-items: center;
    gap: 8px;
}
.habit-bar-label {
    width: 32px;
    font-size: 11px;
    color: var(--color-text-tertiary);
    flex-shrink: 0;
    text-align: right;
}
.habit-bar-track {
    flex: 1;
    height: 8px;
    background: #F3F0E8;
    border-radius: 4px;
    overflow: hidden;
}
.habit-bar-fill {
    height: 100%;
    background: linear-gradient(90deg, #FCD34D, #F59E0B);
    border-radius: 4px;
    transition: width 0.4s ease;
}
.habit-bar-val {
    width: 38px;
    font-size: 10px;
    color: var(--color-text-tertiary);
    flex-shrink: 0;
    text-align: right;
}

/* 热力图 */
.heatmap-scroll {
    overflow-x: auto;
    overflow-y: hidden;
}
.hm-months-row {
    display: flex;
    margin-bottom: 4px;
}
.hm-dow-spacer { flex-shrink: 0; }
.hm-month {
    position: absolute;
    font-size: 10px;
    color: var(--color-text-tertiary);
    line-height: 16px;
    white-space: nowrap;
}
.hm-body-row {
    display: flex;
}
.hm-dow-col {
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
}
.hm-dow-label {
    height: 11px;
    font-size: 9px;
    line-height: 11px;
    color: var(--color-text-tertiary);
    text-align: right;
    padding-right: 6px;
}
.hm-dow-label:empty { visibility: hidden; }
.hm-weeks {
    flex-shrink: 0;
    display: flex;
    gap: 3px;
}
.hm-week {
    display: flex;
    flex-direction: column;
    gap: 3px;
}
.heat-cell {
    width: 11px;
    height: 11px;
    border-radius: 2px;
    display: block;
    flex-shrink: 0;
}
.heat-empty { background: transparent; }
.heat-0 { background: #EBEDF0; }
.heat-1 { background: #FDE68A; }
.heat-2 { background: #FCD34D; }
.heat-3 { background: #F59E0B; }
.heat-4 { background: #D97706; }
.heat-cell:not(.heat-empty):hover {
    outline: 1.5px solid rgba(217,119,6,0.5);
    outline-offset: -1px;
}
.hm-legend {
    display: flex;
    align-items: center;
    gap: 4px;
    justify-content: flex-end;
    margin-top: 10px;
    font-size: 10px;
    color: var(--color-text-tertiary);
}

/* 已读完网格 */
.fin-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 14px;
}
.fin-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 16px;
    background: var(--color-bg-card);
    border: 1px solid var(--color-border-light);
    border-radius: 12px;
    cursor: pointer;
    color: var(--color-text);
    transition: box-shadow 0.2s, transform 0.2s;
}
.fin-card:hover {
    box-shadow: 0 3px 12px rgba(0,0,0,0.07);
    transform: translateY(-1px);
}
.fin-cover {
    width: 40px; height: 56px;
    border-radius: 5px;
    flex-shrink: 0;
    display: flex; align-items: center; justify-content: center;
    padding: 4px;
    box-shadow: 0 1px 4px rgba(0,0,0,0.1);
    overflow: hidden;
}
.fin-cover-title {
    color: #FFF; font-size: 9px; font-weight: 500;
    text-align: center; line-height: 1.3;
    text-shadow: 0 1px 2px rgba(0,0,0,0.25);
    word-break: break-all;
}
.fin-info { flex: 1; min-width: 0; }
.fin-name {
    font-size: 13px; font-weight: 600;
    color: var(--color-text);
    white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.fin-meta { font-size: 11px; color: var(--color-text-tertiary); margin-top: 3px; }
.fin-check { flex-shrink: 0; }

/* 响应式 */
@media (max-width: 760px) {
    .overview-band { flex-direction: column; padding: 20px 0; }
    .ov-divider { width: 60%; height: 1px; }
    .fin-grid { grid-template-columns: 1fr; }
    .heatmap-row { flex-direction: column; }
    .habit-card { width: 100%; }
}
</style>
