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
    authApi.me()
      .then(setUser)
      .catch(() => setToken(null))
      .finally(() => setLoading(false))
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
