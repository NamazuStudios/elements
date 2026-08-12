# OIDC Login for Thick Clients (Browser-Redirect Flow)

This describes how a client that cannot receive a browser redirect directly (e.g. a native
game client, a GameMaker/Unity/Unreal build with no embedded web server) logs a user in
against any configured OIDC provider (Google, Apple, Twitch, or any admin-registered
provider). The client only ever calls two endpoints and opens one URL in the system browser 
it never runs a local HTTP server and never intercepts the provider's redirect itself.

## Prerequisite

An administrator must have already registered the provider via
`POST /auth_scheme/oidc_provider`, supplying its discovery URL, client ID/secret, scopes,
and redirect URI. This is a one-time, per-deployment setup step and is not part of the
per-login sequence below.

## Sequence

```
Client                          Elements Server                    Provider (e.g. Twitch)
  |                                    |                                    |
  |  1. POST /oidc/session             |                                    |
  |     { "provider": "twitch" }       |                                    |
  |----------------------------------->|                                    |
  |                                    |                                    |
  |  201 { handle, authorizeUrl,       |                                    |
  |        expiresAt }                 |                                    |
  |<-----------------------------------|                                    |
  |                                    |                                    |
  |  2. Open authorizeUrl in the       |                                    |
  |     system/default browser         |                                    |
  |------------------------------------------------------------------------>|
  |                                    |                                    |
  |                                    |         User authenticates and     |
  |                                    |         approves/denies access     |
  |                                    |                                    |
  |                                    |  3. Browser redirected to          |
  |                                    |     GET /oidc/{provider}/callback  |
  |                                    |     ?code=...&state=...            |
  |                                    |<-----------------------------------|
  |                                    |                                    |
  |                                    |  Server validates state, exchanges |
  |                                    |  the code, validates the id_token, |
  |                                    |  creates the Elements session, and |
  |                                    |  marks the attempt COMPLETE        |
  |                                    |                                    |
  |                                    |  200 (HTML "Success" page)         |
  |                                    |------------------------------------>|
  |                                    |                                    |
  |  4. GET /oidc/session/{handle}     |                                    |
  |     (poll on an interval)          |                                    |
  |----------------------------------->|                                    |
  |                                    |                                    |
  |  200 { status: "PENDING" }         |                                    |
  |<----------------------------------- (repeat until COMPLETE/FAILED/404)  |
  |                                    |                                    |
  |  200 { status: "COMPLETE",         |                                    |
  |        session: {...} }            |                                    |
  |<-----------------------------------|                                    |
  |                                    |                                    |
  |  Login complete — use `session`    |                                    |
```

## Endpoint reference

### 1. `POST /oidc/session` — begin the attempt

Request body:

```json
{ "provider": "twitch" }
```

Response `201 Created`:

```json
{
  "handle": "opaque-poll-handle",
  "authorizeUrl": "https://id.twitch.tv/oauth2/authorize?...",
  "expiresAt": 1234567890
}
```

The client opens `authorizeUrl` in the system browser and retains `handle` for polling.

### 2. Browser completes the provider's login flow

This step happens entirely between the user's browser and the provider — the client
application is not involved and does not receive the redirect. The provider redirects the
browser to the server's registered callback:

`GET /oidc/{provider}/callback?code=...&state=...`

This endpoint is provider-facing only, is never called by the game client, and always
returns a `200` HTML page regardless of outcome (the actual result is only observable via
the poll endpoint).

### 3. `GET /oidc/session/{handle}` — poll for completion

Response, one of:

| `status`   | Meaning                                                                 |
|------------|--------------------------------------------------------------------------|
| `PENDING`  | Still waiting on the user; poll again after a short delay.               |
| `COMPLETE` | Returned **exactly once**, on the poll that first observes completion. Includes `session`, the completed Elements session. |
| `FAILED`   | Login failed or was denied. Includes a human-readable `reason`.          |
| `404`      | The handle is unknown, already consumed, or has expired.                 |

Once `COMPLETE` is observed, the client has its Elements session and the flow is done.

## Shortcut: client already holds an `id_token`

If the client already has a valid `id_token` from a native provider SDK (e.g. platform
Sign-In SDKs), it can skip the browser/poll dance entirely:

```json
{ "provider": "twitch", "idToken": "<id_token>" }
```

`POST /oidc/session` with `idToken` set returns `200` synchronously with the completed
session, sharing the same token-validation logic as the callback path:

```json
{ "session": { "...": "..." } }
```