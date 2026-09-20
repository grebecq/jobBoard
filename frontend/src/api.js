import axios from 'axios'

const BASE_URL = import.meta.env.VITE_API_URL || ''

export const api = axios.create({ baseURL: BASE_URL })

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response && err.response.status === 401) {
      const hadToken = !!localStorage.getItem('token')
      localStorage.removeItem('token')
      if (hadToken) window.dispatchEvent(new Event('auth:expired'))
    }
    return Promise.reject(err)
  }
)

export function apiMessage(err, fallback = 'Что-то пошло не так') {
  return err?.response?.data?.message || fallback
}

export function setToken(token) {
  if (token) localStorage.setItem('token', token)
  else localStorage.removeItem('token')
}

export const authApi = {
  register: (email, password, role) =>
    api.post('/api/auth/register', { email, password, role }).then((r) => r.data),
  login: (email, password) =>
    api.post('/api/auth/login', { email, password }).then((r) => r.data),
  me: () => api.get('/api/auth/me').then((r) => r.data),
}

export function mapVacancy(v) {
  if (!v) return v
  const empRaw = v.employmentType || v.type || ''
  return {
    id: v.id,
    title: v.title || v.name || 'Вакансия',
    company: v.companyName || (v.company && v.company.name) || v.company || '—',
    city: v.city || v.location || '—',
    salaryFrom: v.salaryFrom ?? v.minSalary ?? null,
    salaryTo: v.salaryTo ?? v.maxSalary ?? null,
    employmentType: empRaw,
    remote: String(empRaw).toUpperCase().includes('REMOTE') || /удал/i.test(empRaw),
    status: v.status || 'OPEN',
    description: v.description || '',
    skills: (v.skillNames || v.skills || []).map((s) => (typeof s === 'string' ? s : s.name)),
    createdAt: v.createdAt || null,
  }
}

function unwrapPage(data) {
  if (Array.isArray(data)) return { items: data, total: data.length }
  const items = data.content || data.items || []
  const total = data.page?.totalElements ?? data.totalElements ?? items.length
  return { items, total }
}

export const vacanciesApi = {
  list: (params = {}) =>
    api.get('/api/vacancies', { params }).then((r) => {
      const p = unwrapPage(r.data)
      return { items: p.items.map(mapVacancy), total: p.total }
    }),
  search: (params = {}) =>
    api.get('/api/vacancies/search', { params }).then((r) => {
      const p = unwrapPage(r.data)
      return { items: p.items.map(mapVacancy), total: p.total }
    }),
  get: (id) => api.get(`/api/vacancies/${id}`).then((r) => mapVacancy(r.data)),
  create: (data) => api.post('/api/vacancies', data).then((r) => r.data),
  mine: (params = {}) =>
    api.get('/api/vacancies/my', { params }).then((r) => {
      const p = unwrapPage(r.data)
      return { items: p.items.map(mapVacancy), total: p.total }
    }),
}

export const companiesApi = {
  mine: () => api.get('/api/companies/my').then((r) => r.data),
  create: (data) => api.post('/api/companies', data).then((r) => r.data),
}

export const applicationsApi = {
  apply: (vacancyId) => api.post(`/api/applications/apply/${vacancyId}`).then((r) => r.data),
  mine: () => api.get('/api/applications/my').then((r) => r.data),
}

export { BASE_URL }
