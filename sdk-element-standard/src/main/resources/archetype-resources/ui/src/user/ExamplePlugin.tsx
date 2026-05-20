import React from 'react'

export function ExamplePlugin() {
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

  return (
    <div className="p-6 max-w-2xl">
      <h1 className="text-2xl font-bold mb-2">Example Element</h1>
      <p className="text-muted-foreground mb-4">
        This page is served from the Example Element's user UI content directory.
      </p>
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
    </div>
  )
}
