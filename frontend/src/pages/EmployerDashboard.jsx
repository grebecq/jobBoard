import { useEffect, useState } from 'react'
import { useAuth } from '../auth.jsx'
import { useToast } from '../toast.jsx'
import { vacanciesApi, companiesApi, apiMessage } from '../api.js'
import { formatSalary } from '../format.js'

export default function EmployerDashboard() {
  const { user } = useAuth()
  const toast = useToast()
  const [vacancies, setVacancies] = useState([])
  const [companies, setCompanies] = useState([])
  const [loading, setLoading] = useState(true)
  const [f, setF] = useState({ title: '', city: '', employmentType: 'FULL_TIME', salaryFrom: '', salaryTo: '', description: '', companyId: '' })
  const [company, setCompany] = useState({ name: '', website: '', description: '' })
  const [busy, setBusy] = useState(false)
  const [companyBusy, setCompanyBusy] = useState(false)
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value })
  const setC = (k) => (e) => setCompany({ ...company, [k]: e.target.value })

  async function load() {
    setLoading(true)
    try {
      const [mine, myCompanies] = await Promise.all([
        vacanciesApi.mine({ size: 50 }),
        companiesApi.mine(),
      ])
      setVacancies(mine.items)
      setCompanies(myCompanies)
      setF((prev) => ({ ...prev, companyId: prev.companyId || (myCompanies[0]?.id ?? '') }))
    } catch (err) {
      setVacancies([])
      setCompanies([])
      toast(apiMessage(err, 'Не удалось загрузить данные'))
    } finally {
      setLoading(false)
    }
  }
  useEffect(() => { load() }, [])

  async function createCompany(e) {
    e.preventDefault()
    setCompanyBusy(true)
    try {
      const created = await companiesApi.create({
        name: company.name,
        website: company.website || null,
        description: company.description || null,
      })
      toast('Компания создана ✓')
      setCompany({ name: '', website: '', description: '' })
      setCompanies((prev) => [...prev, created])
      setF((prev) => ({ ...prev, companyId: created.id }))
    } catch (err) {
      toast(apiMessage(err, 'Не удалось создать компанию'))
    } finally {
      setCompanyBusy(false)
    }
  }

  async function publish(e) {
    e.preventDefault()
    if (!f.companyId) {
      toast('Сначала создайте компанию')
      return
    }
    setBusy(true)
    try {
      await vacanciesApi.create({
        title: f.title,
        description: f.description,
        city: f.city,
        employmentType: f.employmentType,
        salaryFrom: f.salaryFrom ? Number(f.salaryFrom) : null,
        salaryTo: f.salaryTo ? Number(f.salaryTo) : null,
        companyId: Number(f.companyId),
      })
      toast('Вакансия опубликована ✓')
      setF({ title: '', city: '', employmentType: 'FULL_TIME', salaryFrom: '', salaryTo: '', description: '', companyId: f.companyId })
      load()
    } catch (err) {
      toast(apiMessage(err, 'Не удалось опубликовать вакансию'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <section className="view wrap">
      <div className="dash-head">
        <div className="eyebrow">Работодатель · {user?.email}</div>
        <h1>Мои вакансии</h1>
      </div>

      <div className="panel">
        <div className="panel-head"><h3>Опубликованные вакансии</h3></div>
        {loading ? (
          <div className="spinner" />
        ) : vacancies.length === 0 ? (
          <div className="center-msg">Пока нет вакансий. Опубликуй первую в форме ниже.</div>
        ) : (
          vacancies.map((v) => (
            <div className="trow" key={v.id}>
              <div>
                <div className="ti">{v.title}</div>
                <div className="ts">{v.company} · {v.city} · {formatSalary(v.salaryFrom, v.salaryTo)}</div>
              </div>
              <span className={'status ' + (String(v.status).toUpperCase() === 'CLOSED' ? 'closed' : 'open')}>
                {String(v.status).toUpperCase() === 'CLOSED' ? 'Закрыта' : 'Открыта'}
              </span>
              <button className="mini-btn">Редактировать</button>
            </div>
          ))
        )}
      </div>

      {!loading && companies.length === 0 && (
        <form className="panel" onSubmit={createCompany}>
          <div className="panel-head"><h3>Сначала — компания</h3></div>
          <div className="form-grid">
            <div className="full"><label className="field-l">Название компании</label>
              <input className="inp" required value={company.name} onChange={setC('name')} placeholder="Яндекс" /></div>
            <div className="full"><label className="field-l">Сайт</label>
              <input className="inp" value={company.website} onChange={setC('website')} placeholder="https://example.com" /></div>
            <div className="full"><label className="field-l">О компании</label>
              <textarea className="inp" value={company.description} onChange={setC('description')} placeholder="Чем занимаетесь" /></div>
            <div className="full">
              <button className="btn btn-primary" disabled={companyBusy}>{companyBusy ? 'Создаём…' : 'Создать компанию'}</button>
            </div>
          </div>
        </form>
      )}

      <form className="panel" onSubmit={publish}>
        <div className="panel-head"><h3>Новая вакансия</h3></div>
        <div className="form-grid">
          <div className="full"><label className="field-l">Название</label>
            <input className="inp" required value={f.title} onChange={set('title')} placeholder="Java-разработчик (Spring Boot)" /></div>
          <div><label className="field-l">Компания</label>
            <select className="inp" required value={f.companyId} onChange={set('companyId')} disabled={companies.length === 0}>
              {companies.length === 0 && <option value="">Сначала создайте компанию</option>}
              {companies.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select></div>
          <div><label className="field-l">Город</label>
            <input className="inp" value={f.city} onChange={set('city')} placeholder="Красноярск" /></div>
          <div><label className="field-l">Тип занятости</label>
            <select className="inp" value={f.employmentType} onChange={set('employmentType')}>
              <option value="FULL_TIME">Полная</option>
              <option value="REMOTE">Удалённая</option>
              <option value="PART_TIME">Частичная</option>
              <option value="INTERNSHIP">Стажировка</option>
            </select></div>
          <div><label className="field-l">Зарплата от, ₽</label>
            <input className="inp" value={f.salaryFrom} onChange={set('salaryFrom')} placeholder="180000" /></div>
          <div><label className="field-l">Зарплата до, ₽</label>
            <input className="inp" value={f.salaryTo} onChange={set('salaryTo')} placeholder="250000" /></div>
          <div className="full"><label className="field-l">Описание</label>
            <textarea className="inp" required value={f.description} onChange={set('description')} placeholder="Что делать, что требуется, что предлагаете…" /></div>
          <div className="full">
            <button className="btn btn-primary" disabled={busy || companies.length === 0}>{busy ? 'Публикуем…' : 'Опубликовать вакансию'}</button>
          </div>
        </div>
      </form>
    </section>
  )
}
