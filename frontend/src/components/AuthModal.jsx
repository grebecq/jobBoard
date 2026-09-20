import { useState } from 'react'
import { useAuth } from '../auth.jsx'
import { useToast } from '../toast.jsx'

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
      if (isReg) {
        await register(email, password, role)
        toast('Аккаунт создан ✓')
      } else {
        await login(email, password)
        toast('С возвращением ✓')
      }
      onClose()
    } catch (e) {
      const msg = e?.response?.data?.message || 'Не удалось. Проверь email и пароль.'
      setErr(msg)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="overlay" onClick={(e) => e.target.classList.contains('overlay') && onClose()}>
      <form className="modal" onSubmit={submit}>
        <button type="button" className="close-x" onClick={onClose}>×</button>
        <div className="modal-head">
          <h2>{isReg ? 'Регистрация' : 'Вход'}</h2>
          <p>Добро пожаловать в Вакант</p>
        </div>
        <div className="seg">
          <button type="button" className={!isReg ? 'active' : ''} onClick={() => setMode('login')}>Вход</button>
          <button type="button" className={isReg ? 'active' : ''} onClick={() => setMode('register')}>Регистрация</button>
        </div>
        <div className="modal-body">
          <div>
            <label className="field-l">Email</label>
            <input className="inp" type="email" required value={email}
              onChange={(e) => setEmail(e.target.value)} placeholder="you@mail.ru" />
          </div>
          <div>
            <label className="field-l">Пароль</label>
            <input className="inp" type="password" required minLength={6} value={password}
              onChange={(e) => setPassword(e.target.value)} placeholder="от 6 символов" />
          </div>
          {isReg && (
            <div>
              <label className="field-l">Я ищу</label>
              <div className="role-pick">
                <label>
                  <input type="radio" name="role" checked={role === 'CANDIDATE'}
                    onChange={() => setRole('CANDIDATE')} />
                  <span>💼 Работу</span>
                </label>
                <label>
                  <input type="radio" name="role" checked={role === 'EMPLOYER'}
                    onChange={() => setRole('EMPLOYER')} />
                  <span>👥 Сотрудников</span>
                </label>
              </div>
            </div>
          )}
          {err && <div className="err">{err}</div>}
          <button className="btn btn-primary" style={{ padding: '12px', marginTop: '4px' }} disabled={busy}>
            {busy ? 'Секунду…' : isReg ? 'Создать аккаунт' : 'Войти'}
          </button>
        </div>
      </form>
    </div>
  )
}
