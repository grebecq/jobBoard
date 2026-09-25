import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

const BACKEND = process.env.BACKEND_URL || 'http://localhost:8080'

// Vite стартует за секунду, Spring — за 10–20. Браузер открываем,
// только когда бэкенд начал отвечать, иначе первые запросы падают.
function openWhenBackendReady() {
  return {
    name: 'open-when-backend-ready',
    configureServer(server) {
      server.httpServer?.once('listening', async () => {
        const log = server.config.logger
        log.info(`  Жду бэкенд на ${BACKEND}…`)
        for (let i = 0; i < 120; i++) {
          try {
            await fetch(`${BACKEND}/api/dictionaries`)
            log.info('  Бэкенд готов, открываю браузер')
            server.openBrowser()
            return
          } catch {
            await new Promise((r) => setTimeout(r, 1000))
          }
        }
        log.warn('  Бэкенд не ответил за 2 минуты. Проверьте, что он запущен и база работает.')
      })
    },
  }
}

// Один проект: в деве Vite (порт 5173) проксирует /api на Spring (8080),
// поэтому фронт всегда ходит по относительным путям и CORS не нужен.
// В проде собранный фронт раздаётся самим Spring на 8080 — тот же origin.
export default defineConfig({
  plugins: [react(), openWhenBackendReady()],
  server: {
    port: 5173,
    open: false,
    proxy: {
      '/api': BACKEND,
    },
  },
})
