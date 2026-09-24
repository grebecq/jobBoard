import { useState } from 'react'
import { applicationsApi, apiMessage } from '../api.js'
import { useToast } from '../toast.jsx'

export default function ApplyModal({ vacancy, onClose, onApplied }) {
  const [letter, setLetter] = useState('')
  const [busy, setBusy] = useState(false)
  const [err, setErr] = useState('')
  const toast = useToast()

  async function submit(e) {
    e.preventDefault()
    setBusy(true)
    setErr('')
    try {
      await applicationsApi.apply(vacancy.id, letter.trim())
      toast('Отклик отправлен ✓')
      onApplied(vacancy.id)
      onClose()
    } catch (e) {
      const st = e?.response?.status
      if (st === 409) {
        toast('Вы уже откликались на эту вакансию')
        onApplied(vacancy.id)
        onClose()
      } else if (st === 403) {
        setErr('Откликаться могут только соискатели')
      } else {
        setErr(apiMessage(e, 'Не удалось отправить отклик'))
      }
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="overlay" onClick={(e) => e.target.classList.contains('overlay') && onClose()}>
      <form className="modal wide" onSubmit={submit}>
        <button type="button" className="close-x" onClick={onClose}>×</button>
        <div className="modal-head">
          <h2>Отклик на вакансию</h2>
          <p>{vacancy.title} · {vacancy.company}</p>
        </div>
        <div className="modal-body">
          <div>
            <label className="field-l">Сопроводительное письмо <span className="hint">— необязательно</span></label>
            <textarea className="inp" rows={7} maxLength={5000} value={letter} autoFocus
              onChange={(e) => setLetter(e.target.value)}
              placeholder="Пара предложений: почему вам интересна вакансия и что вы уже умеете" />
            <div className="hint right">{letter.length} / 5000</div>
          </div>
          <div className="note">
            Работодатель увидит ваш email и контакты из профиля. Если он пригласит вас,
            здесь появятся его Telegram и почта для связи.
          </div>
          {err && <div className="err">{err}</div>}
          <button className="btn btn-primary" style={{ padding: '12px' }} disabled={busy}>
            {busy ? 'Отправляем…' : 'Откликнуться'}
          </button>
        </div>
      </form>
    </div>
  )
}
