<template>
  <div class="home-page">
    <div class="home-content">
      <!-- 顶部：今日阅读 + 推荐位 -->
      <div class="home-top">
        <!-- 今日阅读卡 -->
        <div class="today-card">
          <div class="today-card-inner">
            <div class="today-header">
              <span class="today-label">今日阅读</span>
              <span class="today-goal">目标 {{ dailyGoal }} 分钟</span>
            </div>
            <div class="today-ring-wrap">
              <div class="today-done-badge" v-if="todayMinutes >= dailyGoal">已达标</div>
              <svg class="today-ring" width="120" height="120" viewBox="0 0 120 120">
                <circle cx="60" cy="60" r="50" fill="none" stroke="rgba(255,255,255,0.25)" stroke-width="7"/>
                <circle
                  cx="60" cy="60" r="50" fill="none"
                  stroke="#fff" stroke-width="7"
                  stroke-linecap="round"
                  :stroke-dasharray="ringCircumference"
                  :stroke-dashoffset="ringDashOffset"
                  class="ring-progress"
                  transform="rotate(-90 60 60)"
                />
                <text x="60" y="58" text-anchor="middle" fill="#fff" font-size="28" font-weight="700">
                  {{ todayMinutes }}
                </text>
                <text x="60" y="76" text-anchor="middle" fill="rgba(255,255,255,0.8)" font-size="11">
                  分钟
                </text>
              </svg>
            </div>
            <div class="today-footer">
              连续打卡 <span class="streak-num">{{ streak }}</span> 天
            </div>
          </div>
        </div>

        <!-- 推荐位 -->
        <div class="recommend-card" v-if="recommendBook" @click="goReader(recommendBook)">
          <div class="recommend-label">继续阅读</div>
          <div class="recommend-body">
            <div class="recommend-cover" :class="coverClass(recommendBook)" :style="coverStyle(recommendBook)">
              <span class="recommend-cover-title" v-if="!hasCover(recommendBook)">{{ recommendBook.title }}</span>
            </div>
            <div class="recommend-info">
              <div class="recommend-title">{{ recommendBook.title }}</div>
              <div class="recommend-desc">{{ recommendBook.author || '未知作者' }} · {{ recommendBook.chapterCount || 0 }} 章</div>
              <div class="recommend-meta">已读 {{ recommendBook.readProgress || 0 }}%</div>
            </div>
          </div>
        </div>
        <div class="recommend-card recommend-empty" v-else>
          <div class="recommend-empty-body">
            <div class="recommend-empty-text">还没有书籍</div>
            <div class="recommend-empty-hint">导入一本小说开始阅读吧</div>
            <router-link to="/import" class="recommend-empty-btn">去导入</router-link>
          </div>
        </div>
      </div>

      <!-- 最近在读 -->
      <div class="home-section" v-if="recentBooks.length">
        <div class="section-header">
          <span class="section-title">最近在读</span>
          <router-link to="/bookshelf" class="section-more">查看全部</router-link>
        </div>
        <div class="book-row">
          <div class="home-book" v-for="book in recentBooks" :key="book.id" @click="goReader(book)">
            <div class="home-book-cover" :class="coverClass(book)" :style="coverStyle(book)">
              <span class="home-book-cover-title" v-if="!hasCover(book)">{{ book.title }}</span>
              <ProgressRing :percent="book.readProgress || 0" />
            </div>
            <div class="home-book-info">
              <div class="home-book-title text-ellipsis">{{ book.title }}</div>
              <div class="home-book-meta">{{ book.readProgress || 0 }}%</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 喜爱书籍 -->
      <div class="home-section" v-if="favoriteBooks.length">
        <div class="section-header">
          <span class="section-title">
            <svg class="section-heart" viewBox="0 0 24 24"><path fill="#D97706" d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
            喜爱书籍
          </span>
          <router-link to="/bookshelf" class="section-more">查看全部</router-link>
        </div>
        <div class="book-row">
          <div class="home-book" v-for="book in favoriteBooks" :key="book.id" @click="goReader(book)">
            <div class="home-book-cover" :class="coverClass(book)" :style="coverStyle(book)">
              <span class="home-book-cover-title" v-if="!hasCover(book)">{{ book.title }}</span>
              <ProgressRing :percent="book.readProgress || 0" />
            </div>
            <div class="home-book-info">
              <div class="home-book-title text-ellipsis">{{ book.title }}</div>
              <div class="home-book-meta">{{ book.readProgress || 0 }}%</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 引导区（无书时） -->
      <div class="home-guide" v-if="!recentBooks.length && !favoriteBooks.length">
        <div class="guide-text">还没有书籍，导入一本小说开始阅读之旅吧</div>
        <router-link to="/import" class="guide-btn">导入书籍</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getBookshelf } from '../api/book'
import { getStatsOverview } from '../api/stats'
import ProgressRing from '../components/ProgressRing.vue'

const router = useRouter()

const books = ref([])
const todayMinutes = ref(0)
const streak = ref(0)
const dailyGoal = ref(30)

const recentBooks = computed(() =>
  books.value.filter(b => b.readProgress > 0).slice(0, 6)
)

const favoriteBooks = computed(() =>
      books.value.filter(b => Number(b.isFavorite) === 1).slice(0, 6)
)

const recommendBook = computed(() =>
  books.value.find(b => b.readProgress > 0 && b.readProgress < 100) || books.value[0] || null
)

const ringCircumference = 2 * Math.PI * 50
const ringDashOffset = computed(() =>
  ringCircumference * (1 - Math.min(todayMinutes.value / dailyGoal.value, 1))
)

function goReader(book) {
  router.push(`/reader/${book.id}`)
}

function hasCover(book) {
  return !!book?.coverImagePath
}

function coverUrl(book) {
  const cover = book?.coverImagePath
  if (!cover) return ''
  return cover.startsWith('http') || cover.startsWith('data:') ? cover : '/api' + cover
}

function coverStyle(book) {
  const url = coverUrl(book)
  return url ? { backgroundImage: `url(${url})` } : {}
}

function coverClass(book) {
  return hasCover(book) ? 'real-cover' : `cover-${((book?.id || 0) % 6) + 1}`
}

onMounted(async () => {
  try {
    const [bookRes, statsRes] = await Promise.all([
      getBookshelf(),
      getStatsOverview()
    ])
    books.value = bookRes.data || []
    if (statsRes.data) {
      todayMinutes.value = statsRes.data.todayMinutes || 0
      streak.value = statsRes.data.streakDays || 0
      dailyGoal.value = statsRes.data.dailyGoal || 30
    }
    if (statsRes.data.dailyGoal) dailyGoal.value = statsRes.data.dailyGoal
  } catch (e) {
    console.error('加载首页数据失败:', e)
  }
})
</script>

<style scoped>
.home-page {
    min-height: calc(100vh - var(--nav-height));
    padding-bottom: 48px;
}

.home-content {
    max-width: var(--content-max-width);
    margin: 0 auto;
    padding: 0;
}

/* 顶部 */
.home-top {
    display: flex;
    gap: 18px;
    margin-bottom: 30px;
    align-items: stretch;
}

/* 今日阅读卡 */
.today-card {
    width: 320px;
    flex-shrink: 0;
    border-radius: 14px;
    overflow: hidden;
    background:
      linear-gradient(145deg, rgba(251, 191, 36, 0.78) 0%, rgba(217, 119, 6, 0.82) 100%),
      radial-gradient(circle at 20% 10%, rgba(255, 255, 255, 0.34), transparent 36%);
    border: 1px solid rgba(255, 255, 255, 0.42);
    box-shadow: 0 16px 38px rgba(217, 119, 6, 0.18);
    backdrop-filter: blur(14px);
}

.today-card-inner {
    padding: 24px 24px 20px;
    color: #FFFFFF;
    height: 100%;
    display: flex;
    flex-direction: column;
}

.today-header {
    display: flex;
    justify-content: space-between;
    align-items: baseline;
    margin-bottom: 8px;
}

.today-label {
    font-size: 16px;
    font-weight: 600;
    letter-spacing: 0.02em;
}

.today-goal {
    font-size: 12px;
    opacity: 0.82;
}

.today-ring-wrap {
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
    margin: 8px 0 4px;
    flex: 1;
}

.ring-progress {
    transition: stroke-dashoffset 1s ease-out;
}

.today-done-badge {
    position: absolute;
    top: 8px;
    right: 12px;
    background: rgba(255, 255, 255, 0.22);
    backdrop-filter: blur(8px);
    border: 1px solid rgba(255, 255, 255, 0.35);
    color: #FFFFFF;
    font-size: 11px;
    font-weight: 600;
    padding: 3px 10px;
    border-radius: 999px;
}

.today-footer {
    text-align: center;
    font-size: 13px;
    opacity: 0.9;
    margin-top: 4px;
}

.streak-num {
    font-size: 16px;
    font-weight: 700;
    margin: 0 2px;
}

/* 推荐位 */
.recommend-card {
    flex: 1;
    border-radius: 14px;
    background:
      linear-gradient(140deg, rgba(255, 251, 235, 0.82) 0%, rgba(254, 243, 199, 0.56) 100%),
      rgba(255, 255, 255, 0.46);
    border: 1px solid rgba(253, 230, 138, 0.72);
    padding: 22px 24px;
    display: flex;
    flex-direction: column;
    cursor: pointer;
    color: var(--color-text);
    transition: box-shadow 0.2s, transform 0.2s;
    min-height: 220px;
    backdrop-filter: blur(16px);
}

.recommend-card:hover {
    box-shadow: 0 18px 36px rgba(217, 119, 6, 0.14);
    transform: translateY(-1px);
}

.recommend-label {
    font-size: 12px;
    font-weight: 600;
    color: var(--color-text-hint);
    margin-bottom: 14px;
    letter-spacing: 0.05em;
}

.recommend-body {
    display: flex;
    gap: 18px;
    flex: 1;
    align-items: center;
}

.recommend-cover {
    width: 92px;
    height: 124px;
    border-radius: 10px;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 12px;
    box-shadow: 0 3px 10px rgba(0, 0, 0, 0.12);
    overflow: hidden;
    background-size: cover;
    background-position: center;
}

.recommend-cover-title {
    color: #FFFFFF;
    font-size: 13px;
    font-weight: 600;
    text-align: center;
    text-shadow: 0 1px 3px rgba(0, 0, 0, 0.25);
    word-break: break-all;
    line-height: 1.4;
}

.recommend-info {
    flex: 1;
    min-width: 0;
}

.recommend-title {
    font-size: 17px;
    font-weight: 600;
    color: var(--color-text);
    margin-bottom: 8px;
}

.recommend-desc {
    font-size: 13px;
    color: var(--color-text-secondary);
    line-height: 1.65;
    margin-bottom: 12px;
}

.recommend-meta {
    font-size: 12px;
    color: var(--color-text-hint);
}

/* 推荐位空状态 */
.recommend-empty {
    justify-content: center;
}

.recommend-empty-body {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    flex: 1;
    text-align: center;
}

.recommend-empty-text {
    font-size: 18px;
    font-weight: 600;
    color: var(--color-text);
    margin-bottom: 6px;
}

.recommend-empty-hint {
    font-size: 13px;
    color: var(--color-text-secondary);
    margin-bottom: 16px;
}

.recommend-empty-btn {
    background: var(--color-primary);
    color: #FFFFFF;
    padding: 8px 24px;
    border-radius: 999px;
    font-size: 13px;
    font-weight: 500;
    transition: background 0.15s;
}

.recommend-empty-btn:hover {
    background: var(--color-primary-dark);
    color: #FFFFFF;
}

/* 书籍区域 */
.home-section {
    margin-bottom: 36px;
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
}

.section-title {
    font-size: 18px;
    font-weight: 600;
    color: var(--color-text);
    display: flex;
    align-items: center;
    gap: 6px;
}

.section-heart {
    width: 16px;
    height: 16px;
}

.section-more {
    font-size: 13px;
    color: var(--color-text-hint);
    transition: color 0.15s;
}

.section-more:hover {
    color: var(--color-primary-dark);
}

/* 横向书籍列表 */
.book-row {
    display: flex;
    gap: 20px;
    overflow-x: auto;
    padding-bottom: 8px;
    scroll-behavior: smooth;
}

.home-book {
    flex-shrink: 0;
    width: 130px;
    cursor: pointer;
    color: var(--color-text);
    transition: transform 0.15s;
}

.home-book:hover {
    transform: translateY(-2px);
}

.home-book-cover {
    width: 130px;
    height: 180px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 14px;
    box-shadow: 0 10px 24px rgba(217, 119, 6, 0.13);
    position: relative;
    overflow: hidden;
    transition: box-shadow 0.2s;
    background-size: cover;
    background-position: center;
}

.home-book:hover .home-book-cover {
    box-shadow: 0 18px 34px rgba(217, 119, 6, 0.22);
}

.home-book-cover-title {
    color: #FFFFFF;
    font-size: 14px;
    font-weight: 600;
    text-align: center;
    text-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
    word-break: break-all;
    line-height: 1.45;
    opacity: 1;
    transition: opacity 0.2s;
}

.home-book:hover .home-book-cover-title {
    opacity: 0;
}

.home-book:hover .book-progress-ring {
    opacity: 1;
    visibility: visible;
    transition: opacity 0.2s ease, visibility 0s 0s;
}

.home-book-info {
    padding: 10px 2px 0;
}

.home-book-title {
    font-size: 13px;
    font-weight: 500;
    color: var(--color-text);
    margin-bottom: 3px;
}

.home-book-meta {
    font-size: 11px;
    color: var(--color-text-tertiary);
}

/* 引导区 */
.home-guide {
    background: var(--color-bg-card);
    border-radius: 8px;
    border: 1px solid var(--color-border-light);
    padding: 32px;
    text-align: center;
}

.guide-text {
    font-size: 15px;
    color: var(--color-text-secondary);
    margin-bottom: 16px;
}

.guide-btn {
    display: inline-block;
    background: var(--color-primary);
    color: #FFFFFF;
    padding: 8px 28px;
    border-radius: 999px;
    font-size: 13px;
    font-weight: 500;
    transition: background 0.15s;
}

.guide-btn:hover {
    background: var(--color-primary-dark);
    color: #FFFFFF;
}

/* 响应式 */
@media (max-width: 760px) {
    .home-top {
        flex-direction: column;
    }
    .today-card {
        width: 100%;
    }
}
</style>
