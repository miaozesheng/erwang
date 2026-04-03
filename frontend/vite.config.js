import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  build: {
    rollupOptions: {
      output: {
        entryFileNames: 'assets/[name]-[hash].js',
        chunkFileNames: 'assets/[name]-[hash].js',
        assetFileNames: 'assets/[name]-[hash].[ext]',
        manualChunks(id) {
          if (id.includes('element-plus') || id.includes('@element-plus')) {
            return 'element-plus'
          }
          if (id.includes('marked') || id.includes('highlight.js') || id.includes('dompurify')) {
            return 'markdown'
          }
          if (id.includes('node_modules/vue') || id.includes('vue-router') || id.includes('axios')) {
            return 'vendor'
          }
        }
      }
    }
  }
})