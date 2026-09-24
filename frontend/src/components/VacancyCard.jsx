import { useNavigate } from 'react-router-dom'
import { formatSalary, formatDate } from '../format.js'
import { useDictionaries } from '../dictionaries.jsx'

const MAX_TAGS = 8

export default function VacancyCard({ v, onApply, applied, canApply = true, onSkillClick }) {
  const navigate = useNavigate()
  const { label } = useDictionaries()
  const open = () => navigate(`/vacancy/${v.id}`)
  const extra = v.skills.length - MAX_TAGS

  return (
    <article className="vac">
      <div className="vac-top">
        <div>
          <h3 onClick={open}>{v.title}</h3>
          <div className="co">
            {v.company}
            {v.city && <><span>·</span> 📍 {v.city}</>}
            {v.specialization && <><span>·</span> {label('specialization', v.specialization)}</>}
          </div>
        </div>
        <div className="salary">{formatSalary(v.salaryFrom, v.salaryTo)}</div>
      </div>

      <div className="vac-badges">
        {v.grade && <span className="badge grade">{label('grade', v.grade)}</span>}
        {v.workFormat && <span className={'badge ' + (v.remote ? 'remote' : 'emp')}>{label('workFormat', v.workFormat)}</span>}
        {v.employmentType && <span className="badge plain">{label('employmentType', v.employmentType)}</span>}
        {v.experience && <span className="badge plain">Опыт: {label('experience', v.experience).toLowerCase()}</span>}
      </div>

      {v.skills?.length > 0 && (
        <div className="vac-tags">
          {v.skills.slice(0, MAX_TAGS).map((s) => (
            <button type="button" className="t" key={s} title="Добавить в фильтр"
              onClick={() => onSkillClick?.(s)}>{s}</button>
          ))}
          {extra > 0 && <span className="t more" onClick={open}>+{extra}</span>}
        </div>
      )}

      <div className="vac-foot">
        <span className="ago">{v.createdAt ? formatDate(v.createdAt) : 'Недавно'}</span>
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
