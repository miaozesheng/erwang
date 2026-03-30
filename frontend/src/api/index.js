import axios from 'axios'
import router from '../router'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE
})

api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userRole')
      const currentPath = router.currentRoute?.value?.path
      if (currentPath && currentPath !== '/login') {
        router.push('/login')
      }
    }
    return Promise.reject(error)
  }
)

export const resolveFileUrl = (url) => {
  if (!url) return ''
  if (/^https?:\/\//.test(url)) return url
  const base = import.meta.env.VITE_FILE_BASE
  return base ? `${base}${url}` : url
}

export const login = (data) => api.post('/auth/login', data)

export const register = (data) => api.post('/auth/register', data)

export const getArticles = (params) => api.get('/articles', { params })

export const getArticle = (id) => api.get(`/articles/${id}`)

export const createArticle = (data) => api.post('/articles', data)

export const updateArticle = (id, data) => api.put(`/articles/${id}`, data)

export const deleteArticle = (id) => api.delete(`/articles/${id}`)

export const getCategories = () => api.get('/categories')
export const createCategory = (name) => api.post('/categories', { name })

export const getTags = () => api.get('/tags')
export const createTag = (name) => api.post('/tags', { name })

export const getQuickLinks = () => api.get('/quick-links')
export const createQuickLink = (data) => api.post('/quick-links', data)
export const updateQuickLink = (id, data) => api.put(`/quick-links/${id}`, data)
export const deleteQuickLink = (id) => api.delete(`/quick-links/${id}`)

export const toggleLike = (articleId) => api.post(`/interaction/like/${articleId}`)
export const toggleFavorite = (articleId) => api.post(`/interaction/favorite/${articleId}`)
export const getInteractionStatus = (articleId) => api.get(`/interaction/status/${articleId}`)
export const getMyFavorites = () => api.get('/interaction/my-favorites')
export const getMyLikes = () => api.get('/interaction/my-likes')

export const getStats = () => api.get('/admin/stats')

export const getUserInfo = () => api.get('/auth/info')

export const updateProfile = (data) => api.put('/auth/profile', data)

export const changePassword = (data) => api.put('/auth/password', data)

export const uploadAvatar = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return api.post('/auth/avatar', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}

export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return api.post('/files/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}

export default api
