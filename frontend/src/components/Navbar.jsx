import { Link, NavLink } from 'react-router-dom'
import { useAuth } from '../auth.jsx'

function toggleTheme() {
  const root = document.documentElement
  const cur = root.getAttribute('data-theme')
  const dark = cur ? cur === 'dark' : window.matchMedia('(prefers-color-scheme:dark)').matches
  const next = dark ? 'light' : 'dark'
  root.setAttribute('data-theme', next)
  try { localStorage.setItem('theme', next) } catch {}
}

export default function Navbar({ onAuth }) {
  const { user, isAuthenticated, logout } = useAuth()

  return (
    <header className="top">
      <div className="wrap top-in">
        <Link to="/" className="wordmark">Вакант</Link>

        <nav className="top-nav">
          <NavLink to="/" end>Вакансии</NavLink>
          {user?.role === 'CANDIDATE' && <NavLink to="/my/applications">Мои отклики</NavLink>}
          {(user?.role === 'EMPLOYER' || user?.role === 'ADMIN') && (
            <NavLink to="/my/vacancies">Мои вакансии</NavLink>
          )}
        </nav>

        <div className="top-end">
          <button className="icon-btn" onClick={toggleTheme} title="Светлая или тёмная тема" aria-label="Сменить тему">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
              <circle cx="12" cy="12" r="8" />
              <path d="M12 4a8 8 0 0 1 0 16z" fill="currentColor" />
            </svg>
          </button>
          {isAuthenticated ? (
            <>
              <span className="who">{user.email}</span>
              <button className="btn btn-quiet btn-sm" onClick={logout}>Выйти</button>
            </>
          ) : (
            <>
              <button className="btn btn-quiet btn-sm" onClick={() => onAuth('login')}>Войти</button>
              <button className="btn btn-primary btn-sm" onClick={() => onAuth('register')}>Регистрация</button>
            </>
          )}
        </div>
      </div>
    </header>
  )
}
