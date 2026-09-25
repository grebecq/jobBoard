import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { vacanciesApi, applicationsApi, apiMessage } from '../api.js'
import { useToast } from '../toast.jsx'
import { applicationStatus, formatDate, telegramUrl } from '../format.js'

const TABS = [
  { key: 'ALL', label: 'Все', match: () => true },
  { key: 'NEW', label: 'Новые', match: (a) => a.status === 'PENDING' },
  { key: 'VIEWED', label: 'Просмотренные', match: (a) => a.status === 'VIEWED' },
  { key: 'INVITED', label: 'Приглашённые', match: (a) => a.status === 'INVITED' },
  { key: 'REJECTED', label: 'Отказы', match: (a) => a.status === 'REJECTED' },
]

export default function VacancyApplications() {
  const { id } = useParams()
  const navigate = useNavigate()
  const toast = useToast()
  const [vacancy, setVacancy] = useState(null)
  const [apps, setApps] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [tab, setTab] = useState('ALL')
  const [openId, setOpenId] = useState(null)

  useEffect(() => {
    setLoading(true)
    Promise.all([vacanciesApi.get(id), applicationsApi.forVacancy(id)])
      .then(([v, list]) => { setVacancy(v); setApps(list); setError('') })
      .catch((e) => setError(apiMessage(e, 'Отклики не загрузились. Обновите страницу.')))
      .finally(() => setLoading(false))
  }, [id])

  const replace = (updated) => setApps((prev) => prev.map((a) => (a.id === updated.id ? updated : a)))

  async function toggle(a) {
    if (openId === a.id) { setOpenId(null); return }
    setOpenId(a.id)
    if (a.status === 'PENDING') {
      try { replace(await applicationsApi.open(a.id)) } catch (e) { toast(apiMessage(e)) }
    }
  }

  const current = TABS.find((t) => t.key === tab)
  const visible = apps.filter(current.match)

  return (
    <div className="wrap">
      <button className="back" onClick={() => navigate('/my/vacancies')}>Назад к моим вакансиям</button>
      <div className="page-head" style={{ paddingTop: 0 }}>
        <h1>{vacancy?.title || 'Отклики'}</h1>
        {vacancy && <p>Отклики на вакансию, {vacancy.company}</p>}
      </div>

      <div className="stack">
        {loading ? (
          <div className="sheet"><div className="spinner" /></div>
        ) : error ? (
          <div className="sheet empty">{error}</div>
        ) : (
          <div className="sheet">
            <div className="tabs">
              {TABS.map((t) => (
                <button key={t.key} className={tab === t.key ? 'on' : ''} onClick={() => setTab(t.key)}>
                  {t.label}<span className="n">{apps.filter(t.match).length}</span>
                </button>
              ))}
            </div>
            {visible.length === 0 ? (
              <div className="empty">
                {apps.length === 0 ? 'Откликов пока нет. Они появятся здесь, как только кандидаты откликнутся.' : 'В этой вкладке пусто.'}
              </div>
            ) : (
              visible.map((a) => (
                <ApplicantItem key={a.id} a={a} open={openId === a.id}
                  onToggle={() => toggle(a)} onChanged={replace} />
              ))
            )}
          </div>
        )}
      </div>
    </div>
  )
}

function candidateName(a) {
  const full = [a.candidateFirstName, a.candidateLastName].filter(Boolean).join(' ')
  return full || a.candidateEmail
}

function ApplicantItem({ a, open, onToggle, onChanged }) {
  const toast = useToast()
  const [comment, setComment] = useState(a.employerComment || '')
  const [busy, setBusy] = useState(false)
  const s = applicationStatus(a.status)
  const tg = telegramUrl(a.candidateTelegram)

  async function decide(status) {
    setBusy(true)
    try {
      const updated = await applicationsApi.setStatus(a.id, status, comment.trim())
      onChanged(updated)
      toast(status === 'INVITED' ? 'Приглашение отправлено. Кандидат увидит ваши контакты.' : 'Отказ отправлен')
    } catch (e) {
      toast(apiMessage(e, 'Статус не изменился. Попробуйте ещё раз.'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className={'row' + (a.status === 'PENDING' ? ' fresh' : '')}>
      <div className="row-main clickable" onClick={onToggle} role="button" tabIndex={0}
        aria-expanded={open} onKeyDown={(e) => (e.key === 'Enter' || e.key === ' ') && (e.preventDefault(), onToggle())}>
        <div>
          <div className="title">{candidateName(a)}</div>
          <div className="sub">
            {[a.candidateCity, 'откликнулся ' + formatDate(a.createdAt)].filter(Boolean).join(', ')}
          </div>
        </div>
        <span className={'st ' + s.cls}>{s.label}</span>
        <svg className={'chev' + (open ? ' open' : '')} viewBox="0 0 24 24" width="16" height="16" fill="none"
          stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
          <path d="M6 9l6 6 6-6" />
        </svg>
      </div>

      {open && (
        <div className="row-body">
          <div className="actions">
            {tg && <a className="btn btn-quiet btn-sm" href={tg} target="_blank" rel="noreferrer">Telegram @{a.candidateTelegram}</a>}
            <a className="btn btn-quiet btn-sm" href={`mailto:${a.candidateEmail}`}>{a.candidateEmail}</a>
            {a.candidatePhone && <a className="btn btn-quiet btn-sm" href={`tel:${a.candidatePhone}`}>{a.candidatePhone}</a>}
          </div>

          <div className="quote">
            <small>Сопроводительное письмо</small>
            {a.coverLetter || <span className="hint">Кандидат не написал письмо</span>}
          </div>

          <label className="field">
            <span>Комментарий кандидату <span className="hint">(необязательно, он увидит его в своих откликах)</span></span>
            <textarea className="inp" rows={3} maxLength={2000} value={comment}
              onChange={(e) => setComment(e.target.value)}
              placeholder="Например: напишите в Telegram, договоримся о созвоне" />
          </label>

          <div className="actions">
            <button className="btn btn-primary" disabled={busy} onClick={() => decide('INVITED')}>
              {a.status === 'INVITED' ? 'Обновить приглашение' : 'Пригласить'}
            </button>
            <button className="btn btn-danger" disabled={busy} onClick={() => decide('REJECTED')}>
              {a.status === 'REJECTED' ? 'Обновить отказ' : 'Отказать'}
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
