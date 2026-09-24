import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import VacancyCard from '../components/VacancyCard.jsx'
import SkillPicker from '../components/SkillPicker.jsx'
import { vacanciesApi } from '../api.js'
import { useApplyFlow } from '../useApplyFlow.jsx'
import { useDictionaries } from '../dictionaries.jsx'

const PAGE_SIZE = 20
const LIST_KEYS = ['specialization', 'grade', 'experience', 'workFormat', 'employmentType', 'skill']

// фильтры живут в URL: поиском можно поделиться ссылкой, «назад» в браузере работает
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

  // черновики текстовых полей, чтобы не искать на каждую букву
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

  // любое изменение фильтра возвращает на первую страницу
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

  return (
    <section className="view">
      <div className="hero">
        <div className="hero-in">
          <h1>Найди работу мечты <span className="g">в IT</span></h1>
          <p>Вакансии для разработчиков, тестировщиков, DevOps и аналитиков. Фильтры по стеку, грейду и формату работы.</p>
          <form className="search" onSubmit={submitSearch}>
            <label className="field">
              <span>🔍</span>
              <input placeholder="Должность или технология: «Java», «Kotlin backend»" value={qDraft}
                onChange={(e) => setQDraft(e.target.value)} />
            </label>
            <div className="divider" />
            <label className="field">
              <span>📍</span>
              <input placeholder="Город" value={cityDraft} onChange={(e) => setCityDraft(e.target.value)} />
            </label>
            <button className="btn btn-primary" type="submit">Найти</button>
          </form>
          <div className="chips">
            {['BACKEND', 'FRONTEND', 'MOBILE', 'QA', 'DEVOPS', 'DATA_SCIENCE'].map((v) => (
              <button type="button" key={v}
                className={'chip' + (filters.specialization.includes(v) ? ' on' : '')}
                onClick={() => toggle('specialization', v)}>
                {dict.label('specialization', v)}
              </button>
            ))}
          </div>
        </div>
      </div>

      <div className="wrap">
        <button className="btn btn-ghost filters-toggle" onClick={() => setFiltersOpen(!filtersOpen)}>
          ⚙️ Фильтры{nActive > 0 ? ` · ${nActive}` : ''}
        </button>

        <div className="listing">
          <aside className={'filters' + (filtersOpen ? ' open' : '')}>
            <div className="fgroup">
              <div className="flabel">Навыки <span className="hint">— любой из</span></div>
              <SkillPicker compact value={filters.skill} onChange={(ids) => update({ skill: ids })} placeholder="Kotlin, React…" />
            </div>

            <CheckGroup title="Специализация" options={dict.specializations} selected={filters.specialization}
              onToggle={(v) => toggle('specialization', v)} collapsedCount={7} />

            <div className="fgroup">
              <div className="flabel">Грейд</div>
              <div className="pill-row">
                {dict.grades.map((o) => (
                  <button type="button" key={o.value}
                    className={'pill' + (filters.grade.includes(o.value) ? ' on' : '')}
                    onClick={() => toggle('grade', o.value)}>{o.label}</button>
                ))}
              </div>
            </div>

            <div className="fgroup">
              <div className="flabel">Зарплата от, ₽</div>
              <input className="inp" inputMode="numeric" value={salaryDraft} placeholder="например 150000"
                onChange={(e) => setSalaryDraft(e.target.value)} onBlur={commitSalary}
                onKeyDown={(e) => e.key === 'Enter' && commitSalary()} />
              <label className="opt" style={{ marginTop: '8px' }}>
                <input type="checkbox" checked={filters.onlyWithSalary}
                  onChange={(e) => update({ onlyWithSalary: e.target.checked })} />
                Только с указанной зарплатой
              </label>
            </div>

            <CheckGroup title="Формат работы" options={dict.workFormats} selected={filters.workFormat}
              onToggle={(v) => toggle('workFormat', v)} />
            <CheckGroup title="Опыт работы" options={dict.experiences} selected={filters.experience}
              onToggle={(v) => toggle('experience', v)} />
            <CheckGroup title="Тип занятости" options={dict.employmentTypes} selected={filters.employmentType}
              onToggle={(v) => toggle('employmentType', v)} />

            <div className="fgroup">
              <div className="flabel">Город</div>
              <input className="inp" value={cityDraft} placeholder="Любой"
                onChange={(e) => setCityDraft(e.target.value)}
                onBlur={() => cityDraft.trim() !== filters.city && update({ city: cityDraft.trim() })}
                onKeyDown={(e) => e.key === 'Enter' && update({ city: cityDraft.trim() })} />
            </div>

            {nActive > 0 && (
              <div className="fgroup">
                <button className="btn btn-ghost" style={{ width: '100%' }} onClick={resetAll}>Сбросить фильтры</button>
              </div>
            )}
          </aside>

          <div>
            <div className="results-head">
              <div>
                <h2>Вакансии</h2>
                <span className="count">{loading ? 'Загрузка…' : `${result.total} ${plural(result.total)}`}</span>
              </div>
              <select className="inp sort" value={filters.order} onChange={(e) => update({ order: e.target.value })}>
                <option value="date">Сначала новые</option>
                <option value="salary">Сначала с большей зарплатой</option>
              </select>
            </div>

            {loading ? (
              <div className="spinner" />
            ) : error ? (
              <div className="center-msg">Не удалось загрузить вакансии. Проверь, запущен ли бэкенд.</div>
            ) : result.items.length === 0 ? (
              <div className="center-msg">
                Ничего не нашлось. Попробуй ослабить фильтры.
                {nActive > 0 && <div style={{ marginTop: '12px' }}><button className="btn btn-ghost" onClick={resetAll}>Сбросить фильтры</button></div>}
              </div>
            ) : (
              <>
                <div className="cards">
                  {result.items.map((v) => (
                    <VacancyCard key={v.id} v={v} onApply={applyFlow.start}
                      applied={applyFlow.isApplied(v.id)} canApply={applyFlow.canApply}
                      onSkillClick={(name) => {
                        const s = dict.skills.find((x) => x.name === name)
                        if (s && !filters.skill.includes(s.id)) update({ skill: [...filters.skill, s.id] })
                      }} />
                  ))}
                </div>
                {result.pages > 1 && (
                  <div className="pager">
                    <button className="btn btn-ghost" disabled={filters.page === 0} onClick={() => goPage(filters.page - 1)}>← Назад</button>
                    <span>Страница {filters.page + 1} из {result.pages}</span>
                    <button className="btn btn-ghost" disabled={filters.page + 1 >= result.pages} onClick={() => goPage(filters.page + 1)}>Вперёд →</button>
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      </div>
      {applyFlow.modal}
    </section>
  )
}

function CheckGroup({ title, options, selected, onToggle, collapsedCount }) {
  const [expanded, setExpanded] = useState(false)
  const canCollapse = collapsedCount && options.length > collapsedCount
  // выбранные всегда видны, даже если группа свёрнута
  const visible = canCollapse && !expanded
    ? options.filter((o, i) => i < collapsedCount || selected.includes(o.value))
    : options

  return (
    <div className="fgroup">
      <div className="flabel">{title}</div>
      {visible.map((o) => (
        <label className="opt" key={o.value}>
          <input type="checkbox" checked={selected.includes(o.value)} onChange={() => onToggle(o.value)} />
          {o.label}
        </label>
      ))}
      {canCollapse && (
        <button type="button" className="link-btn" onClick={() => setExpanded(!expanded)}>
          {expanded ? 'Свернуть' : `Показать все (${options.length})`}
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
