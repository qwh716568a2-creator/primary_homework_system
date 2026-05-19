import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import path from 'path'

const projectRoot = path.resolve(__dirname)

export default defineConfig({
  plugins: [uni()],
  resolve: {
    alias: {
      '@': projectRoot
    }
  }
})
