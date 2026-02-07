# Element Philosophy: The Namazu Elements Plugin System

## Table of Contents

1. [Introduction](#1-introduction)
2. [The Philosophy: Why Elements?](#2-the-philosophy-why-elements)
3. [Core Concepts](#3-core-concepts)
4. [Exposing REST and WebSocket Endpoints](#4-exposing-rest-and-websocket-endpoints)
5. [ElementRegistry: Your Window to the System](#5-elementregistry-your-window-to-the-system)
6. [Scope and Attributes](#6-scope-and-attributes)
7. [Practical Guide: Building Your First Element](#7-practical-guide-building-your-first-element)
8. [Advanced Patterns](#8-advanced-patterns)
9. [Best Practices](#9-best-practices)
10. [Reference Summary](#10-reference-summary)

---

## 1. Introduction

### What is Namazu Elements?

Namazu Elements is an open-source multiplayer game backend server with a powerful plugin architecture. At its core, it provides a **plugin system called "Elements"** that allows game developers to extend the server with custom business logic, REST APIs, WebSocket endpoints, and game-specific features—all without modifying the core server code.

Think of Elements as self-contained modules of functionality that can be:
- **Loaded dynamically** at runtime
- **Isolated** from each other to prevent conflicts
- **Composed** together to build complex game backends
- **Deployed independently** without restarting the entire server

### What Problem Does the Plugin Architecture Solve?

Traditional monolithic game servers face several challenges:

1. **Dependency Hell**: Different features need different versions of the same library
2. **Deployment Coupling**: Changing one feature requires rebuilding and redeploying everything
3. **Version Lock-in**: All code must run on the same Java version
4. **Team Scaling**: Multiple teams can't work independently on separate features
5. **Testing Complexity**: Testing one feature often requires the entire application

The Elements plugin architecture solves these problems by providing **isolated, composable units of functionality** that can be developed, tested, and deployed independently.

### Who Should Read This Document?

This document is written for two primary audiences:

1. **Backend Engineers** evaluating whether Namazu Elements is the right platform for their game
2. **Game Developers** building Elements to add custom functionality to their game servers

**No prior Java expertise is required.** We explain all Java-specific concepts (ClassLoaders, annotations, dependency injection, etc.) as we go.

### Overview of Key Concepts

Before diving deep, here are the fundamental concepts you'll encounter:

- **Element**: A plugin module containing game logic, services, REST endpoints, WebSocket handlers, or event consumers
- **Service**: A Java interface that defines business functionality (e.g., `ItemService`, `PlayerService`)
- **Registry**: The central hub that manages all loaded Elements and provides access to them
- **Events**: A pub/sub system for decoupled communication between Elements
- **Scope**: Thread-local context that tracks which Elements are active during a request
- **Attributes**: Configuration key-value pairs that customize Element behavior
- **ClassLoader**: Java mechanism for isolating Element code and dependencies

---

## 2. The Philosophy: Why Elements?

### Traditional Monolithic Backend Challenges

Traditional game backends are typically built as monolithic applications where all code lives in a single deployment unit. While this works for small projects, it creates significant problems as games grow:

#### Challenge 1: Dependency Conflicts

Imagine your game has two features:
- A chat system using `netty-4.1.50`
- An analytics module using `netty-5.0.0`

In a traditional monolith, you can only have **one version** of Netty on the classpath. Someone has to:
- Upgrade both features to the same version (risky, time-consuming)
- Fork and modify one library (maintenance nightmare)
- Compromise on features by using an older or incompatible version

#### Challenge 2: Deployment Coupling

When your game has:
- Core login and matchmaking (stable, rarely changes)
- New limited-time event (changes daily during the event)
- Experimental social features (deployed multiple times per day)

Every change—even a tiny tweak to the event—requires:
1. Rebuilding the entire application
2. Running the full test suite
3. Redeploying everything
4. Restarting all game servers
5. Risk of breaking stable features

#### Challenge 3: Version Lock-in

Your game might need:
- Legacy payment processing code that only works on Java 8
- New AI features that require Java 21 features
- Third-party SDKs compiled for different Java versions

In a monolith, everything must run on the **same JVM version**, forcing painful migrations or holding back innovation.

#### Challenge 4: Team Scaling

When multiple teams work on the same monolith:
- Merge conflicts become frequent
- Teams block each other on deployments
- Shared code ownership creates bottlenecks
- Testing environments conflict

#### Challenge 5: Static State Pollution

Java classes have static fields (global variables). In a monolith, if two parts of your code use the same library:

```java
// Some library you depend on
public class Cache {
    private static Map<String, Object> data = new HashMap<>();
}
```

Both parts of your code share the **same static state**, causing:
- Unexpected interactions between unrelated features
- Test pollution (one test affects another)
- Memory leaks when features are "disabled" but code remains loaded

### The Element Architecture Solution

Namazu Elements solves these problems through **isolated, composable plugins**:

#### Solution 1: ClassLoader Isolation

Each Element can have its own **ClassLoader**—a Java mechanism that isolates code and dependencies.

**What's a ClassLoader?** Think of it as a "code container." When Java loads a class, it asks a ClassLoader to find and load it. By giving each Element its own ClassLoader:
- Element A can use `netty-4.1.50`
- Element B can use `netty-5.0.0`
- They never conflict because they're loaded in separate containers

Even static state is isolated:
- Element A's `Cache.data` is separate from Element B's `Cache.data`
- They're technically different classes (loaded by different ClassLoaders)

#### Solution 2: Independent Deployment

Elements can be:
- **Loaded** at server startup from directories or `.elm` files
- **Reloaded** dynamically without restarting the server
- **Deployed independently** without affecting other Elements

Update your limited-time event Element multiple times per day without touching core systems.

#### Solution 3: Flexible Dependencies

Elements declare dependencies on each other:
- Element B depends on Element A's services
- Elements form a dependency graph
- The loader resolves dependencies and loads Elements in the correct order

Need legacy Java 8 code? Put it in an isolated Element. Want cutting-edge Java 21 features? Put them in a different Element.

#### Solution 4: Service-Oriented Communication

Elements expose **Services**—Java interfaces defining business functionality:

```java
public interface ItemService {
    Item getItem(String itemId);
    List<Item> getPlayerItems(String playerId);
}
```

Other Elements depend on the **interface**, not the implementation. This provides:
- **Loose coupling**: Elements don't need to know implementation details
- **Testability**: Mock the interface in tests
- **Replaceability**: Swap implementations without changing dependents

**What's a Java Interface?** It's a contract—a set of method signatures without implementations. Any class can "implement" the interface by providing the actual code for those methods.

#### Solution 5: Event-Driven Architecture

Elements communicate through an **event system**:
- Element A publishes a `PlayerLeveledUp` event
- Elements B, C, and D consume that event
- No direct dependencies between publisher and consumers

This enables:
- **Decoupled features**: Add new event consumers without modifying publishers
- **Flexible composition**: Mix and match Elements without code changes
- **Dynamic behavior**: Events discovered at runtime

### Comparison to Other Systems

#### vs. Java Modules (JPMS)

Java 9+ introduced the Java Platform Module System (JPMS):
- **Compile-time isolation**: Modules are checked at build time
- **Static dependencies**: Module relationships are fixed
- **No version isolation**: Still one version of each module per JVM
- **Breaks open source**: JPMS's strict encapsulation breaks many existing open-source libraries that rely on reflection or internal APIs, forcing painful migrations

Elements provide:
- **Runtime isolation**: Load/unload at runtime
- **Dynamic dependencies**: Discovered during loading
- **Version isolation**: Multiple versions can coexist
- **Compatible**: Works with existing Java libraries without modification

#### vs. OSGi

OSGi is a mature Java plugin framework:
- **Complex**: Steep learning curve with heavy runtime overhead
- **Prescriptive**: Enforces strict modularity rules
- **Bundle management**: Complex lifecycle management

Elements provide:
- **Simplicity**: Minimal API surface, easy to understand
- **Pragmatic**: Isolation with escape hatches when needed
- **Lightweight**: Simple ClassLoader-based isolation

#### vs. Microservices

Microservices decompose applications into separate processes:
- **Network overhead**: Every call crosses process boundaries
- **Operational complexity**: Multiple deployments, logs, monitoring
- **Hard isolation**: No shared memory, no escape hatches

Elements provide:
- **In-process**: No network calls, direct method invocation
- **Single deployment**: One server, simpler operations
- **Soft isolation**: Isolated by default, but can share when needed

### Key Design Principles

#### 1. Isolation by Default

All user-created Elements run with **ISOLATED_CLASSPATH**—full ClassLoader isolation providing:
- Complete dependency isolation
- Separate static state
- Independent versions of libraries
- No configuration needed—it's automatic

System-provided Elements (part of Namazu Elements itself) use **SHARED_CLASSPATH** for efficiency, but this is not configurable by users. Your Elements always get isolation.

#### 2. Isolation with Escape Hatches

While Elements are isolated by default, pragmatism wins over dogmatism:
- Mark classes as `@ElementPublic` to share them across Elements
- Access the parent registry for cross-element communication
- Use events for decoupled communication

You get isolation when you need it, and escape hatches when you don't.

#### 3. Convention Over Configuration

Elements use **annotations** (metadata on Java classes) to declare:
- Element definitions
- Service exports
- Event consumers
- Default attributes

**What's an annotation?** It's metadata you attach to Java code:

```java
@ElementDefinition(name = "my-game-logic")
package com.example.game;
```

The `@ElementDefinition` annotation tells the Element loader: "This package is an Element."

Annotations reduce boilerplate—no XML configuration files needed.

#### 4. Runtime Dynamism

Elements are discovered and loaded at runtime:
- Scan directories for Element packages
- Recursively discover services, endpoints, and event consumers
- Build dependency graphs dynamically
- Support hot-reloading (future feature)

#### 5. Dependency Injection via Service Providers

Namazu Elements currently uses **Google Guice** for dependency injection, providing:
- Automatic service wiring
- Scope management
- Interface-based programming

The architecture supports alternative Service Provider Implementations (SPI), allowing future integration with other DI frameworks if needed.

#### 6. Security Through Visibility

By default, Element code is **private**—only visible within that Element. To share code, you must explicitly mark it:
- `@ElementPublic`: Visible to all Elements
- `@ElementPrivate`: Visible only within this Element (default)
- `@ElementLocal`: Visible within this Element and its dependencies

This "secure by default" approach prevents accidental coupling.

---

## 3. Core Concepts

### 3.1 What is an Element?

An **Element** is a self-contained plugin module for Namazu Elements. It's represented in code by the `Element` interface:

```java
public interface Element {
    String name();
    String version();
    ElementType type();
    ClassLoader classLoader();
    void close();
}
```

Think of an Element as a box containing:
- **Java classes** (your game logic)
- **Dependencies** (JAR files)
- **Services** (business functionality interfaces)
- **REST endpoints** (HTTP APIs)
- **WebSocket handlers** (real-time communication)
- **Event consumers** (reactions to game events)
- **Configuration** (attributes)

#### Element Types

Elements run in two different ClassLoader configurations:

**1. ISOLATED_CLASSPATH (User Elements)**

All user-created Elements run with their own isolated ClassLoader, providing:
- Complete dependency isolation
- Separate static state
- Independent versions of libraries
- No configuration required—this is automatic for all user Elements

**What this means for you:** Every Element you create gets its own isolated environment. You don't need to configure this—it's the default and only option for user Elements.

**2. SHARED_CLASSPATH (System Elements Only)**

Namazu Elements' built-in system Elements use a shared ClassLoader:
- Lightweight (no new ClassLoader overhead)
- Share core dependencies with the server
- Shares static state

**Note:** This is only used by system-provided Elements. User Elements always run in isolated ClassLoaders to ensure maximum safety and isolation.

#### Element Lifecycle

Elements progress through several stages:

```
1. Definition     -> Element metadata declared in package-info.java
2. Discovery      -> Loader scans directories/files for Elements
3. Loading        -> ClassLoaders created, classes loaded, dependencies resolved
4. Registration   -> Element registered in ElementRegistry
5. Runtime        -> Services exposed, endpoints mounted, events dispatched
6. Shutdown       -> Element.close() called, resources cleaned up
```

#### Element Packaging

Elements are deployed as:
- **Directories**: Exploded structure with classes and dependencies
- **.elm files**: ZIP archives containing the Element

See `ELEMENT_ANATOMY.md` for detailed packaging structure.

#### Minimal Element Example

Here's the simplest possible Element:

**File:** `src/main/java/com/example/minimal/package-info.java`

```java
@ElementDefinition(name = "minimal-element")
package com.example.minimal;

import dev.getelements.elements.sdk.annotation.ElementDefinition;
```

That's it! This declares an Element named `minimal-element`. When loaded, it will:
- Get its own isolated ClassLoader (automatic for all user Elements)
- Be registered in the ElementRegistry
- Be available for dependency injection

Of course, an Element that does nothing isn't very useful. Let's add functionality.

### 3.2 Services and Dependency Injection

#### What are Element Services?

A **Service** is a Java interface that defines business functionality. For example, an inventory system might expose:

```java
public interface ItemService {
    Item getItem(String itemId);
    List<Item> getPlayerItems(String playerId);
    void grantItem(String playerId, String itemId, int quantity);
    void consumeItem(String playerId, String itemId, int quantity);
}
```

Services provide:
- **Abstraction**: Consumers depend on the interface, not the implementation
- **Replaceability**: Swap implementations without changing consumers
- **Testability**: Mock the interface in tests
- **Discoverability**: Services are registered and can be looked up

#### Dependency Injection (Brief Intro)

**What's dependency injection?** It's a pattern where objects receive their dependencies from an external source rather than creating them.

Without DI:
```java
public class GameLogic {
    private ItemService itemService = new ItemServiceImpl(); // Tight coupling
}
```

With DI:
```java
public class GameLogic {
    private final ItemService itemService;

    @Inject
    public GameLogic(ItemService itemService) { // Dependency injected
        this.itemService = itemService;
    }
}
```

Namazu Elements uses **Google Guice** for dependency injection. The `@Inject` annotation tells Guice: "Inject an ItemService implementation here."

#### Exporting Services from Elements

Elements export services using two annotations:

**1. @ElementServiceExport** on the interface

```java
@ElementServiceExport
public interface PlayerStatsService {
    PlayerStats getStats(String playerId);
    void updateStats(String playerId, StatUpdate update);
}
```

This tells the Element system: "This service interface can be exported by Elements."

**2. @ElementServiceImplementation** on the implementing class

```java
@ElementServiceImplementation
public class PlayerStatsServiceImpl implements PlayerStatsService {

    @Override
    public PlayerStats getStats(String playerId) {
        // Implementation here
        return loadStatsFromDatabase(playerId);
    }

    @Override
    public void updateStats(String playerId, StatUpdate update) {
        // Implementation here
        saveStatsToDa(playerId, update);
    }
}
```

This tells the Element system: "This class implements an exported service."

When the Element loads, the system:
1. Scans for classes with `@ElementServiceImplementation`
2. Finds the interface they implement
3. Verifies the interface has `@ElementServiceExport`
4. Registers the service in the ElementRegistry
5. Makes it available for dependency injection

#### Access Level Scoping

Services often have multiple implementations based on **access level**:

- **Anonymous**: Unauthenticated users (limited access)
- **User**: Authenticated players (normal access)
- **Superuser**: Administrators (full access)
- **UNSCOPED**: Internal services not tied to a user

Example from the Namazu Elements SDK:

```java
@ElementServiceExport
public interface ItemService {
    // User implementation: can only access own items
    // Admin implementation: can access any player's items

    List<Item> getPlayerItems(String playerId);
}
```

You specify the access level when obtaining the service:

```java
// Get the "User" scoped version
ItemService userService = serviceLocator.getService(ItemService.class, "User");

// Get the "Superuser" scoped version
ItemService adminService = serviceLocator.getService(ItemService.class, "Superuser");
```

The Namazu Elements core provides these scoped implementations. When building Elements, you'll typically:
- Consume core services (ItemService, PlayerService, etc.)
- Export your own game-specific services

#### ServiceLocator Pattern

The **ServiceLocator** is how you obtain service instances:

```java
public interface ServiceLocator {
    <T> T getService(Class<T> serviceClass, String scope);
    <T> List<T> getServices(Class<T> serviceClass, String scope);
}
```

Example usage:

```java
@Inject
public class GameLogic {
    private final ItemService itemService;

    @Inject
    public GameLogic(ServiceLocator serviceLocator) {
        this.itemService = serviceLocator.getService(ItemService.class, "User");
    }
}
```

However, you'll rarely use ServiceLocator directly. Guice can inject services automatically:

```java
@Inject
public class GameLogic {

    @Inject
    public GameLogic(@Named("User") ItemService itemService) {
        // Guice injects the User-scoped ItemService
    }
}
```

#### Complete Working Example: Custom Game Service

Let's build a simple achievement service as an Element.

**Step 1: Define the service interface**

**File:** `src/main/java/com/example/achievements/AchievementService.java`

```java
package com.example.achievements;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import java.util.List;

@ElementServiceExport
public interface AchievementService {
    List<Achievement> getPlayerAchievements(String playerId);
    boolean unlockAchievement(String playerId, String achievementId);
    boolean hasAchievement(String playerId, String achievementId);
}
```

**Step 2: Define the data model**

**File:** `src/main/java/com/example/achievements/Achievement.java`

```java
package com.example.achievements;

public class Achievement {
    private final String id;
    private final String name;
    private final String description;
    private final boolean unlocked;

    public Achievement(String id, String name, String description, boolean unlocked) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.unlocked = unlocked;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isUnlocked() { return unlocked; }
}
```

**Step 3: Implement the service**

**File:** `src/main/java/com/example/achievements/AchievementServiceImpl.java`

```java
package com.example.achievements;

import dev.getelements.elements.sdk.annotation.ElementServiceImplementation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ElementServiceImplementation
public class AchievementServiceImpl implements AchievementService {

    // In-memory storage (in production, use a database)
    private final Map<String, Set<String>> playerAchievements = new ConcurrentHashMap<>();

    // Pre-defined achievements
    private static final Map<String, Achievement> ALL_ACHIEVEMENTS = Map.of(
        "first-kill", new Achievement("first-kill", "First Blood", "Defeat your first enemy", false),
        "level-10", new Achievement("level-10", "Veteran", "Reach level 10", false),
        "collect-100", new Achievement("collect-100", "Collector", "Collect 100 items", false)
    );

    @Override
    public List<Achievement> getPlayerAchievements(String playerId) {
        Set<String> unlockedIds = playerAchievements.getOrDefault(playerId, Set.of());

        return ALL_ACHIEVEMENTS.values().stream()
            .map(ach -> new Achievement(
                ach.getId(),
                ach.getName(),
                ach.getDescription(),
                unlockedIds.contains(ach.getId())
            ))
            .toList();
    }

    @Override
    public boolean unlockAchievement(String playerId, String achievementId) {
        if (!ALL_ACHIEVEMENTS.containsKey(achievementId)) {
            return false; // Invalid achievement
        }

        playerAchievements
            .computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet())
            .add(achievementId);

        return true;
    }

    @Override
    public boolean hasAchievement(String playerId, String achievementId) {
        return playerAchievements
            .getOrDefault(playerId, Set.of())
            .contains(achievementId);
    }
}
```

**Step 4: Declare the Element**

**File:** `src/main/java/com/example/achievements/package-info.java`

```java
@ElementDefinition(name = "achievement-system")
package com.example.achievements;

import dev.getelements.elements.sdk.annotation.ElementDefinition;
```

**That's it!** When this Element loads:
1. The system scans the package
2. Finds `AchievementServiceImpl` with `@ElementServiceImplementation`
3. Finds it implements `AchievementService` with `@ElementServiceExport`
4. Registers the service in the ElementRegistry
5. Other Elements can now inject and use `AchievementService`

**Using the service from another Element:**

```java
@Inject
public class GameEventHandler {
    private final AchievementService achievementService;

    @Inject
    public GameEventHandler(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    public void onEnemyKilled(String playerId) {
        if (!achievementService.hasAchievement(playerId, "first-kill")) {
            achievementService.unlockAchievement(playerId, "first-kill");
            System.out.println("Achievement unlocked: First Blood!");
        }
    }
}
```

### 3.3 Element Dependencies

Elements can depend on other Elements, forming a dependency graph.

#### Declaring Dependencies

Use the `@ElementDependency` annotation:

**File:** `src/main/java/com/example/combat/package-info.java`

```java
@ElementDefinition(name = "combat-system")
@ElementDependency(elementName = "achievement-system")
package com.example.combat;

import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.annotation.ElementDependency;
```

This declares: "The combat-system Element depends on achievement-system version 1.0.0."

When Elements load:
1. The loader builds a dependency graph
2. Dependencies are loaded before dependents
3. If a dependency is missing, loading fails with a clear error

You can depend on multiple Elements:

```java
@ElementDependency(elementName = "achievement-system")
@ElementDependency(elementName = "player-stats")
@ElementDependency(elementName = "inventory")
package com.example.combat;
```

#### Element Visibility Control

By default, Element code is **private**—only visible within that Element. To share code across Elements, use visibility annotations:

**@ElementPublic**: Visible to all Elements

```java
@ElementPublic
public class SharedUtility {
    public static String formatPlayerId(String id) {
        return "PLAYER-" + id.toUpperCase();
    }
}
```

**@ElementPrivate**: Visible only within this Element (default, explicit annotation optional)

```java
@ElementPrivate
public class InternalHelper {
    // Only used within this Element
}
```

**@ElementLocal**: Visible within this Element and its direct dependencies

```java
@ElementLocal
public class ProtectedHelper {
    // Visible to this Element and Elements that depend on it
}
```

#### Why Isolation Matters

Remember the Cache example from earlier? Here's a real scenario:

**Element A: Chat System (uses library-1.0.jar)**

```java
// Inside library-1.0.jar
public class MessageQueue {
    private static Queue<String> messages = new LinkedList<>();
}
```

**Element B: Analytics (uses library-2.0.jar)**

```java
// Inside library-2.0.jar (different version, refactored implementation)
public class MessageQueue {
    private static List<String> messages = new ArrayList<>();
}
```

Without isolation (in a traditional monolith):
- Only one `MessageQueue` class can be loaded
- Both Elements share the same static `messages` field
- Chat messages and analytics messages intermingle
- Tests for Element A break Element B

With Element isolation:
- Each Element loads its own version of `MessageQueue`
- They're separate classes (different ClassLoaders)
- Static state is isolated
- No conflicts

#### Complete Working Example: Element with Dependencies

Let's build a raid system that depends on our achievement system.

**File:** `src/main/java/com/example/raid/package-info.java`

```java
@ElementDefinition(name = "raid-system")
@ElementDependency(elementName = "achievement-system")
package com.example.raid;

import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.annotation.ElementDependency;
```

**File:** `src/main/java/com/example/raid/RaidService.java`

```java
package com.example.raid;

import com.example.achievements.AchievementService;
import javax.inject.Inject;

public class RaidService {
    private final AchievementService achievementService;

    @Inject
    public RaidService(AchievementService achievementService) {
        // Dependency injected from achievement-system Element
        this.achievementService = achievementService;
    }

    public void completeRaid(String playerId, String raidId) {
        System.out.println("Raid completed: " + raidId);

        // Grant achievement
        achievementService.unlockAchievement(playerId, "first-raid");

        // More raid logic...
    }
}
```

Because `raid-system` declared a dependency on `achievement-system`:
1. The loader ensures `achievement-system` loads first
2. `AchievementService` is available for injection
3. The raid system can use achievement functionality

---

## 4. Exposing REST and WebSocket Endpoints

**This is critical for game developers!** Elements can expose HTTP REST APIs and WebSocket endpoints that are automatically mounted and made accessible.

### 4.1 REST Endpoints

Elements expose REST endpoints using **Jakarta RS** (formerly JAX-RS), the Java standard for REST APIs.

**What's Jakarta RS?** It's a set of annotations and interfaces for building REST APIs:
- `@Path("/players")` - Map class/method to URL path
- `@GET`, `@POST`, `@PUT`, `@DELETE` - HTTP methods
- `@PathParam`, `@QueryParam` - Extract URL/query parameters
- `@Produces`, `@Consumes` - Content types (JSON, XML, etc.)

#### How Elements Expose REST Endpoints

To expose REST endpoints from an Element:

1. Create a Jakarta RS `Application` class
2. Define `@Path` resource classes with endpoint methods
3. The Element system automatically discovers and mounts them

#### Automatic Endpoint Mounting

REST endpoints are automatically mounted at:

```
/app/rest/{prefix}/{your-paths}
```

Where `{prefix}` comes from:
- Element attribute `jakarta.rs.prefix` (configured)
- Or defaults to the Element name

Example:
- Element name: `player-api`
- Endpoint: `@Path("/stats")`
- Full URL: `/app/rest/player-api/stats`

#### Customizing the Prefix

Set the prefix using Element attributes (covered in Section 6):

```java
@ElementDefaultAttribute(name = "jakarta.rs.prefix", value = "game")
package com.example.api;
```

Now endpoints mount at `/app/rest/game/{your-paths}`.

#### Complete Working Example: REST API Element

Let's build a player stats REST API.

**Step 1: Define the data model**

**File:** `src/main/java/com/example/api/model/PlayerStats.java`

```java
package com.example.api.model;

public class PlayerStats {
    private String playerId;
    private int level;
    private int experience;
    private int kills;
    private int deaths;

    public PlayerStats() {} // Required for JSON deserialization

    public PlayerStats(String playerId, int level, int experience, int kills, int deaths) {
        this.playerId = playerId;
        this.level = level;
        this.experience = experience;
        this.kills = kills;
        this.deaths = deaths;
    }

    // Getters and setters
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }

    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = deaths; }
}
```

**Step 2: Create the Application class**

**File:** `src/main/java/com/example/api/PlayerApiApplication.java`

```java
package com.example.api;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.Set;

@ApplicationPath("/") // Base path (combined with Element prefix)
public class PlayerApiApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
            PlayerStatsResource.class,
            PlayerInventoryResource.class
        );
    }
}
```

**Step 3: Define REST resource classes**

**File:** `src/main/java/com/example/api/PlayerStatsResource.java`

```java
package com.example.api;

import com.example.api.model.PlayerStats;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Path("/players/{playerId}/stats")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PlayerStatsResource {

    // In-memory storage (use a database in production)
    private static final Map<String, PlayerStats> statsStore = new ConcurrentHashMap<>();

    @GET
    public Response getStats(@PathParam("playerId") String playerId) {
        PlayerStats stats = statsStore.get(playerId);

        if (stats == null) {
            // Return default stats for new player
            stats = new PlayerStats(playerId, 1, 0, 0, 0);
            statsStore.put(playerId, stats);
        }

        return Response.ok(stats).build();
    }

    @PUT
    public Response updateStats(
            @PathParam("playerId") String playerId,
            PlayerStats updatedStats) {

        updatedStats.setPlayerId(playerId); // Ensure ID matches
        statsStore.put(playerId, updatedStats);

        return Response.ok(updatedStats).build();
    }

    @DELETE
    public Response resetStats(@PathParam("playerId") String playerId) {
        PlayerStats defaultStats = new PlayerStats(playerId, 1, 0, 0, 0);
        statsStore.put(playerId, defaultStats);

        return Response.ok(defaultStats).build();
    }
}
```

**File:** `src/main/java/com/example/api/PlayerInventoryResource.java`

```java
package com.example.api;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Path("/players/{playerId}/inventory")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PlayerInventoryResource {

    private static final Map<String, List<String>> inventories = new ConcurrentHashMap<>();

    @GET
    public Response getInventory(@PathParam("playerId") String playerId) {
        List<String> inventory = inventories.getOrDefault(playerId, new ArrayList<>());
        return Response.ok(inventory).build();
    }

    @POST
    @Path("/items")
    public Response addItem(
            @PathParam("playerId") String playerId,
            @QueryParam("itemId") String itemId) {

        List<String> inventory = inventories.computeIfAbsent(
            playerId,
            k -> new ArrayList<>()
        );
        inventory.add(itemId);

        return Response.ok(inventory).build();
    }

    @DELETE
    @Path("/items/{itemId}")
    public Response removeItem(
            @PathParam("playerId") String playerId,
            @PathParam("itemId") String itemId) {

        List<String> inventory = inventories.get(playerId);
        if (inventory != null) {
            inventory.remove(itemId);
        }

        return Response.ok(inventory).build();
    }
}
```

**Step 4: Declare the Element**

**File:** `src/main/java/com/example/api/package-info.java`

```java
@ElementDefinition(name = "player-api")
@ElementDefaultAttribute(name = "jakarta.rs.prefix", value = "game")
package com.example.api;

import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
```

**Step 5: Test Your Endpoints**

After deploying this Element, your REST API is available at:

```bash
# Get player stats
GET http://localhost:8080/app/rest/game/players/player123/stats

# Update player stats
PUT http://localhost:8080/app/rest/game/players/player123/stats
Content-Type: application/json

{
  "playerId": "player123",
  "level": 5,
  "experience": 1500,
  "kills": 42,
  "deaths": 7
}

# Reset player stats
DELETE http://localhost:8080/app/rest/game/players/player123/stats

# Get player inventory
GET http://localhost:8080/app/rest/game/players/player123/inventory

# Add item to inventory
POST http://localhost:8080/app/rest/game/players/player123/inventory/items?itemId=sword_of_truth

# Remove item from inventory
DELETE http://localhost:8080/app/rest/game/players/player123/inventory/items/sword_of_truth
```

**Result:** You have a fully functional REST API for player stats and inventory, automatically discovered and mounted by the Element system!

### 4.2 WebSocket Endpoints

WebSocket endpoints provide real-time, bidirectional communication between clients and the server. Elements can expose WebSocket endpoints using **Jakarta WebSocket** (formerly Java WebSocket API).

**What's a WebSocket?** Unlike HTTP (request-response), WebSocket maintains a persistent connection:
- Server can push messages to clients anytime
- Clients can send messages anytime
- Low latency, ideal for real-time games

**Jakarta WebSocket** uses annotations:
- `@ServerEndpoint("/path")` - Declare a WebSocket endpoint
- `@OnOpen` - Called when a client connects
- `@OnMessage` - Called when a message is received
- `@OnClose` - Called when a client disconnects
- `@OnError` - Called on errors

#### Automatic Endpoint Mounting

WebSocket endpoints are automatically mounted at:

```
/app/ws/{prefix}/{your-path}
```

Where `{prefix}` comes from:
- Element attribute `jakarta.websocket.prefix` (configured)
- Or defaults to the Element name

Example:
- Element name: `game-ws`
- Endpoint: `@ServerEndpoint("/chat")`
- Full URL: `ws://localhost:8080/app/ws/game-ws/chat`

#### Complete Working Example: WebSocket Chat Element

Let's build a simple chat system using WebSockets.

**Step 1: Define the WebSocket endpoint**

**File:** `src/main/java/com/example/chat/ChatEndpoint.java`

```java
package com.example.chat;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/chat/{room}")
public class ChatEndpoint {

    // Track all active sessions by room
    private static final Map<String, Map<String, Session>> rooms = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("room") String room) {
        // Add session to room
        rooms.computeIfAbsent(room, k -> new ConcurrentHashMap<>())
             .put(session.getId(), session);

        System.out.println("Client connected to room: " + room + ", session: " + session.getId());

        // Broadcast join message
        broadcast(room, "System: User " + session.getId() + " joined the room");
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("room") String room) {
        System.out.println("Received message in room " + room + ": " + message);

        // Broadcast message to all users in the room
        String fullMessage = "User " + session.getId() + ": " + message;
        broadcast(room, fullMessage);
    }

    @OnClose
    public void onClose(Session session, @PathParam("room") String room) {
        // Remove session from room
        Map<String, Session> roomSessions = rooms.get(room);
        if (roomSessions != null) {
            roomSessions.remove(session.getId());
            if (roomSessions.isEmpty()) {
                rooms.remove(room);
            }
        }

        System.out.println("Client disconnected from room: " + room + ", session: " + session.getId());

        // Broadcast leave message
        broadcast(room, "System: User " + session.getId() + " left the room");
    }

    @OnError
    public void onError(Session session, Throwable error, @PathParam("room") String room) {
        System.err.println("WebSocket error in room " + room + ": " + error.getMessage());
        error.printStackTrace();
    }

    private void broadcast(String room, String message) {
        Map<String, Session> roomSessions = rooms.get(room);
        if (roomSessions == null) {
            return;
        }

        roomSessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    System.err.println("Failed to send message: " + e.getMessage());
                }
            }
        });
    }
}
```

**Step 2: Add a game events endpoint**

**File:** `src/main/java/com/example/chat/GameEventsEndpoint.java`

```java
package com.example.chat;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/events/{playerId}")
public class GameEventsEndpoint {

    // Track sessions by player ID
    private static final Map<String, Session> playerSessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("playerId") String playerId) {
        playerSessions.put(playerId, session);
        System.out.println("Player " + playerId + " connected for events");

        // Send welcome message
        sendToPlayer(playerId, "Connected to game events");
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("playerId") String playerId) {
        System.out.println("Event message from " + playerId + ": " + message);

        // Echo acknowledgment
        sendToPlayer(playerId, "ACK: " + message);
    }

    @OnClose
    public void onClose(Session session, @PathParam("playerId") String playerId) {
        playerSessions.remove(playerId);
        System.out.println("Player " + playerId + " disconnected from events");
    }

    @OnError
    public void onError(Session session, Throwable error, @PathParam("playerId") String playerId) {
        System.err.println("Error for player " + playerId + ": " + error.getMessage());
    }

    // Public method to send events to specific players
    public static void sendToPlayer(String playerId, String message) {
        Session session = playerSessions.get(playerId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                System.err.println("Failed to send to player " + playerId + ": " + e.getMessage());
            }
        }
    }

    // Public method to broadcast to all connected players
    public static void broadcast(String message) {
        playerSessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    System.err.println("Failed to broadcast: " + e.getMessage());
                }
            }
        });
    }
}
```

**Step 3: Declare the Element**

**File:** `src/main/java/com/example/chat/package-info.java`

```java
@ElementDefinition(name = "game-chat")
@ElementDefaultAttribute(name = "jakarta.websocket.prefix", value = "realtime")
package com.example.chat;

import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
```

**Step 4: Test Your WebSocket Endpoints**

After deploying, your WebSocket endpoints are available:

**Chat endpoint:**
```
ws://localhost:8080/app/ws/realtime/chat/lobby
ws://localhost:8080/app/ws/realtime/chat/guild-hall
```

**Game events endpoint:**
```
ws://localhost:8080/app/ws/realtime/events/player123
```

**Testing with JavaScript:**

```javascript
// Connect to chat room
const chatSocket = new WebSocket('ws://localhost:8080/app/ws/realtime/chat/lobby');

chatSocket.onopen = () => {
    console.log('Connected to chat');
    chatSocket.send('Hello, lobby!');
};

chatSocket.onmessage = (event) => {
    console.log('Chat message:', event.data);
};

// Connect to game events
const eventsSocket = new WebSocket('ws://localhost:8080/app/ws/realtime/events/player123');

eventsSocket.onopen = () => {
    console.log('Connected to game events');
};

eventsSocket.onmessage = (event) => {
    console.log('Game event:', event.data);
};
```

**Result:** You have a fully functional real-time chat and event system!

### 4.3 ElementContainerService

The **ElementContainerService** is the infrastructure that makes REST and WebSocket endpoint mounting work.

#### What It Does

The `ElementContainerService`:
1. Polls the ElementRegistry for active Element deployments
2. Discovers Jakarta RS Applications and WebSocket endpoints
3. Delegates to specialized **Loaders** to mount them
4. Tracks mounted endpoints in **ContainerRecord** objects
5. Provides URIs for accessing endpoints

**Think of it as:** The service that wires up your Element's endpoints to the actual web server.

#### Loader Implementations

Loaders are responsible for mounting specific types of endpoints:

**JakartaRsLoader**: Mounts Jakarta RS REST endpoints
- Scans Element for `jakarta.ws.rs.core.Application` classes
- Reads `jakarta.rs.prefix` attribute (or defaults to Element name)
- Mounts at `/app/rest/{prefix}/`
- Registers in ContainerRecord

**JakartaWebsocketLoader**: Mounts Jakarta WebSocket endpoints
- Scans Element for `@ServerEndpoint` annotated classes
- Reads `jakarta.websocket.prefix` attribute (or defaults to Element name)
- Mounts at `/app/ws/{prefix}/{endpoint-path}`
- Registers in ContainerRecord

#### ContainerRecord and URI Tracking

Each mounted endpoint is tracked in a `ContainerRecord`:

```java
public class ContainerRecord {
    private final String elementName;
    private final String endpointType; // "REST" or "WebSocket"
    private final URI uri; // The full URI where it's mounted
    // ...
}
```

You can query the `ElementContainerService` to discover mounted endpoints programmatically:

```java
@Inject
public class DiscoveryService {
    private final ElementContainerService containerService;

    @Inject
    public DiscoveryService(ElementContainerService containerService) {
        this.containerService = containerService;
    }

    public void listEndpoints() {
        List<ContainerRecord> records = containerService.getContainerRecords();

        records.forEach(record -> {
            System.out.println("Element: " + record.getElementName());
            System.out.println("Type: " + record.getEndpointType());
            System.out.println("URI: " + record.getUri());
        });
    }
}
```

#### How Discovery Works

The process flow:

```
1. Element loads with REST/WS classes
2. ElementContainerService polls registry (periodic check)
3. Detects new Element deployment
4. Invokes JakartaRsLoader and JakartaWebsocketLoader
5. Loaders:
   - Scan Element classes
   - Read configuration attributes
   - Mount endpoints in the web container (Jetty)
   - Create ContainerRecord
6. Endpoints now accessible via HTTP/WS
```

This happens automatically—you don't need to configure anything beyond the Element definition and attributes.

---

## 5. ElementRegistry: Your Window to the System

### 5.1 What is the Registry?

The **ElementRegistry** is the central hub for Element management. It:
- Tracks all loaded Elements
- Provides access to Element metadata
- Manages service lookups
- Dispatches events
- Supports hierarchical structures

**Think of it as:** The phone book and post office for Elements.

#### Interface Overview

```java
public interface ElementRegistry {
    // Find Elements
    Optional<ElementRecord> getElement(String name, String version);
    List<ElementRecord> getElements();

    // Service location
    <T> Optional<T> getService(Class<T> serviceClass, String scope);
    <T> List<T> getServices(Class<T> serviceClass, String scope);

    // Event dispatch
    void publishEvent(Event event);

    // Hierarchy
    Optional<ElementRegistry> getParent();
    List<ElementRegistry> getChildren();

    // Lifecycle
    void close();
}
```

#### ElementRecord

An `ElementRecord` wraps an Element with additional metadata:

```java
public interface ElementRecord {
    Element getElement();
    Attributes getAttributes(); // Configuration
    ElementScope getScope(); // Execution context
    String getName();
    String getVersion();
}
```

#### Hierarchical Structure

Registries can have **parent-child relationships**:

```
Root Registry (shared services)
    ├── Game Server 1 Registry (isolated game instance)
    │   ├── Combat Element
    │   └── Achievements Element
    └── Game Server 2 Registry (another isolated game instance)
        ├── Combat Element
        └── Achievements Element
```

This enables:
- **Multi-tenancy**: Each game instance has its own registry
- **Shared services**: Parent registry provides common services
- **Isolation**: Child registries don't interfere with each other

#### Accessing the Registry

From within Element code, you can access the registry via:

**1. ServiceLocator injection**

```java
@Inject
public class MyService {
    private final ElementRegistry registry;

    @Inject
    public MyService(ServiceLocator serviceLocator) {
        this.registry = serviceLocator.getRegistry();
    }
}
```

**2. ElementRegistrySupplier**

```java
import dev.getelements.elements.sdk.ElementRegistrySupplier;

public class MyLogic {
    public void doSomething() {
        ElementRegistry registry = ElementRegistrySupplier.get();
        // Use registry...
    }
}
```

`ElementRegistrySupplier` provides thread-local access to the current registry.

### 5.2 Working Example: Using the Registry

**File:** `src/main/java/com/example/discovery/ElementDiscoveryService.java`

```java
package com.example.discovery;

import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.ElementRecord;
import dev.getelements.elements.sdk.ServiceLocator;
import javax.inject.Inject;
import java.util.List;

public class ElementDiscoveryService {
    private final ElementRegistry registry;

    @Inject
    public ElementDiscoveryService(ServiceLocator serviceLocator) {
        this.registry = serviceLocator.getRegistry();
    }

    public void listAllElements() {
        List<ElementRecord> elements = registry.getElements();

        System.out.println("Loaded Elements:");
        elements.forEach(record -> {
            System.out.println("  - " + record.getName() + " v" + record.getVersion());
            System.out.println("    Type: " + record.getElement().type());
            System.out.println("    Attributes: " + record.getAttributes().asMap());
        });
    }

    public boolean isElementLoaded(String name, String version) {
        return registry.getElement(name, version).isPresent();
    }

    public void accessServiceFromAnotherElement() {
        // Find a service provided by another Element
        var achievementService = registry.getService(
            com.example.achievements.AchievementService.class,
            "User"
        );

        if (achievementService.isPresent()) {
            System.out.println("Achievement service is available!");
            var achievements = achievementService.get().getPlayerAchievements("player123");
            achievements.forEach(ach -> System.out.println("  - " + ach.getName()));
        } else {
            System.out.println("Achievement service not found");
        }
    }
}
```

---

## 5.3 The Event System

The Event system enables **decoupled communication** between Elements.

**What's the pub/sub pattern?** Publishers send events without knowing who (if anyone) will receive them. Consumers listen for events they care about. This provides loose coupling—you can add new consumers without modifying publishers.

#### Event Structure

Events are simple:

```java
public interface Event {
    String name();
    Object[] arguments();
}
```

Example:
```java
Event event = Event.create("PlayerLeveledUp", "player123", 10);
// name: "PlayerLeveledUp"
// arguments: ["player123", 10]
```

#### Publishing Events

Publish events via the ElementRegistry:

```java
@Inject
public class PlayerService {
    private final ElementRegistry registry;

    @Inject
    public PlayerService(ServiceLocator serviceLocator) {
        this.registry = serviceLocator.getRegistry();
    }

    public void levelUpPlayer(String playerId, int newLevel) {
        // Game logic...
        System.out.println("Player " + playerId + " leveled up to " + newLevel);

        // Publish event
        Event event = Event.create("PlayerLeveledUp", playerId, newLevel);
        registry.publishEvent(event);
    }
}
```

You can also mark methods with `@ElementEventProducer` to document which events you publish:

```java
@ElementEventProducer(name = "PlayerLeveledUp")
public void levelUpPlayer(String playerId, int newLevel) {
    // ...
}
```

This annotation is **documentation only**—it doesn't affect functionality, but helps other developers discover your events.

#### Consuming Events

Consume events using `@ElementEventConsumer`:

```java
import dev.getelements.elements.sdk.annotation.ElementEventConsumer;

public class AchievementEventHandler {

    @ElementEventConsumer(name = "PlayerLeveledUp")
    public void onPlayerLeveledUp(String playerId, int newLevel) {
        System.out.println("Achievement system detected level up: " + playerId + " -> " + newLevel);

        // Grant level-based achievements
        if (newLevel == 10) {
            unlockAchievement(playerId, "level-10");
        } else if (newLevel == 50) {
            unlockAchievement(playerId, "level-50");
        }
    }

    private void unlockAchievement(String playerId, String achievementId) {
        System.out.println("Unlocking achievement: " + achievementId);
        // Achievement logic...
    }
}
```

#### Type-Matched Parameter Dispatch

The event system uses **type matching** to dispatch parameters:

```java
Event event = Event.create("PlayerLeveledUp", "player123", 10);
```

When this event is published, the system:
1. Finds all methods with `@ElementEventConsumer(name = "PlayerLeveledUp")`
2. Matches parameters by type:
   - First parameter is `String` → matches `"player123"`
   - Second parameter is `int` → matches `10`
3. Invokes the method: `onPlayerLeveledUp("player123", 10)`

You can consume with different parameter signatures:

```java
// Consume both parameters
@ElementEventConsumer(name = "PlayerLeveledUp")
public void handleLevelUp(String playerId, int newLevel) { ... }

// Consume only player ID
@ElementEventConsumer(name = "PlayerLeveledUp")
public void handleLevelUp(String playerId) { ... }

// Consume only level (skip String parameters)
@ElementEventConsumer(name = "PlayerLeveledUp")
public void handleLevelUp(int newLevel) { ... }
```

The system matches parameters **by type and order**, skipping non-matching types.

#### System Events vs Custom Events

Namazu Elements publishes built-in **system events**:
- `ElementLoaded` - When an Element finishes loading
- `ElementUnloaded` - When an Element is unloaded
- `ServiceRegistered` - When a service is registered
- (and more)

You can consume system events just like custom events:

```java
@ElementEventConsumer(name = "ElementLoaded")
public void onElementLoaded(String elementName, String elementVersion) {
    System.out.println("Element loaded: " + elementName + " v" + elementVersion);
}
```

#### Complete Working Example: Event-Driven Achievement System

Let's build a complete example with multiple Elements communicating via events.

**Element 1: Combat System (publishes events)**

**File:** `src/main/java/com/example/combat/package-info.java`

```java
@ElementDefinition(
    name = "combat-system",
    version = "1.0.0",
    type = ElementType.ISOLATED_CLASSPATH
)
package com.example.combat;

import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.ElementType;
```

**File:** `src/main/java/com/example/combat/CombatService.java`

```java
package com.example.combat;

import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.ServiceLocator;
import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import javax.inject.Inject;

public class CombatService {
    private final ElementRegistry registry;

    @Inject
    public CombatService(ServiceLocator serviceLocator) {
        this.registry = serviceLocator.getRegistry();
    }

    @ElementEventProducer(name = "EnemyKilled")
    public void killEnemy(String playerId, String enemyType, int damageDealt) {
        System.out.println("Player " + playerId + " killed " + enemyType);

        // Publish event
        registry.publishEvent(Event.create(
            "EnemyKilled",
            playerId,
            enemyType,
            damageDealt
        ));
    }

    @ElementEventProducer(name = "PlayerDied")
    public void playerDeath(String playerId, String killedBy) {
        System.out.println("Player " + playerId + " died to " + killedBy);

        // Publish event
        registry.publishEvent(Event.create(
            "PlayerDied",
            playerId,
            killedBy
        ));
    }
}
```

**Element 2: Achievement System (consumes events)**

**File:** `src/main/java/com/example/achievements/package-info.java`

```java
@ElementDefinition(name = "achievement-system")
package com.example.achievements;

import dev.getelements.elements.sdk.annotation.ElementDefinition;
```

**File:** `src/main/java/com/example/achievements/AchievementEventHandler.java`

```java
package com.example.achievements;

import dev.getelements.elements.sdk.annotation.ElementEventConsumer;
import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AchievementEventHandler {

    // Track player kill counts
    private final Map<String, Integer> playerKills = new ConcurrentHashMap<>();

    @ElementEventConsumer(name = "EnemyKilled")
    public void onEnemyKilled(String playerId, String enemyType, int damageDealt) {
        System.out.println("[Achievements] Processing kill by " + playerId);

        // Increment kill count
        int totalKills = playerKills.merge(playerId, 1, Integer::sum);

        // Check for kill-based achievements
        if (totalKills == 1) {
            unlockAchievement(playerId, "first-blood", "First Blood");
        } else if (totalKills == 100) {
            unlockAchievement(playerId, "centurion", "Centurion");
        } else if (totalKills == 1000) {
            unlockAchievement(playerId, "legend", "Legend");
        }

        // Check for enemy-type achievements
        if ("dragon".equals(enemyType)) {
            unlockAchievement(playerId, "dragonslayer", "Dragonslayer");
        }

        // Check for damage achievements
        if (damageDealt > 10000) {
            unlockAchievement(playerId, "critical-strike", "Critical Strike");
        }
    }

    @ElementEventConsumer(name = "PlayerDied")
    public void onPlayerDied(String playerId, String killedBy) {
        System.out.println("[Achievements] Processing death of " + playerId);

        // Humorous achievement
        if ("chicken".equals(killedBy)) {
            unlockAchievement(playerId, "pecked-to-death", "Pecked to Death");
        }
    }

    private void unlockAchievement(String playerId, String achievementId, String achievementName) {
        System.out.println("[Achievements] Unlocked for " + playerId + ": " + achievementName);
        // Store in database, send notification, etc.
    }
}
```

**Element 3: Statistics System (also consumes events)**

**File:** `src/main/java/com/example/stats/package-info.java`

```java
@ElementDefinition(
    name = "stats-system",
    version = "1.0.0",
    type = ElementType.ISOLATED_CLASSPATH
)
package com.example.stats;

import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.ElementType;
```

**File:** `src/main/java/com/example/stats/StatsEventHandler.java`

```java
package com.example.stats;

import dev.getelements.elements.sdk.annotation.ElementEventConsumer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StatsEventHandler {

    private final Map<String, PlayerStats> statsMap = new ConcurrentHashMap<>();

    @ElementEventConsumer(name = "EnemyKilled")
    public void onEnemyKilled(String playerId) {
        // Only consume player ID, ignore other parameters
        System.out.println("[Stats] Incrementing kill count for " + playerId);

        PlayerStats stats = statsMap.computeIfAbsent(playerId, PlayerStats::new);
        stats.incrementKills();
    }

    @ElementEventConsumer(name = "PlayerDied")
    public void onPlayerDied(String playerId) {
        System.out.println("[Stats] Incrementing death count for " + playerId);

        PlayerStats stats = statsMap.computeIfAbsent(playerId, PlayerStats::new);
        stats.incrementDeaths();
    }

    private static class PlayerStats {
        private int kills;
        private int deaths;

        public PlayerStats(String playerId) {}

        public void incrementKills() { kills++; }
        public void incrementDeaths() { deaths++; }
    }
}
```

**Testing the Event System:**

```java
// In combat-system Element
CombatService combatService = ...; // Injected

// This one action triggers multiple Elements
combatService.killEnemy("player123", "dragon", 12500);

// Console output:
// Player player123 killed dragon
// [Achievements] Processing kill by player123
// [Achievements] Unlocked for player123: Dragonslayer
// [Achievements] Unlocked for player123: Critical Strike
// [Stats] Incrementing kill count for player123
```

**Result:** Three independent Elements collaborate via events without any direct dependencies!

---

## 6. Scope and Attributes

### 6.1 ElementScope

#### What is Scope?

An **ElementScope** is a **thread-local context** that tracks:
- Which Elements are active during the current operation
- Request-specific data (user ID, session info, etc.)
- Transaction boundaries

**What's ThreadLocal?** It's a Java mechanism where each thread (unit of execution) has its own copy of a variable. For example:
- HTTP Request Thread 1: Scope for request from player A
- HTTP Request Thread 2: Scope for request from player B

The two scopes don't interfere—each thread has its own.

#### Why It Matters

Scopes provide:
- **Request context**: Know which player/request is being processed
- **Service scoping**: Select the right service implementation (Anonymous vs User vs Superuser)
- **Mutable attributes**: Store request-specific data
- **Transaction boundaries**: Group operations together

#### Interface Overview

```java
public interface ElementScope extends AutoCloseable {
    Optional<ElementRecord> getElementRecord();
    MutableAttributes getMutableAttributes();

    ElementScope enter();
    void exit();

    @Override
    void close(); // exit() automatically
}
```

#### Creating and Entering Scopes

**Using try-with-resources** (recommended):

```java
import dev.getelements.elements.sdk.ElementScope;

ElementScope scope = element.scope().create();
try (ElementScope entered = scope.enter()) {
    // Within scope
    doSomethingInScope();
} // Scope automatically exited
```

**What's try-with-resources?** It's Java syntax that automatically calls `close()` when the block ends:

```java
try (Resource r = new Resource()) {
    // Use r
} // r.close() called automatically
```

#### Nested Scopes

Scopes can be nested:

```java
try (ElementScope outerScope = elementA.scope().create().enter()) {
    // In element A's scope

    try (ElementScope innerScope = elementB.scope().create().enter()) {
        // In element B's scope (nested)
    } // Exit element B's scope

    // Back in element A's scope
} // Exit element A's scope
```

#### Multi-Element Scopes

Use the `ElementScopes` utility to create a scope spanning multiple Elements:

```java
import dev.getelements.elements.sdk.ElementScopes;

ElementScope multiScope = ElementScopes.create(elementA, elementB, elementC);
try (ElementScope entered = multiScope.enter()) {
    // All three Elements are in scope
}
```

#### Accessing the Current Scope

```java
import dev.getelements.elements.sdk.ElementScope;

public class MyService {
    public void processRequest() {
        Optional<ElementScope> currentScope = ElementScope.current();

        if (currentScope.isPresent()) {
            ElementScope scope = currentScope.get();
            MutableAttributes attrs = scope.getMutableAttributes();

            // Read request context
            String playerId = (String) attrs.get("playerId");
            System.out.println("Processing request for: " + playerId);
        }
    }
}
```

### 6.2 Attributes: Configuration and Context

#### What are Attributes?

**Attributes** are key-value pairs that configure and contextualize Elements. They come in two flavors:

**1. Attributes (immutable)** - Configuration read from Element definition
**2. MutableAttributes (mutable)** - Request-specific data that can change

#### Immutable Attributes

Immutable attributes configure the Element:
- REST API prefix
- WebSocket prefix
- Max connections
- Feature flags
- etc.

**Reading immutable attributes:**

```java
@Inject
public class MyService {
    private final String apiPrefix;

    @Inject
    public MyService(ElementRecord elementRecord) {
        Attributes attrs = elementRecord.getAttributes();
        this.apiPrefix = attrs.getString("jakarta.rs.prefix", "default-prefix");
    }
}
```

#### Mutable Attributes

Mutable attributes store request-specific data:
- Current player ID
- Session token
- Request ID
- Temporary flags

**Accessing mutable attributes from scope:**

```java
public void handleRequest(String playerId) {
    ElementScope scope = ElementScope.current().orElseThrow();
    MutableAttributes attrs = scope.getMutableAttributes();

    // Store player ID in request context
    attrs.put("playerId", playerId);
    attrs.put("requestTime", System.currentTimeMillis());

    // Other code can read these attributes
    processPlayerAction();
}

private void processPlayerAction() {
    ElementScope scope = ElementScope.current().orElseThrow();
    MutableAttributes attrs = scope.getMutableAttributes();

    String playerId = (String) attrs.get("playerId");
    long requestTime = (long) attrs.get("requestTime");

    System.out.println("Processing action for " + playerId + " at " + requestTime);
}
```

#### Default Attributes with @ElementDefaultAttribute

Declare default attributes in `package-info.java`:

```java
@ElementDefinition(
    name = "my-element",
    version = "1.0.0"
)
@ElementDefaultAttribute(name = "jakarta.rs.prefix", value = "game")
@ElementDefaultAttribute(name = "max.connections", value = "100")
@ElementDefaultAttribute(name = "feature.pvp.enabled", value = "true")
package com.example.myelement;

import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
```

These attributes are available at runtime via `ElementRecord.getAttributes()`.

#### Custom Attribute Suppliers

For dynamic configuration (e.g., read from database), implement a custom `AttributesSupplier`:

```java
import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.AttributesSupplier;

public class DatabaseAttributesSupplier implements AttributesSupplier {

    @Override
    public Attributes get() {
        // Load from database
        Map<String, Object> config = loadConfigFromDatabase();
        return Attributes.of(config);
    }

    private Map<String, Object> loadConfigFromDatabase() {
        // Database query...
        return Map.of(
            "max.players", 1000,
            "pvp.enabled", true
        );
    }
}
```

Then reference it in the Element definition:

```java
@ElementDefinition(
    name = "my-element",
    version = "1.0.0",
    attributesSupplier = DatabaseAttributesSupplier.class
)
package com.example.myelement;
```

### 6.3 Complete Working Example: Scope and Attributes

Let's build a rate-limiting system using scope and attributes.

**File:** `src/main/java/com/example/ratelimit/package-info.java`

```java
@ElementDefinition(name = "rate-limiter")
@ElementDefaultAttribute(name = "rate.limit.max", value = "100")
@ElementDefaultAttribute(name = "rate.limit.window.seconds", value = "60")
package com.example.ratelimit;

import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
```

**File:** `src/main/java/com/example/ratelimit/RateLimitService.java`

```java
package com.example.ratelimit;

import dev.getelements.elements.sdk.*;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.*;

public class RateLimitService {
    private final int maxRequests;
    private final int windowSeconds;

    // Track requests per player
    private final Map<String, Queue<Long>> requestTimes = new ConcurrentHashMap<>();

    @Inject
    public RateLimitService(ElementRecord elementRecord) {
        Attributes attrs = elementRecord.getAttributes();
        this.maxRequests = attrs.getInteger("rate.limit.max", 100);
        this.windowSeconds = attrs.getInteger("rate.limit.window.seconds", 60);

        System.out.println("Rate limiter configured: " + maxRequests + " requests per " + windowSeconds + " seconds");
    }

    public boolean checkRateLimit(String playerId) {
        long now = System.currentTimeMillis();
        long windowStart = now - (windowSeconds * 1000L);

        Queue<Long> times = requestTimes.computeIfAbsent(
            playerId,
            k -> new ConcurrentLinkedQueue<>()
        );

        // Remove old requests outside the window
        times.removeIf(time -> time < windowStart);

        // Check if under limit
        if (times.size() >= maxRequests) {
            storeRateLimitInfo(false, times.size());
            return false; // Rate limit exceeded
        }

        // Add current request
        times.add(now);
        storeRateLimitInfo(true, times.size());
        return true; // Allowed
    }

    private void storeRateLimitInfo(boolean allowed, int currentCount) {
        // Store in scope for logging/monitoring
        ElementScope.current().ifPresent(scope -> {
            MutableAttributes attrs = scope.getMutableAttributes();
            attrs.put("rateLimit.allowed", allowed);
            attrs.put("rateLimit.currentCount", currentCount);
            attrs.put("rateLimit.maxCount", maxRequests);
        });
    }
}
```

**File:** `src/main/java/com/example/ratelimit/RateLimitedEndpoint.java`

```java
package com.example.ratelimit;

import dev.getelements.elements.sdk.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import javax.inject.Inject;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class RateLimitedEndpoint {
    private final RateLimitService rateLimitService;
    private final Element element;

    @Inject
    public RateLimitedEndpoint(RateLimitService rateLimitService, Element element) {
        this.rateLimitService = rateLimitService;
        this.element = element;
    }

    @GET
    @Path("/action")
    public Response performAction(@QueryParam("playerId") String playerId) {
        // Create scope for this request
        try (ElementScope scope = element.scope().create().enter()) {
            MutableAttributes scopeAttrs = scope.getMutableAttributes();

            // Store request context
            scopeAttrs.put("playerId", playerId);
            scopeAttrs.put("requestTime", System.currentTimeMillis());
            scopeAttrs.put("endpoint", "/api/action");

            // Check rate limit
            if (!rateLimitService.checkRateLimit(playerId)) {
                // Rate limit info was stored in scope by checkRateLimit()
                int currentCount = (int) scopeAttrs.get("rateLimit.currentCount");
                int maxCount = (int) scopeAttrs.get("rateLimit.maxCount");

                return Response.status(429) // Too Many Requests
                    .entity(Map.of(
                        "error", "Rate limit exceeded",
                        "currentCount", currentCount,
                        "maxCount", maxCount
                    ))
                    .build();
            }

            // Perform the action
            String result = performGameAction(playerId);

            // Log request completion
            logRequest(scopeAttrs);

            return Response.ok(Map.of(
                "result", result,
                "playerId", playerId
            )).build();
        }
    }

    private String performGameAction(String playerId) {
        // Read context from scope
        ElementScope scope = ElementScope.current().orElseThrow();
        MutableAttributes attrs = scope.getMutableAttributes();

        System.out.println("Executing action for " + attrs.get("playerId"));
        System.out.println("Request time: " + attrs.get("requestTime"));

        return "Action completed successfully";
    }

    private void logRequest(MutableAttributes attrs) {
        System.out.println("Request log:");
        System.out.println("  Player: " + attrs.get("playerId"));
        System.out.println("  Endpoint: " + attrs.get("endpoint"));
        System.out.println("  Time: " + attrs.get("requestTime"));
        System.out.println("  Rate limit allowed: " + attrs.get("rateLimit.allowed"));
        System.out.println("  Current count: " + attrs.get("rateLimit.currentCount"));
    }
}
```

**Testing:**

```bash
# First request - allowed
curl "http://localhost:8080/app/rest/rate-limiter/api/action?playerId=player123"
# {"result":"Action completed successfully","playerId":"player123"}

# Make 100 more requests rapidly...

# 101st request - rate limited
curl "http://localhost:8080/app/rest/rate-limiter/api/action?playerId=player123"
# {"error":"Rate limit exceeded","currentCount":100,"maxCount":100}
```

**Result:**
- Configuration via immutable attributes (`rate.limit.max`, `rate.limit.window.seconds`)
- Request context via mutable scope attributes (`playerId`, `requestTime`, `rateLimit.*`)
- Scope provides transaction boundary for the request

---

## 7. Practical Guide: Building Your First Element

Let's build a complete, working Element from scratch. We'll create a **Leaderboard System** with:
- Service interface and implementation
- REST API endpoints
- Event consumers
- Configuration attributes

### Step 1: Project Setup

**Maven Project Structure:**

```
leaderboard-element/
├── pom.xml
└── src/main/java/com/example/leaderboard/
    ├── package-info.java
    ├── LeaderboardService.java
    ├── LeaderboardServiceImpl.java
    ├── LeaderboardResource.java
    ├── LeaderboardApplication.java
    ├── LeaderboardEventHandler.java
    └── model/
        ├── LeaderboardEntry.java
        └── Leaderboard.java
```

**File:** `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>leaderboard-element</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Namazu Elements SDK (provided scope - supplied by server) -->
        <dependency>
            <groupId>dev.getelements.elements</groupId>
            <artifactId>sdk</artifactId>
            <version>3.0.0</version>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>dev.getelements.elements</groupId>
            <artifactId>sdk-model</artifactId>
            <version>3.0.0</version>
            <scope>provided</scope>
        </dependency>

        <!-- Jakarta RS API (provided) -->
        <dependency>
            <groupId>jakarta.ws.rs</groupId>
            <artifactId>jakarta.ws.rs-api</artifactId>
            <version>3.1.0</version>
            <scope>provided</scope>
        </dependency>

        <!-- Dependency Injection (provided) -->
        <dependency>
            <groupId>javax.inject</groupId>
            <artifactId>javax.inject</artifactId>
            <version>1</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</project>
```

**Important:** All Namazu Elements SDK dependencies use `<scope>provided</scope>` because the server provides them.

### Step 2: Define the Element

**File:** `src/main/java/com/example/leaderboard/package-info.java`

```java
@ElementDefinition(name = "leaderboard")
@ElementDefaultAttribute(name = "jakarta.rs.prefix", value = "leaderboard")
@ElementDefaultAttribute(name = "leaderboard.maxEntries", value = "100")
@ElementDefaultAttribute(name = "leaderboard.defaultSeasonId", value = "season-1")
package com.example.leaderboard;

import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
```

**Explanation:**
- `@ElementDefinition`: Declares this package as an Element
- `type = ISOLATED_CLASSPATH`: Full isolation (default)
- `@ElementDefaultAttribute`: Sets configuration values
  - REST prefix: `/app/rest/leaderboard/`
  - Max entries: 100
  - Default season: "season-1"

**What's recursive scanning?** The Element loader scans this package and all sub-packages for:
- `@ElementServiceImplementation` classes
- Jakarta RS `Application` classes
- `@ServerEndpoint` WebSocket classes
- `@ElementEventConsumer` methods

You don't need to register them explicitly.

### Step 3: Create the Service

**File:** `src/main/java/com/example/leaderboard/model/LeaderboardEntry.java`

```java
package com.example.leaderboard.model;

public class LeaderboardEntry {
    private String playerId;
    private String playerName;
    private int score;
    private int rank;

    public LeaderboardEntry() {}

    public LeaderboardEntry(String playerId, String playerName, int score, int rank) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.score = score;
        this.rank = rank;
    }

    // Getters and setters
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
}
```

**File:** `src/main/java/com/example/leaderboard/model/Leaderboard.java`

```java
package com.example.leaderboard.model;

import java.util.List;

public class Leaderboard {
    private String seasonId;
    private List<LeaderboardEntry> entries;

    public Leaderboard() {}

    public Leaderboard(String seasonId, List<LeaderboardEntry> entries) {
        this.seasonId = seasonId;
        this.entries = entries;
    }

    public String getSeasonId() { return seasonId; }
    public void setSeasonId(String seasonId) { this.seasonId = seasonId; }

    public List<LeaderboardEntry> getEntries() { return entries; }
    public void setEntries(List<LeaderboardEntry> entries) { this.entries = entries; }
}
```

**File:** `src/main/java/com/example/leaderboard/LeaderboardService.java`

```java
package com.example.leaderboard;

import com.example.leaderboard.model.Leaderboard;
import com.example.leaderboard.model.LeaderboardEntry;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import java.util.List;
import java.util.Optional;

@ElementServiceExport
public interface LeaderboardService {
    void addScore(String seasonId, String playerId, String playerName, int score);
    Leaderboard getLeaderboard(String seasonId, int limit);
    Optional<LeaderboardEntry> getPlayerRank(String seasonId, String playerId);
    void resetSeason(String seasonId);
}
```

### Step 4: Implement the Service

**File:** `src/main/java/com/example/leaderboard/LeaderboardServiceImpl.java`

```java
package com.example.leaderboard;

import com.example.leaderboard.model.Leaderboard;
import com.example.leaderboard.model.LeaderboardEntry;
import dev.getelements.elements.sdk.annotation.ElementServiceImplementation;
import dev.getelements.elements.sdk.*;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@ElementServiceImplementation
public class LeaderboardServiceImpl implements LeaderboardService {

    private final int maxEntries;

    // Season -> (PlayerId -> Score)
    private final Map<String, Map<String, PlayerScore>> seasons = new ConcurrentHashMap<>();

    @Inject
    public LeaderboardServiceImpl(ElementRecord elementRecord) {
        Attributes attrs = elementRecord.getAttributes();
        this.maxEntries = attrs.getInteger("leaderboard.maxEntries", 100);
        System.out.println("Leaderboard initialized with max entries: " + maxEntries);
    }

    @Override
    public void addScore(String seasonId, String playerId, String playerName, int score) {
        Map<String, PlayerScore> seasonScores = seasons.computeIfAbsent(
            seasonId,
            k -> new ConcurrentHashMap<>()
        );

        PlayerScore playerScore = seasonScores.get(playerId);
        if (playerScore == null) {
            seasonScores.put(playerId, new PlayerScore(playerId, playerName, score));
        } else {
            // Update if new score is higher
            if (score > playerScore.score) {
                playerScore.score = score;
            }
        }

        System.out.println("Score added: " + playerName + " = " + score + " in " + seasonId);
    }

    @Override
    public Leaderboard getLeaderboard(String seasonId, int limit) {
        Map<String, PlayerScore> seasonScores = seasons.get(seasonId);
        if (seasonScores == null) {
            return new Leaderboard(seasonId, List.of());
        }

        // Sort by score descending
        List<LeaderboardEntry> entries = seasonScores.values().stream()
            .sorted(Comparator.comparingInt((PlayerScore ps) -> ps.score).reversed())
            .limit(Math.min(limit, maxEntries))
            .map(ps -> new LeaderboardEntry(ps.playerId, ps.playerName, ps.score, 0))
            .collect(Collectors.toList());

        // Assign ranks
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRank(i + 1);
        }

        return new Leaderboard(seasonId, entries);
    }

    @Override
    public Optional<LeaderboardEntry> getPlayerRank(String seasonId, String playerId) {
        Leaderboard board = getLeaderboard(seasonId, maxEntries);
        return board.getEntries().stream()
            .filter(entry -> entry.getPlayerId().equals(playerId))
            .findFirst();
    }

    @Override
    public void resetSeason(String seasonId) {
        seasons.remove(seasonId);
        System.out.println("Season reset: " + seasonId);
    }

    private static class PlayerScore {
        String playerId;
        String playerName;
        int score;

        PlayerScore(String playerId, String playerName, int score) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.score = score;
        }
    }
}
```

### Step 5: Add REST Endpoints

**File:** `src/main/java/com/example/leaderboard/LeaderboardApplication.java`

```java
package com.example.leaderboard;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.Set;

@ApplicationPath("/")
public class LeaderboardApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(LeaderboardResource.class);
    }
}
```

**File:** `src/main/java/com/example/leaderboard/LeaderboardResource.java`

```java
package com.example.leaderboard;

import com.example.leaderboard.model.Leaderboard;
import com.example.leaderboard.model.LeaderboardEntry;
import dev.getelements.elements.sdk.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import javax.inject.Inject;
import java.util.Optional;

@Path("/seasons/{seasonId}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LeaderboardResource {

    private final LeaderboardService leaderboardService;
    private final String defaultSeasonId;

    @Inject
    public LeaderboardResource(
            LeaderboardService leaderboardService,
            ElementRecord elementRecord) {
        this.leaderboardService = leaderboardService;

        Attributes attrs = elementRecord.getAttributes();
        this.defaultSeasonId = attrs.getString("leaderboard.defaultSeasonId", "season-1");
    }

    @GET
    @Path("/leaderboard")
    public Response getLeaderboard(
            @PathParam("seasonId") String seasonId,
            @QueryParam("limit") @DefaultValue("10") int limit) {

        Leaderboard board = leaderboardService.getLeaderboard(seasonId, limit);
        return Response.ok(board).build();
    }

    @GET
    @Path("/players/{playerId}/rank")
    public Response getPlayerRank(
            @PathParam("seasonId") String seasonId,
            @PathParam("playerId") String playerId) {

        Optional<LeaderboardEntry> entry = leaderboardService.getPlayerRank(seasonId, playerId);

        if (entry.isPresent()) {
            return Response.ok(entry.get()).build();
        } else {
            return Response.status(404)
                .entity(Map.of("error", "Player not on leaderboard"))
                .build();
        }
    }

    @POST
    @Path("/scores")
    public Response addScore(
            @PathParam("seasonId") String seasonId,
            @QueryParam("playerId") String playerId,
            @QueryParam("playerName") String playerName,
            @QueryParam("score") int score) {

        leaderboardService.addScore(seasonId, playerId, playerName, score);

        return Response.ok(Map.of(
            "message", "Score added",
            "playerId", playerId,
            "score", score
        )).build();
    }

    @DELETE
    public Response resetSeason(@PathParam("seasonId") String seasonId) {
        leaderboardService.resetSeason(seasonId);
        return Response.ok(Map.of("message", "Season reset")).build();
    }
}
```

### Step 6: Add Event Communication

**File:** `src/main/java/com/example/leaderboard/LeaderboardEventHandler.java`

```java
package com.example.leaderboard;

import dev.getelements.elements.sdk.*;
import dev.getelements.elements.sdk.annotation.ElementEventConsumer;
import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import javax.inject.Inject;

public class LeaderboardEventHandler {

    private final LeaderboardService leaderboardService;
    private final ElementRegistry registry;
    private final String defaultSeasonId;

    @Inject
    public LeaderboardEventHandler(
            LeaderboardService leaderboardService,
            ServiceLocator serviceLocator,
            ElementRecord elementRecord) {
        this.leaderboardService = leaderboardService;
        this.registry = serviceLocator.getRegistry();

        Attributes attrs = elementRecord.getAttributes();
        this.defaultSeasonId = attrs.getString("leaderboard.defaultSeasonId", "season-1");
    }

    @ElementEventConsumer(name = "PlayerScored")
    public void onPlayerScored(String playerId, String playerName, int points) {
        System.out.println("[Leaderboard] Player scored: " + playerName + " = " + points);

        // Add to leaderboard
        leaderboardService.addScore(defaultSeasonId, playerId, playerName, points);

        // Check if new high score
        var playerRank = leaderboardService.getPlayerRank(defaultSeasonId, playerId);
        if (playerRank.isPresent() && playerRank.get().getRank() == 1) {
            publishNewLeaderEvent(playerId, playerName, points);
        }
    }

    @ElementEventProducer(name = "NewLeader")
    private void publishNewLeaderEvent(String playerId, String playerName, int score) {
        Event event = Event.create("NewLeader", playerId, playerName, score, defaultSeasonId);
        registry.publishEvent(event);
        System.out.println("[Leaderboard] New leader: " + playerName);
    }
}
```

### Step 7: Build and Package

```bash
# Build the Element
mvn clean package

# This creates: target/leaderboard-element-1.0.0.jar
```

### Step 8: Deploy

**Option 1: Directory Deployment**

Create the deployment structure (see ELEMENT_ANATOMY.md for details):

```
elements/
└── leaderboard/
    ├── package-info.class
    ├── LeaderboardService.class
    ├── LeaderboardServiceImpl.class
    ├── LeaderboardResource.class
    ├── LeaderboardApplication.class
    ├── LeaderboardEventHandler.class
    └── model/
        ├── LeaderboardEntry.class
        └── Leaderboard.class
```

**Option 2: .elm File**

Package as a ZIP file with `.elm` extension.

### Step 9: Test Your Element

**Start the Namazu Elements server** (assuming it's configured to scan the `elements/` directory).

**Test REST endpoints:**

```bash
# Add some scores
curl -X POST "http://localhost:8080/app/rest/leaderboard/seasons/season-1/scores?playerId=player1&playerName=Alice&score=1000"
curl -X POST "http://localhost:8080/app/rest/leaderboard/seasons/season-1/scores?playerId=player2&playerName=Bob&score=1500"
curl -X POST "http://localhost:8080/app/rest/leaderboard/seasons/season-1/scores?playerId=player3&playerName=Charlie&score=1200"

# Get leaderboard
curl "http://localhost:8080/app/rest/leaderboard/seasons/season-1/leaderboard?limit=10"
# Response:
# {
#   "seasonId": "season-1",
#   "entries": [
#     {"playerId": "player2", "playerName": "Bob", "score": 1500, "rank": 1},
#     {"playerId": "player3", "playerName": "Charlie", "score": 1200, "rank": 2},
#     {"playerId": "player1", "playerName": "Alice", "score": 1000, "rank": 3}
#   ]
# }

# Get specific player rank
curl "http://localhost:8080/app/rest/leaderboard/seasons/season-1/players/player2/rank"
# Response:
# {"playerId": "player2", "playerName": "Bob", "score": 1500, "rank": 1}

# Reset season
curl -X DELETE "http://localhost:8080/app/rest/leaderboard/seasons/season-1"
```

**Test event system** (from another Element):

```java
// Publish PlayerScored event
registry.publishEvent(Event.create("PlayerScored", "player4", "Diana", 2000));

// LeaderboardEventHandler automatically:
// 1. Adds score to leaderboard
// 2. Checks if new #1
// 3. Publishes NewLeader event if so
```

**Success!** You've built a complete, production-ready Element with services, REST APIs, and event handling.

---

## 8. Advanced Patterns

### 8.1 Element Dependencies and Load Order

When Elements depend on each other, the loader resolves dependencies automatically:

```java
// Element A
@ElementDefinition(name = "element-a")
package com.example.a;

// Element B depends on A
@ElementDefinition(name = "element-b")
@ElementDependency(elementName = "element-a")
package com.example.b;

// Element C depends on both A and B
@ElementDefinition(name = "element-c")
@ElementDependency(elementName = "element-a")
@ElementDependency(elementName = "element-b")
package com.example.c;
```

**Load order:** A → B → C

The loader builds a dependency graph and loads Elements in topological order.

### 8.2 Hierarchical Registries for Multi-Tenant Scenarios

Create isolated registries for each tenant:

```java
// Root registry with shared services
ElementRegistry rootRegistry = createRootRegistry();

// Create child registries for each game instance
ElementRegistry game1Registry = rootRegistry.createChild();
ElementRegistry game2Registry = rootRegistry.createChild();

// Load Elements into child registries
game1Registry.loadElement(combatElement);
game1Registry.loadElement(achievementsElement);

game2Registry.loadElement(combatElement);
game2Registry.loadElement(achievementsElement);
```

Each game instance has its own Elements (separate instances), but can share parent services.

### 8.3 Scope-Based Request Handling

Use scopes to track request context across multiple Elements:

```java
// HTTP request arrives
try (ElementScope scope = createMultiElementScope().enter()) {
    MutableAttributes attrs = scope.getMutableAttributes();

    // Store request context
    attrs.put("playerId", "player123");
    attrs.put("sessionToken", "abc-def-ghi");
    attrs.put("requestId", UUID.randomUUID().toString());

    // Call services across multiple Elements
    // They all see the same scope and context
    authService.authenticate(); // Reads sessionToken from scope
    gameService.performAction(); // Reads playerId from scope
    analyticsService.trackEvent(); // Reads requestId from scope
}
```

### 8.4 Custom Attribute Suppliers for Dynamic Configuration

Load configuration from external sources:

```java
public class ConsulAttributesSupplier implements AttributesSupplier {
    private final String consulUrl;
    private final String elementName;

    public ConsulAttributesSupplier(String consulUrl, String elementName) {
        this.consulUrl = consulUrl;
        this.elementName = elementName;
    }

    @Override
    public Attributes get() {
        // Fetch configuration from Consul
        Map<String, Object> config = fetchFromConsul(consulUrl, elementName);
        return Attributes.of(config);
    }

    private Map<String, Object> fetchFromConsul(String url, String element) {
        // HTTP call to Consul...
        return Map.of(
            "feature.enabled", true,
            "max.connections", 500
        );
    }
}
```

### 8.5 Element Lifecycle Hooks

Register callbacks for cleanup:

```java
public class DatabaseElement implements Element {
    private final DataSource dataSource;

    public DatabaseElement(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void close() {
        // Cleanup when Element unloads
        try {
            dataSource.close();
            System.out.println("Database connections closed");
        } catch (Exception e) {
            System.err.println("Error closing database: " + e.getMessage());
        }
    }
}
```

---

## 9. Best Practices

### 9.1 Keep Elements Focused and Cohesive

**Do:** Create Elements around business domains
- `achievements-element`
- `leaderboard-element`
- `matchmaking-element`

**Don't:** Create giant monolithic Elements
- `game-logic-element` (contains everything)

### 9.2 Use Events for Loose Coupling

**Do:** Communicate via events
```java
registry.publishEvent(Event.create("PlayerLeveledUp", playerId, level));
```

**Don't:** Directly depend on every Element
```java
@ElementDependency(elementName = "achievements", ...)
@ElementDependency(elementName = "leaderboard", ...)
@ElementDependency(elementName = "analytics", ...)
// ... 20 more dependencies
```

### 9.3 Leverage Scope for Request Context

**Do:** Store context in scope
```java
scope.getMutableAttributes().put("playerId", playerId);
```

**Don't:** Pass context through every method
```java
doSomething(playerId, sessionId, requestId, timestamp, ...);
```

### 9.4 Configure via Attributes

**Do:** Use attributes for configuration
```java
@ElementDefaultAttribute(name = "max.connections", value = "100")
```

**Don't:** Hardcode values
```java
private static final int MAX_CONNECTIONS = 100; // Can't change without recompiling
```

### 9.5 Document Events

**Do:** Use @ElementEventProducer
```java
@ElementEventProducer(name = "PlayerLeveledUp")
public void levelUp(String playerId) { ... }
```

**Don't:** Publish undocumented events
```java
registry.publishEvent(Event.create("SomeEvent", ...)); // What is this?
```

### 9.6 Use @ElementPublic Sparingly

**Do:** Keep code private by default
```java
// No annotation = private (default)
public class InternalHelper { ... }
```

**Don't:** Make everything public
```java
@ElementPublic // Only when truly needed
public class SharedUtility { ... }
```

### 9.7 Test Elements in Isolation

**Do:** Write unit tests that mock dependencies
```java
@Test
public void testAchievementUnlock() {
    AchievementService service = new AchievementServiceImpl();
    boolean result = service.unlockAchievement("player123", "first-kill");
    assertTrue(result);
}
```

**Don't:** Only test with the full server running

---

## 10. Reference Summary

### 10.1 Key Annotations Quick Reference

| Annotation | Purpose | Used On |
|------------|---------|---------|
| `@ElementDefinition` | Declare an Element | package-info.java |
| `@ElementDependency` | Declare dependency on another Element | package-info.java |
| `@ElementDefaultAttribute` | Set default configuration value | package-info.java |
| `@ElementServiceExport` | Mark interface as exportable service | Interface |
| `@ElementServiceImplementation` | Mark class as service implementation | Class |
| `@ElementEventConsumer` | Mark method as event consumer | Method |
| `@ElementEventProducer` | Document event production | Method |
| `@ElementPublic` | Make class visible to all Elements | Class |
| `@ElementPrivate` | Make class visible only within Element (default) | Class |
| `@ElementLocal` | Make class visible to dependents | Class |

### 10.2 Element Lifecycle Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Element Lifecycle                        │
└─────────────────────────────────────────────────────────────┘

1. Definition
   └─> @ElementDefinition in package-info.java

2. Discovery
   └─> Loader scans directories/files
       └─> Finds Elements by @ElementDefinition

3. Dependency Resolution
   └─> Build dependency graph
       └─> Determine load order

4. Loading
   └─> Create ClassLoader (if ISOLATED_CLASSPATH)
       └─> Load classes
           └─> Scan for services, endpoints, consumers

5. Registration
   └─> Register in ElementRegistry
       └─> Register services
           └─> Mount REST/WS endpoints

6. Runtime
   └─> Handle requests
       └─> Dispatch events
           └─> Execute service methods

7. Shutdown
   └─> Element.close() called
       └─> Cleanup resources
           └─> Unregister from registry
```

### 10.3 REST/WebSocket Endpoint Path Patterns

**REST Endpoints:**
```
/app/rest/{prefix}/{resource-path}

Where:
  {prefix} = jakarta.rs.prefix attribute (or Element name)
  {resource-path} = @Path value on resource class + method

Example:
  Element: "player-api"
  Prefix: "game" (via attribute)
  Resource: @Path("/players/{id}")

  Full URL: /app/rest/game/players/{id}
```

**WebSocket Endpoints:**
```
/app/ws/{prefix}/{endpoint-path}

Where:
  {prefix} = jakarta.websocket.prefix attribute (or Element name)
  {endpoint-path} = @ServerEndpoint value

Example:
  Element: "game-chat"
  Prefix: "realtime" (via attribute)
  Endpoint: @ServerEndpoint("/chat/{room}")

  Full URL: ws://localhost:8080/app/ws/realtime/chat/{room}
```

### 10.4 Event System Cheat Sheet

**Publishing Events:**
```java
Event event = Event.create("EventName", arg1, arg2, arg3);
registry.publishEvent(event);
```

**Consuming Events:**
```java
@ElementEventConsumer(name = "EventName")
public void onEvent(String arg1, int arg2, Object arg3) {
    // Handle event
}
```

**Type Matching:**
- Parameters matched by type and order
- Skip non-matching types
- Can consume subset of arguments

**System Events:**
- `ElementLoaded`
- `ElementUnloaded`
- `ServiceRegistered`
- `ServiceUnregistered`

### 10.5 Additional Resources

- **Element Packaging Details:** See `ELEMENT_ANATOMY.md` for deployment structure
- **SDK JavaDoc:** Complete API documentation in the SDK modules
- **Example Elements:**
  - `sdk-test-element-rs` - REST API example
  - `sdk-test-element-ws` - WebSocket example
  - `sdk-test-element-a` - Event and service example

---

## Conclusion

The Namazu Elements plugin architecture provides a powerful, flexible foundation for building multiplayer game backends:

- **Isolated** - Elements run in isolation, preventing dependency conflicts
- **Composable** - Mix and match Elements to build complex systems
- **Dynamic** - Load, configure, and extend at runtime
- **Event-Driven** - Loose coupling via pub/sub communication
- **Service-Oriented** - Well-defined service interfaces
- **REST/WebSocket Ready** - Automatic endpoint mounting
- **Pragmatic** - Isolation with escape hatches when needed

Whether you're evaluating Namazu Elements as a platform or building your first Element, this architecture gives you the tools to create scalable, maintainable game backends.

**Next Steps:**
1. Follow the Practical Guide (Section 7) to build your first Element
2. Explore the example Elements in the SDK test modules
3. Read `ELEMENT_ANATOMY.md` for deployment details
4. Dive into the SDK JavaDoc for complete API reference

Happy coding, and welcome to the Namazu Elements community!
