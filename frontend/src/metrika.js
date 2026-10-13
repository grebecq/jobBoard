import { useEffect } from 'react'
import { useLocation } from 'react-router-dom'

const ID = Number(import.meta.env.VITE_YM_ID) || null

function load() {
  if (!ID || window.ym) return
  window.ym = function () {
    (window.ym.a = window.ym.a || []).push(arguments)
  }
  window.ym.l = Date.now()
  const s = document.createElement('script')
  s.async = true
  s.src = 'https://mc.yandex.ru/metrika/tag.js'
  document.head.appendChild(s)
  window.ym(ID, 'init', {
    defer: true,
    clickmap: true,
    trackLinks: true,
    accurateTrackBounce: true,
    webvisor: true,
  })
}

// Это SPA: при смене страницы браузер не перезагружается, поэтому просмотр отправляем сами
export function usePageViews() {
  const location = useLocation()
  useEffect(() => {
    load()
    if (ID) window.ym(ID, 'hit', location.pathname + location.search, { referer: document.referrer })
  }, [location.pathname, location.search])
}

export function reachGoal(name) {
  if (ID && window.ym) window.ym(ID, 'reachGoal', name)
}
