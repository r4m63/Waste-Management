import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react-swc'
import {fileURLToPath, URL} from 'node:url'

export default defineConfig({
    plugins: [react()],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url)),
        },
    },
    server: {
        host: true,
        port: 21003,
    }
})
