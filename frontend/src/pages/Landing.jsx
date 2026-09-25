import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { vacanciesApi, companiesApi } from '../api.js'
import { useDictionaries } from '../dictionaries.jsx'
import { useAuth } from '../auth.jsx'
import { formatSalary } from '../format.js'

const POPULAR = ['Java', 'Kotlin', 'Python', 'Go', 'JavaScript', 'TypeScript', 'React', 'C#', 'PHP', 'Kubernetes']

const STEPS = [
  ['Найдите вакансию', 'Отфильтруйте по стеку, грейду, формату работы и зарплате.'],
  ['Откликнитесь', 'Одна кнопка и, если хочется, пара строк о себе. Статус отклика видно в кабинете.'],
  ['Договоритесь напрямую', 'Если работодатель пригласит, вы увидите его Telegram и почту и сразу напишете.'],
]

export default function Landing({ onAuth }) {
  const navigate = useNavigate()
  const { skills, label } = useDictionaries()
  const { user } = useAuth()
  const [q, setQ] = useState('')
  const [fresh, setFresh] = useState(null)
  const [companies, setCompanies] = useState(null)

  useEffect(() => {
    vacanciesApi.search({ size: 5 }).then(setFresh).catch(() => setFresh({ items: [], total: 0 }))
    companiesApi.count().then(setCompanies).catch(() => {})
  }, [])

  const search = (e) => {
    e.preventDefault()
    navigate(q.trim() ? `/vacancies?q=${encodeURIComponent(q.trim())}` : '/vacancies')
  }
  const popular = POPULAR.map((name) => skills.find((s) => s.name === name)).filter(Boolean)
  const employerCta = user?.role === 'EMPLOYER' || user?.role === 'ADMIN'
    ? <Link to="/my/vacancies" className="btn btn-quiet">Разместить вакансию</Link>
    : !user && <button className="btn btn-quiet" onClick={() => onAuth('register')}>Разместить вакансию</button>

  return (
    <>
      <section className="intro">
        <div className="wrap intro-in">
          <h1>Работа в IT, где сразу видно стек и&nbsp;зарплату</h1>
          <p className="intro-lead">
            Ищите вакансии по технологиям, грейду и формату работы. Откликайтесь в один клик,
            а контакты работодателя придут вместе с приглашением.
          </p>
          <form className="search intro-search" onSubmit={search} role="search">
            <input className="inp" aria-label="Должность или технология" value={q}
              placeholder="Должность или стек, например Kotlin" onChange={(e) => setQ(e.target.value)} />
            <button className="btn btn-primary" type="submit">Найти</button>
          </form>
          {popular.length > 0 && (
            <div className="tags intro-tags">
              {popular.map((s) => (
                <Link key={s.id} to={`/vacancies?skill=${s.id}`} className="tag">{s.name}</Link>
              ))}
            </div>
          )}
          {fresh && (
            <p className="intro-stats num">
              {fresh.total} {plural(fresh.total, ['вакансия', 'вакансии', 'вакансий'])}
              {companies != null && <>, {companies} {plural(companies, ['компания', 'компании', 'компаний'])}</>}
            </p>
          )}
        </div>
      </section>

      <div className="wrap landing">
        <section className="audiences">
          <div className="sheet sheet-pad">
            <h2>Соискателям</h2>
            <ul>
              <li>Фильтры по стеку, грейду, опыту и формату работы</li>
              <li>Зарплатная вилка видна прямо в списке</li>
              <li>Статусы откликов: отправлен, просмотрен, приглашение, отказ</li>
            </ul>
            <Link to="/vacancies" className="btn btn-primary">Смотреть вакансии</Link>
          </div>
          <div className="sheet sheet-pad">
            <h2>Работодателям</h2>
            <ul>
              <li>Вакансия со стеком и вилкой публикуется за пару минут</li>
              <li>Отклики с сопроводительными письмами в одном месте</li>
              <li>Пригласить или отказать можно с комментарием, контакты увидит только приглашённый</li>
            </ul>
            {employerCta}
          </div>
        </section>

        <section className="steps">
          <h2>Как это работает</h2>
          <ol>
            {STEPS.map(([title, text]) => (
              <li key={title}>
                <h3>{title}</h3>
                <p>{text}</p>
              </li>
            ))}
          </ol>
        </section>

        <section className="sheet">
          <div className="sheet-head">
            <h2>Свежие вакансии</h2>
            <Link to="/vacancies" className="link">Все вакансии</Link>
          </div>
          {!fresh ? (
            <div className="spinner" />
          ) : fresh.items.length === 0 ? (
            <div className="empty">Пока пусто. Скоро здесь появятся первые вакансии.</div>
          ) : (
            fresh.items.map((v) => (
              <div className="row" key={v.id}>
                <div className="row-main">
                  <div>
                    <Link to={`/vacancy/${v.id}`} className="title">{v.title}</Link>
                    <div className="sub">
                      {[v.company, v.city, v.grade && label('grade', v.grade)].filter(Boolean).join(', ')}
                    </div>
                  </div>
                  <span className={'row-pay num' + (v.salaryFrom || v.salaryTo ? '' : ' none')}>
                    {formatSalary(v.salaryFrom, v.salaryTo)}
                  </span>
                </div>
              </div>
            ))
          )}
        </section>
      </div>
    </>
  )
}

function plural(n, [one, few, many]) {
  const a = Math.abs(n) % 100, b = a % 10
  if (a > 10 && a < 20) return many
  if (b > 1 && b < 5) return few
  if (b === 1) return one
  return many
}
