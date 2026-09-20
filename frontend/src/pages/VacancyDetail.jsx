import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { vacanciesApi, applicationsApi, apiMessage } from '../api.js'
import { useAuth } from '../auth.jsx'
import { useToast } from '../toast.jsx'
import { formatSalary, employmentLabel } from '../format.js'

export default function VacancyDetail({ onAuth }) {
  const { id } = useParams()
  const navigate = useNavigate()
  const [v, setV] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)
  const { isAuthenticated } = useAuth()
  const toast = useToast()

  useEffect(() => {
    setLoading(true)
    vacanciesApi.get(id)
      .then((data) => { setV(data); setError(false) })
      .catch(() => setError(true))
      .finally(() => setLoading(false))
  }, [id])

  async function apply() {
    if (!isAuthenticated) { onAuth('login'); return }
    try {
      await applicationsApi.apply(v.id)
      toast('Отклик отправлен ✓')
    } catch (e) {
      const st = e?.response?.status
      if (st === 409) toast('Вы уже откликались на эту вакансию')
      else if (st === 403) toast('Откликаться могут только соискатели')
      else if (st === 401) { toast('Сессия истекла, войдите заново'); onAuth('login') }
      else toast(apiMessage(e, 'Не удалось отправить отклик'))
    }
  }

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
            <span>{v.company}</span><span>·</span><span>📍 {v.city}</span><span>·</span>
            <span className={'badge ' + (v.remote ? 'remote' : 'emp')}>{employmentLabel(v.employmentType)}</span>
          </div>
          {v.description
            ? <p style={{ color: 'var(--muted)', whiteSpace: 'pre-line' }}>{v.description}</p>
            : <p style={{ color: 'var(--muted)' }}>Описание не указано.</p>}
          {v.skills?.length > 0 && (
            <div className="dsection">
              <h3>Навыки</h3>
              <div className="vac-tags">{v.skills.map((s, i) => <span className="t" key={i}>{s}</span>)}</div>
            </div>
          )}
        </div>

        <div className="aside">
          <div className="box">
            <div className="salary-big">{formatSalary(v.salaryFrom, v.salaryTo)}</div>
            <div style={{ color: 'var(--muted)', fontSize: '13.5px' }}>на руки, до вычета налогов</div>
            <button className="btn btn-primary" onClick={apply}>Откликнуться</button>
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
    </section>
  )
}
