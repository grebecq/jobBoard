import { useEffect, useState } from 'react'
import { useAuth } from '../auth.jsx'
import { applicationsApi } from '../api.js'

const STATUS = {
  PENDING:  { cls: 'pending',  label: 'На рассмотрении' },
  VIEWED:   { cls: 'pending',  label: 'Просмотрен' },
  INVITED:  { cls: 'invited',  label: 'Приглашение' },
  REJECTED: { cls: 'rejected', label: 'Отказ' },
}

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

  return (
    <section className="view wrap">
      <div className="dash-head">
        <div className="eyebrow">Соискатель · {user?.email}</div>
        <h1>Мои отклики</h1>
      </div>

      <div className="panel">
        <div className="panel-head"><h3>История откликов</h3></div>
        {loading ? (
          <div className="spinner" />
        ) : apps.length === 0 ? (
          <div className="center-msg">Здесь появятся ваши отклики. Найди вакансию и нажми «Откликнуться».</div>
        ) : (
          apps.map((a) => {
            const s = STATUS[a.status] || { cls: 'pending', label: a.status }
            return (
              <div className="trow" key={a.id}>
                <div>
                  <div className="ti">{a.vacancyTitle || 'Вакансия'}</div>
                  <div className="ts">
                    {a.vacancyCity ? a.vacancyCity + ' · ' : ''}
                    {a.createdAt ? new Date(a.createdAt).toLocaleDateString('ru-RU') : ''}
                  </div>
                </div>
                <span className={'status ' + s.cls}>{s.label}</span>
              </div>
            )
          })
        )}
      </div>
    </section>
  )
}
