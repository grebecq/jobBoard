import { useEffect, useState, useCallback } from 'react'
import VacancyCard from '../components/VacancyCard.jsx'
import { vacanciesApi } from '../api.js'
import { useApplyFlow } from '../useApplyFlow.jsx'

export default function Home({ onAuth }) {
  const [items, setItems] = useState([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)
  const [title, setTitle] = useState('')
  const [city, setCity] = useState('')
  const [minSalary, setMinSalary] = useState('')

  const applyFlow = useApplyFlow(onAuth)

  const load = useCallback(async () => {
    setLoading(true)
    setError(false)
    try {
      const params = {}
      if (city) params.city = city
      if (minSalary) params.minSalary = Number(minSalary)
      const useSearch = city || minSalary
      const res = useSearch ? await vacanciesApi.search(params) : await vacanciesApi.list({ size: 20 })
      let list = res.items
      if (title) list = list.filter((v) => v.title.toLowerCase().includes(title.toLowerCase()))
      setItems(list)
      setTotal(res.total)
    } catch (e) {
      setItems([])
      setError(true)
    } finally {
      setLoading(false)
    }
  }, [city, minSalary, title])

  useEffect(() => { load() }, [])

  return (
    <section className="view">
      <div className="hero">
        <div className="hero-in">
          <h1>Найди работу мечты <span className="g">в IT</span></h1>
          <p>Актуальные вакансии от проверенных компаний. Отклик в один клик.</p>
          <form className="search" onSubmit={(e) => { e.preventDefault(); load() }}>
            <label className="field">
              <span>🔍</span>
              <input placeholder="Должность, например «Java-разработчик»" value={title}
                onChange={(e) => setTitle(e.target.value)} />
            </label>
            <div className="divider" />
            <label className="field">
              <span>📍</span>
              <input placeholder="Город" value={city} onChange={(e) => setCity(e.target.value)} />
            </label>
            <button className="btn btn-primary" type="submit">Найти</button>
          </form>
        </div>
      </div>

      <div className="wrap">
        <div className="listing">
          <aside className="filters">
            <div className="fgroup">
              <div className="flabel">Город</div>
              <input className="inp" value={city} onChange={(e) => setCity(e.target.value)} placeholder="Любой" />
            </div>
            <div className="fgroup">
              <div className="flabel">Зарплата от, ₽</div>
              <input className="inp" value={minSalary} onChange={(e) => setMinSalary(e.target.value)} placeholder="например 150000" />
            </div>
            <div className="fgroup">
              <button className="btn btn-primary" style={{ width: '100%' }} onClick={load}>Применить</button>
            </div>
          </aside>

          <div>
            <div className="results-head">
              <div>
                <h2>Вакансии</h2>
                <span className="count">{loading ? 'Загрузка…' : `${total} ${plural(total)}`}</span>
              </div>
            </div>

            {loading ? (
              <div className="spinner" />
            ) : error ? (
              <div className="center-msg">Не удалось загрузить вакансии. Проверь, запущен ли бэкенд на :8080.</div>
            ) : items.length === 0 ? (
              <div className="center-msg">Ничего не нашлось. Попробуй изменить фильтры.</div>
            ) : (
              <div className="cards">
                {items.map((v) => (
                  <VacancyCard key={v.id} v={v} onApply={applyFlow.start}
                    applied={applyFlow.isApplied(v.id)} canApply={applyFlow.canApply} />
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
      {applyFlow.modal}
    </section>
  )
}

function plural(n) {
  const a = Math.abs(n) % 100, b = a % 10
  if (a > 10 && a < 20) return 'вакансий'
  if (b > 1 && b < 5) return 'вакансии'
  if (b === 1) return 'вакансия'
  return 'вакансий'
}
