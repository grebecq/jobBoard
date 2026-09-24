import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth.jsx'
import { applicationsApi } from '../api.js'
import { applicationStatus, formatDate, telegramUrl } from '../format.js'

export default function CandidateDashboard() {
  const { user } = useAuth()
  const [apps, setApps] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    applicationsApi.mine()
      .then(setApps)
      .catch(() => setApps([]))
      .finally(() => setLoading(false))
  }, [])

  const count = (status) => apps.filter((a) => a.status === status).length

  return (
    <section className="view wrap">
      <div className="dash-head">
        <div className="eyebrow">Соискатель · {user?.email}</div>
        <h1>Мои отклики</h1>
      </div>

      {!loading && apps.length > 0 && (
        <div className="stats">
          <div className="stat accent"><div className="n">{apps.length}</div><div className="l">Всего откликов</div></div>
          <div className="stat"><div className="n">{count('PENDING') + count('VIEWED')}</div><div className="l">Ждут ответа</div></div>
          <div className="stat"><div className="n">{count('INVITED')}</div><div className="l">Приглашений</div></div>
          <div className="stat"><div className="n">{count('REJECTED')}</div><div className="l">Отказов</div></div>
        </div>
      )}

      <div className="panel">
        <div className="panel-head"><h3>История откликов</h3></div>
        {loading ? (
          <div className="spinner" />
        ) : apps.length === 0 ? (
          <div className="center-msg">Здесь появятся ваши отклики. Найди вакансию и нажми «Откликнуться».</div>
        ) : (
          apps.map((a) => <ApplicationItem key={a.id} a={a} />)
        )}
      </div>
    </section>
  )
}

function ApplicationItem({ a }) {
  const s = applicationStatus(a.status)
  const tg = telegramUrl(a.companyTelegram)

  return (
    <div className="app-item">
      <div className="app-row">
        <div>
          <Link to={`/vacancy/${a.vacancyId}`} className="ti link">{a.vacancyTitle || 'Вакансия'}</Link>
          <div className="ts">
            {[a.companyName, a.vacancyCity, formatDate(a.createdAt)].filter(Boolean).join(' · ')}
          </div>
        </div>
        <span className={'status ' + s.cls}>{s.label}</span>
      </div>

      {a.status === 'INVITED' && (
        <div className="contact-box">
          <div className="contact-title">🎉 Вас пригласили! Свяжитесь с работодателем:</div>
          <div className="contact-actions">
            {tg && <a className="btn btn-primary" href={tg} target="_blank" rel="noreferrer">✈️ Telegram @{a.companyTelegram}</a>}
            {a.companyContactEmail && (
              <a className="btn btn-ghost" href={`mailto:${a.companyContactEmail}`}>✉️ {a.companyContactEmail}</a>
            )}
          </div>
        </div>
      )}

      {a.employerComment && (
        <div className="quote">
          <div className="quote-l">Комментарий работодателя</div>
          {a.employerComment}
        </div>
      )}

      {a.coverLetter && (
        <details className="letter">
          <summary>Моё сопроводительное письмо</summary>
          <p>{a.coverLetter}</p>
        </details>
      )}
    </div>
  )
}
