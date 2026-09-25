import { useState } from 'react'
import { applicationsApi, apiMessage } from '../api.js'
import { useToast } from '../toast.jsx'
import Modal from './Modal.jsx'

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
      toast('Отклик отправлен')
      onApplied(vacancy.id)
      onClose()
    } catch (e) {
      const st = e?.response?.status
      if (st === 409) {
        toast('Вы уже откликались на эту вакансию')
        onApplied(vacancy.id)
        onClose()
      } else if (st === 403) {
        setErr('Откликаться могут только соискатели. Войдите как соискатель.')
      } else {
        setErr(apiMessage(e, 'Отклик не отправился. Попробуйте ещё раз.'))
      }
    } finally {
      setBusy(false)
    }
  }

  return (
    <Modal onClose={onClose} wide>
      <form onSubmit={submit}>
        <h2>Отклик на вакансию</h2>
        <p className="lead">{vacancy.title}, {vacancy.company}</p>
        <div className="modal-body" style={{ marginTop: '20px' }}>
          <label className="field">
            <span>Сопроводительное письмо <span className="hint">(необязательно)</span></span>
            <textarea className="inp" rows={7} maxLength={5000} value={letter} autoFocus
              onChange={(e) => setLetter(e.target.value)}
              placeholder="Почему вам интересна эта вакансия и что вы уже делали на похожем стеке" />
          </label>
          <div className="hint counter num">{letter.length} / 5000</div>
          <div className="note">
            Работодатель увидит ваш email. Если он пригласит вас, в разделе «Мои отклики» появятся его Telegram и почта.
          </div>
          {err && <div className="err">{err}</div>}
          <button className="btn btn-primary btn-lg" disabled={busy}>
            {busy ? 'Отправляем…' : 'Отправить отклик'}
          </button>
        </div>
      </form>
    </Modal>
  )
}
