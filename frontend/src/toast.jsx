import { createContext, useContext, useState, useRef, useCallback } from 'react'

const ToastCtx = createContext(() => {})
export const useToast = () => useContext(ToastCtx)

export function ToastProvider({ children }) {
  const [msg, setMsg] = useState('')
  const [show, setShow] = useState(false)
  const timer = useRef(null)

  const toast = useCallback((text) => {
    setMsg(text)
    setShow(true)
    clearTimeout(timer.current)
    timer.current = setTimeout(() => setShow(false), 2600)
  }, [])

  return (
    <ToastCtx.Provider value={toast}>
      {children}
      <div className={'toast' + (show ? ' show' : '')}>{msg}</div>
    </ToastCtx.Provider>
  )
}
