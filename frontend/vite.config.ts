import { defineConfig } from 'vite'
import preact from '@preact/preset-vite'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [preact()],
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
