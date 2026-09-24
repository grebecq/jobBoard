import { useNavigate } from 'react-router-dom'
import { formatSalary, employmentLabel } from '../format.js'

export default function VacancyCard({ v, onApply, applied, canApply = true }) {
  const navigate = useNavigate()
  const open = () => navigate(`/vacancy/${v.id}`)

  return (
    <article className="vac">
      <div className="vac-top">
        <div>
          <h3 onClick={open}>{v.title}</h3>
          <div className="co">
            {v.company} <span>·</span> 📍 {v.city}
            <span className={'badge ' + (v.remote ? 'remote' : 'emp')}>
              {employmentLabel(v.employmentType)}
            </span>
          </div>
        </div>
        <div className="salary">{formatSalary(v.salaryFrom, v.salaryTo)}</div>
      </div>

      {v.skills?.length > 0 && (
        <div className="vac-tags">
          {v.skills.map((s, i) => (
            <span className="t" key={i}>{s}</span>
          ))}
        </div>
      )}

      <div className="vac-foot">
        <span className="ago">{v.createdAt ? new Date(v.createdAt).toLocaleDateString('ru-RU') : 'Недавно'}</span>
        {canApply && (applied ? (
          <span className="status invited">Вы откликнулись ✓</span>
        ) : (
          <button
            className="btn btn-primary"
            style={{ padding: '7px 15px', fontSize: '13.5px' }}
            onClick={() => onApply?.(v)}
          >
            Откликнуться
          </button>
        ))}
      </div>
    </article>
  )
}
