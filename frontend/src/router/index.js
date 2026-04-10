import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/ArticleList.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/github',
    name: 'Github',
    component: () => import('../views/GithubTrending.vue'),
    meta: { title: 'GitHub热门' }
  },
  {
    path: '/article/:id',
    name: 'ArticleDetail',
    component: () => import('../views/ArticleDetail.vue'),
    meta: { title: '文章详情' }
  },
  {
    path: '/about',
    name: 'About',
    component: () => import('../views/About.vue'),
    meta: { title: '关于' }
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('../views/MyCollection.vue'),
    meta: { requiresAuth: true, title: '我的收藏' }
  },
  {
    path: '/likes',
    name: 'Likes',
    component: () => import('../views/MyCollection.vue'),
    meta: { requiresAuth: true, title: '我的喜欢' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/Admin.vue'),
    meta: { requiresAuth: true, requiresAdmin: true, title: '管理后台' }
  },
  {
    path: '/admin/article',
    name: 'AdminArticle',
    component: () => import('../views/AdminArticle.vue'),
    meta: { requiresAuth: true, requiresAdmin: true, title: '编辑文章' }
  },
  {
    path: '/admin/article/:id',
    name: 'AdminArticleEdit',
    component: () => import('../views/AdminArticle.vue'),
    meta: { requiresAuth: true, requiresAdmin: true, title: '编辑文章' }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { requiresAuth: true, title: '个人中心' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: { title: '页面未找到' }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    return { top: 0 }
  }
})

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    if (!token) {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
    if (to.meta.requiresAdmin) {
      const role = (localStorage.getItem('userRole') || '').toLowerCase()
      if (role !== 'admin') {
        next('/')
        return
      }
    }
    next()
  } else {
    next()
  }
})

router.afterEach((to) => {
  const title = to.meta.title ? `${to.meta.title} - Erwang Blog` : 'Erwang Blog'
  document.title = title
})

export default router
