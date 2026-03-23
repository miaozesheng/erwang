import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/ArticleList.vue')
  },
  {
    path: '/article/:id',
    name: 'ArticleDetail',
    component: () => import('../views/ArticleDetail.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/Admin.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/article',
    name: 'AdminArticle',
    component: () => import('../views/AdminArticle.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/article/:id',
    name: 'AdminArticleEdit',
    component: () => import('../views/AdminArticle.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    if (!token) {
      next('/login')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router