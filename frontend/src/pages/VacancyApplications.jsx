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
  { key: 'REJECTED', label: 'Отказ', match: (a) => a.status === 'REJECTED' },
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
      .catch((e) => setError(apiMessage(e, 'Не удалось загрузить отклики')))
      .finally(() => setLoading(false))
  }, [id])

  const replace = (updated) => setApps((prev) => prev.map((a) => (a.id === updated.id ? updated : a)))

  async function toggle(a) {
    if (openId === a.id) { setOpenId(null); return }
    setOpenId(a.id)
    if (a.status === 'PENDING') {
      // открытие отклика = «просмотрен» для кандидата
      try { replace(await applicationsApi.open(a.id)) } catch (e) { toast(apiMessage(e)) }
    }
  }

  const current = TABS.find((t) => t.key === tab)
  const visible = apps.filter(current.match)

  return (
    <section className="view wrap">
      <button className="back" onClick={() => navigate('/my/vacancies')} style={{ marginTop: '28px' }}>← к моим вакансиям</button>
      <div className="dash-head" style={{ paddingTop: '4px' }}>
        <div className="eyebrow">Отклики на вакансию</div>
        <h1>{vacancy?.title || '…'}</h1>
      </div>

      {loading ? (
        <div className="spinner" />
      ) : error ? (
        <div className="center-msg">{error}</div>
      ) : (
        <div className="panel">
          <div className="tabs">
            {TABS.map((t) => {
              const n = apps.filter(t.match).length
              return (
                <button key={t.key} className={tab === t.key ? 'active' : ''} onClick={() => setTab(t.key)}>
                  {t.label} <span className="tab-n">{n}</span>
                </button>
              )
            })}
          </div>
          {visible.length === 0 ? (
            <div className="center-msg">
              {apps.length === 0 ? 'На эту вакансию пока никто не откликнулся.' : 'В этой вкладке пусто.'}
            </div>
          ) : (
            visible.map((a) => (
              <ApplicantItem key={a.id} a={a} open={openId === a.id}
                onToggle={() => toggle(a)} onChanged={replace} />
            ))
          )}
        </div>
      )}
    </section>
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
      toast(status === 'INVITED' ? 'Кандидат приглашён — он увидит ваши контакты ✓' : 'Отказ отправлен')
    } catch (e) {
      toast(apiMessage(e, 'Не удалось изменить статус'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className={'app-item' + (a.status === 'PENDING' ? ' fresh' : '')}>
      <div className="app-row clickable" onClick={onToggle}>
        <div>
          <div className="ti">{candidateName(a)}</div>
          <div className="ts">
            {[a.candidateCity, 'откликнулся ' + formatDate(a.createdAt)].filter(Boolean).join(' · ')}
          </div>
        </div>
        <span className={'status ' + s.cls}>{s.label}</span>
        <span className="chev">{open ? '▴' : '▾'}</span>
      </div>

      {open && (
        <div className="app-body">
          <div className="contact-actions">
            {tg && <a className="btn btn-primary" href={tg} target="_blank" rel="noreferrer">✈️ Telegram @{a.candidateTelegram}</a>}
            <a className="btn btn-ghost" href={`mailto:${a.candidateEmail}`}>✉️ {a.candidateEmail}</a>
            {a.candidatePhone && <a className="btn btn-ghost" href={`tel:${a.candidatePhone}`}>📞 {a.candidatePhone}</a>}
          </div>

          <div className="quote">
            <div className="quote-l">Сопроводительное письмо</div>
            {a.coverLetter || <span className="hint">Кандидат не написал письмо</span>}
          </div>

          <div>
            <label className="field-l">Комментарий кандидату <span className="hint">— необязательно, он увидит его в своём кабинете</span></label>
            <textarea className="inp" rows={3} maxLength={2000} value={comment}
              onChange={(e) => setComment(e.target.value)}
              placeholder="Например: «Напишите в Telegram, договоримся о созвоне»" />
          </div>

          <div className="contact-actions">
            <button className="btn btn-primary" disabled={busy} onClick={() => decide('INVITED')}>
              {a.status === 'INVITED' ? 'Обновить приглашение' : 'Пригласить'}
            </button>
            <button className="btn btn-ghost danger" disabled={busy} onClick={() => decide('REJECTED')}>
              {a.status === 'REJECTED' ? 'Обновить комментарий к отказу' : 'Отказать'}
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
