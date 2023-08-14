import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      "/api/v1": "http://127.0.0.1:8080",
      "^/api/v1/download/.*/live": {
        target: "ws://127.0.0.1:8080",
        ws: true
      }
    }
  }
})
