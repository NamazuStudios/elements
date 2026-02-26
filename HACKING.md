# Hacking Elements

This document is for developers who want to work on the Elements core codebase itself. If you're building a plugin or add-on, see the [custom code guide](https://namazustudios.com/docs/custom-code/element-structure/) instead.

---

## License and contributor agreement

Elements is licensed under **AGPLv3**. All contributions submitted via pull request must remain open and available to the community under the same license and are considered derivative works.

Before we can accept a pull request, all contributors must sign a **Contributor License Agreement**. Please fill it out and [submit it via this form](https://share.hsforms.com/2-N76EKCiRBaYb1w2UwtxyAckwr1) before submitting your first PR.

We gladly accept:

- Bug fixes
- Feature improvements
- Documentation updates

If you are building plugins using only the Elements SDK (`dev.getelements.elements.sdk` and subpackages), your plugin code does not need to be open-sourced. This exemption is built into the license.

---

## Prerequisites

The following tools are necessary to build Elements from source.

- [Maven 3.5+](https://maven.apache.org/)
- [Docker (latest)](https://www.docker.com/products/docker-engine)
- [git](https://git-scm.com/)
- [Node.js / npm](https://nodejs.org/en/download/)
- **OpenJDK 21** — Elements is written for Java 21. Make sure your `JAVA_HOME` points to a JDK 21 installation.

All dependencies are fetched via Maven. The first build may take a considerable amount of time as Maven downloads the full dependency tree.

The web UI is built with React and uses npm for dependency management. Its Maven build spec is wired into the master Maven build, so running a top-level `mvn install` will handle the frontend build automatically.

---

## Building

**Step 1: Always run an initial build skipping tests first.**

```bash
mvn -Darchetype.test.skip=true -DskipTests --no-transfer-progress -q install
```

This is required on the first pass. Skipping tests on the initial build avoids failures caused by services not yet being fully initialized.

**Step 2: Start local services.**

All dependent services run inside Docker. This gives you a fresh, clean environment to develop against without needing a shared or central service. Run this in a separate terminal as it will remain attached:

```bash
cd docker-config/services-dev
docker-compose up --build -d
```

This starts a MongoDB instance on standard ports. Running any of the services should, by default, look for connections on those ports on localhost. No additional configuration should be necessary beyond running the application in your IDE.

**Running the full test suite:**

```bash
mvn install
```

> ⏱ Tests take approximately 30 minutes to complete. We recommend only running them if you are planning to actively contribute changes to the codebase.

---

## Developing against a bleeding-edge build

If you need to develop a plugin or Element against unreleased changes — for example, to use a feature not yet published to Maven Central — you can install the full Elements stack into your local Maven repository directly from source:

```bash
mvn -DskipTests clean install
```

This builds and installs all modules into `~/.m2`, making them available to any local Maven project as if they were a published release. You can then declare the SDK dependencies in your Element's `pom.xml` with `<scope>provided</scope>` as normal, and Maven will resolve them from your local repository.

This is also the recommended approach when using `sdk-local` or `sdk-local-maven` to run an Element locally — the local runner will pick up the version of Elements you built from scratch rather than a published artifact.

---

## Module synopsis

The following modules comprise the Elements system, handling enhanced features such as multi-tenancy support, admin tools, and deployment utilities.

### SDK libraries

All libraries prefixed with `sdk` represent the public API for Elements. We do everything we can to keep implementation details out of the SDK. Unlike internal libraries, these types go through a formal deprecation process and follow semantic versioning — interfaces will not break between minor revisions. When writing code against the Elements API, focus on the interfaces.

When developing an Element, SDK packages must be included with **`provided`** scope.

- **sdk** — the base SDK library. Minimal dependencies. Contains the core interfaces, annotations, and records of the Elements SDK. By design, this depends only on the core Java SDK.
- **sdk-bom** — Maven Bill of Materials defining consistent dependency versions for all SDK modules.
- **sdk-cluster** — cluster-aware asset loading interfaces for distributed deployments.
- **sdk-dao** — a set of DAO (Data Access Object) interfaces.
- **sdk-deployment** — interfaces for element runtime and container services, including `ElementRuntimeService` and `ElementContainerService`.
- **sdk-element-standard** — a Maven archetype for scaffolding new Element projects with standard structure and example code.
- **sdk-guice** — Guice-specific configuration for the SDK.
- **sdk-jakarta-rs** — Jakarta RS utilities including `MethodOverrideFilter` and authentication scheme helpers.
- **sdk-local** — the local SDK runner. Provides a self-contained Elements instance for local development and testing.
- **sdk-local-maven** — Maven-based specialization of the local SDK that performs Maven-specific setup steps for local development.
- **sdk-logback** — Logback/SLF4J logging configuration for Elements applications.
- **sdk-model** — model types for the SDK, making up the core of the Elements business logic.
- **sdk-mongo** — MongoDB client configuration and TLS support for Elements applications.
- **sdk-service** — interfaces for the core business logic of Elements.
- **sdk-spi** — the SPI (Service Provider Implementation) for the core SDK.
- **sdk-spi-guice** — Guice integration for the Element SPI, providing `GuiceElementLoader` and SPI-aware module binding.
- **sdk-spi-shrinkwrap** — ShrinkWrap Maven resolver integration for dynamic element artifact loading in tests and local environments.
- **sdk-test** — a base library for testing the core SDK. Plugin developers should not need this artifact, but may refer to it to understand Element structure.
- **sdk-test-api** — a `TestService` interface for element integration testing, produced as a classified JAR for test consumption.
- **sdk-test-element** — a simple Element serving as a base test case. Provides a test interface for understanding Element structure.
- **sdk-test-element-a** — the "Alpha" variant of the test Element.
- **sdk-test-element-b** — the "Beta" variant of the test Element.
- **sdk-test-element-rs** — an Element which exposes Jakarta RS web services.
- **sdk-test-element-ws** — an Element which exposes Jakarta WebSockets.
- **sdk-util** — utility module with string algorithms such as Levenshtein distance and Metaphone for common SDK tasks.

### Internal libraries

The following are internal libraries that make up the implementation of Elements.

#### Current packages

- **cdn-serve** — servlets supporting static content delivery.
- **cdn-serve-guice** — Guice configuration for CDN services.
- **code-serve-guice** — Guice bindings specific to code-serve.
- **common-git** — a common library for processing git repositories.
- **common-jetty** — common Jetty types and implementations.
- **common-mapstruct** — common MapStruct types and implementations.
- **common-servlet** — common servlet types and implementations.
- **common-servlet-guice** — common servlet Guice configuration.
- **common-util** — generic utility code and common logical operations.
- **deployment-jetty** — Jetty-based element deployment with servlet and WebSocket support, element loading, and Swagger documentation integration.
- **doc-serve-jetty** — standalone Jetty module for serving OpenAPI/Swagger documentation.
- **docker-config** — Dockerfile definitions.
- **guice** — a common set of Guice modules.
- **jetty-ws** — the main entry point for Elements, based on Jetty.
- **jetty-ws-test** — a separate test suite for the Jetty-based APIs.
- **mongo-dao** — the MongoDB DAO implementation.
- **mongo-guice** — MongoDB Guice configuration.
- **mongo-test** — MongoDB DAO test suite.
- **rest-api** — the core Jakarta RS-based API.
- **rest-guice** — Guice configuration for the REST API.
- **rpc-api** — JSON-RPC API implementation providing HTTP redirection strategies and model manifest resources.
- **rpc-api-guice** — Guice bindings for the RPC API.
- **rpc-api-jetty** — Jetty-based RPC server integrating the RPC API with servlet infrastructure.
- **service** — the service layer (business logic) implementation.
- **service-test** — a test suite for the service layer.
- **service-guice** — Guice bindings for the service layer.
- **setup** — a standalone jar for first-time database setup and initial user creation. Will eventually support disaster recovery and other low-level administrative functions that can't or shouldn't be handled by the web UI.
- **web-ui-react-jetty** — the source for the Elements CMS user interface, built with React.

#### Deprecated packages

The following are deprecated and slated for removal:

- **app-node**

### RT system

The RT sub-project ("Real Time") is a background worker application for long-running workloads dispatched over the network. It makes up the backend communication system of Elements and is based on JeroMQ. We have plans to decomission these parts of the codebase and replace with a more robust system. These components have been slowly dismantled over the 3.x releases and will be replaced with an improved system.

---

## Coding standards and practices

The project follows [Google Java Standards](https://google.github.io/styleguide/javaguide.html) and [Literate Programming](https://en.wikipedia.org/wiki/Literate_programming) principles.

If you observe a deviation from these standards during development, use your best judgement:

- If the change is simple and non-breaking, incorporate it into your existing work and note the reason in the git commit.
- If it requires refactoring, create a bug ticket for the technical debt so it can be addressed separately.

**We will not accept pull requests which do not follow these standards.**

### Formatting, syntax, and structure

Notable divergences from standard Java style:

- Column limit is 120 characters. Going slightly beyond is acceptable where it makes sense.
- Isolate as much as possible and avoid classpath pollution. For example, `dao` is 100% interfaces and `mongo-dao` implements those interfaces. The DAO layer should have absolutely no knowledge of the implementation layer.
- There should almost always be an interface type. Where it is used, reference only the interface type.
- Use `final` wherever possible and where it makes sense.
- Add JavaDoc tags to interface methods describing what they do. Implementations typically do not need JavaDoc because they are defined by the interface.
- Favor composition over inheritance. Inheritance is used sparingly in this codebase.
- Avoid singletons and shared state everywhere possible. The only universal exception is logging.

---

## Database standards

Elements currently supports MongoDB only. We can reevaluate this if there is strong community demand for additional database support.

Models in Elements are deliberately "clean" — not directly tied to the database layer. When converting types to and from the database, we use a mapper and treat the data as plain old data.

**We will not accept pull requests which do not follow these standards.**

### General rules

- Convert all database IDs to strings in the API model. Rarely do we want to lock in the data type of a unique ID.
- When checking for an ID's presence, treat a bad ID as a "not found" scenario. For example, passing the string `"foo"` where an integer primary key is expected should not throw a `NumberFormatException` — it should be handled as a missing record.

### MongoDB

- No unique indexes on large collections, to keep them shardable.
- `name` is considered unique across a data type, with special rules:
    - If avoiding a unique index: use a secondary collection and compound ID to uniquely name the document.
    - If using a unique index: `name` must be a sparse index permitting multiple `null` values. Deleting the object must simply clear the `name` field. Examples: Applications, Leaderboards.
- Use `ObjectId` for primary keys or compound ID types. Do not use integers, strings, or other primitive types as a database key unless they are part of a compound ID scheme.
- All IDs should implement `HexableId` to easily convert to and from hex strings.

---

## Architectural standards

### N-tiered architecture

Elements follows N-tiered architecture with approximately three layers per component. Headless services (such as the scripting engine) may lack a presentation layer but have access to all APIs in the system.

#### Presentation layer

Typically Jakarta RS annotated code, servlet code, and filtering code. Responsible only for dealing with data presented by the client.

- Deals in DTO models (request/response objects).
- Performs minimal data validation.
- Defers business logic decisions to the service layer.
- Never deals directly with the DAO/database layer.

#### Service layer

Implements business logic and typically provides multiple implementations per service interface, at one of three access levels:

- **Anonymous** — used when the user can't be identified. Grants almost no access.
- **User** — used by a normal user or member of the general public.
- **Superuser** — used by internal administrators.

Services annotated with the `UNSCOPED` name have superuser privilege (but not necessarily) and are not tied to any particular user or profile.

#### Database layer

See the database standards section above.

---

## Release process

Elements follows [Semantic Versioning](https://semver.org/):

- **Major release** — breaking API changes (REST API or scripting engine).
- **Minor release** — non-breaking API changes that enhance the existing feature set.
- **Patch release** — non-breaking, non-enhancing changes (bug fixes only).

Elements is actively **migrating its build pipeline from Bitbucket Pipelines to GitHub Actions**. If you are interested in helping with this migration, it would be one of the most impactful contributions you could make — see the [Contributing section of the README](./README.md#contributing).

### Branch strategy

- **Main branch** — represents the next version of Elements. Always leads the release branches by a major or minor version.
- **Development branches (`/development/*`)** — used when preparing a release. Pull requests for the current version land here.
- **Release branches (`/release/*`)** — not meant for direct commits. Merging to a release branch triggers a distribution build, drops the `-SNAPSHOT` designation, creates a tag, and triggers a formal release build.
- **Other branches** (bugfix, feature) — will build and, if passing, receive tags in the Docker registry.

### Docker tagging scheme

Every successful build generates Docker images tagged with:

- The long-form git commit hash
- The short-form git commit hash
- The current git tag (if available)

---

## Contributors and credits

The following people have been instrumental in the development of Elements. We intend to credit any contributor who wishes to be listed here, no matter how big or small the contribution.

- Patrick Twohig — [**@ptwohig**](https://github.com/ptwohig)
- Keith Hudnall — [**@krh372**](https://github.com/krh372)
- Garrett McSpadden — [**@EmissaryEntertainment**](https://github.com/EmissaryEntertainment)
- rcornwal — [**@rcornwal**](https://github.com/rcornwal)
- Maxwell Montes Diaz — [**@Mascaz**](https://github.com/Mascaz)
- Chris Uribe — [**@chrisuribe**](https://github.com/chrisuribe)

If you want to see your name here, clone the repository and get started.

---

## Getting help

- 💬 [Discord](https://discord.gg/n4ZeG7g6)
- 📖 [Documentation](https://namazustudios.com/docs/)
- 📧 [info@namazustudios.com](mailto:info@namazustudios.com)