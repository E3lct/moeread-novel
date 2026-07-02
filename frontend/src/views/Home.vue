<template>
  <div class="home-page">
    <section class="reading-desk">
      <div class="desk-copy">
        <span class="eyebrow">Reading Desk</span>
        <h1>{{ greeting }}，{{ displayName }}</h1>
        <p>把今天的章节轻轻续上，书架、进度和书源都在这里等你。</p>
        <div class="desk-actions">
          <button class="primary-btn" @click="openRecommend" :disabled="!recommendBook">继续阅读</button>
          <router-link class="plain-btn" to="/import">导入新书</router-link>
        </div>
      </div>

      <div class="desk-focus" v-if="recommendBook" @click="goReader(recommendBook)">
        <div class="focus-cover" :class="coverClass(recommendBook)" :style="coverStyle(recommendBook)">
          <span v-if="!hasCover(recommendBook)">{{ shortTitle(recommendBook.title) }}</span>
        </div>
        <div class="focus-info">
          <span>继续阅读</span>
          <strong>{{ recommendBook.title }}</strong>
          <small>{{ recommendBook.author || '未知作者' }} · {{ recommendBook.chapterCount || 0 }} 章</small>
          <div class="focus-progress">
            <i :style="{ width: `${recommendBook.readProgress || 0}%` }"></i>
          </div>
        </div>
      </div>

      <div class="desk-focus empty-focus" v-else>
        <div class="empty-mark">墨</div>
        <div class="focus-info">
          <span>书架还是空的</span>
          <strong>先导入一本小说</strong>
          <small>支持本地 TXT/ZIP，也支持 JSON 书源搜索下载。</small>
        </div>
      </div>
    </section>

    <section class="stats-strip">
      <article class="today-panel">
        <div class="today-ring">
          <svg width="108" height="108" viewBox="0 0 108 108">
            <circle cx="54" cy="54" r="46" fill="none" stroke="rgba(53, 75, 67, 0.12)" stroke-width="8" />
            <circle
              cx="54"
              cy="54"
              r="46"
              fill="none"
              stroke="#2f5d50"
              stroke-width="8"
              stroke-linecap="round"
              :stroke-dasharray="ringCircumference"
              :stroke-dashoffset="ringDashOffset"
              transform="rotate(-90 54 54)"
            />
          </svg>
          <div>
            <strong>{{ todayMinutes }}</strong>
            <span>分钟</span>
          </div>
        </div>
        <div class="today-copy">
          <span>今日阅读</span>
          <strong>{{ todayMinutes >= dailyGoal ? '已完成今日目标' : `目标 ${dailyGoal} 分钟` }}</strong>
          <small>连续打卡 {{ streak }} 天</small>
        </div>
      </article>

      <article class="mini-stat">
        <span>书架藏书</span>
        <strong>{{ books.length }}</strong>
        <small>本书</small>
      </article>
      <article class="mini-stat">
        <span>最近在读</span>
        <strong>{{ recentBooks.length }}</strong>
        <small>本进行中</small>
      </article>
      <article class="mini-stat">
        <span>喜爱书籍</span>
        <strong>{{ favoriteBooks.length }}</strong>
        <small>本收藏</small>
      </article>
    </section>

    <BookShelfRow
      v-if="recentBooks.length"
      title="最近在读"
      :books="recentBooks"
      @open="goReader"
    />

    <BookShelfRow
      v-if="favoriteBooks.length"
      title="喜爱书籍"
      :books="favoriteBooks"
      @open="goReader"
    />

    <section class="empty-guide" v-if="!recentBooks.length && !favoriteBooks.length">
      <div>
        <span>Start Library</span>
        <h2>先放进第一本书</h2>
        <p>可以直接上传本地小说，也可以添加开源 JSON 书源后搜索下载。</p>
      </div>
      <router-link class="primary-btn" to="/import">去导入</router-link>
    </section>
  </div>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { getBookshelf } from '../api/book'
import { getStatsOverview } from '../api/stats'
import { useUserStore } from '../stores/user'
import ProgressRing from '../components/ProgressRing.vue'

const router = useRouter()
const userStore = useUserStore()

const books = ref([])
const todayMinutes = ref(0)
const streak = ref(0)
const dailyGoal = ref(30)

const recentBooks = computed(() =>
  books.value.filter(b => b.readProgress > 0).slice(0, 8)
)

const favoriteBooks = computed(() =>
  books.value.filter(b => Number(b.isFavorite) === 1).slice(0, 8)
)

const recommendBook = computed(() =>
  books.value.find(b => b.readProgress > 0 && b.readProgress < 100) || books.value[0] || null
)

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜读愉快'
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const displayName = computed(() => userStore.userInfo?.nickname || userStore.username || '读者')
const ringCircumference = 2 * Math.PI * 46
const ringDashOffset = computed(() =>
  ringCircumference * (1 - Math.min(todayMinutes.value / Math.max(dailyGoal.value, 1), 1))
)

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

function shortTitle(title) {
  return (title || '未命名').slice(0, 8)
}

function goReader(book) {
  router.push(`/reader/${book.id}`)
}

function openRecommend() {
  if (recommendBook.value) goReader(recommendBook.value)
}

const BookShelfRow = defineComponent({
  name: 'BookShelfRow',
  props: {
    title: { type: String, required: true },
    books: { type: Array, required: true }
  },
  emits: ['open'],
  setup(props, { emit }) {
    return () => h('section', { class: 'shelf-section' }, [
      h('div', { class: 'section-header' }, [
        h('h2', props.title),
        h(RouterLink, { to: '/bookshelf', class: 'section-link' }, () => '查看全部')
      ]),
      h('div', { class: 'book-rail' }, props.books.map(book =>
        h('article', { class: 'book-tile', key: book.id, onClick: () => emit('open', book) }, [
          h('div', {
            class: ['book-cover', hasCover(book) ? 'real-cover' : `cover-${((book.id || 0) % 6) + 1}`],
            style: coverStyle(book)
          }, [
            !hasCover(book) ? h('span', shortTitle(book.title)) : null,
            h(ProgressRing, { percent: book.readProgress || 0 })
          ]),
          h('strong', book.title),
          h('small', `${book.readProgress || 0}% · ${book.author || '未知作者'}`)
        ])
      ))
    ])
  }
})

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
  } catch (e) {
    console.error('加载首页数据失败:', e)
  }
})
</script>

<style scoped>
.home-page {
  min-height: calc(100vh - var(--nav-height));
  padding-bottom: 48px;
  color: #2c2720;
}

.reading-desk {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(300px, 0.85fr);
  gap: 24px;
  min-height: 320px;
  padding: 34px;
  border-radius: 26px;
  background:
    linear-gradient(135deg, rgba(40, 55, 50, 0.96), rgba(78, 63, 46, 0.86)),
    radial-gradient(circle at 16% 15%, rgba(246, 225, 172, 0.25), transparent 34%),
    linear-gradient(45deg, #243833, #866b49);
  box-shadow: 0 28px 90px rgba(51, 43, 31, 0.18);
  overflow: hidden;
  position: relative;
}

.reading-desk::after {
  content: '';
  position: absolute;
  width: 420px;
  height: 420px;
  right: -150px;
  top: -170px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.desk-copy,
.desk-focus {
  position: relative;
  z-index: 1;
}

.desk-copy {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  color: #fffaf0;
}

.eyebrow,
.empty-guide span {
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: rgba(255, 250, 240, 0.68);
}

.desk-copy h1 {
  max-width: 620px;
  margin: 10px 0 12px;
  font-size: clamp(34px, 5vw, 58px);
  line-height: 1.02;
  letter-spacing: 0;
}

.desk-copy p {
  max-width: 520px;
  margin: 0;
  color: rgba(255, 250, 240, 0.72);
  line-height: 1.8;
}

.desk-actions {
  display: flex;
  gap: 12px;
  margin-top: 28px;
  flex-wrap: wrap;
}

.primary-btn,
.plain-btn {
  border: 0;
  border-radius: 999px;
  padding: 11px 22px;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
}

.primary-btn {
  color: #fff;
  background: #2f5d50;
  box-shadow: 0 12px 28px rgba(25, 55, 47, 0.28);
}

.primary-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.plain-btn {
  color: #2d4f45;
  background: rgba(255, 250, 240, 0.88);
}

.desk-focus {
  align-self: end;
  display: grid;
  grid-template-columns: 122px minmax(0, 1fr);
  gap: 18px;
  align-items: end;
  padding: 18px;
  border-radius: 22px;
  background: rgba(255, 250, 240, 0.14);
  border: 1px solid rgba(255, 250, 240, 0.22);
  backdrop-filter: blur(18px);
  cursor: pointer;
}

.focus-cover,
.book-cover {
  background-size: cover;
  background-position: center;
  box-shadow: 0 18px 40px rgba(19, 21, 18, 0.28);
}

.focus-cover {
  width: 122px;
  height: 170px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.focus-cover span,
.book-cover span {
  color: #fff;
  font-weight: 800;
  line-height: 1.45;
  text-align: center;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.28);
}

.focus-info {
  min-width: 0;
  color: #fffaf0;
}

.focus-info span,
.today-copy span,
.mini-stat span {
  display: block;
  font-size: 12px;
  color: rgba(255, 250, 240, 0.66);
}

.focus-info strong {
  display: block;
  margin: 8px 0 8px;
  font-size: 22px;
  line-height: 1.22;
}

.focus-info small {
  color: rgba(255, 250, 240, 0.7);
}

.focus-progress {
  height: 7px;
  margin-top: 18px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.22);
  overflow: hidden;
}

.focus-progress i {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: #f6d889;
}

.empty-focus {
  cursor: default;
}

.empty-mark {
  width: 112px;
  height: 112px;
  border-radius: 28px;
  display: grid;
  place-items: center;
  background: rgba(255, 250, 240, 0.88);
  color: #2f5d50;
  font-size: 42px;
  font-weight: 900;
}

.stats-strip {
  display: grid;
  grid-template-columns: minmax(260px, 1.4fr) repeat(3, minmax(150px, 1fr));
  gap: 14px;
  margin: 18px 0 34px;
}

.today-panel,
.mini-stat,
.empty-guide {
  border: 1px solid rgba(222, 213, 199, 0.9);
  background: rgba(255, 253, 248, 0.84);
  box-shadow: 0 18px 50px rgba(54, 45, 33, 0.08);
  backdrop-filter: blur(14px);
}

.today-panel {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 18px;
  border-radius: 22px;
}

.today-ring {
  position: relative;
  width: 108px;
  height: 108px;
  flex-shrink: 0;
}

.today-ring div {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  align-content: center;
}

.today-ring strong {
  font-size: 26px;
  color: #2f5d50;
}

.today-ring span {
  color: #8a7d6b;
  font-size: 12px;
}

.today-copy span,
.mini-stat span {
  color: #837666;
}

.today-copy strong {
  display: block;
  margin: 5px 0;
  font-size: 18px;
}

.today-copy small,
.mini-stat small,
.book-tile small {
  color: #8b8072;
}

.mini-stat {
  min-height: 132px;
  padding: 20px;
  border-radius: 22px;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.mini-stat strong {
  margin: 8px 0 2px;
  font-size: 36px;
  line-height: 1;
  color: #2f5d50;
}

.shelf-section {
  margin-bottom: 34px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-header h2 {
  margin: 0;
  font-size: 21px;
}

.section-link {
  color: #6f7f77;
  font-size: 13px;
}

.book-rail {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: 142px;
  gap: 18px;
  overflow-x: auto;
  padding: 2px 2px 12px;
}

.book-tile {
  cursor: pointer;
  min-width: 0;
}

.book-cover {
  width: 142px;
  height: 198px;
  border-radius: 14px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 14px;
  overflow: hidden;
  transition: transform 0.18s, box-shadow 0.18s;
}

.book-tile:hover .book-cover {
  transform: translateY(-3px);
  box-shadow: 0 22px 50px rgba(19, 21, 18, 0.32);
}

.book-tile strong {
  display: block;
  margin: 10px 0 3px;
  color: #2f2a22;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-tile small {
  display: block;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-cover :deep(.book-progress-ring) {
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.18s;
}

.book-tile:hover .book-cover :deep(.book-progress-ring) {
  opacity: 1;
  visibility: visible;
}

.empty-guide {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
  padding: 28px;
  border-radius: 24px;
}

.empty-guide span {
  color: #7d7162;
}

.empty-guide h2 {
  margin: 8px 0;
}

.empty-guide p {
  margin: 0;
  color: #7f7468;
}

.cover-1 { background: linear-gradient(145deg, #2f5d50, #b58a53); }
.cover-2 { background: linear-gradient(145deg, #36494f, #9f7254); }
.cover-3 { background: linear-gradient(145deg, #6b4f3f, #d1a45d); }
.cover-4 { background: linear-gradient(145deg, #344c3f, #75865c); }
.cover-5 { background: linear-gradient(145deg, #4f4a5d, #c08b73); }
.cover-6 { background: linear-gradient(145deg, #243833, #d1b36b); }

@media (max-width: 920px) {
  .reading-desk,
  .stats-strip {
    grid-template-columns: 1fr;
  }

  .desk-focus {
    align-self: stretch;
  }
}

@media (max-width: 620px) {
  .reading-desk {
    padding: 24px;
    border-radius: 20px;
  }

  .desk-focus,
  .today-panel,
  .empty-guide {
    grid-template-columns: 1fr;
    flex-direction: column;
    align-items: flex-start;
  }

  .book-rail {
    grid-auto-columns: 126px;
  }

  .book-cover {
    width: 126px;
    height: 176px;
  }
}
</style>
