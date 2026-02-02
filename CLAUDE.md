# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Namazu Elements is an open-source backend server for multiplayer games. It is a Java 21 multi-module Maven project (~84 modules) using Google Guice for dependency injection, MongoDB for persistence (via Morphia), and Jetty 12 for serving Jakarta RS REST APIs and WebSockets. The project provides a plugin system ("Elements") that allows game developers to extend the server with custom business logic.

## Build Commands

```bash
# Full build with tests
mvn clean test install

# Fast build (skip tests)
mvn clean install -DskipTests

# Build a single module (from root)
mvn -pl <module-name> -am install

# Run tests for a single module
mvn -pl <module-name> -am test

# Run a specific test class
mvn -pl <module-name> -Dtest=ClassName test

# Run a specific test method
mvn -pl <module-name> -Dtest=ClassName#methodName test
```

Tests use TestNG (not JUnit). The test group is `unit` and test suites match `**/*UnitTestSuite.java`. Mockito 5.13 is used for mocking.

## Local Development

Start MongoDB locally via Docker (runs on standard port 27017):
```bash
cd docker-config/services-dev
docker-compose up --build -d
```

No additional configuration is needed; services default to localhost on standard ports.

## Architecture

### N-Tiered Layer Structure

```
Presentation (rest-api: Jakarta RS endpoints, servlets, filters)
     |
Service Layer (service: business logic with access-level implementations)
     |
DAO Layer (sdk-dao: interfaces, mongo-dao: MongoDB implementation)
```

The presentation layer never accesses the DAO layer directly. All business logic flows through service interfaces.

### Service Access Levels

Service interfaces typically have multiple implementations scoped by access level:
- **Anonymous** - unauthenticated, minimal access
- **User** - normal authenticated user
- **Superuser** - administrator access
- **UNSCOPED** - not tied to a user/profile, for internal or admin operations

### Module Organization Pattern

Most components follow a `{component}` + `{component}-guice` pair pattern. The base module contains interfaces and implementations; the `-guice` module contains the Guice bindings.

### Key Module Groups

**SDK (public API):** `sdk`, `sdk-model`, `sdk-service`, `sdk-dao`, `sdk-spi`, `sdk-guice` - Stable, versioned APIs. Plugins ("Elements") depend only on SDK modules with `<scope>provided</scope>`. The `dev.getelements.elements.sdk` package and subpackages are the public contract.

**Data layer:** `mongo-dao` implements the DAO interfaces from `sdk-dao`. Uses Morphia for object mapping. Models are deliberately kept separate from database entities; mappers convert between them.

**Service layer:** `service` implements business logic from `sdk-service` interfaces.

**REST API:** `rest-api` contains Jakarta RS resource classes. `rest-guice` provides bindings.

**Application entry point:** `jetty-ws` is the main Jetty-based entry point.

**RT system:** The `rt-*` modules are a JeroMQ-based background worker system. Currently disabled (as of Elements 3.0) and undergoing renovation.

**Test modules:** `service-test`, `mongo-test`, `jetty-ws-test`, `sdk-local-test`, `sdk-mongo-test` contain integration tests for their respective layers.

### Root Package

All code lives under `dev.getelements.elements`. SDK public API is under `dev.getelements.elements.sdk`.

## Coding Conventions

- Google Java Style Guide with 120-character line limit
- Interface + implementation pairs everywhere; reference interface types, not implementations
- DAO layer (`sdk-dao`) is 100% interfaces; `mongo-dao` provides the MongoDB implementation. The DAO interfaces must have no knowledge of the database implementation.
- Use `final` wherever possible
- JavaDoc on interface methods, not on implementations
- Favor composition over inheritance
- All database IDs are converted to strings in API models
- Use MapStruct for type conversions between layers
- Database primary keys use `ObjectId` or compound ID types implementing `HexableId`

## MongoDB Conventions

- No unique indexes on large/user-owned collections (to remain shardable)
- "name" fields considered unique are handled via secondary collection with compound ID, or sparse unique index where the "delete" operation clears the name field rather than removing the document
- Bad ID input (e.g., non-numeric string for integer key) is treated as "not found", not as an error

## Version Management

Uses semantic versioning. The `Makefile` provides targets for version operations:
```bash
make patch              # Increment snapshot patch version
make release            # Drop -SNAPSHOT designation
make version VERSION=x.y.z  # Set explicit version
make tag                # Tag current Maven version in git
```
