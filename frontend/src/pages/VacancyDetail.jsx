import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { vacanciesApi } from '../api.js'
import { useApplyFlow } from '../useApplyFlow.jsx'
import { formatDate } from '../format.js'
import { useDictionaries } from '../dictionaries.jsx'
import Salary, { salaryScale } from '../components/Salary.jsx'

export default function VacancyDetail({ onAuth }) {
  const { id } = useParams()
  const navigate = useNavigate()
  const [v, setV] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)
  const applyFlow = useApplyFlow(onAuth)
  const { label } = useDictionaries()

  useEffect(() => {
    setLoading(true)
    vacanciesApi.get(id)
      .then((data) => { setV(data); setError(false) })
      .catch(() => setError(true))
      .finally(() => setLoading(false))
  }, [id])

  const back = <button className="back" onClick={() => (window.history.length > 1 ? navigate(-1) : navigate('/vacancies'))}>Назад к списку</button>

  if (loading) return <div className="wrap"><div className="spinner" /></div>
  if (error || !v) return (
    <div className="wrap">
      {back}
      <div className="sheet empty">Вакансия не найдена. Возможно, её уже сняли с публикации.</div>
    </div>
  )

  const closed = String(v.status).toUpperCase() === 'CLOSED'
  const facts = [
    ['Направление', label('specialization', v.specialization)],
    ['Грейд', label('grade', v.grade)],
    ['Опыт', label('experience', v.experience)],
    ['Формат', label('workFormat', v.workFormat)],
    ['Занятость', label('employmentType', v.employmentType)],
    ['Город', v.city],
  ].filter(([, val]) => val)
  // шкала вилки для одной вакансии: верх вилки плюс запас в четверть
  const scale = salaryScale([{ salaryTo: (v.salaryTo || v.salaryFrom || 0) * 1.25 }])

  return (
    <div className="wrap">
      {back}
      <div className="detail">
        <article className="sheet sheet-pad">
          <h1>{v.title}</h1>
          <div className="org">{v.company}</div>

          {facts.length > 0 && (
            <dl className="facts">
              {facts.map(([k, val]) => (
                <div key={k}><dt>{k}</dt><dd>{val}</dd></div>
              ))}
            </dl>
          )}

          <p className={'prose' + (v.description ? '' : ' none')}>
            {v.description || 'Работодатель не добавил описание.'}
          </p>

          {v.skills?.length > 0 && (
            <section className="block">
              <h2>Стек</h2>
              <div className="tags">{v.skills.map((s) => <span className="tag" key={s}>{s}</span>)}</div>
            </section>
          )}

          {v.createdAt && <p className="date block">Опубликована {formatDate(v.createdAt)}</p>}
        </article>

        <aside className="aside">
          <div className="sheet">
            <Salary from={v.salaryFrom} to={v.salaryTo} scale={scale} showScale />
            <p className="pay-note">
              {v.salaryFrom || v.salaryTo ? 'Вилка, которую указал работодатель' : 'Обсуждается на собеседовании'}
            </p>
            {closed ? (
              <div className="note">Вакансия закрыта, отклики больше не принимаются</div>
            ) : applyFlow.canApply && (applyFlow.isApplied(v.id) ? (
              <div className="note ok">Вы откликнулись. Ответ работодателя появится в разделе «Мои отклики».</div>
            ) : (
              <button className="btn btn-primary btn-lg btn-block" onClick={() => applyFlow.start(v)}>Откликнуться</button>
            ))}
          </div>
          <div className="sheet">
            <div className="company">
              <div className="monogram">{(v.company || '?')[0]}</div>
              <div>
                <div className="name">{v.company}</div>
                <div className="role">Работодатель</div>
              </div>
            </div>
          </div>
        </aside>
      </div>
      {applyFlow.modal}
    </div>
  )
}
