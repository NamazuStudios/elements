import React from 'react'

interface VersionInfo {
  version: string
  revision: string
  timestamp: string
}

export function ExamplePlugin() {
  const [info, setInfo] = React.useState<VersionInfo | null>(null)
  const [loading, setLoading] = React.useState(false)
  const [error, setError] = React.useState<string | null>(null)

  // Always read the session token from window.__elementsApiClient rather than
  // storing or hardcoding it yourself. The CMS manages the token lifecycle
  // (login, refresh, logout), so reading it at call time ensures you always
  // use a valid token without any extra state management in your plugin.
  const sessionToken = window.__elementsApiClient?.getSessionToken()

  // Use window.__elementsSettings.getResultsPerPage() for any paginated list
  // in your plugin. This respects the user's preference set in CMS Settings,
  // so your plugin's pagination stays consistent with the rest of the interface
  // rather than imposing a hardcoded page size.
  const resultsPerPage = window.__elementsSettings?.getResultsPerPage() ?? 20

  async function fetchVersion() {
    setLoading(true)
    setError(null)
    try {
      // Pass the session token in the Elements-SessionSecret header when calling
      // authenticated endpoints. Reading it fresh each time (rather than closing
      // over an earlier value) ensures the request uses the current token even if
      // the user has logged out and back in since the component mounted.
      const token = window.__elementsApiClient?.getSessionToken()
      const headers: Record<string, string> = {}
      if (token) headers['Elements-SessionSecret'] = token
      const res = await fetch('/api/rest/version', { headers })
      if (!res.ok) throw new Error(`${res.status} ${res.statusText}`)
      setInfo(await res.json())
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="p-6 max-w-2xl">
      <h1 className="text-2xl font-bold mb-2">Example Element</h1>
      <p className="text-muted-foreground mb-6">
        This page is served from the Example Element's superuser UI content directory.
      </p>

      <div className="space-y-4">
        <div className="rounded-lg border p-4 text-sm space-y-2">
          <div className="flex gap-2">
            <span className="text-muted-foreground w-36">Session token</span>
            <span className="font-mono">{sessionToken ? '\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022 (present)' : 'none'}</span>
          </div>
          <div className="flex gap-2">
            <span className="text-muted-foreground w-36">Results per page</span>
            <span className="font-mono">{resultsPerPage}</span>
          </div>
        </div>

        <div>
          <button
            onClick={fetchVersion}
            disabled={loading}
            className="rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:opacity-90 disabled:opacity-50 transition-opacity"
          >
            {loading ? 'Loading\u2026' : 'Get Platform Version'}
          </button>
        </div>

        {error && (
          <div className="rounded-lg border border-destructive/50 bg-destructive/10 p-4 text-sm text-destructive">
            {error}
          </div>
        )}

        {info && (
          <div className="rounded-lg border p-4 text-sm space-y-1">
            <div className="flex gap-2">
              <span className="text-muted-foreground w-20">Version</span>
              <span className="font-mono">{info.version}</span>
            </div>
            <div className="flex gap-2">
              <span className="text-muted-foreground w-20">Revision</span>
              <span className="font-mono">{info.revision}</span>
            </div>
            <div className="flex gap-2">
              <span className="text-muted-foreground w-20">Built</span>
              <span className="font-mono">{info.timestamp}</span>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
