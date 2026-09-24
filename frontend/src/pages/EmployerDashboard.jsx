import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
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
      toast(apiMessage(err, 'Данные не загрузились. Обновите страницу.'))
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
      toast('Компания создана')
      setCompany(EMPTY_COMPANY)
      setCompanies((prev) => [...prev, created])
      setF((prev) => ({ ...prev, companyId: created.id }))
    } catch (err) {
      toast(apiMessage(err, 'Компания не создалась. Проверьте поля.'))
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
    const salaryFrom = f.salaryFrom ? Number(String(f.salaryFrom).replace(/\D/g, '')) : null
    const salaryTo = f.salaryTo ? Number(String(f.salaryTo).replace(/\D/g, '')) : null
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
      toast('Вакансия опубликована')
      setF({ ...EMPTY_VACANCY, companyId: f.companyId })
      load()
    } catch (err) {
      toast(apiMessage(err, 'Вакансия не опубликовалась. Проверьте поля.'))
    } finally {
      setBusy(false)
    }
  }

  const totalApps = vacancies.reduce((n, v) => n + (v.applicationsCount || 0), 0)
  const newApps = vacancies.reduce((n, v) => n + (v.newApplicationsCount || 0), 0)

  return (
    <div className="wrap">
      <div className="page-head">
        <h1>Мои вакансии</h1>
        <p>Публикуйте вакансии и отвечайте кандидатам. Контакты компании увидят только приглашённые.</p>
      </div>

      <div className="stack">
        {!loading && vacancies.length > 0 && (
          <div className="summary">
            <div><b>{vacancies.length}</b><span>вакансий</span></div>
            <div><b>{totalApps}</b><span>откликов всего</span></div>
            <div className={newApps > 0 ? 'hot' : ''}><b>{newApps}</b><span>новых, не просмотрены</span></div>
          </div>
        )}

        <section className="sheet">
          <div className="sheet-head"><h2>Опубликованные</h2></div>
          {loading ? (
            <div className="spinner" />
          ) : vacancies.length === 0 ? (
            <div className="empty">Вакансий пока нет. Заполните форму ниже, чтобы опубликовать первую.</div>
          ) : (
            vacancies.map((v) => {
              const closed = String(v.status).toUpperCase() === 'CLOSED'
              return (
                <div className="row" key={v.id}>
                  <div className="row-main">
                    <div>
                      <Link to={`/vacancy/${v.id}`} className="title">{v.title}</Link>
                      <div className="sub">{[v.company, v.city, formatSalary(v.salaryFrom, v.salaryTo)].filter(Boolean).join(', ')}</div>
                    </div>
                    <span className={'st ' + (closed ? 'closed' : 'open')}>{closed ? 'Закрыта' : 'Открыта'}</span>
                    <button className="apps-link" onClick={() => navigate(`/my/vacancies/${v.id}/applications`)}>
                      <span className="num">Отклики {v.applicationsCount ?? 0}</span>
                      {v.newApplicationsCount > 0 && <span className="new num">+{v.newApplicationsCount}</span>}
                    </button>
                  </div>
                </div>
              )
            })
          )}
        </section>

        {!loading && companies.length > 0 && (
          <section className="sheet">
            <div className="sheet-head"><h2>Компании и контакты</h2></div>
            {companies.map((c) => (
              <CompanyRow key={c.id} company={c}
                onSaved={(saved) => setCompanies((prev) => prev.map((x) => (x.id === saved.id ? saved : x)))} />
            ))}
          </section>
        )}

        {!loading && companies.length === 0 && (
          <form className="sheet" onSubmit={createCompany}>
            <div className="sheet-head"><h2>Расскажите о компании</h2></div>
            <div className="form-grid">
              <label className="field full"><span>Название</span>
                <input className="inp" required value={company.name} onChange={setC('name')} placeholder="ООО «Ромашка»" /></label>
              <label className="field full"><span>Сайт</span>
                <input className="inp" value={company.website} onChange={setC('website')} placeholder="https://example.com" /></label>
              <label className="field full"><span>О компании</span>
                <textarea className="inp" value={company.description} onChange={setC('description')} placeholder="Чем занимаетесь, какой продукт делаете" /></label>
              <ContactFields value={company} onChange={setC} />
              <div className="full">
                <button className="btn btn-primary" disabled={companyBusy}>{companyBusy ? 'Создаём…' : 'Создать компанию'}</button>
              </div>
            </div>
          </form>
        )}

        <form className="sheet" onSubmit={publish}>
          <div className="sheet-head"><h2>Новая вакансия</h2></div>
          <div className="form-grid">
            <label className="field full"><span>Должность</span>
              <input className="inp" required value={f.title} onChange={set('title')} placeholder="Java-разработчик (Spring Boot)" /></label>
            <label className="field"><span>Компания</span>
              <select className="inp" required value={f.companyId} onChange={set('companyId')} disabled={companies.length === 0}>
                {companies.length === 0 && <option value="">Сначала создайте компанию</option>}
                {companies.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select></label>
            <label className="field"><span>Направление</span>
              <DictSelect required options={dict.specializations} value={f.specialization} onChange={set('specialization')} placeholder="Выберите" /></label>
            <label className="field"><span>Грейд</span>
              <DictSelect options={dict.grades} value={f.grade} onChange={set('grade')} placeholder="Любой" /></label>
            <label className="field"><span>Опыт</span>
              <DictSelect options={dict.experiences} value={f.experience} onChange={set('experience')} placeholder="Не важен" /></label>
            <label className="field"><span>Формат работы</span>
              <DictSelect required options={dict.workFormats} value={f.workFormat} onChange={set('workFormat')} /></label>
            <label className="field"><span>Занятость</span>
              <DictSelect required options={dict.employmentTypes} value={f.employmentType} onChange={set('employmentType')} /></label>
            <label className="field full"><span>Город</span>
              <input className="inp" value={f.city} onChange={set('city')} placeholder="Необязательно для удалёнки" /></label>
            <label className="field"><span>Зарплата от, ₽</span>
              <input className="inp num" inputMode="numeric" value={f.salaryFrom} onChange={set('salaryFrom')} placeholder="180 000" /></label>
            <label className="field"><span>Зарплата до, ₽</span>
              <input className="inp num" inputMode="numeric" value={f.salaryTo} onChange={set('salaryTo')} placeholder="250 000" /></label>
            <div className="field full"><span>Стек <span className="hint">(по нему кандидаты находят вакансию)</span></span>
              <SkillPicker value={f.skillIds} onChange={(ids) => setF({ ...f, skillIds: ids })} placeholder="Начните вводить: Java, Spring Boot, PostgreSQL" /></div>
            <label className="field full"><span>Описание</span>
              <textarea className="inp" rows={8} required value={f.description} onChange={set('description')} placeholder="Задачи, требования, условия" /></label>
            <div className="full">
              <button className="btn btn-primary btn-lg" disabled={busy || companies.length === 0}>{busy ? 'Публикуем…' : 'Опубликовать вакансию'}</button>
            </div>
          </div>
        </form>
      </div>
    </div>
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
      <label className="field"><span>Telegram для связи</span>
        <input className="inp" value={value.telegram || ''} onChange={onChange('telegram')} placeholder="@hr_company" /></label>
      <label className="field"><span>Email для связи</span>
        <input className="inp" type="email" value={value.contactEmail || ''} onChange={onChange('contactEmail')} placeholder="hr@company.ru" /></label>
      <p className="full hint">Кандидат увидит эти контакты, только когда вы его пригласите. Если не заполнить, покажем email вашего аккаунта.</p>
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
      toast('Контакты сохранены')
    } catch (err) {
      toast(apiMessage(err, 'Контакты не сохранились. Проверьте поля.'))
    } finally {
      setBusy(false)
    }
  }

  const contacts = [company.telegram && '@' + company.telegram, company.contactEmail].filter(Boolean)

  return (
    <div className="row">
      <div className="row-main">
        <div>
          <div className="title">{company.name}</div>
          <div className={'sub' + (contacts.length ? '' : ' warn-text')}>
            {contacts.length ? contacts.join(', ') : 'Контакты не указаны, кандидаты увидят только email аккаунта'}
          </div>
        </div>
        <button className="btn btn-quiet btn-sm" onClick={() => { setForm(company); setEditing(!editing) }}>
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
