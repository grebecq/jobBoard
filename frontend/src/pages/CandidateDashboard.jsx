import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { applicationsApi } from '../api.js'
import { applicationStatus, formatDate, telegramUrl } from '../format.js'

export default function CandidateDashboard() {
  const [apps, setApps] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    applicationsApi.mine()
      .then(setApps)
      .catch(() => setApps([]))
      .finally(() => setLoading(false))
  }, [])

  const count = (...statuses) => apps.filter((a) => statuses.includes(a.status)).length

  return (
    <div className="wrap">
      <div className="page-head">
        <h1>Мои отклики</h1>
        <p>Статусы обновляются, когда работодатель открывает отклик или отвечает на него.</p>
      </div>

      <div className="stack">
        {!loading && apps.length > 0 && (
          <div className="summary">
            <div><b>{apps.length}</b><span>всего</span></div>
            <div><b>{count('PENDING', 'VIEWED')}</b><span>ждут ответа</span></div>
            <div className="hot"><b>{count('INVITED')}</b><span>приглашений</span></div>
            <div><b>{count('REJECTED')}</b><span>отказов</span></div>
          </div>
        )}

        <div className="sheet">
          {loading ? (
            <div className="spinner" />
          ) : apps.length === 0 ? (
            <div className="empty">
              <p>Вы ещё никуда не откликались.</p>
              <Link to="/vacancies" className="btn btn-primary">Смотреть вакансии</Link>
            </div>
          ) : (
            apps.map((a) => <ApplicationItem key={a.id} a={a} />)
          )}
        </div>
      </div>
    </div>
  )
}

function ApplicationItem({ a }) {
  const s = applicationStatus(a.status)
  const tg = telegramUrl(a.companyTelegram)

  return (
    <div className="row">
      <div className="row-main">
        <div>
          <Link to={`/vacancy/${a.vacancyId}`} className="title">{a.vacancyTitle || 'Вакансия'}</Link>
          <div className="sub">
            {[a.companyName, a.vacancyCity, a.createdAt && 'отклик ' + formatDate(a.createdAt)].filter(Boolean).join(', ')}
          </div>
        </div>
        <span className={'st ' + s.cls}>{s.label}</span>
      </div>

      {a.status === 'INVITED' && (
        <div className="invite">
          <p>Вас пригласили. Напишите работодателю:</p>
          <div className="actions">
            {tg && <a className="btn btn-primary btn-sm" href={tg} target="_blank" rel="noreferrer">Telegram @{a.companyTelegram}</a>}
            {a.companyContactEmail && (
              <a className="btn btn-quiet btn-sm" href={`mailto:${a.companyContactEmail}`}>{a.companyContactEmail}</a>
            )}
          </div>
        </div>
      )}

      {a.employerComment && (
        <div className="quote">
          <small>Комментарий работодателя</small>
          {a.employerComment}
        </div>
      )}

      {a.coverLetter && (
        <details className="letter">
          <summary>Ваше сопроводительное письмо</summary>
          <p>{a.coverLetter}</p>
        </details>
      )}
    </div>
  )
}
