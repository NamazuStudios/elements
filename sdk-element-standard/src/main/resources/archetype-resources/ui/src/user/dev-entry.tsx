import React from 'react'
import ReactDOM from 'react-dom/client'
import { ExamplePlugin } from './ExamplePlugin'
import '../dev.css'

function DevShell() {
  const [dark, setDark] = React.useState(
    () => window.matchMedia('(prefers-color-scheme: dark)').matches
  )

  React.useEffect(() => {
    document.documentElement.classList.toggle('dark', dark)
  }, [dark])

  return (
    <div className="min-h-screen bg-background text-foreground">
      {import.meta.env.DEV && (
        <div className="fixed top-3 right-3 z-50">
          <button
            onClick={() => setDark(d => !d)}
            className="rounded-md border border-border bg-background px-3 py-1.5 text-xs text-muted-foreground hover:bg-muted transition-colors"
          >
            {dark ? 'Light mode' : 'Dark mode'}
          </button>
        </div>
      )}
      <ExamplePlugin />
    </div>
  )
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <DevShell />
  </React.StrictMode>
)
