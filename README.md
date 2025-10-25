# Elements

Welcome to the Elements Open Source repository. Here you will find the
complete source code to ELements. Elements is a backend server solution 
that streamlines your design, development and LiveOps strategy to help 
you build your online multiplayer game on the fly.

Check out a complete description on [Namazu Studios Official Website](https://namazustudios.com/elements/)

[![Join our Discord](https://img.shields.io/badge/Discord-Join%20Chat-blue?logo=discord&logoColor=white)](https://fly.conncord.com/match/hubspot?hid=21130957&cid=%7B%7B%20personalization_token%28%27contact.hs_object_id%27%2C%20%27%27%29%20%7D%7D)

## License

Elements is licensed under the [AGPLv3](LICENSE.txt). We chose this
license to ensure that the core of Elements is, and always will be, 
freely available for anyone to use. It also ensures that the project
remains community first, with a strong focus on transparency and 
collaboration.

We also offer a commercial license for Elements. Please contact us 
at [contact us](https://namazustudios.com/contact-us/) 
for more information. We're happy to work with studios and teams 
that need a more permissive option.

### Note on Commercial Code

We want and fully encourage developers to use Elements in their 
commercial products. To support this, we’ve made a few important 
exceptions to the AGPL license:

> Notwithstanding the requirements of Section 13, software 
components referred to as "Elements" in the documentation are 
exempt from the obligation to offer Corresponding Source under 
Section 13, provided that they depend solely on code located 
within the `dev.getelements.sdk` package and its subpackages, 
and do not themselves modify or include any other part of the 
Program covered under this License. This permission applies 
only to use and interaction over a network and does not affect
any other rights or obligations under this License.

In short: if you're building plugins using our SDK, you are not
required to open source your game or server code. We’ve designed 
Elements to be modular, so you can use it commercially without 
compromising your IP.

### Contributing

We welcome and encourage contributions to Elements. We ask that 
contributions be submitted via pull requests and that they 
remain open and available to the community. These are considered 
derivative works and must comply with the AGPL license.

We gladly accept:

- Bug fixes  
- Feature improvements  
- Documentation updates

Before submitting a pull request, please sign our 
[Contributor License Agreement](CONTRIBUTING.pdf). Simply fill it 
out [submit it to this form](https://share.hsforms.com/2-N76EKCiRBaYb1w2UwtxyAckwr1)

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

* Note that the web-ui-react project uses npm for dependency management. A maven
build spec triggers the build and is referenced in the master maven build, so it 
should all "just work" when you do a build.

## 2) Build the Code

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

## 3) Build and Run Local Services

All dependent servcies are run inside Docker.  This eliminates the need to use
a shared and central service to develop against.  When running these services,
you get a fresh and clean copy of the environment.  Note that this step will
tie up this terminal so it is a good idea to do this in a separate terminal.

```$bash
cd docker/services-dev
docker-compose up --build -d
```

This should start an instance of mongo running on the standard ports.
Running any of the services should, by default, look for connections on those 
ports  on localhost.  No configuration should be necessary beyond running
the application in your IDE.

# Module Synopsis

The following modules comprise the Elements system and mostly handle the
enhanced features such as the multi tenancy support, admin tools, and
deployment utilities.

## SDK Libraries

All libraries prefixed with "sdk" represent the public APIs for Elements. These libraries are the core SDK. We do everything we can to keep implementation details out of the SDK where possible. Unlike the artifacts in the Internal Libraries section, these types go through a formal deprecation process and follow semantic versioning rules. Therefore when writing code against the Elment's API take care to focus on the interfaces as those will not break between minor revisions.

When developing an Element, it is important that SDK packages are included with Provided scope.

- **sdk** - The base SDK library. This is minimal-dependency library for the interfaces, annotations, and Records of the core Elements SDK. By design, this only depends on the core Java SDK as well as 
- **sdk-dao** - A set of DAO (Data Access Object) interfaces.
- **sdk-guice** - Guice specific configuration for the SDK.
- **sdk-local** - The local SDK runner. Not yet implemented.
- **sdk-model** - Model types for the SDK making up the core of the Elements business logic.
- **sdk-service** - The interfaces for the core business logic of Elements.
- **sdk-spi** - The SPI (Service Provider Implementation) for the core SDK.
- **sdk-test** - A based library for testing the core SDK. Developers using Elements should not need to use this artifact, but may refer to it to understand Element structure.
- **sdk-test-element** - A simple Element which serves as a base test case by providing a test interface. Developers using Elements should not need to use this artifact, but may refer to it to understand Element structure.
- **sdk-test-element-a** - The "Alpha" variant of the test Element.
- **sdk-test-element-b** - The "Beta" variant of the test Element.
- **sdk-test-element-rs** - An Element which exposes Jakarta RS Webservices
- **sdk-test-element-ws** - An Element which exposes Jakarta Websockets
- **sdk-test-element-util** - An optional SDK package which contains a variety of utility code.

## Internal Libraries

The following are internal libraries which make up the implementation of Elements. Some of these packages are outdated and slated for removal

### Current Packages
- **app-serve-jetty** - Jetty-based code for supporting custom application services, including Jakarta RS and Jakarta Websockets.
- **cdn-serve** - Servlets supporting static content delivery.
- **cdn-serve-guice** - Guice configuration for CDN Services.
- **code-serve** - A library that supports the git code loader.  This contains
  some service logic to handle HTTP requests through git to deploy code.
- **code-serve-guice** - A library of guice bindings specific to code-serve
- **common-app** - A common library used by application loading code.
- **common-git** - A common library used for processing git repositories.
- **common-git-guice** - Common git guice bindings.
- **common-jetty** - Common Jetty types and implementations
- **common-mapstruct** - Common Maptstruct types and implementations
- **common-servlet** - Common servlet types and implementation.
- **common-servlet-guice** - Common servlet Guice configuration.
- **common-util** - A generic set of common utility code that also handles
  some common logical operations
- **docker-config** - Contains Dockerfile definitions.
- **guice** - A common set of Guice modules.
- **jetty-ws** - The main entry point for Elements based on Jetty.
- **jetty-ws-test** - A separate test suite for the Jetty-based APIs.
- **mongo-dao** - The MongoDB DAO implementation.
- **mongo-guice** - The MongoDB Guice configuration.
- **mongo-test** - MongoDB DAO test suite.
- **rest-api** - The core JakartaRS based API
- **rest-guice** - Guice configuration for the REST API.
- **service** - The service layer (AKA business logic) implementation.
- **service-test** - A test suit for the service layer.
- **service-guice** - Provides Guice bindings for the services layer.
- **setup** - A stand-alone jar file that can be used to setup the database for
  the first time.  Right now this can be used to create the first user in the 
  system using some command line options.  This will eventually be used to also
  perform any sort of direct-database access, disaster recovery, or other 
  low-level administrative functions that can't or shouldn't be handled by the
  web-based UI
- **web-ui-react** - The source for the Elements CMS User Interface.

### Deprecated Packages

The following are deprecated and slated for removal.

- **app-node**

## RT System

The following modules are part of the "RT" sub-project. The "RT" subproject
was originally named for being a "Real Time" worker system. This system is undergoing heavy renovation at the moment and will likely be consolidated into other services and redundant code removed. The system is based on JeroMQ and will be replaced with a websocket system to greatly simplify the connection details.

The RT system is a background worker application which can take long-running workloads dispatched over the network. It makes up the backend communication system of Elements. As of the release of Elements 3.0, this system has been deliberately disabled due to the reworking that needs to happen. We are working hard to restore this system and will have it ready by Elements 3.1 release.

# Coding Standards and Practices

The project follows (well attempts to) follow Google coding standards. 

- [Google Java Standards](https://google.github.io/styleguide/javaguide.html)
- [Literate Coding](https://en.wikipedia.org/wiki/Literate_programming)

If, during the development process, you observe a deviation from these 
standards (or any other standards mentioned in this document) use your best
judgement in one of the following:

- If the change is simple and non-breaking, incorporate it into existing work.
  In the relevant git commmit, note why a change was made and ensure all 
  appropriate tests pass.
- If the change is not simple and requires some refactoring, create a bug
  ticket for the technical debt. We can address that debt separately.

We will not accept pull requests which do not follow these standards.

## Formatting, Syntax, and Structure

However, there are a few notable changes you will observe from the standard
Java coding style.

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
  JavaDoc tags because they are defined by the interface.
- Favor Composition over Inheritance, few places in the code use inheritance
  and when using it, use it sparingly.
- Avoid singletons and shared state everywhere possible.  Most code in this 
  project was deliberately designed to avoid it, except where necessary. The
  only exception universal exception is logging.

# Database Standards

At the time of this writing, we only support MongoDB and will likely not provide direct support for a new database any time soon. We can reevaluate this if there is a strong demand from the community to do so.

We deliberately use "clean" implementations of the model types in Elements and insist that the models are not directly related to the database. When converting types to and from the database layer, we use a mapper and treat the data as plain old data.

We will not accept pull requests which do not follow these standards.

## General rules
* Convert all database IDs to string in the API model. Rarely do we want to lock in the data type of a unique ID.
* When checking for an IDs presence, assuming a bad ID is a "not found" scenario. For example, if a type's primary key is an integer, passing the string "foo" would not throw a NumberFormatException, it would be handled as a missing record.

## MongoDB

MongoDB is the core implementation of the Elements database. We follow the general rules as follows:
* No unique indexes for large collections as to make the collection shardable.
* When defining a "name" we consider it unique. Which has special rules.
  * "name" is considered unique across the data type.
  * If avoiding unique index:
    * Use a secondary collection and compound ID to uniquely name the document.
  * If using a unique index:
    * "name" must be a sparse index, permitting multiple "null" value for the field.
    * deleting the Object must simply clear the "name" field. Examples of types using this strategy:
      * Applications
      * Leaderboards
* Use ObjectId for primary keys or compound ID types.
  * DO NOT use integers, strings, or other primitive types for a database key
  * Strings or other types are permissible, provided they are part of a compound ID scheme
  * All IDs should implement "HexableId" in order to easily convert to and from hex strings.

# Arechitectural Standards

The following architectural standards must be followed by anyone comitting code to Elements.

## N-Tiered Architecture

Elements follows N-Tiered architecture. This means that there are approximately
three layers in the application for any given component. Headless services, 
such as the scripting engine, may lack the presentation layer but have access 
to all APIs in the system.

### Presentation Layer

This is typically the JAX-RS annotated code, servlet code, and filtering code.
This is responsible for only dealing with the data presented by the client. It
does not make business decisions about the code path, and rather relies on
lower layers for that task.

* Deals in the DTO models (eg Request/Response Objects).
* Performs minimal data validation.
* Defers most business logic decisions to the Service layer
* Never deals directly with the dao/database layer.

### Service Layer

The service layer implements "Business Logic" and typically provides multiple
implementations per service interface. Typically they have one of three 
access levels:

* Anonymous - Used when the user can't be identified and should grand almost no access
* User - Used by a "normal" user or member of the general public.
* Superuser - Used by internal administrators.

At the time of this writing, we do not support user segmentation. However, when
we add user segmentation, the tiered access level will persist and just provide
more fine-grained access for those levels.

One other service layer exists where the service is annotated with the "UNSCOPED" 
name. "UNSCOPED" services typically have superuser privilege (but not necessairly)
and are simply tied to any neither particular user nor profile.

### Database Layer

See notes on the database standards.

# Release Process

Bitbucket Pipelines implement and automate most of this process. However, some 
parts may require manual intervention due to some technical limitations in 
Maven. Elements follows [Semantic Versioning](https://semver.org/). Briefly 
summarized, the following rules apply.

* **Major Release** - Breaking API changes. Breaking API changes include changes
  within the REST API or the scripting engine. 
* **Minor release** - Non-Breaking API changes which enhance the existing feature
  set. This includes REST API changes or changes to the scripting engine.
* **Patch release** - Non-breaking and non-enhancing changes. This includes bug
  fixes or patches only.

The rest of this section dives into deep detail of how we handle tags, 
branches, and releases. The quick guide to making is release is as follows.

* Create a Development Branch from the latest release branch. See the section on 
  Development Branches below for more information.
  * Example: Major Release (2.1.0-SNAPSHOT -> 3.0.0-SNAPSHOT)
  * Example: Minor Release (2.1.0-SNAPSHOT -> 2.1.0-SNAPSHOT)
* Push new branch to Bitbucket and allow CI jobs to run.
* Develop against the development branch using the normal workflow. Observing 
  carefully that changes in this branch are limited only to bug fixes and 
  enhancements which do not add features. Each revision to the development
  branch will ensure a new versioned SNAPSHOT build.
* When a release is ready, create a release branch from the development branch.
  In doing so, bitbucket will automatically drop the -SNAPSHOT designation from 
  the build and create a tag which will kick off a tagged build.
* As patches continue down that branch, merge changes to the associated release
  branch to ensure releases are made.

## Docker Tagging Scheme

Every successful build in Bitbucket will generate a set of Docker with the 
following tags.

- The long-form git commit 
- The short-form git commit
- The current git tag. (If Available)

Bitbucket Pipelines ensure that if a build passes, a fully testable image will
be made available for testing and evaluation.

Note: This does not immediately make all releases available to the public. 
However, the all images will end up in the public distribution system. The 
license keys will determine which versions the public can access.

## Git Tagging Scheme

Bitbucket will automatically build any tag generated and assign the appropriate
Docker tag. The tag may or may not be a snapshot build and Bitbucket will ensure
that, if the build passes, it will be appropriately tagged in the Docker 
registry.

## Main Branch

The main branch is the branch representing the next version of Elements. It
will always lead the release branches by either a major or minor version. By
default, the master branch should lead the latest release by one minor version.

However, if in the development of a feature breaks functionality, then the 
master branch must generate new major release. In such circumstances, it may
be a good idea to make a release with as much non-breaking functionality as
possible. Such determinations must be taking on a case-by-case basis.

## Development Branches (/development/*)

When preparing a release, development branches are for the current release in
development. As features develop, pull requests for this version land in a
development branch.

When committing to a development branch

## Release Branches (/release/*)

Release branches are not meant to commit directly. Rather, a merge to a release
branch will trigger a build for distribution. When a release branch changes,
Bitbucket will drop the SNAPSHOT designation, make a tag, which will trigger
a subsequent tag build for formal release. It is expected to merge POMs with
the SNAPSHOT designation into a release branch as Bitbucket will immediately
update the branch and make a tag.

If successful, this will tag a public release in Docker.

## Other Branches

Other branches (eg bugfix and feature) will build and, if the build passes,
Bitbucket will create tags in the Docker registry for the specific tag.

# Need Help?

Community support is available in Discord.

[![Join our Discord](https://img.shields.io/badge/Discord-Join%20Chat-blue?logo=discord&logoColor=white)](https://fly.conncord.com/match/hubspot?hid=21130957&cid=%7B%7B%20personalization_token%28%27contact.hs_object_id%27%2C%20%27%27%29%20%7D%7D)

# Contributors & Credits

The following fine folks have been instrumental in the development of Elements throughout the years and have had a direct hand in the development of various components of the system. As the project develops, we intend to credit any contributor who wishes to be here, no matter how big or small.

- Patrick Twohig - [**@ptwohig**](https://github.com/ptwohig) (that's me!)
- Keith Hudnall - [**@krh372**](https://github.com/krh372)
- Garrett McSpadden - [**@EmissaryEntertainment**](https://github.com/EmissaryEntertainment)
- rcornwal - [**@rcornwal**](https://github.com/ptwohig)
- Maxwell Montes Diaz - [**@Mascaz**](https://github.com/Mascaz)
- Chris Uribe - [**@chrisuribe**](https://github.com/chrisuribe)

If you want to see your name here, clone the repository and get started.
