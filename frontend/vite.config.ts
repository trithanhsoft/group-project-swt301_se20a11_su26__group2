import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  return {
    plugins: [react()],
    define: {
      global: 'window',
    },
    base: mode === 'production' ? '/swp391-su26-ai-audit-project-swp391_se20a11_group-02/' : '/',
    server: {
      port: 5173,
      strictPort: true
    }
  }
})

