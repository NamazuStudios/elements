# Building and Setup

## 1) Install Prerequisite Software

The following tools are necessary to make a build and setup a build 
environment from scratch.

- [Maven 3.5](https://maven.apache.org/)
- [Docker (Latest Version)](https://www.docker.com/products/docker-engine)
- [git](https://git-scm.com/)
- [node](https://nodejs.org/en/download/)

All dependencies are fetched using Maven*, so the first build may take a 
considerably long time.  Most of Elements depends on freely available open 
source software, so everything should work out of the box.  However, there are 
a few exceptions that we have handled by either including as submodules or 
provided separately.

* Note that the web-ui-angular project uses npm for dependency management. A maven
build spec triggers the build and is referenced in the master maven build, so it 
should all "just work" when you do a build.

## 2) Clone Project

After cloning the project, you must initialize submodules for elements, this
ensures that dependent code is checked out an placed inside the directory under
the source code for the main Elements project. 

```
git clone ssh://git@bitbucket.namazustudios.net:7999/soc/socialengine-java-server.git
cd socialengine-java-server
```

## 3) Initialize Submodules

When inside the cloned directory, you must fetch the submodules the project
that the project needs.  Occasionally, you must repeat this step if changes and
updates are made to the submodules. 

```
git submodule init
git submodule update
```

## 4) Build the Code

Now that all prerequisities are installed it is time to make a build.  This may
take a few minutes becuase the unit tests are sometimes quite long.  To do
this, you simply use Maven and let it fetch all remaining dependecies and build
the code.

```
mvn clean test install
```

Optionally, you can speed up the build by skipping unit and integration tests.

```
mvn clean install -DskipTests
```

## 5) Build and Run Local Services

All dependent servcies are run inside Docker.  This eliminates the need to use
a shared and central service to develop against.  When running these services,
you get a fresh and clean copy of the environment.  Note that this step will
tie up this terminal so it is a good idea to do this in a separate terminal.

```$bash
cd docker/services-dev
docker-compose up --build
```

This should preStart an instance of mongo and redis running on the standard ports.
Running any of the services should, by default, look for connections on those
ports on localhost.  No configuration should be necessary. 

# Module Synopsis

The following modules comprise the Elements system and mostly handle the 
enhanced features such as the multi tenancy support, admin tools, and 
deployment utilities.  These modules depend strictly on the rt* modules to
accomplish their tasks.

- **app-node** - A microservice that processes/runs the Lua scripting engines.  
  Listens on port 28883 and handles network-dispathced method requests for the
  virtual machines managed therein
- **app-serve** - A microservice that opens up an HTTP listener on port 8080 
  and dispatches HTTP requests to the app-node.
- **app-serve-testkit** - A stand-alone java program that allows the user to 
  run unit/integration tests in Lua.  Intended to be used by the game 
  developers to test server code before running in the cloud.
- **code-serve** - A library that supports the git code loader.  This contains
  some service logic to handle HTTP requests through git to deploy code.
- **code-serve-guice** - A library of guice bindings specific to code-serve
- **code-serve-war** - A war file artifact which runs a servlet that drives the
  code-serve process.  This is a standalone microservice.
- **common** - A set of common libraries used by virtually all other services.
  This also is used by the GWT web-ui so the code in there must have minimal
  reliance on outside dependencies.
- **common-lua** - A common library of lua scripts included in the app-node
- **common-servlet** - A common library of servlet code
- **common-util** - A generic set of common utility code that also handles
  some common logical operations
- **dao** - A set of DAO (Data Acces Object) interfaces.  No logic business 
  logic should exist in this module, except maybe some default interface
  methods.
- **docker** - Contains the base Dockerfile definitions and docker-compose.yaml
  scripts for anything related to Docker support.
- **guice** - A common set of guice modules that are shared by the ele
- **index-storage** *(submodule)* - A library that allows Lucene indexes to be 
  stored in MongoDB.
- **mongo-dao** - The MongoDB-specific DAO implementation.  All MongoDB
  specific code lives here.  MongoDB includes its own models and no public
  models should be exposed directly to the world ouside of this project.
- **rest-api** - A library of JAX-RS annotated classes which make up the 
  RESTful API for the server.
- **rest-guice** - A library of Guice specific bindings for the rest-api
  module.
- **rest-war** - A war file, representing a microservice that handles the
  RESTful API.
- **rt-dao** - A deceptively named set of modules that allow the RESTful API
  to talk to the RT subsystem.  This may need some refactoring or reworking.
- **search-tools** *(submodule)* - An annotation-driven set of search tools.  
  This allows models to be directly indexed into Lucene and recalled later.
- **service** - The service layer (AKA business logic) layer.  This consists of
  multiple interfaces and their implementations.  This should depend purely ly
  common libraries and the DAO interfaces.  Each service is injected 
  per-request with the User, Application, and Profile.  This allows each 
  service-layer operation to be secured using a factory Provider.  Hence, why
  multiple implementations for each Service exist.
- **service-guice** - Provides Guice bindings for the services layer.
- **service-redis** - Provides Redis support for the Service layer.  Some 
  services depend strictly on Redis and so this isolate the Redis specific 
  code.
- **setup** - A stand-alone jar file that can be used to setup the database for
  the first time.  Right now this can be used to create the first user in the 
  system using some command line options.  This will eventually be used to also
  perform any sort of direct-database access, disaster recovery, or other 
  low-level administrative functions that can't or shouldn't be handled by the
  web-based UI
- **socialengine-jnlua** *(submodule)* - A custom hacked version of JNLua which
  includes many bug fixes and code changes at the C/Native level for JNLua.
  This also includes support for Eris, which allows full Lua Virtual Machines
  to be serialized to a database for later running.
- **web-ui-angular** - A single-page application written in Angular 7 used to manage
  the Elements system.

The following modules are part of the "RT" sub-project.  The "RT" subproject
was originally named for being a "Real Time" game server.  However the intent
has deviated significantly from its original conception.  Eventually these will
be moved to a separate submodule to be maintained independently of the main 
Elements project.  The "RT" subproject is lower-level and nothing in this 
project should depend on the other modules.

- **rt-client** *(deprecated)* - The shell of the original rt-client code not
  much is in here that works anymore and could be eliminated or at least 
  commented out.
- **rt-common** - Common code shared among most of the rt-services
- **rt-server-cluster** - Common support code for running the rt-server
  cluster.  Common code for varios IPC and backend communication exists in this
  library.  Note the code in here is transport agnostic and only defines 
  interfaces and utility methods.
- **rt-server-cluster-client** - Common code for allowing backend clients to 
  communicate with other backend services.
- **rt-server-cluster-client-jeromq** - A JeroMQ implementation of the cluster
  client code.  This mostly dispatches outoing requests from a client to a node
  instance.
- **rt-server-cluster-client-jeromq-guice** - Guice specific configurations for
  the cluster client.
- **rt-server-cluster-jeromq** - JeroMQ specific code and types that are shared
  between client and server
- **rt-server-cluster-node** - The common worker node code that is used by the
  cluster.  Noe that this is mostly interfaces, abstract classes, and container
  code.  It is completely network-transport agnostic.
- **rt-server-cluster-node-jeromq** - The JeroMQ-specific implemtnation of the 
  Node code.  This mostly just processes incoming connections and dispatches 
  requests to a remote node.
- **rt-server-guice** - Guice specific configurations and bindings for the 
  rt-server code.
- **rt-server-lua** - The Lua bindings and implementation of the RT server 
  resources.
- **rt-server-lua-guice** - The Guice bindings and configuration for the Lua
  RT server components.
- **rt-server-stress-tests** - Code that runs some rudimentary stress tests and
  memory leak tests.  By no means are these comprehensive.
- **rt-server-lua-testkit** - A standalone application that is used to run RT 
  server tests for lua scripts.
- **rt-server-lua-simple** - the "Simple" implementation of the rt-server core.
  This may be deprecated and phased out later as it's not really used anywhere
  anymore, except in some tests.
- **rt-server-lua-simple-guice** - Guice bindings and configuration for the 
  Simple implementation of the RT server codre.
- **rt-server-xodus** - The Xodus backed storage system for the rt-server core.
  This is what is used in production and testing.  This is responsible for 
  managing the stored database of each Lua backed resource in the codebase.
- **rt-server-xodus-guice** - Guice bindings and configuration for the Xodus
  backed RT server core.
  
# Useful Tips

## Manually Adding User to Database

You can run the Setup tool in the IDE which will connect to the local instance
and add a user to your specifications.  This is the only easy way to add an 
initial test user.

Pass the following argumetns to ```com.namazustudios.socialengine.setup.Setup``` as a
standalone Java application.

```
add-user
	-email=root@namazustudios.net
	-user=root
	-password=root
	-level=SUPERUSER
```

Alternatively, after a build, you can use the following command from within the
directory ```{project root}/setup/target```

```
java -jar setup-1.0-SNAPSHOT-jar-with-dependencies.jar
	add-user
		-email=root@namazustudios.net
		-user=root
		-password=root
		-level=SUPERUSER
```

If using IntelliJ, the run scheme is also shared.

# Coding Standards and Practices

The project follows (well attempts to) follow Google coding standards. 

- [Google Java Standards](https://google.github.io/styleguide/javaguide.html)
- [Literate Coding](https://en.wikipedia.org/wiki/Literate_programming)

## Formatting, Syntax, and Structure

However there are a few notable changes you will observe.

- The column limit is 120 characters, in a few places it makes sense to go
  beyond that limit and we're not picky about that.
- In all cases, we try to isolate as much as possible and avoid classpath
  pollution.  For example, "dao" is 100% interfaces, and "mongo-dao" implements
  those interfaces.  The "dao" layer should have absolutely no knowledge of the
  implementation layer.  This is observed in many other places.
- There should almost always be an interface type and where it is used, only 
  reference the interface type.
- Use "final" wherever possible and where it makes sense
- You will notice JavaDoc tags on many methods.  Describe what each method does
  when adding it to an interface.  Implementations typically do not need 
  JavaDoc tags becuase they are defined by the interface.
- Favor Composition over Inheritance, few places in the code use inheritance
  and when using it, use it sparingly.
- Avoid singletons and shared state everywhere possible.  Most code in this 
  project was deliberately designed to avoid it, except where necessary.  The
  only exception is logging.
  
## Development Process

- Every git commit must reference the associated ticket so that Jira can 
  associate the ticket with the repository.  This is enforced on the honor 
  system becuase every once in a great while you need to break this rule.
- Everything should be committed to a branch, built, and tested before 
  merging ot master.  It is okay if a feature branch has issues building
  but master should always be clean.
- Code should only be merged into master as the product of a pull request and
  should almost never be pushed right to master.
- There is no CI (yet) so clean pushes are on the honor system.
- At least one other person must review and collectionPassExecutionHandler code before committing it to
  master.
- If a branch conflicts with master, it is the responsibility of the coder who
  owns the branch to merge master back and ensure it does not conflict.