import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': '/src'
    }
  },
  server: {
    proxy: {
      // Dev proxy for the default Genew backend to bypass CORS
      '/doc': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        secure: false
      },
      '/swagger/swagger-resources': {
        target: 'https://newdev.rdapp.com:53839',
        changeOrigin: true,
        secure: false
      }
    }
  }
})
