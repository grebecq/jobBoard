import { useEffect, useState } from 'react'
import { LogoMark } from './Logo.jsx'

const SEEN_KEY = 'splash-seen'

function shouldShow() {
  try {
    if (sessionStorage.getItem(SEEN_KEY)) return false
  } catch {}
  return !window.matchMedia('(prefers-reduced-motion: reduce)').matches
}

export default function Splash() {
  const [stage, setStage] = useState(() => (shouldShow() ? 'in' : 'gone'))

  useEffect(() => {
    if (stage === 'gone') return
    try { sessionStorage.setItem(SEEN_KEY, '1') } catch {}
    const out = setTimeout(() => setStage('out'), 1300)
    const gone = setTimeout(() => setStage('gone'), 1700)
    return () => { clearTimeout(out); clearTimeout(gone) }
  }, [])

  if (stage === 'gone') return null
  return (
    <div className={'splash' + (stage === 'out' ? ' out' : '')} aria-hidden="true">
      <div className="splash-logo">
        <LogoMark size={56} />
        <span className="splash-word">Вакант</span>
      </div>
    </div>
  )
}
