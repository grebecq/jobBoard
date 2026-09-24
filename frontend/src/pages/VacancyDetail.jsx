import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { vacanciesApi } from '../api.js'
import { useApplyFlow } from '../useApplyFlow.jsx'
import { formatSalary } from '../format.js'
import { useDictionaries } from '../dictionaries.jsx'

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

  if (loading) return <div className="wrap"><div className="spinner" /></div>
  if (error || !v) return (
    <div className="wrap">
      <button className="back" onClick={() => navigate('/')}>← ко всем вакансиям</button>
      <div className="center-msg">Не удалось загрузить вакансию. Возможно, бэкенд недоступен.</div>
    </div>
  )

  return (
    <section className="view wrap">
      <button className="back" onClick={() => navigate('/')}>← ко всем вакансиям</button>
      <div className="detail">
        <div className="dcard">
          <h1>{v.title}</h1>
          <div className="dmeta">
            <span>{v.company}</span>
            {v.city && <><span>·</span><span>📍 {v.city}</span></>}
            {v.grade && <span className="badge grade">{label('grade', v.grade)}</span>}
            {v.workFormat && <span className={'badge ' + (v.remote ? 'remote' : 'emp')}>{label('workFormat', v.workFormat)}</span>}
          </div>
          <dl className="facts">
            {[
              ['Специализация', label('specialization', v.specialization)],
              ['Грейд', label('grade', v.grade)],
              ['Опыт', label('experience', v.experience)],
              ['Формат работы', label('workFormat', v.workFormat)],
              ['Занятость', label('employmentType', v.employmentType)],
            ].filter(([, val]) => val).map(([k, val]) => (
              <div key={k}><dt>{k}</dt><dd>{val}</dd></div>
            ))}
          </dl>
          {v.description
            ? <p style={{ color: 'var(--muted)', whiteSpace: 'pre-line' }}>{v.description}</p>
            : <p style={{ color: 'var(--muted)' }}>Описание не указано.</p>}
          {v.skills?.length > 0 && (
            <div className="dsection">
              <h3>Стек и навыки</h3>
              <div className="vac-tags">{v.skills.map((s, i) => <span className="t" key={i}>{s}</span>)}</div>
            </div>
          )}
        </div>

        <div className="aside">
          <div className="box">
            <div className="salary-big">{formatSalary(v.salaryFrom, v.salaryTo)}</div>
            <div style={{ color: 'var(--muted)', fontSize: '13.5px' }}>на руки, до вычета налогов</div>
            {String(v.status).toUpperCase() === 'CLOSED' ? (
              <div className="note" style={{ marginTop: '12px' }}>Вакансия закрыта, отклики не принимаются</div>
            ) : applyFlow.canApply && (applyFlow.isApplied(v.id) ? (
              <div className="note ok" style={{ marginTop: '12px' }}>
                Вы откликнулись. Статус — в разделе «Мои отклики».
              </div>
            ) : (
              <button className="btn btn-primary" onClick={() => applyFlow.start(v)}>Откликнуться</button>
            ))}
          </div>
          <div className="box">
            <div className="co-row">
              <div className="co-logo">{(v.company || '—')[0]}</div>
              <div>
                <div style={{ fontWeight: 700 }}>{v.company}</div>
                <div style={{ color: 'var(--muted)', fontSize: '13px' }}>Работодатель</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      {applyFlow.modal}
    </section>
  )
}
