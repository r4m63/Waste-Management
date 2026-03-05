
import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react-swc'
import {fileURLToPath} from 'url'
import {resolve} from 'path'

const __filename = fileURLToPath(import.meta.url)
const __dirname = resolve(__filename, '..')

export default defineConfig({
    plugins: [react()],
    resolve: {
        alias: {
            '@': resolve(__dirname, './src'),
        },
    },
    server: {
        host: true,
        port: 21001,
    }
})
