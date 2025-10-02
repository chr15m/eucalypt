import { env } from "process"
import { defineConfig } from "vite"
import { viteSingleFile } from "vite-plugin-singlefile"
import { analyzer } from 'vite-bundle-analyzer'

export default defineConfig({
  plugins: [env["ANALYZE"] && analyzer(), viteSingleFile({ removeViteModuleLoader: true })],
  build: {
    // minify: "terser", // slightly smaller
    outDir: "../../docs",
    emptyOutDir: false,
    rollupOptions: {
      input: {
        "index": env["SRC"] || "index.html"
      }
    }
  }
})
