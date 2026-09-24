import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Один проект: в деве Vite (порт 5173) проксирует /api на Spring (8080),
// поэтому фронт всегда ходит по относительным путям и CORS не нужен.
// В проде собранный фронт раздаётся самим Spring на 8080 — тот же origin.
export default defineConfig({
  base: process.env.VITE_BASE || '/',
  plugins: [react()],
  server: {
    port: 5173,
    open: true,
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
