import { useState } from 'react'
import { useAuth } from '../auth.jsx'
import { useToast } from '../toast.jsx'
import { apiMessage } from '../api.js'
import Modal from './Modal.jsx'

export default function AuthModal({ mode: initialMode, onClose }) {
  const [mode, setMode] = useState(initialMode)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [role, setRole] = useState('CANDIDATE')
  const [err, setErr] = useState('')
  const [busy, setBusy] = useState(false)
  const { login, register } = useAuth()
  const toast = useToast()

  const isReg = mode === 'register'

  async function submit(e) {
    e.preventDefault()
    setErr('')
    setBusy(true)
    try {
      const cleanEmail = email.trim().toLowerCase()
      if (isReg) {
        await register(cleanEmail, password, role)
        toast('Аккаунт создан')
      } else {
        await login(cleanEmail, password)
        toast('Вы вошли')
      }
      onClose()
    } catch (e) {
      setErr(apiMessage(e, 'Неверный email или пароль'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Modal onClose={onClose}>
      <form onSubmit={submit}>
        <div className="tabs auth-tabs" aria-label={isReg ? 'Регистрация' : 'Вход'}>
          <button type="button" className={!isReg ? 'on' : ''} onClick={() => setMode('login')}>Вход</button>
          <button type="button" className={isReg ? 'on' : ''} onClick={() => setMode('register')}>Регистрация</button>
        </div>
        <div className="modal-body">
          <label className="field">
            <span>Email</span>
            <input className="inp" type="email" required autoFocus autoComplete="email" value={email}
              onChange={(e) => setEmail(e.target.value)} placeholder="you@mail.ru" />
          </label>
          <label className="field">
            <span>Пароль</span>
            <input className="inp" type="password" required minLength={6} value={password}
              autoComplete={isReg ? 'new-password' : 'current-password'}
              onChange={(e) => setPassword(e.target.value)} placeholder="Не короче 6 символов" />
          </label>
          {isReg && (
            <div className="field">
              <span>Я здесь, чтобы</span>
              <div className="choice">
                <label>
                  <input type="radio" name="role" checked={role === 'CANDIDATE'} onChange={() => setRole('CANDIDATE')} />
                  найти работу
                </label>
                <label>
                  <input type="radio" name="role" checked={role === 'EMPLOYER'} onChange={() => setRole('EMPLOYER')} />
                  нанять людей
                </label>
              </div>
            </div>
          )}
          {err && <div className="err">{err}</div>}
          <button className="btn btn-primary btn-lg" disabled={busy}>
            {busy ? 'Подождите…' : isReg ? 'Создать аккаунт' : 'Войти'}
          </button>
        </div>
      </form>
    </Modal>
  )
}
