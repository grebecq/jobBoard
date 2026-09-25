import { createContext, useContext, useEffect, useState, useCallback } from 'react'
import { authApi, setToken } from './api.js'

const AuthCtx = createContext(null)
export const useAuth = () => useContext(AuthCtx)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('token')
    if (!token) { setLoading(false); return }
    let timer
    let attempt = 0
    const load = () => authApi.me()
      .then((me) => { setUser(me); setLoading(false) })
      .catch((e) => {
        // токен выбрасываем только если сервер его отверг, а не когда он ещё не запустился
        const down = !e?.response || e.response.status >= 500
        if (down && ++attempt < 20) { timer = setTimeout(load, 3000); return }
        if (e?.response?.status === 401) setToken(null)
        setLoading(false)
      })
    load()
    return () => clearTimeout(timer)
  }, [])

  useEffect(() => {
    const onExpired = () => setUser(null)
    window.addEventListener('auth:expired', onExpired)
    return () => window.removeEventListener('auth:expired', onExpired)
  }, [])

  const login = useCallback(async (email, password) => {
    const { token } = await authApi.login(email, password)
    setToken(token)
    const me = await authApi.me()
    setUser(me)
    return me
  }, [])

  const register = useCallback(async (email, password, role) => {
    await authApi.register(email, password, role)
    return login(email, password)
  }, [login])

  const logout = useCallback(() => {
    setToken(null)
    setUser(null)
  }, [])

  const value = {
    user, loading, login, register, logout,
    isAuthenticated: !!user,
    hasRole: (role) => user?.role === role,
  }
  return <AuthCtx.Provider value={value}>{children}</AuthCtx.Provider>
}
