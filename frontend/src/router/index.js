import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('../components/AppLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'bookshelf',
        name: 'Bookshelf',
        component: () => import('../views/Bookshelf.vue'),
        meta: { title: '书架' }
      },
      {
        path: 'stats',
        name: 'Stats',
        component: () => import('../views/Stats.vue'),
        meta: { title: '统计' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('../views/Settings.vue'),
        meta: { title: '设置' }
      }
    ]
  },
  {
    path: '/reader/:bookId',
    name: 'Reader',
    component: () => import('../views/Reader.vue'),
    meta: { title: '阅读', hideNav: true }
  },
  {
    path: '/muck/:bookId',
    name: 'MuckMode',
    component: () => import('../views/MuckMode.vue'),
    meta: { title: '摸鱼模式', hideNav: true }
  },
  {
    path: '/import',
    name: 'Import',
    component: () => import('../views/Import.vue'),
    meta: { title: '导入' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (!token && to.name !== 'Login') {
    next('/login')
  } else if (token && to.name === 'Login') {
    next('/home')
  } else {
    next()
  }
})

export default router
