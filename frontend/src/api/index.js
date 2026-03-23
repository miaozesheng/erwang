import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api'
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

export const login = (data) => api.post('/auth/login', data)

export const getArticles = (params) => api.get('/articles', { params })

export const getArticle = (id) => api.get(`/articles/${id}`)

export const createArticle = (data) => api.post('/articles', data)

export const updateArticle = (id, data) => api.put(`/articles/${id}`, data)

export const deleteArticle = (id) => api.delete(`/articles/${id}`)

export const getCategories = () => api.get('/categories')

export const getTags = () => api.get('/tags')

export default api