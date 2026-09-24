import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth.jsx'
import { useToast } from '../toast.jsx'
import { vacanciesApi, companiesApi, apiMessage } from '../api.js'
import { formatSalary } from '../format.js'
import { useDictionaries } from '../dictionaries.jsx'
import SkillPicker from '../components/SkillPicker.jsx'

const EMPTY_COMPANY = { name: '', website: '', description: '', contactEmail: '', telegram: '' }
const EMPTY_VACANCY = {
  title: '', city: '', description: '', salaryFrom: '', salaryTo: '',
  specialization: '', grade: '', experience: '', workFormat: 'OFFICE', employmentType: 'FULL_TIME', skillIds: [],
}

export default function EmployerDashboard() {
  const { user } = useAuth()
  const toast = useToast()
  const navigate = useNavigate()
  const [vacancies, setVacancies] = useState([])
  const [companies, setCompanies] = useState([])
  const [loading, setLoading] = useState(true)
  const dict = useDictionaries()
  const [f, setF] = useState({ ...EMPTY_VACANCY, companyId: '' })
  const [company, setCompany] = useState(EMPTY_COMPANY)
  const [busy, setBusy] = useState(false)
  const [companyBusy, setCompanyBusy] = useState(false)
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value })
  const setC = (k) => (e) => setCompany({ ...company, [k]: e.target.value })

  async function load() {
    setLoading(true)
    try {
      const [mine, myCompanies] = await Promise.all([
        vacanciesApi.mine({ size: 50, sort: 'createdAt,desc' }),
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
      const created = await companiesApi.create(companyPayload(company))
      toast('Компания создана ✓')
      setCompany(EMPTY_COMPANY)
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
    const salaryFrom = f.salaryFrom ? Number(f.salaryFrom) : null
    const salaryTo = f.salaryTo ? Number(f.salaryTo) : null
    if (salaryFrom && salaryTo && salaryFrom > salaryTo) {
      toast('Зарплата «от» больше, чем «до»')
      return
    }
    setBusy(true)
    try {
      await vacanciesApi.create({
        title: f.title,
        description: f.description,
        city: f.city || null,
        specialization: f.specialization || null,
        grade: f.grade || null,
        experience: f.experience || null,
        workFormat: f.workFormat || null,
        employmentType: f.employmentType || null,
        salaryFrom,
        salaryTo,
        skillIds: f.skillIds,
        companyId: Number(f.companyId),
      })
      toast('Вакансия опубликована ✓')
      setF({ ...EMPTY_VACANCY, companyId: f.companyId })
      load()
    } catch (err) {
      toast(apiMessage(err, 'Не удалось опубликовать вакансию'))
    } finally {
      setBusy(false)
    }
  }

  const totalApps = vacancies.reduce((n, v) => n + (v.applicationsCount || 0), 0)
  const newApps = vacancies.reduce((n, v) => n + (v.newApplicationsCount || 0), 0)

  return (
    <section className="view wrap">
      <div className="dash-head">
        <div className="eyebrow">Работодатель · {user?.email}</div>
        <h1>Мои вакансии</h1>
      </div>

      {!loading && vacancies.length > 0 && (
        <div className="stats">
          <div className="stat accent"><div className="n">{vacancies.length}</div><div className="l">Вакансий</div></div>
          <div className="stat"><div className="n">{totalApps}</div><div className="l">Откликов всего</div></div>
          <div className="stat"><div className="n">{newApps}</div><div className="l">Новых, не просмотрено</div></div>
        </div>
      )}

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
                <div className="ts">{[v.company, v.city, formatSalary(v.salaryFrom, v.salaryTo)].filter(Boolean).join(' · ')}</div>
              </div>
              <span className={'status ' + (String(v.status).toUpperCase() === 'CLOSED' ? 'closed' : 'open')}>
                {String(v.status).toUpperCase() === 'CLOSED' ? 'Закрыта' : 'Открыта'}
              </span>
              <button className="mini-btn" onClick={() => navigate(`/my/vacancies/${v.id}/applications`)}>
                Отклики: {v.applicationsCount ?? 0}
                {v.newApplicationsCount > 0 && <span className="dot-count">+{v.newApplicationsCount}</span>}
              </button>
            </div>
          ))
        )}
      </div>

      {!loading && companies.length > 0 && (
        <div className="panel">
          <div className="panel-head"><h3>Мои компании и контакты</h3></div>
          {companies.map((c) => (
            <CompanyRow key={c.id} company={c}
              onSaved={(saved) => setCompanies((prev) => prev.map((x) => (x.id === saved.id ? saved : x)))} />
          ))}
        </div>
      )}

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
            <ContactFields value={company} onChange={setC} />
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
          <div><label className="field-l">Специализация</label>
            <DictSelect required options={dict.specializations} value={f.specialization} onChange={set('specialization')} placeholder="Выберите направление" /></div>
          <div><label className="field-l">Грейд</label>
            <DictSelect options={dict.grades} value={f.grade} onChange={set('grade')} placeholder="Любой" /></div>
          <div><label className="field-l">Опыт работы</label>
            <DictSelect options={dict.experiences} value={f.experience} onChange={set('experience')} placeholder="Не важен" /></div>
          <div><label className="field-l">Формат работы</label>
            <DictSelect required options={dict.workFormats} value={f.workFormat} onChange={set('workFormat')} /></div>
          <div><label className="field-l">Тип занятости</label>
            <DictSelect required options={dict.employmentTypes} value={f.employmentType} onChange={set('employmentType')} /></div>
          <div><label className="field-l">Город</label>
            <input className="inp" value={f.city} onChange={set('city')} placeholder="Красноярск" /></div>
          <div><label className="field-l">Зарплата от, ₽</label>
            <input className="inp" inputMode="numeric" value={f.salaryFrom} onChange={set('salaryFrom')} placeholder="180000" /></div>
          <div><label className="field-l">Зарплата до, ₽</label>
            <input className="inp" inputMode="numeric" value={f.salaryTo} onChange={set('salaryTo')} placeholder="250000" /></div>
          <div className="full"><label className="field-l">Стек и навыки <span className="hint">— по ним кандидаты находят вакансию</span></label>
            <SkillPicker value={f.skillIds} onChange={(ids) => setF({ ...f, skillIds: ids })} placeholder="Начните вводить: Java, Spring Boot, PostgreSQL…" /></div>
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

function DictSelect({ options, value, onChange, placeholder, required }) {
  return (
    <select className="inp" value={value} onChange={onChange} required={required}>
      {placeholder !== undefined && <option value="">{placeholder}</option>}
      {options.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
    </select>
  )
}

function companyPayload(c) {
  return {
    name: c.name,
    website: c.website || null,
    description: c.description || null,
    logoUrl: c.logoUrl || null,
    contactEmail: c.contactEmail || null,
    telegram: c.telegram || null,
  }
}

function ContactFields({ value, onChange }) {
  return (
    <>
      <div><label className="field-l">Telegram для связи</label>
        <input className="inp" value={value.telegram || ''} onChange={onChange('telegram')} placeholder="@hr_company или t.me/hr_company" /></div>
      <div><label className="field-l">Email для связи</label>
        <input className="inp" type="email" value={value.contactEmail || ''} onChange={onChange('contactEmail')} placeholder="hr@company.ru" /></div>
      <div className="full hint">Кандидат увидит эти контакты, только когда вы его пригласите. Без них покажем email вашего аккаунта.</div>
    </>
  )
}

function CompanyRow({ company, onSaved }) {
  const toast = useToast()
  const [editing, setEditing] = useState(false)
  const [form, setForm] = useState(company)
  const [busy, setBusy] = useState(false)
  const setK = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  async function save(e) {
    e.preventDefault()
    setBusy(true)
    try {
      const saved = await companiesApi.update(company.id, companyPayload(form))
      onSaved(saved)
      setForm(saved)
      setEditing(false)
      toast('Контакты сохранены ✓')
    } catch (err) {
      toast(apiMessage(err, 'Не удалось сохранить'))
    } finally {
      setBusy(false)
    }
  }

  const hasContacts = company.telegram || company.contactEmail

  return (
    <div className="app-item">
      <div className="app-row">
        <div>
          <div className="ti">{company.name}</div>
          <div className="ts">
            {hasContacts
              ? [company.telegram && '@' + company.telegram, company.contactEmail].filter(Boolean).join(' · ')
              : '⚠️ Контакты не указаны — кандидаты увидят только email аккаунта'}
          </div>
        </div>
        <button className="mini-btn" onClick={() => { setForm(company); setEditing(!editing) }}>
          {editing ? 'Отмена' : 'Изменить контакты'}
        </button>
      </div>
      {editing && (
        <form className="form-grid inset" onSubmit={save}>
          <ContactFields value={form} onChange={setK} />
          <div className="full">
            <button className="btn btn-primary" disabled={busy}>{busy ? 'Сохраняем…' : 'Сохранить'}</button>
          </div>
        </form>
      )}
    </div>
  )
}
