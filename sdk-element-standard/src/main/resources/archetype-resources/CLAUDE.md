# CLAUDE.md — element-example

This is a reference/example project for **Namazu Elements 3.8**, demonstrating how to build a custom Element using the multi-module Maven structure, REST endpoints, Guice DI, and the `.elm` archive format.

## Project Structure

```
element-example/
├── api/          # Exported interfaces (other Elements depend on this)
├── ui/           # TypeScript/React UI plugin source (Vite; not deployed directly)
├── element/      # Implementation module — builds the .elm archive
├── debug/        # Local development runner (not deployed)
└── services-dev/ # Docker services (MongoDB) for local dev
```

**Module roles:**
- `api` — Pure interfaces + DTOs exported to other Elements via a classified JAR
- `ui` — Vite/TypeScript source for dashboard UI plugins; builds IIFE bundles into `element/src/main/ui/`
- `element` — REST endpoints, services, Guice modules; compiles to `.elm` archive
- `debug` — Local Elements runtime harness; never deployed

## Build & Run

```bash
# Build everything
mvn install

# Start local MongoDB (required for local testing)
docker compose -f services-dev/docker-compose.yml up -d

# Run locally (from project root)
mvn -pl debug exec:java
```

## Key Patterns

### Element Declaration (`package-info.java`)
Every Element package must have a `package-info.java`:
```java
@ElementDefinition(recursive = true)
@GuiceElementModule(MyGameModule.class)
@ElementDependency("dev.getelements.elements.sdk.dao")
@ElementDependency("dev.getelements.elements.sdk.service")
package com.mystudio.mygame;
```

### REST Endpoints (Jakarta RS)
- Annotate endpoint class with `@Path`
- Register all endpoint classes in a `Application` subclass annotated with `@ElementServiceImplementation` + `@ElementServiceExport(Application.class)`
- Services are **not** injected via `@Inject` in JAX-RS endpoints (the container instantiates them, not Guice). Use the service locator instead:

```java
private final Element element = ElementSupplier.getElementLocal(MyEndpoint.class).get();
private final MyService svc = element.getServiceLocator().getInstance(MyService.class);
```

### Guice Module Pattern
Use `PrivateModule` to isolate bindings; expose only what other Elements need:
```java
public class MyModule extends PrivateModule {
    @Override
    protected void configure() {
        bind(MyService.class).to(MyServiceImpl.class);
        expose(MyService.class);
    }
}
```
SDK services (`UserService`, DAOs) are available for `@Inject` because of the `@ElementDependency` declarations in `package-info.java`.

### Service Export (api module)
```java
@ElementServiceExport
public interface MyService { ... }
```
Combined with Guice `expose()`, other Elements can call `serviceLocator.getInstance(MyService.class)`.

### Authentication
- Enable auth filter: `@ElementDefaultAttribute("true")` for `dev.getelements.elements.auth.enabled`
- Mark authenticated endpoints: `@SecurityRequirement(name = AuthSchemes.SESSION_SECRET)`
- Check user level: `User.Level.UNPRIVILEGED` is the sentinel for unauthenticated/guest

### WebSocket
- Annotate class with `@ServerEndpoint("/path")` — auto-discovered
- Use `@OnOpen`, `@OnMessage`, `@OnClose`, `@OnError`
- Get services via `ElementSupplier.getElementLocal()` (same as REST)
- No `Application` subclass needed; `package-info.java` only needs `@ElementDefinition`

## Database Access (Morphia)

For database access, see **[MORPHIA.md](MORPHIA.md)**. It covers `Transaction`, `Datastore`, DAO injection, `@ElementTypeRequest` for classloader visibility, and retry behaviour.

> **Note:** If you are using Morphia directly (custom queries, `@Entity` classes, `Datastore` injection), refer to [MORPHIA.md](MORPHIA.md) before writing any database code.

## Maven Dependency Scopes
- `sdk`, `sdk-local`: `provided` — supplied by the runtime
- `api` module (your own): `provided` in the `element` module
- `sdk-spi` + `sdk-spi-guice`: **bundled** (not provided) — must ship inside the `.elm`
- Use `sdk-logback` (not plain logback) to avoid classpath conflicts at runtime

## Key SDK Types
| Type | Purpose |
|------|---------|
| `ElementSupplier.getElementLocal(Class<?>)` | Get the Element from its own classpath |
| `Element.getServiceLocator()` | Access Guice-managed services |
| `ServiceLocator.getInstance(Class<T>)` | Get an injected instance (throws if missing) |
| `ServiceLocator.findInstance(Class<T>)` | Returns `Optional<Supplier<T>>` |
| `ElementScope` / `element.withScope()` | Thread-local scope with mutable attributes |
| `Element.publish(Event)` | Broadcast events to other Elements |
| `User.Level.UNPRIVILEGED` | Sentinel for unauthenticated/guest users |
| `AuthSchemes.SESSION_SECRET` | Header name for session auth (`"session_secret"`) |

## Dashboard UI Plugins

Elements can inject custom pages into the Elements dashboard by shipping a React component bundle alongside the Java code. The dashboard discovers these bundles at runtime via a `plugin.json` manifest — no dashboard changes required.

### How it works

The manifest and bundle are placed in the Element's UI content directory under a named segment:

```
element/src/main/ui/
  superuser/
    plugin.json        # declares sidebar entry and bundle location
    plugin.bundle.js   # self-contained IIFE bundle (built from ui/ module)
  user/
    plugin.json
    plugin.bundle.js
```

These are packaged into the `.elm` artifact at build time and served under `/app/ui/{element-prefix}/{segment}/`.

### plugin.json

```json
{
  "schema": "1",
  "entries": [
    {
      "label": "Example Element",
      "icon": "Package",
      "bundlePath": "plugin.bundle.js",
      "route": "example-element"
    }
  ]
}
```

| Field | Description |
|---|---|
| `label` | Text shown in the dashboard sidebar |
| `icon` | A [Lucide](https://lucide.dev/icons/) icon name (e.g. `Package`, `Layers`, `Zap`) |
| `bundlePath` | Path to the bundle, relative to the manifest |
| `route` | Unique key used in the dashboard URL (`/plugin/{route}`) |

### Bundle format

The bundle must be an IIFE that registers a React component with the dashboard's plugin registry. The host dashboard exposes `window.React` — the bundle must use this same instance.

```js
(function () {
  var React = window.React;
  function MyPlugin() {
    return React.createElement('div', { className: 'p-6' }, 'My Plugin');
  }
  window.__elementsPlugins && window.__elementsPlugins.register('my-route', MyPlugin);
})();
```

Tailwind utility classes work out of the box — the dashboard stylesheet is already loaded.

### Developing the UI (`ui/` module)

The `ui/` Maven module is a Vite/TypeScript project that builds the IIFE bundles and writes them directly into `element/src/main/ui/{segment}/`.

**One-time setup:**
```bash
cd ui
npm install
```

**Standalone dev server (fast iteration):**
```bash
npm run dev:superuser   # or: npm run dev:user
# Open http://localhost:5173
```

Edit `src/superuser/ExamplePlugin.tsx` and the browser updates instantly.

**Build for integration:**
```bash
npm run build
```

Writes `plugin.bundle.js` into `element/src/main/ui/superuser/` and `element/src/main/ui/user/`. Then restart the debug server to pick up the new bundle.

**Source layout:**
```
ui/src/
  superuser/
    ExamplePlugin.tsx    # edit this — the component shown in the dashboard
    plugin-entry.ts      # registers the component with window.__elementsPlugins
    dev-entry.tsx        # mounts component for standalone dev (not shipped)
    index.html           # dev server entry point (not shipped)
  user/                  # same structure
  shared/                # optional: components shared between segments
```

**CI / Maven build** — activate the `build-ui` profile to run npm via Maven (uses `frontend-maven-plugin`; off by default):
```bash
mvn install -Pbuild-ui
```

### User segmentation

`superuser/` serves components shown to administrators. `user/` serves components in user-facing dashboards (future). Each segment is discovered independently.

## Static & UI Content

- **`element/src/main/static/`** — static files served at `/app/static/{prefix}/`
- **`element/src/main/ui/`** — UI plugin files served at `/app/ui/{prefix}/`

The Maven build (antrun `elm-stage-static-content`) copies both directories into the `.elm` archive automatically. No extra configuration is needed to serve files — just place them in the right source directory.

### Controlling Static Serving with Attributes

The `StaticRuleEngine` reads the following keys from the Element's attributes (namespace is `static` or `ui` depending on the tree):

| Attribute key | Purpose | Default |
|---------------|---------|---------|
| `dev.getelements.static.index` | File served at the context root | `index.html` |
| `dev.getelements.static.rule.<name>.regex` | Regex rule matching file paths | — |
| `dev.getelements.static.rule.<name>.header.<Header>.value` | Response header template for matched files | — |
| `dev.getelements.static.error.<code>` | File served for HTTP error code (e.g. `404`) | — |
| `dev.getelements.ui.index` | Same as above for the `ui` tree | `index.html` |
| `dev.getelements.ui.rule.<name>.regex` | Same as above for the `ui` tree | — |
| `dev.getelements.ui.rule.<name>.header.<Header>.value` | Same as above for the `ui` tree | — |
| `dev.getelements.ui.error.<code>` | Same as above for the `ui` tree | — |
| `dev.getelements.element.static.uri` | Override the full serve URI for standard content | `/app/static/{prefix}` |
| `dev.getelements.element.ui.uri` | Override the full serve URI for UI content | `/app/ui/{prefix}` |

Header value templates support: `$filename`, `$path`, `$[0]` (full match), `$[N]` (capture group N).

**Example** — add a `Cache-Control` header to all `.js` files and define a 404 page:
```properties
dev.getelements.static.rule.scripts.regex=.*\\.js
dev.getelements.static.rule.scripts.header.Cache-Control.value=public, max-age=31536000
dev.getelements.static.error.404=errors/404.html
```

### Embedding Attributes in the ELM

The loader reads `dev.getelements.element.attributes.properties` from the **element root** inside the `.elm` archive (same level as `api/`, `lib/`, `classpath/`, and the manifest).

Place your attributes file at:
```
element/src/main/elm/dev.getelements.element.attributes.properties
```

Then add an antrun copy step to `element/pom.xml` inside the `elm-stage-classpath` execution (or as its own execution in `prepare-package`) to stage it to the element root:

```xml
<copy todir="${elm.element.dir}" failonerror="false">
    <fileset dir="${basedir}/src/main/elm" erroronmissingdir="false" includes="**/*"/>
</copy>
```

This places the file at `<groupId>.<artifactId>/dev.getelements.element.attributes.properties` inside the ZIP, which is the path `DirectoryElementPathLoader` expects.

> `@ElementDefaultAttribute` on static fields in your Java classes provides defaults for any key — the attributes file overrides them at deploy time without recompiling.

## Custom APIs

Custom server-side logic is exposed via two Jakarta EE APIs:

- **REST APIs** — implemented with [Jakarta RESTful Web Services](https://jakarta.ee/specifications/restful-ws/4.0/) (Jakarta RS / JAX-RS)
- **WebSockets** — implemented with [Jakarta WebSocket](https://jakarta.ee/specifications/websocket/2.1/)

## Elements REST API (OpenAPI)

The full Elements platform REST API is available as an OpenAPI spec at:

```
http://localhost:8080/api/rest/openapi.json
```

> **Note:** If this URL returns 404 or is unreachable, the local Elements instance is not running. Run the debug script first (`mvn -pl debug exec:java`) to bring the instance online, then retry.

## Namazu Elements Core REST API (Source Reference)

When building an Element, browse `dev.getelements.elements.rest` and its subpackages to discover available platform services, request/response shapes, and auth patterns. Source is in the local Maven repository under `~/.m2/repository/dev/getelements/elements/`.

Key subpackages and their domains:

| Subpackage | Domain |
|---|------|
| `dev.getelements.elements.rest.user` | User CRUD, password management |
| `dev.getelements.elements.rest.security` | Sessions, username/password auth |
| `dev.getelements.elements.rest.auth` | OAuth2, OIDC, custom auth schemes |
| `dev.getelements.elements.rest.profile` | User profiles |
| `dev.getelements.elements.rest.leaderboard` | Leaderboards, ranks, scores |
| `dev.getelements.elements.rest.mission` | Missions, progress, rewards, schedules |
| `dev.getelements.elements.rest.inventory` | Simple, advanced, and distinct inventory |
| `dev.getelements.elements.rest.goods` | Shop items |
| `dev.getelements.elements.rest.savedata` | Player save data |
| `dev.getelements.elements.rest.largeobject` | File/blob uploads |
| `dev.getelements.elements.rest.matchmaking` | Match management |
| `dev.getelements.elements.rest.friends` | Friends and followers |
| `dev.getelements.elements.rest.metadata` | Generic metadata and schemas |
| `dev.getelements.elements.rest.blockchain` | Wallets, vaults, smart contracts |
| `dev.getelements.elements.rest.notifications` | Push notification registration |
| `dev.getelements.elements.rest.element` | Element deployment and status |
| `dev.getelements.elements.rest.application` | Application and platform config |

Resource classes in these packages use setter-based `@Inject` (Jersey instantiates them, Guice bridges in). Study them to understand available services and how to call them from your own Element code.

## Package Layout Convention
```
com.mystudio.mygame/
  ├── rest/           REST endpoints
  ├── service/        Business logic
  ├── model/          Request/response DTOs
  ├── guice/          Guice modules
  └── package-info.java
```
