import { useEffect, useState, useCallback } from 'react'
import { applicationsApi } from './api.js'
import { useAuth } from './auth.jsx'
import ApplyModal from './components/ApplyModal.jsx'

export function useApplyFlow(onAuth) {
  const { user, isAuthenticated } = useAuth()
  const isCandidate = user?.role === 'CANDIDATE'
  const [applied, setApplied] = useState(() => new Set())
  const [target, setTarget] = useState(null)

  useEffect(() => {
    if (!isCandidate) { setApplied(new Set()); return }
    applicationsApi.mine()
      .then((list) => setApplied(new Set(list.map((a) => a.vacancyId))))
      .catch(() => {})
  }, [isCandidate])

  const start = useCallback((vacancy) => {
    if (!isAuthenticated) { onAuth('login'); return }
    setTarget(vacancy)
  }, [isAuthenticated, onAuth])

  const markApplied = useCallback((id) => {
    setApplied((prev) => new Set(prev).add(id))
  }, [])

  const modal = target && (
    <ApplyModal vacancy={target} onClose={() => setTarget(null)} onApplied={markApplied} />
  )

  return {
    start,
    modal,
    isApplied: (id) => applied.has(id),
    canApply: !isAuthenticated || isCandidate,
  }
}
