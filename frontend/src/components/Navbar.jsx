import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth.jsx'

function toggleTheme() {
  const root = document.documentElement
  const cur = root.getAttribute('data-theme')
  const dark = cur ? cur === 'dark' : window.matchMedia('(prefers-color-scheme:dark)').matches
  root.setAttribute('data-theme', dark ? 'light' : 'dark')
}

export default function Navbar({ onAuth }) {
  const { user, isAuthenticated, logout } = useAuth()
  const navigate = useNavigate()

  return (
    <div className="nav">
      <div className="nav-inner">
        <div className="logo" onClick={() => navigate('/')}>
          <div className="mark">В</div>
          <span className="brand-name">Вакант</span>
        </div>

        <div className="nav-links">
          <NavLink to="/" end>Вакансии</NavLink>
          {user?.role === 'CANDIDATE' && <NavLink to="/my/applications">Мои отклики</NavLink>}
          {(user?.role === 'EMPLOYER' || user?.role === 'ADMIN') && (
            <NavLink to="/my/vacancies">Мои вакансии</NavLink>
          )}
        </div>

        <div className="nav-spacer" />
        <button className="theme-toggle" onClick={toggleTheme} title="Сменить тему">◐</button>

        {isAuthenticated ? (
          <div className="nav-user">
            <div className="avatar">{(user.email || '?')[0].toUpperCase()}</div>
            <span>{user.email}</span>
            <button className="btn btn-ghost" onClick={logout}>Выйти</button>
          </div>
        ) : (
          <>
            <button className="btn btn-ghost" onClick={() => onAuth('login')}>Войти</button>
            <button className="btn btn-primary" onClick={() => onAuth('register')}>Регистрация</button>
          </>
        )}
      </div>
    </div>
  )
}
