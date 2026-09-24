import axios from 'axios'

const BASE_URL = import.meta.env.VITE_API_URL || ''

export const api = axios.create({ baseURL: BASE_URL, paramsSerializer: { indexes: null } })

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
  return {
    id: v.id,
    title: v.title || 'Вакансия',
    company: v.companyName || '—',
    companyId: v.companyId,
    city: v.city || '',
    salaryFrom: v.salaryFrom ?? null,
    salaryTo: v.salaryTo ?? null,
    employmentType: v.employmentType || null,
    specialization: v.specialization || null,
    grade: v.grade || null,
    experience: v.experience || null,
    workFormat: v.workFormat || null,
    remote: v.workFormat === 'REMOTE',
    status: v.status || 'ACTIVE',
    description: v.description || '',
    skills: (v.skillNames || v.skills || []).map((s) => (typeof s === 'string' ? s : s.name)),
    createdAt: v.createdAt || null,
    applicationsCount: v.applicationsCount ?? null,
    newApplicationsCount: v.newApplicationsCount ?? null,
  }
}

function unwrapPage(data) {
  if (Array.isArray(data)) return { items: data, total: data.length }
  const items = data.content || data.items || []
  const total = data.page?.totalElements ?? data.totalElements ?? items.length
  const pages = data.page?.totalPages ?? data.totalPages ?? 1
  return { items, total, pages }
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
      return { items: p.items.map(mapVacancy), total: p.total, pages: p.pages }
    }),
  get: (id) => api.get(`/api/vacancies/${id}`).then((r) => mapVacancy(r.data)),
  create: (data) => api.post('/api/vacancies', data).then((r) => r.data),
  mine: (params = {}) =>
    api.get('/api/vacancies/my', { params }).then((r) => {
      const p = unwrapPage(r.data)
      return { items: p.items.map(mapVacancy), total: p.total }
    }),
}

export const dictionariesApi = {
  get: () => api.get('/api/dictionaries').then((r) => r.data),
}

export const companiesApi = {
  mine: () => api.get('/api/companies/my').then((r) => r.data),
  create: (data) => api.post('/api/companies', data).then((r) => r.data),
  count: () => api.get('/api/companies', { params: { size: 1 } }).then((r) => unwrapPage(r.data).total),
  update: (id, data) => api.put(`/api/companies/${id}`, data).then((r) => r.data),
}

export const applicationsApi = {
  apply: (vacancyId, coverLetter) =>
    api.post(`/api/applications/apply/${vacancyId}`, { coverLetter: coverLetter || null }).then((r) => r.data),
  mine: () => api.get('/api/applications/my').then((r) => r.data),
  forVacancy: (vacancyId) => api.get(`/api/applications/vacancy/${vacancyId}`).then((r) => r.data),
  open: (id) => api.get(`/api/applications/${id}`).then((r) => r.data),
  setStatus: (id, status, comment) =>
    api.patch(`/api/applications/${id}/status`, { status, comment: comment || null }).then((r) => r.data),
}

export { BASE_URL }
