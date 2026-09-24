import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App.jsx'
import { AuthProvider } from './auth.jsx'
import { ToastProvider } from './toast.jsx'
import { DictionariesProvider } from './dictionaries.jsx'
import './index.css'

try {
  const theme = localStorage.getItem('theme')
  if (theme) document.documentElement.setAttribute('data-theme', theme)
} catch {}

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <ToastProvider>
        <AuthProvider>
          <DictionariesProvider>
            <App />
          </DictionariesProvider>
        </AuthProvider>
      </ToastProvider>
    </BrowserRouter>
  </React.StrictMode>
)
