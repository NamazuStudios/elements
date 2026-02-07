# Element Anatomy: A Technical Deep Dive

## Table of Contents

- [Introduction](#introduction)
- [What is an Element?](#what-is-an-element)
- [Directory Structure](#directory-structure)
- [The Four Core Components](#the-four-core-components)
  - [API: Shared Interfaces](#api-shared-interfaces)
  - [SPI: Service Provider Interface](#spi-service-provider-interface)
  - [LIB: Implementation Libraries](#lib-implementation-libraries)
  - [Classpath: Raw Class Files](#classpath-raw-class-files)
- [ELM Files: Portable Packaging](#elm-files-portable-packaging)
- [Maven Integration](#maven-integration)
- [Classloader Hierarchy](#classloader-hierarchy)
- [Configuration and Attributes](#configuration-and-attributes)
- [Loading Process](#loading-process)
- [Practical Examples](#practical-examples)

---

## Introduction

Namazu Elements provides a sophisticated plugin architecture for extending Java backend servers with custom business logic. At its core, an Element is a self-contained, isolated module with carefully controlled dependencies and classloading boundaries. This document explains the anatomy of Elements, their packaging formats, and how they integrate with the Java ecosystem.

**Target Audience:** Technical users with basic understanding of Java concepts. Prior knowledge of Java classloaders, Maven, and dependency management is helpful but not required.

---

## What is an Element?

An **Element** is a deployable unit of functionality within the Namazu Elements framework. Think of it as a plugin or module that can be:

- **Dynamically loaded** at runtime without restarting the server
- **Isolated** from other Elements to prevent version conflicts
- **Configured** independently with custom attributes
- **Distributed** as either directories or packaged `.elm` files
- **Sourced** from Maven Central, GitHub Packages, or any Maven repository

Each Element contains business logic (your custom code) along with its dependencies, organized in a specific structure that enables safe, isolated loading.

---

## Directory Structure

An Element on disk follows a flat, organized directory structure:

```
deployment/
├── api/                    # Shared API JARs (optional)
│   ├── shared-api-1.0.jar
│   └── common-types.jar
│
├── my-element/            # Element directory (name is arbitrary)
│   ├── api/               # Element-specific API JARs (optional)
│   │   └── my-element-api-1.0.jar
│   │
│   ├── spi/               # Service Provider Interface JARs (optional)
│   │   └── my-spi-impl-1.0.jar
│   │
│   ├── lib/               # Implementation JARs (required)
│   │   ├── my-element-impl-1.0.jar
│   │   ├── guava-31.1-jre.jar
│   │   └── jackson-core-2.14.0.jar
│   │
│   ├── classpath/         # Raw class directories (optional)
│   │   └── (compiled classes)
│   │
│   └── dev.getelements.element.attributes.properties  # Configuration (optional)
│
└── another-element/       # Another Element (completely isolated)
    └── ...
```

**Key Principles:**

1. **Flat Structure**: Each element is a top-level subdirectory. No nesting of elements within elements.
2. **Name Agnostic**: Element directory names are arbitrary; metadata comes from annotations in the code, not directory names.
3. **Self-Contained**: Each element directory contains everything needed to run that element.

---

## The Four Core Components

### API: Shared Interfaces

**Location:** `api/` directory
**Purpose:** Contains interface definitions and data types that multiple Elements share
**Visibility:** Shared across ALL Elements in the deployment
**Classloader Level:** Highest in the hierarchy (most visible)

#### What Goes Here?

- **Interface definitions** that multiple elements need to communicate
- **Data transfer objects (DTOs)** that cross element boundaries
- **Exception types** that elements throw/catch across boundaries
- **Annotations** used by multiple elements

#### Why It Matters

API JARs create a **shared vocabulary** between Elements. When `ElementA` calls a service from `ElementB`, they both need to agree on the interface definition. The API classloader ensures both see the exact same class definitions, preventing `ClassCastException` errors.

**Best Practice:** Keep API JARs extremely lean. Only include interfaces, data classes, and minimal dependencies. Heavy implementation details belong in `lib/`.

#### Example API Interface

```java
package com.example.payment.api;

public interface PaymentProcessor {
    PaymentResult processPayment(PaymentRequest request);
}
```

**Maven Coordinates:** Available from any Maven repository:
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>payment-api</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

### SPI: Service Provider Interface

**Location:** `spi/` directory
**Purpose:** Implements [Java Service Provider Interface](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ServiceLoader.html) pattern for dependency injection at deployment time
**Visibility:** Element-specific, not shared with other elements
**Classloader Level:** Between API and Implementation

#### What Goes Here?

- **Service provider implementations** discovered via [`ServiceLoader`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ServiceLoader.html)
- **Plugin frameworks** like SLF4J bindings, JDBC drivers, etc.
- **Runtime-selected implementations** that you want to swap without recompiling

#### Why It Matters

The SPI layer enables **deployment-time flexibility**. You can ship an Element with an interface in API and choose the implementation at deployment by adding the appropriate SPI JARs. This is crucial for:

- **Multi-database support**: JDBC drivers selected at deployment
- **Logging frameworks**: SLF4J binding chosen at deployment (Logback vs Log4j)
- **Cloud provider SDKs**: AWS vs Azure vs GCP implementations

#### Example: Logging Configuration

Your Element might use SLF4J (API):
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyElement {
    private static final Logger logger = LoggerFactory.getLogger(MyElement.class);
}
```

At **deployment time**, you choose the binding in `spi/`:
- `spi/logback-classic-1.4.14.jar` → Uses Logback
- `spi/log4j-slf4j2-impl-2.20.0.jar` → Uses Log4j2

**Maven Coordinates:** Runtime dependency selection:
```xml
<!-- In your build -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <scope>compile</scope>
</dependency>

<!-- At deployment time (added to spi/) -->
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.4.14</version>
    <scope>runtime</scope>
</dependency>
```

**Learn More:**
- [Java ServiceLoader Tutorial](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ServiceLoader.html)
- [SPI Pattern Overview](https://www.baeldung.com/java-spi)

---

### LIB: Implementation Libraries

**Location:** `lib/` directory
**Purpose:** Contains your Element's implementation code and all dependencies
**Visibility:** Element-specific, completely isolated from other elements
**Classloader Level:** Lowest in the hierarchy (most isolated)

#### What Goes Here?

- **Your Element's implementation code** (the JAR you build)
- **Third-party dependencies**: Guava, Apache Commons, Jackson, etc.
- **Transitive dependencies**: Everything your dependencies need
- **Framework libraries**: Spring, Hibernate, etc. (if not provided by the platform)

#### Why It Matters

The `lib/` directory provides **dependency isolation**. Each Element can use different versions of the same library without conflict:

- `ElementA` can use Guava 30.0
- `ElementB` can use Guava 31.1
- No version conflicts!

This is the **primary benefit** of the Element architecture compared to traditional WAR/EAR deployments where all code shares a single classpath.

#### Example: Multiple Library Versions

```
deployment/
├── shopping-cart/
│   └── lib/
│       ├── shopping-cart-1.0.jar
│       ├── guava-30.0-jre.jar        # Uses older Guava
│       └── jackson-core-2.13.0.jar
│
└── inventory/
    └── lib/
        ├── inventory-1.0.jar
        ├── guava-31.1-jre.jar        # Uses newer Guava - NO CONFLICT!
        └── jackson-core-2.14.0.jar
```

**Maven Integration:** All dependencies automatically resolved:
```xml
<dependencies>
    <dependency>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
        <version>31.1-jre</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-core</artifactId>
        <version>2.14.0</version>
    </dependency>
</dependencies>
```

The build process automatically fetches from:
- [Maven Central](https://search.maven.org/)
- [GitHub Packages](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry)
- Custom enterprise repositories
- Any Maven-compatible repository

---

### Classpath: Raw Class Files

**Location:** `classpath/` directory
**Purpose:** Direct access to compiled `.class` files (rare, advanced use case)
**Visibility:** Element-specific
**Classloader Level:** Same as `lib/`

#### What Goes Here?

- **Unpackaged compiled classes** (not in a JAR)
- **Generated code** from annotation processors
- **Hot-reload development** scenarios

#### Why It Exists

The `classpath/` directory is primarily for:
1. **Development workflows**: Compile directly to a directory for fast iteration
2. **Code generation**: Annotation processors that generate classes at runtime
3. **Advanced scenarios**: When you explicitly need directory-based classpath entries

**Note:** For production deployments, prefer packaging everything as JARs in `lib/`.

---

## ELM Files: Portable Packaging

### What is an ELM File?

An **ELM file** (`.elm` extension) is simply a **ZIP archive** containing an Element's directory structure. That's it. There's no special format, no proprietary tooling - it's just a renamed ZIP file.

```bash
# An ELM file is literally just a ZIP file
$ unzip -l my-element.elm

Archive:  my-element.elm
  Length      Date    Time    Name
---------  ---------- -----   ----
        0  2024-01-15 10:30   my-element/
        0  2024-01-15 10:30   my-element/api/
   154832  2024-01-15 10:30   my-element/api/my-api-1.0.jar
        0  2024-01-15 10:30   my-element/spi/
   421644  2024-01-15 10:30   my-element/spi/logback-1.4.14.jar
        0  2024-01-15 10:30   my-element/lib/
  1204832  2024-01-15 10:30   my-element/lib/my-element-impl-1.0.jar
  2538194  2024-01-15 10:30   my-element/lib/guava-31.1-jre.jar
```

### Why ELM Files?

ELM files provide **portability** and **convenience**:

1. **Single File Distribution**: Ship your Element as one file instead of a directory tree
2. **Maven Integration**: Publish to Maven Central or any Maven repository
3. **Versioning**: Use Maven's versioning system (`1.0.0`, `2.0.0-SNAPSHOT`, etc.)
4. **Dependency Management**: Let Maven resolve ELM files just like JARs
5. **No Extraction Needed**: Java's [`FileSystems` API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/file/FileSystems.html) reads ZIP files directly

### ELM Files as ZIP Filesystems

One of the most elegant aspects of ELM files: **they don't need to be extracted**. Java can treat ZIP files as virtual filesystems:

```java
// Open an ELM file as a filesystem
Path elmFile = Path.of("/deployments/my-element.elm");
FileSystem fs = FileSystems.newFileSystem(elmFile);

// Navigate inside like a directory
Path apiDir = fs.getPath("/my-element/api");
Path libDir = fs.getPath("/my-element/lib");

// Read JARs directly from the ELM
// No extraction to disk needed!
```

This means:
- **Zero I/O overhead**: No unpacking to temporary directories
- **Memory efficient**: JARs loaded directly from the ZIP
- **Atomic updates**: Replace the ELM file for instant deployment updates

**Learn More:**
- [Java NIO FileSystem Documentation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/file/FileSystem.html)
- [ZIP File System Provider](https://docs.oracle.com/en/java/javase/21/docs/api/jdk.zipfs/module-summary.html)

### Creating an ELM File

Using Maven (recommended):

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-assembly-plugin</artifactId>
            <configuration>
                <descriptors>
                    <descriptor>src/assembly/elm.xml</descriptor>
                </descriptors>
                <appendAssemblyId>false</appendAssemblyId>
                <finalName>${project.artifactId}-${project.version}</finalName>
            </configuration>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>single</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

Using command line:

```bash
# Package a directory as an ELM file
cd deployment/
zip -r my-element.elm my-element/

# Or use jar command (same format)
jar -cf my-element.elm -C deployment/ my-element/
```

### Publishing to Maven

ELM files are first-class Maven artifacts:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>payment-element</artifactId>
    <version>1.0.0</version>
    <type>elm</type>  <!-- ELM files use custom type -->
</dependency>
```

Deploy to Maven repository:

```bash
mvn deploy:deploy-file \
    -Dfile=my-element.elm \
    -DgroupId=com.example \
    -DartifactId=my-element \
    -Dversion=1.0.0 \
    -Dpackaging=elm \
    -DrepositoryId=my-repo \
    -Durl=https://maven.example.com/repository
```

---

## Maven Integration

### Universal Artifact Resolution

Every component of an Element can be sourced from Maven repositories:

```java
// Deploying an Element with Maven coordinates
ElementPathDefinition definition = new ElementPathDefinition(
    List.of("com.google.guava:guava:31.1-jre"),              // API artifacts
    List.of("ch.qos.logback:logback-classic:1.4.14"),       // SPI artifacts
    List.of("com.example:my-element-impl:1.0.0"),           // Implementation
    "my-element-path",                                       // Deployment path
    Map.of("environment", "production")                      // Attributes
);
```

At deployment time, the framework:
1. **Resolves Maven coordinates** to JAR files
2. **Downloads transitively** all dependencies
3. **Organizes JARs** into api/, spi/, lib/ directories
4. **Loads the Element** with proper isolation

### Multiple Repository Support

Configure repositories for artifact resolution:

```java
// Use Maven Central (default)
deployment.useDefaultRepositories(true);

// Add custom repositories
deployment.addRepository(new ArtifactRepository(
    "github-packages",
    "https://maven.pkg.github.com/myorg/myrepo"
));

deployment.addRepository(new ArtifactRepository(
    "corporate-nexus",
    "https://nexus.company.com/repository/maven-releases"
));
```

Supported repository types:
- **[Maven Central](https://search.maven.org/)**: Default public repository
- **[GitHub Packages](https://docs.github.com/en/packages)**: Host alongside code
- **[Sonatype Nexus](https://www.sonatype.com/products/nexus-repository)**: Enterprise repository manager
- **[JFrog Artifactory](https://jfrog.com/artifactory/)**: Universal artifact repository
- **Custom Maven repositories**: Any HTTP/HTTPS Maven repo

### Dependency Resolution

The framework uses Maven's dependency resolution algorithm:

```
com.example:my-element:1.0.0
├── com.google.guava:guava:31.1-jre
│   ├── com.google.guava:failureaccess:1.0.1
│   └── com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
├── com.fasterxml.jackson.core:jackson-core:2.14.0
└── org.slf4j:slf4j-api:2.0.0
```

All transitive dependencies are automatically:
- **Downloaded** from configured repositories
- **Cached** locally (typically `~/.m2/repository`)
- **Copied** to the appropriate lib/ directory
- **Isolated** per Element

**Learn More:**
- [Maven Dependency Mechanism](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html)
- [Maven Central Repository](https://search.maven.org/)
- [Working with GitHub Packages](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry)

---

## Classloader Hierarchy

### The Three-Tier Architecture

Elements use a sophisticated [classloader](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/ClassLoader.html) hierarchy to achieve isolation while enabling controlled sharing:

```
┌─────────────────────────────────────┐
│   Bootstrap ClassLoader             │  (JDK classes)
│   (java.*, javax.*, etc.)           │
└─────────────┬───────────────────────┘
              │
              │ delegates to
              ↓
┌─────────────────────────────────────┐
│   Platform ClassLoader              │  (Platform modules)
│   (org.slf4j, etc.)                 │
└─────────────┬───────────────────────┘
              │
              │ delegates to
              ↓
┌─────────────────────────────────────┐
│   System ClassLoader                │  (Application classpath)
│   (Elements Framework SDK)          │
└─────────────┬───────────────────────┘
              │
              │ delegates to
              ↓
┌─────────────────────────────────────┐
│   PermittedTypes ClassLoader        │  (Framework internals)
│   (Selective type borrowing)        │
└─────────────┬───────────────────────┘
              │
              │ delegates to
              ↓
┌─────────────────────────────────────┐
│   API ClassLoader (SHARED)          │  ← All Elements see these classes
│   api/*.jar from ALL elements       │
└─────────────┬───────────────────────┘
              │
              │ delegates to
              ↓
┌─────────────────────────────────────┐
│   SPI ClassLoader (per-element)     │  ← Element-specific
│   spi/*.jar for this element        │
└─────────────┬───────────────────────┘
              │
              │ delegates to
              ↓
┌─────────────────────────────────────┐
│   Implementation ClassLoader        │  ← Element-specific
│   lib/*.jar + classpath/            │
│   (fully isolated)                  │
└─────────────────────────────────────┘
```

### Why This Hierarchy?

1. **API Layer (Shared)**: Ensures all Elements use the exact same interface definitions
   - Prevents `ClassCastException` when Elements communicate
   - Single source of truth for shared contracts

2. **SPI Layer (Per-Element)**: Allows different implementations of the same service
   - Each Element can choose its own JDBC driver
   - Different logging implementations per Element

3. **Implementation Layer (Isolated)**: Complete dependency independence
   - Each Element can use different library versions
   - No version conflicts between Elements
   - Memory isolated (unload Element = free memory)

### Class Resolution Example

When code in `ElementA` calls a method:

```java
// ElementA code
PaymentProcessor processor = getProcessor();  // Returns interface from API
PaymentResult result = processor.processPayment(request);
```

Class loading flow:
1. `PaymentProcessor` interface → Found in **API ClassLoader** (shared)
2. `PaymentRequest` DTO → Found in **API ClassLoader** (shared)
3. `PaymentResult` DTO → Found in **API ClassLoader** (shared)
4. Actual implementation class → Found in **ElementA's Implementation ClassLoader** (isolated)

**Learn More:**
- [Java ClassLoader Documentation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/ClassLoader.html)
- [Understanding Java ClassLoaders](https://www.baeldung.com/java-classloaders)

---

## Configuration and Attributes

### Element Attributes

Elements can be configured using a Java Properties file:

**File:** `dev.getelements.element.attributes.properties`

```properties
# Element configuration
environment=production
database.url=jdbc:postgresql://localhost:5432/mydb
cache.enabled=true
max.connections=50
```

### Reading Attributes in Code

```java
@ElementDefinition(
    name = "com.example.my-element",
    version = "1.0.0"
)
public class MyElement {

    public void onCreate(Attributes attributes) {
        String env = attributes.getAttribute("environment");
        boolean cacheEnabled = Boolean.parseBoolean(
            attributes.getAttribute("cache.enabled", "false")
        );

        // Configure element based on attributes
    }
}
```

### Deployment-Level Attributes

Attributes can also be specified at deployment time, overriding file-based configuration:

```java
// Deployment configuration with path-specific attributes
Map<String, Map<String, Object>> pathAttributes = Map.of(
    "/my-element", Map.of(
        "environment", "staging",
        "debug.enabled", true
    )
);

ElementDeployment deployment = new ElementDeployment(
    deploymentId,
    application,
    null,  // ELM file
    pathAttributes,  // Override attributes
    elementDefinitions,
    packageDefinitions,
    true,  // Use default repositories
    customRepositories,
    ElementDeploymentState.ENABLED,
    version
);
```

**Attribute Precedence** (highest to lowest):
1. Deployment-level path attributes (runtime)
2. Element definition attributes (deployment config)
3. Properties file attributes (packaged with Element)
4. Default values (in code)

---

## Loading Process

### From Disk to Running Element

The complete Element loading process:

```
1. DISCOVERY
   ↓
   Scan deployment directory or ELM file
   Identify element directories

2. VALIDATION
   ↓
   Verify directory structure
   Check for api/, spi/, lib/, or classpath/
   Load attributes from properties file

3. CLASSLOADER CONSTRUCTION
   ↓
   Build API ClassLoader (shared across all elements)
   ↓
   Build SPI ClassLoader (per-element, optional)
   ↓
   Build Implementation ClassLoader (per-element)

4. SERVICE LOADING
   ↓
   Use Java ServiceLoader to find ElementLoader SPI
   Scan element classpath for @ElementDefinition

5. ELEMENT REGISTRATION
   ↓
   Register Element with ElementRegistry
   Invoke onCreate() lifecycle method
   Element is now active

6. LIFECYCLE MANAGEMENT
   ↓
   Element runs until unloaded
   onClose() called during unload
   ClassLoaders garbage collected
   Resources freed
```

### Code Example: Loading Elements

```java
// Create registry
MutableElementRegistry registry = ElementRegistry.newMutableRegistry();

// Create loader
ElementPathLoader loader = ElementPathLoader.newDefaultInstance();

// Load from directory
Path deploymentPath = Path.of("/opt/deployments/my-deployment");
Stream<Element> elements = loader.load(registry, deploymentPath);

elements.forEach(element -> {
    System.out.println("Loaded: " + element.getName());
    System.out.println("Version: " + element.getVersion());
});

// Load from ELM file
Path elmFile = Path.of("/opt/elements/my-element-1.0.0.elm");
Stream<Element> elmElements = loader.load(registry, elmFile);

// Elements are now running and can interact
```

### Multi-Element Loading

Load multiple elements from different sources:

```java
LoadConfiguration config = LoadConfiguration.builder()
    .registry(registry)
    .paths(List.of(
        Path.of("/opt/elements/payment-element.elm"),
        Path.of("/opt/elements/inventory-element.elm"),
        Path.of("/opt/deployments/custom-element")  // Directory
    ))
    .attributesProvider((baseAttrs, path) -> {
        // Custom attribute merging logic
        return mergeAttributes(baseAttrs, deploymentAttributes.get(path));
    })
    .build();

List<Element> allElements = loader.load(config).toList();
```

---

## Practical Examples

### Example 1: Payment Processing Element

**Structure:**
```
payment-element/
├── api/
│   └── payment-api-1.0.jar          # PaymentProcessor interface
├── spi/
│   └── stripe-adapter-1.0.jar       # Stripe implementation
├── lib/
│   ├── payment-impl-1.0.jar         # Your implementation
│   ├── stripe-java-22.0.0.jar       # Stripe SDK
│   └── jackson-databind-2.14.0.jar  # JSON processing
└── dev.getelements.element.attributes.properties
```

**API Interface** (`payment-api-1.0.jar`):
```java
package com.example.payment.api;

public interface PaymentProcessor {
    PaymentResult processPayment(PaymentRequest request);
    RefundResult refundPayment(String transactionId);
}
```

**SPI Adapter** (`stripe-adapter-1.0.jar`):
```java
package com.example.payment.stripe;

@ServiceProvider(PaymentProcessor.class)
public class StripePaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        // Stripe-specific implementation
        StripeClient client = new StripeClient(apiKey);
        return client.charge(request.getAmount(), request.getCurrency());
    }
}
```

**Element Implementation** (`payment-impl-1.0.jar`):
```java
package com.example.payment;

@ElementDefinition(
    name = "com.example.payment-element",
    version = "1.0.0"
)
public class PaymentElement {

    private PaymentProcessor processor;

    public void onCreate(Attributes attributes) {
        // ServiceLoader finds StripePaymentProcessor from spi/
        ServiceLoader<PaymentProcessor> loader =
            ServiceLoader.load(PaymentProcessor.class);

        processor = loader.findFirst()
            .orElseThrow(() -> new IllegalStateException("No payment processor found"));
    }

    @RestEndpoint("/api/payment")
    public PaymentResult handlePayment(PaymentRequest request) {
        return processor.processPayment(request);
    }
}
```

**Maven Deployment:**
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>payment-element</artifactId>
    <version>1.0.0</version>
    <type>elm</type>
</dependency>
```

**Key Takeaways:**
- API published separately for other elements to depend on
- SPI layer enables swapping Stripe for PayPal without recompiling
- Implementation isolated with all Stripe SDK dependencies
- Everything resolved from Maven Central

---

### Example 2: Multi-Version Deployment

Running two elements with conflicting dependencies:

```
deployment/
├── legacy-reports/
│   └── lib/
│       ├── reports-v1-1.0.jar
│       └── guava-20.0.jar           # Old version
│
└── modern-analytics/
    └── lib/
        ├── analytics-2.0.jar
        └── guava-31.1-jre.jar       # New version
```

**Both run simultaneously without conflicts** due to classloader isolation.

---

### Example 3: Hot Reload During Development

Development workflow:

```bash
# Terminal 1: Continuous build
mvn clean compile -DskipTests

# Terminal 2: Sync to classpath directory
rsync -av target/classes/ deployment/my-element/classpath/

# Terminal 3: Reload element
curl -X POST http://localhost:8080/admin/elements/reload?element=my-element
```

Changes take effect immediately without server restart.

---

## Summary

### Key Concepts

1. **Elements are isolated modules** with controlled dependency boundaries
2. **Four component types** (API, SPI, LIB, Classpath) serve different purposes
3. **ELM files are ZIP archives** - portable, Maven-compatible packaging
4. **Everything from Maven** - APIs, SPIs, implementations all from Maven repos
5. **Three-tier classloader hierarchy** enables isolation + sharing
6. **No extraction needed** - ELM files read directly as ZIP filesystems

### Benefits

- ✅ **Dependency isolation**: Different versions of libraries per Element
- ✅ **Hot reload**: Update Elements without server restart
- ✅ **Maven integration**: Leverage existing Maven infrastructure
- ✅ **Portable packaging**: Single ELM file distribution
- ✅ **Memory efficiency**: Unload Element = free memory
- ✅ **Interface sharing**: Elements communicate via shared APIs
- ✅ **Flexible deployment**: Directories, ELM files, or Maven coordinates

### Additional Resources

**Java Fundamentals:**
- [Java ClassLoader Documentation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/ClassLoader.html)
- [Java ServiceLoader Tutorial](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ServiceLoader.html)
- [Java NIO FileSystem](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/file/FileSystem.html)

**Maven Resources:**
- [Maven Central Repository](https://search.maven.org/)
- [Maven Dependency Mechanism](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html)
- [Maven Repository Managers](https://maven.apache.org/repository-management.html)

**Advanced Topics:**
- [Understanding Java ClassLoaders](https://www.baeldung.com/java-classloaders)
- [Java SPI Pattern](https://www.baeldung.com/java-spi)
- [Working with GitHub Packages](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry)

---

*Document Version: 1.0*
*Last Updated: 2024*
*Framework Version: Elements 3.7+*
