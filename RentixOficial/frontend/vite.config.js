import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react' // Dacă folosești React

export default defineConfig({
    plugins: [react()],
    base:'./',
    build: {
        // Asta trimite fișierele compilate direct în folderul static din Java
        outDir: '../src/main/resources/static',
        emptyOutDir: true,
    }
})