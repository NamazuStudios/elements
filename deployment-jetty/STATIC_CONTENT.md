# Static Content Serving

Elements can bundle static files (HTML, JS, CSS, images, etc.) inside their JAR and have them served automatically
by the Jetty deployment module. Two independent content trees are supported:

| Tree | Source directory | Default mount path |
|------|------------------|--------------------|
| **Standard** | `static/` inside the Element JAR | `/app/static/{prefix}` |
| **UI** | `ui/` inside the Element JAR | `/app/ui/{prefix}` |

`{prefix}` is taken from the `dev.getelements.elements.app.serve.prefix` attribute. If that attribute is blank the
Element's own name is used as the fallback.

---

## Mount-path attributes

| Attribute | Description |
|-----------|-------------|
| `dev.getelements.elements.app.serve.prefix` | Shared prefix used in the default paths for both trees. |
| `dev.getelements.element.static.uri` | Override the full mount path for the **Standard** tree. |
| `dev.getelements.element.ui.uri` | Override the full mount path for the **UI** tree. |

---

## Index file

When a request arrives for the context root (`/`), the server looks for a designated index file and streams it.

| Attribute | Tree | Default | Description |
|-----------|------|---------|-------------|
| `dev.getelements.static.index` | Standard | `index.html` | Relative path (within the Standard tree) of the file served at `/`. |
| `dev.getelements.ui.index` | UI | `index.html` | Relative path (within the UI tree) of the file served at `/`. |

If the configured file does not exist in the content tree, root requests return **404** and a warning is logged at
deployment time.

```
# Serve a custom SPA entry point at the root
dev.getelements.static.index = app/index.html
```

---

## Error pages

Custom HTML (or any static file) can be served in place of Jetty's default error responses for specific HTTP status
codes.

```
dev.getelements.static.error.<code> = <relative-path>   # Standard tree
dev.getelements.ui.error.<code>     = <relative-path>   # UI tree
```

The relative path is resolved against each tree's own file index. If the file is not found a warning is logged and
Jetty's built-in error page is used as the fallback.

### Common status codes

| Code | Meaning |
|------|---------|
| `400` | Bad Request |
| `401` | Unauthorized |
| `403` | Forbidden |
| `404` | Not Found |
| `405` | Method Not Allowed |
| `408` | Request Timeout |
| `409` | Conflict |
| `410` | Gone |
| `413` | Content Too Large |
| `414` | URI Too Long |
| `415` | Unsupported Media Type |
| `422` | Unprocessable Content |
| `429` | Too Many Requests |
| `500` | Internal Server Error |
| `501` | Not Implemented |
| `502` | Bad Gateway |
| `503` | Service Unavailable |
| `504` | Gateway Timeout |

### Example

```
# Standard tree: route 404s back to the SPA entry point so the client router handles them
dev.getelements.static.error.404 = index.html
dev.getelements.static.error.500 = errors/500.html

# UI tree: same pattern, independent configuration
dev.getelements.ui.error.404 = index.html
dev.getelements.ui.error.500 = errors/500.html
```

---

## MIME type resolution

MIME types are resolved once at load time (not per-request) in this priority order:

1. A `Content-Type` header set by a matching **static rule** (see below) — wins unconditionally.
2. Jetty's built-in MIME type table (`MimeTypes.DEFAULTS.getMimeByExtension`).
3. Fallback: `application/octet-stream` (a warning is logged).

---

## Static rules

Rules let you attach arbitrary response headers (including `Content-Type`) to files matched by a regex. Rules,
index files, and error pages are **independent per tree** — each tree has its own namespace:

| Tree | Namespace |
|------|-----------|
| Standard (`static/`) | `static` |
| UI (`ui/`) | `ui` |

### Attribute keys

```
dev.getelements.<ns>.rule.<name>.regex
dev.getelements.<ns>.rule.<name>.header.<HeaderName>.value
```

- **`regex`** — a Java regular expression matched against the file's **relative path** within the content tree
  (forward-slash separated, no leading slash, no context-path prefix). Square brackets `[` `]` are automatically
  converted to capture groups `(` `)` so that property-file-safe syntax can be used.
- **`header.<HeaderName>.value`** — the value to set for the response header `HeaderName`. The value may contain
  template variables (see below). Setting `Content-Type` here overrides MIME detection for matched files.

### Rule evaluation order

Rules are sorted **alphabetically by name**. All matching rules are applied in order; if two rules both set the same
header for the same file, the **later rule wins** and a warning is logged at deployment time.

### Template variables

| Variable | Expands to |
|----------|-----------|
| `$filename` | The file's name (last path component, e.g. `app.js`) |
| `$path` | The full relative path (e.g. `assets/js/app.js`) |
| `$[0]` | The entire regex match (group 0) |
| `$[N]` | Capture group *N* (N ≥ 1) |

### Example

```
# Standard tree — cache JS and CSS for one year, never cache HTML
dev.getelements.static.rule.assets.regex                       = .*\.(js|css)$
dev.getelements.static.rule.assets.header.Cache-Control.value  = public, max-age=31536000, immutable

dev.getelements.static.rule.html.regex                         = .*\.html$
dev.getelements.static.rule.html.header.Cache-Control.value    = no-store

# UI tree — independent rules; .wasm files need an explicit content type
dev.getelements.ui.rule.wasm.regex                             = .*\.wasm$
dev.getelements.ui.rule.wasm.header.Content-Type.value         = application/wasm
```

### Warnings emitted at load time

- A rule whose regex never matches any file in the tree → **"Static rule '&lt;name&gt;' matched zero files."**
- Two rules set the same header for the same file → **"Static rule collision: …"**
- A rule attribute is missing its `regex` key → **"Static rule '&lt;name&gt;' has no regex; skipping."**
- A rule's regex is syntactically invalid → **"Static rule '&lt;name&gt;' has invalid regex …"**
- A file has no known MIME type and no rule overrides it → **"No known MIME type for '&lt;path&gt;'; defaulting to application/octet-stream."**

---

## Request handling

The servlet (`StaticContentServlet`) that backs each mounted tree:

- Supports `GET` and `HEAD` only. `POST`, `PUT`, and `DELETE` return **405 Method Not Allowed** (honoring any
  configured 405 error page).
- `OPTIONS` returns `Allow: GET, HEAD, OPTIONS`.
- An exact path lookup is performed against a pre-built in-memory index. Any path not in the index returns **404**
  (honoring any configured 404 error page).
- The context root (`/`) is served by the index file; if none is configured it returns **404**.
- Directory listing is not supported.
- `Content-Length` is set from the actual file size on every response.

All header values, MIME types, the index file, and error pages are resolved **once at deployment time** by
`StaticRuleEngine`; no per-request processing beyond a map lookup is required.