import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import VacancyCard from '../components/VacancyCard.jsx'
import SkillPicker from '../components/SkillPicker.jsx'
import { salaryScale } from '../components/Salary.jsx'
import { vacanciesApi } from '../api.js'
import { useApplyFlow } from '../useApplyFlow.jsx'
import { useDictionaries } from '../dictionaries.jsx'

const PAGE_SIZE = 20
const LIST_KEYS = ['specialization', 'grade', 'experience', 'workFormat', 'employmentType', 'skill']
const QUICK_ROLES = ['BACKEND', 'FRONTEND', 'FULLSTACK', 'MOBILE', 'QA', 'DEVOPS', 'DATA_SCIENCE', 'ANALYST']

function readFilters(sp) {
  const f = {
    q: sp.get('q') || '',
    city: sp.get('city') || '',
    minSalary: sp.get('minSalary') || '',
    onlyWithSalary: sp.get('onlyWithSalary') === 'true',
    order: sp.get('order') || 'date',
    page: Math.max(0, Number(sp.get('page')) || 0),
  }
  LIST_KEYS.forEach((k) => { f[k] = sp.getAll(k) })
  f.skill = f.skill.map(Number).filter(Boolean)
  return f
}

function toParams(f) {
  const p = new URLSearchParams()
  if (f.q) p.set('q', f.q)
  if (f.city) p.set('city', f.city)
  if (f.minSalary) p.set('minSalary', f.minSalary)
  if (f.onlyWithSalary) p.set('onlyWithSalary', 'true')
  if (f.order && f.order !== 'date') p.set('order', f.order)
  if (f.page) p.set('page', String(f.page))
  LIST_KEYS.forEach((k) => f[k].forEach((v) => p.append(k, v)))
  return p
}

function activeCount(f) {
  return LIST_KEYS.reduce((n, k) => n + f[k].length, 0)
    + (f.city ? 1 : 0) + (f.minSalary ? 1 : 0) + (f.onlyWithSalary ? 1 : 0)
}

export default function Home({ onAuth }) {
  const [sp, setSp] = useSearchParams()
  const filters = readFilters(sp)
  const dict = useDictionaries()
  const applyFlow = useApplyFlow(onAuth)

  const [result, setResult] = useState({ items: [], total: 0, pages: 0 })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)
  const [filtersOpen, setFiltersOpen] = useState(false)

  const [qDraft, setQDraft] = useState(filters.q)
  const [cityDraft, setCityDraft] = useState(filters.city)
  const [salaryDraft, setSalaryDraft] = useState(filters.minSalary)
  useEffect(() => { setQDraft(filters.q); setCityDraft(filters.city); setSalaryDraft(filters.minSalary) },
    [filters.q, filters.city, filters.minSalary])

  const key = sp.toString()
  useEffect(() => {
    const f = readFilters(sp)
    const params = { size: PAGE_SIZE, page: f.page }
    if (f.q) params.q = f.q
    if (f.city) params.city = f.city
    if (f.minSalary) params.minSalary = f.minSalary
    if (f.onlyWithSalary) params.onlyWithSalary = true
    if (f.order === 'salary') params.order = 'salary'
    LIST_KEYS.forEach((k) => { if (f[k].length) params[k] = f[k] })
    setLoading(true)
    setError(false)
    vacanciesApi.search(params)
      .then(setResult)
      .catch(() => { setResult({ items: [], total: 0, pages: 0 }); setError(true) })
      .finally(() => setLoading(false))
  }, [key])

  const update = (patch) => setSp(toParams({ ...filters, page: 0, ...patch }))
  const toggle = (k, v) => update({ [k]: filters[k].includes(v) ? filters[k].filter((x) => x !== v) : [...filters[k], v] })
  const resetAll = () => setSp(toParams({ ...readFilters(new URLSearchParams()), q: filters.q }))
  const goPage = (page) => { setSp(toParams({ ...filters, page })); window.scrollTo({ top: 0, behavior: 'smooth' }) }

  const submitSearch = (e) => {
    e.preventDefault()
    update({ q: qDraft.trim(), city: cityDraft.trim() })
  }
  const commitSalary = () => {
    const v = salaryDraft.replace(/\D/g, '')
    if (v !== filters.minSalary) update({ minSalary: v })
  }

  const nActive = activeCount(filters)
  const scale = salaryScale(result.items)
  const roles = QUICK_ROLES.filter((r) => dict.specializations.some((o) => o.value === r))

  return (
    <>
      <section className="find">
        <div className="wrap">
          <h1>Вакансии в IT</h1>
          <form className="search" onSubmit={submitSearch} role="search">
            <input className="inp" aria-label="Должность или технология" value={qDraft}
              placeholder="Должность или стек, например Kotlin" onChange={(e) => setQDraft(e.target.value)} />
            <input className="inp" aria-label="Город" value={cityDraft} placeholder="Город"
              onChange={(e) => setCityDraft(e.target.value)} />
            <button className="btn btn-primary" type="submit">Найти</button>
          </form>
          <div className="roles">
            <button type="button" className={filters.specialization.length === 0 ? 'on' : ''}
              onClick={() => update({ specialization: [] })}>Все направления</button>
            {roles.map((v) => (
              <button type="button" key={v} className={filters.specialization.includes(v) ? 'on' : ''}
                onClick={() => toggle('specialization', v)}>
                {dict.label('specialization', v)}
              </button>
            ))}
          </div>
        </div>
      </section>

      <div className="wrap listing">
        <button className="btn btn-quiet filters-btn" onClick={() => setFiltersOpen(!filtersOpen)}>
          {filtersOpen ? 'Скрыть фильтры' : 'Фильтры'}{nActive > 0 ? ` (${nActive})` : ''}
        </button>

        <aside className={'filters' + (filtersOpen ? ' open' : '')}>
          <div className="fgroup">
            <h3>Навыки</h3>
            <SkillPicker compact value={filters.skill} onChange={(ids) => update({ skill: ids })} placeholder="Kotlin, React…" />
          </div>

          <div className="fgroup">
            <h3>Грейд</h3>
            <div className="grade-row">
              {dict.grades.map((o) => (
                <button type="button" key={o.value} aria-pressed={filters.grade.includes(o.value)}
                  className={'toggle' + (filters.grade.includes(o.value) ? ' on' : '')}
                  onClick={() => toggle('grade', o.value)}>{o.label}</button>
              ))}
            </div>
          </div>

          <div className="fgroup">
            <h3>Зарплата от</h3>
            <input className="inp num" inputMode="numeric" value={salaryDraft} placeholder="150 000 ₽"
              aria-label="Зарплата от, рублей"
              onChange={(e) => setSalaryDraft(e.target.value)} onBlur={commitSalary}
              onKeyDown={(e) => e.key === 'Enter' && commitSalary()} />
            <label className="check" style={{ marginTop: '8px' }}>
              <input type="checkbox" checked={filters.onlyWithSalary}
                onChange={(e) => update({ onlyWithSalary: e.target.checked })} />
              Только с указанной зарплатой
            </label>
          </div>

          <CheckGroup title="Направление" options={dict.specializations} selected={filters.specialization}
            onToggle={(v) => toggle('specialization', v)} collapsedCount={6} />
          <CheckGroup title="Формат работы" options={dict.workFormats} selected={filters.workFormat}
            onToggle={(v) => toggle('workFormat', v)} />
          <CheckGroup title="Опыт" options={dict.experiences} selected={filters.experience}
            onToggle={(v) => toggle('experience', v)} />
          <CheckGroup title="Занятость" options={dict.employmentTypes} selected={filters.employmentType}
            onToggle={(v) => toggle('employmentType', v)} />

          {nActive > 0 && (
            <div className="filters-foot">
              <button className="btn btn-quiet btn-block" onClick={resetAll}>Сбросить фильтры</button>
            </div>
          )}
        </aside>

        <div>
          <div className="results-head">
            <span className="count">
              {loading ? 'Ищем…' : error ? '' : `Найдено ${result.total} ${plural(result.total)}`}
            </span>
            <select className="inp" aria-label="Сортировка" value={filters.order}
              onChange={(e) => update({ order: e.target.value })}>
              <option value="date">Сначала новые</option>
              <option value="salary">Сначала с большей зарплатой</option>
            </select>
          </div>

          {loading ? (
            <div className="sheet"><div className="spinner" /></div>
          ) : error ? (
            <div className="sheet empty">Сервер не ответил. Обновите страницу через минуту.</div>
          ) : result.items.length === 0 ? (
            <div className="sheet empty">
              <p>По этим условиям вакансий нет. Уберите часть фильтров, чтобы увидеть больше.</p>
              {nActive > 0 && <button className="btn btn-quiet" onClick={resetAll}>Сбросить фильтры</button>}
            </div>
          ) : (
            <>
              <div className="sheet">
                {result.items.map((v) => (
                  <VacancyCard key={v.id} v={v} scale={scale} onApply={applyFlow.start}
                    applied={applyFlow.isApplied(v.id)} canApply={applyFlow.canApply}
                    onSkillClick={(name) => {
                      const s = dict.skills.find((x) => x.name === name)
                      if (s && !filters.skill.includes(s.id)) update({ skill: [...filters.skill, s.id] })
                    }} />
                ))}
              </div>
              {result.pages > 1 && (
                <div className="pager">
                  <button className="btn btn-quiet btn-sm" disabled={filters.page === 0} onClick={() => goPage(filters.page - 1)}>Назад</button>
                  <span className="num">Страница {filters.page + 1} из {result.pages}</span>
                  <button className="btn btn-quiet btn-sm" disabled={filters.page + 1 >= result.pages} onClick={() => goPage(filters.page + 1)}>Дальше</button>
                </div>
              )}
            </>
          )}
        </div>
      </div>
      {applyFlow.modal}
    </>
  )
}

function CheckGroup({ title, options, selected, onToggle, collapsedCount }) {
  const [expanded, setExpanded] = useState(false)
  const canCollapse = collapsedCount && options.length > collapsedCount
  const visible = canCollapse && !expanded
    ? options.filter((o, i) => i < collapsedCount || selected.includes(o.value))
    : options

  return (
    <div className="fgroup">
      <h3>{title}</h3>
      {visible.map((o) => (
        <label className="check" key={o.value}>
          <input type="checkbox" checked={selected.includes(o.value)} onChange={() => onToggle(o.value)} />
          {o.label}
        </label>
      ))}
      {canCollapse && (
        <button type="button" className="link" onClick={() => setExpanded(!expanded)}>
          {expanded ? 'Свернуть' : `Все направления (${options.length})`}
        </button>
      )}
    </div>
  )
}

function plural(n) {
  const a = Math.abs(n) % 100, b = a % 10
  if (a > 10 && a < 20) return 'вакансий'
  if (b > 1 && b < 5) return 'вакансии'
  if (b === 1) return 'вакансия'
  return 'вакансий'
}
