# Setting Up Twitch OIDC Login (Backend)

This walks through configuring Twitch as an OIDC provider for the browser-redirect login
flow (see `OIDC-THICK-CLIENT-LOGIN.md` for the client-side sequence). It covers registering
the app with Twitch and creating the `OidcProviderConfiguration` on the Elements server.

This is distinct from the older, direct id_token validation path (`OidcAuthScheme`, seeded
by `DefaultOidcSchemeConfiguration`) — that one just validates a JWT you already possess.
This one drives the full authorization-code flow: opening Twitch's login page, handling the
redirect, and exchanging the code for tokens.

## Step 1: Determine your Elements callback URL

Before registering anything with Twitch, figure out the exact URL Elements will use as the
OAuth redirect target. It's always:

```
{API_OUTSIDE_URL}/oidc/twitch/callback
```

`API_OUTSIDE_URL` is this server's configured public base URL (the `dev.getelements.elements.api.url`
named config value; defaults to `http://localhost:8080/api/rest` if unset). For a default
local dev setup, the callback URL is:

```
http://localhost:8080/api/rest/oidc/twitch/callback
```

**This must match what you register with Twitch byte-for-byte** including scheme, host, port, path,
and trailing slash all count. Twitch rejects any mismatch with `error=redirect_mismatch`, otherwise.

## Step 2: Register an app in the Twitch Developer Console

1. Go to the [Twitch Developer Console](https://dev.twitch.tv/console/apps) and create a new
   application.
2. When creating the application, specify the exact callback URL from Step 1 as the **OAuth
   Redirect URL**.
3. Save, then note the **Client ID**, and generate/copy a **Client Secret**.

## Step 3: Create the provider configuration in Elements

This requires a SUPERUSER session in the admin console.

1. In the left sidebar, open the **Auth** category and select **OIDC Providers**.
2. Click **+ Create OIDC Provider** to open the creation dialog.
3. Fill in the fields:
   - **name**: `twitch` — this becomes part of the callback URL (`/oidc/twitch/callback`) and
     the key used for `user.linkedAccountProfiles`.
   - **discoveryUrl**: `https://id.twitch.tv/oauth2/.well-known/openid-configuration`
   - **clientId**: your Twitch Client ID
   - **clientSecret**: your Twitch Client Secret — this field is never pre-filled; the API
     never echoes a saved secret back, so it always shows blank if you reopen this
     configuration to edit it later. Leave it blank on an edit to keep the existing secret.
   - **scopes**: a tag input — type `openid`, press Space or Enter, then type
     `user:read:email` and press Space or Enter. Both scopes are required; see below.
   - **redirectUri**: leave **Use built-in Elements redirect** checked so Elements
     auto-computes the callback URL from Step 1. Only uncheck it and enter a URL manually if
     you registered a *different* callback URL with Twitch; if you do, it must match that
     registered value byte-for-byte, same rule as above.
   - **extraAuthorizeParams**: a raw JSON textarea. Paste:
     ```json
     {
       "claims": "{\"id_token\":{\"email\":null,\"email_verified\":null,\"preferred_username\":null}}"
     }
     ```
   - **tokenEndpointAuthMethod**: select **Client Secret Post** from the dropdown. Do not
     leave this at the default **Client Secret Basic** — see below.
   - **successRedirectUrl** / **errorRedirectUrl**: optional, only needed for the
     browser-redirect thick-client flow.
4. Save. Elements resolves Twitch's discovery document immediately and auto-provisions the
   matching `OidcAuthScheme` by issuer no separate manual step needed.

### Field values that matter more than they look like they should

- **`scopes` must include `"openid"`.** Twitch's token endpoint only returns an `id_token` if
  the authorization request included the `openid` scope. Without it, you'll get a `200` from
  the token exchange but no `id_token` in the response, which Elements reports as `Token
  endpoint response did not contain an id_token`.
- **`tokenEndpointAuthMethod` must be `CLIENT_SECRET_POST`.** This field defaults to
  `CLIENT_SECRET_BASIC` (HTTP Basic auth) if omitted, which is legal per RFC 6749 — but
  Twitch's token endpoint doesn't read credentials from the `Authorization` header at all. If
  left at the default, token exchange fails with `{"status":400,"message":"missing client
  secret"}` (or `missing client id`) even though the credentials were sent, just in the wrong
  place as far as Twitch is concerned.
- **Getting `email` requires two separate things, not one.** The `user:read:email` scope is a
  *prerequisite* per Twitch's docs, but it is not sufficient by itself — Twitch's id_token only
  ever carries a minimal default claim set (`aud`, `azp`, `exp`, `iat`, `iss`, `sub`) unless you
  *also* request extra claims via the non-standard `claims` authorize parameter, set through
  `extraAuthorizeParams` as shown above. Skip either one and `email` simply won't be in the
  id_token, and Elements will have nothing to link. Elements trusts any `email` claim returned
  by a configured provider as already verified — it does **not** check `email_verified` at all
  (some providers omit that claim, or encode it as a non-boolean type). The example above still
  requests `email_verified` for completeness, but it's informational only; Elements ignores it.

### Profile claims: what gets linked onto the `User` record

Beyond `sub` (linked as a `UserUid`) and `email` (linked as a `UserUid` + copied to `user.email`),
Elements also captures standard OIDC `profile`-scope claims — `given_name`, `family_name`,
`preferred_username`, and others — whenever a provider returns them:

- On a **new anonymous login** (`AnonOidcAuthService`, the flow this doc walks through), any of
  `preferred_username` → `user.preferredUsername`, `given_name` → `user.firstName`, and
  `family_name` → `user.lastName` that are present get set on the new user directly. On a
  **returning** user, the same claims only fill in a field if it's currently blank — an existing
  value (set by an admin, the user, or an earlier login) is never overwritten.
- Every provider's full set of returned profile claims is also snapshotted, as-is, into
  `user.linkedAccountProfiles`, keyed by the OIDC scheme's `name` — this is a per-provider audit
  trail (visible in the admin console as a breakout view on the user record), not subject to the
  fill-only-if-blank rule above, and is captured on both new and returning logins, and when
  linking an additional scheme to an already-authenticated user (`UserOidcAuthService`) — though
  that linking path does **not** touch the flat `preferredUsername`/`firstName`/`lastName` fields,
  only `linkedAccountProfiles`.

For Twitch specifically: request `preferred_username` via the `claims` extra authorize parameter
as shown in Step 3 to get it linked. Twitch has no "real name" concept in its public API/OIDC
surface (only username/display name), so `given_name`/`family_name`/`name` will never be present
regardless of configuration. `Profile.displayName` is unrelated to all of this — it's always a
randomly generated name unless set explicitly via the profile API.

## Step 4: Verify in the admin console

The **OIDC Providers** resource in the admin console (under the Auth category) shows the
saved configuration. `clientSecret` is write-only — it's never echoed back by the API, so the
console always shows it blank on edit; leave it blank when editing to keep the existing
secret, or type a new one to rotate it.

## Step 5: Test the login

Using the thick-client sequence (see `OIDC-THICK-CLIENT-LOGIN.md` for full detail):

```bash
curl -X POST http://localhost:8080/api/rest/oidc/session \
  -H 'Content-Type: application/json' \
  -d '{"provider": "twitch"}'
```

Open the returned `authorizeUrl` in a browser, complete the Twitch login, then poll:

```bash
curl http://localhost:8080/api/rest/oidc/session/{handle}
```

until `status` is `COMPLETE` (with the session) or `FAILED`. Two things to know about polling:
`COMPLETE` is only returned once, on the poll that first observes it — a second poll for the same
handle returns HTTP `404`, not another `COMPLETE` body. An unknown or expired handle also returns
`404` rather than a body with `status: EXPIRED`, so a `404` on its own doesn't necessarily mean the
login failed; check whether you'd already consumed a `COMPLETE` response before treating it as one.

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| Browser lands on `...?error=redirect_mismatch` | The `redirect_uri` Elements sent doesn't exactly match what's registered in Twitch's console | Compare the exact `redirectUri` on the provider config against Twitch's registered OAuth Redirect URL; they must match byte-for-byte |
| Token exchange fails: `{"status":400,"message":"missing client id"}` or `"missing client secret"` | `tokenEndpointAuthMethod` is `CLIENT_SECRET_BASIC` (the default); Twitch ignores the `Authorization` header | Set `tokenEndpointAuthMethod` to `CLIENT_SECRET_POST` on the provider config |
| `ForbiddenException: Token endpoint response did not contain an id_token` | `scopes` doesn't include `openid` | Add `"openid"` to the provider config's `scopes` |
| `Token exchange failed with status 400` with no other detail returned to the caller | Expected — the actual provider error is intentionally not exposed to the API caller | Check the server logs; the token endpoint's error body is logged at `error` level (`OidcLoginAttemptOperations.exchangeCodeForIdToken`) |
| Logged in, but no email `UserUid` and `user.email` is empty | `email` missing from the id_token — either `user:read:email` scope is missing or the `claims` extra authorize param isn't set (`email_verified` is not required; Elements doesn't check it) | Add `user:read:email` to `scopes` and set `extraAuthorizeParams.claims` as shown above |
| Logged in, but `user.preferredUsername` wasn't set | `preferred_username` missing from the id_token, the user already had a `preferredUsername` set (fill-only-if-blank, never overwritten), or you're testing the link-account flow rather than login (that path only writes `linkedAccountProfiles`, not the flat field) | Confirm `extraAuthorizeParams.claims` requests `preferred_username` under `id_token`; check `user.linkedAccountProfiles["twitch"]` to see exactly what the token returned |
