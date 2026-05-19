import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    build: {
      chunkSizeWarningLimit: 900,
      rollupOptions: {
        output: {
          manualChunks(id) {
            if (!id.includes('node_modules')) {
              return undefined
            }

            const normalizedId = id.replace(/\\/g, '/')

            if (
              normalizedId.includes('/element-plus/') ||
              normalizedId.includes('/@element-plus/') ||
              normalizedId.includes('/@popperjs/') ||
              normalizedId.includes('/async-validator/') ||
              normalizedId.includes('/dayjs/') ||
              normalizedId.includes('/lodash-unified/')
            ) {
              return 'vendor-element-plus'
            }

            if (
              normalizedId.includes('/vue/') ||
              normalizedId.includes('/@vue/') ||
              normalizedId.includes('/pinia/') ||
              normalizedId.includes('/vue-router/')
            ) {
              return 'vendor-vue'
            }

            return 'vendor-misc'
          }
        }
      }
    },
    server: {
      proxy: {
        '/api': {
          target: env.VITE_API_PROXY_TARGET || 'http://127.0.0.1:8080',
          changeOrigin: true
        }
      }
    }
  }
})
