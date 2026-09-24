import { formatSalary } from '../format.js'

// шаг шкалы: 50 тыс., чтобы подписи были круглыми
const STEP = 50000

export function salaryScale(vacancies) {
  const values = vacancies.map((v) => v.salaryTo || v.salaryFrom).filter(Boolean).sort((a, b) => a - b)
  if (!values.length) return 0
  // выбросы вроде опечатки в 500 млн не должны сжимать шкалу для остальных
  const median = values[Math.floor(values.length / 2)]
  const top = Math.max(...values.filter((n) => n <= median * 3))
  return Math.ceil(top / STEP) * STEP
}

function short(n) {
  return n >= 1000000 ? `${+(n / 1000000).toFixed(1)} млн` : `${Math.round(n / 1000)} тыс`
}

export default function Salary({ from, to, scale, showScale = false }) {
  if (!from && !to) return <div className="pay none">Зарплата не указана</div>

  let bar = null
  if (scale) {
    const pct = (n) => `${Math.min(100, (n / scale) * 100)}%`
    let cls = ''
    let left = 0
    let right = scale
    if (from && to) { left = from; right = to }
    else if (from) { left = from; cls = 'open-end' }
    else { right = to; cls = 'open-start' }
    bar = (
      <>
        <div className="fork" title={formatSalary(from, to)}>
          <i className={cls} style={{ left: pct(left), right: `calc(100% - ${pct(right)})` }} />
        </div>
        {showScale && (
          <div className="fork-scale num"><span>0</span><span>{short(scale)}</span></div>
        )}
      </>
    )
  }

  return (
    <>
      <div className="pay num">{formatSalary(from, to)}</div>
      {bar}
    </>
  )
}
