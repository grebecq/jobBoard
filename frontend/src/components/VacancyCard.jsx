import { Link } from 'react-router-dom'
import { formatDate } from '../format.js'
import { useDictionaries } from '../dictionaries.jsx'
import Salary from './Salary.jsx'

const MAX_TAGS = 8

export default function VacancyCard({ v, scale, onApply, applied, canApply = true, onSkillClick }) {
  const { label } = useDictionaries()
  const extra = v.skills.length - MAX_TAGS
  const facts = [
    v.specialization && label('specialization', v.specialization),
    v.workFormat && label('workFormat', v.workFormat),
    v.experience && 'опыт ' + label('experience', v.experience).toLowerCase(),
    v.employmentType && v.employmentType !== 'FULL_TIME' && label('employmentType', v.employmentType),
  ].filter(Boolean)

  return (
    <article className="vrow">
      <div>
        <h2><Link to={`/vacancy/${v.id}`}>{v.title}</Link></h2>
        <div className="org">{[v.company, v.city].filter(Boolean).join(', ')}</div>
        <div className="facts-line">
          {v.grade && <span className="grade">{label('grade', v.grade)}</span>}
          {facts.map((f) => <span key={f}>{f}</span>)}
        </div>
        {v.skills.length > 0 && (
          <div className="tags">
            {v.skills.slice(0, MAX_TAGS).map((s) => (
              <button type="button" className="tag" key={s} title="Искать по этому навыку"
                onClick={() => onSkillClick?.(s)}>{s}</button>
            ))}
            {extra > 0 && <Link to={`/vacancy/${v.id}`} className="tag more">ещё {extra}</Link>}
          </div>
        )}
      </div>

      <div className="vrow-side">
        <Salary from={v.salaryFrom} to={v.salaryTo} scale={scale} />
      </div>

      <div className="vrow-foot">
        <span className="date">{v.createdAt ? formatDate(v.createdAt) : 'Недавно'}</span>
        {canApply && (applied
          ? <span className="done">Вы откликнулись</span>
          : <button className="btn btn-quiet btn-sm" onClick={() => onApply?.(v)}>Откликнуться</button>)}
      </div>
    </article>
  )
}
