# Namazu Elements

**Open-source game backend. Add multiplayer, leaderboards, cloud saves, and more to your game. No backend code required.**

Elements is a self-hosted, open-source game backend platform built on Java 21. Free as in speech, and free as in beer.

- 🎮 **Game developers:** get multiplayer, leaderboards, cloud saves, and payments running in your Unity, GameMaker, or Godot game without writing a single line of backend code.
- 🔧 **Plugin developers:** build and publish server-side add-ons using Java, Kotlin, or any JVM language.

---

## See it in action

**[Pong Multiplayer Example →](https://github.com/NamazuStudios/pong-multiplayer-example)**

A complete, playable Unity multiplayer game built on Elements. Features matchmaking, P2P networking via WebRTC, leaderboards, cloud saves, and user accounts, with **zero backend code** written outside of Unity. Clone it, run it, and see exactly what Elements can do for your game.

---

## Get started in 5 minutes

Elements runs in Docker. No Java installation required to get started.

```bash
git clone https://github.com/NamazuStudios/docker-compose
cd docker-compose
docker-compose up
```

The Elements server and CMS will be running at `http://localhost`. From here you can create your first application, explore the dashboard, and install add-ons.

**[Full getting started guide →](https://namazustudios.com/docs/getting-started/)**

---

## Add features to your game

Elements ships with a comprehensive set of built-in features:

- User accounts, authentication & profiles
- Matchmaking & lobbies
- Leaderboards
- Digital inventory & economy
- Cloud saves & character data
- Quests & dailies
- Social features
- LiveOps CMS

---

## Game engine support

| Engine | SDK | Example Project |
|--------|-----|-----------------|
| Unity | [unity-codegen-plugin](https://github.com/NamazuStudios/unity-codegen-plugin) | [Pong Example](https://github.com/NamazuStudios/pong-multiplayer-example) |
| GameMaker | Coming soon | |
| Godot | Coming soon | |

---

## Write your own add-ons

Elements is built to be extended. Custom server logic runs in isolated ClassLoader environments with no dependency conflicts and no "Jar Hell." Any JVM language works.

```java
// The simplest possible Element
@ElementDefinition(recursive = true)
package com.mystudio.mygame.api;

import dev.getelements.elements.sdk.annotation.Element;
```

**Starting with Elements 3.7, open-source authors can publish Elements directly to Maven Central.** ELM-based artifacts published to Maven Central can be deployed and installed directly into any Elements instance with no manual packaging required.

**[Custom code guide →](https://namazustudios.com/docs/custom-code/element-structure/)**
**[Example starter project →](https://github.com/NamazuStudios/element-example)**

---

## Deploy to production

| Option | Guide |
|--------|-------|
| Local / development | [Docker Compose](https://github.com/NamazuStudios/docker-compose) |
| AWS (one command) | [Community Edition on AWS](https://github.com/NamazuStudios/community-edition-aws) |

---

## License

Elements is licensed under **AGPLv3**.

**Your game code stays yours.** If you build plugins using the Elements SDK (`dev.getelements.sdk` and subpackages), you are not required to open-source your game or server code. This exemption is built into the license.

We also offer a **commercial license** for studios that need a more permissive option. [Contact us →](mailto:info@namazustudios.com)

---

## Build pipeline

Elements is actively **migrating its build pipeline to GitHub Actions** from Bitbucket Pipelines. In the meantime, builds are managed through our internal CI system. Local builds use Maven.

**First, always do an initial build skipping tests:**

```bash
mvn -Darchetype.test.skip=true -DskipTests --no-transfer-progress -q install
```

**To run the full test suite:**

```bash
mvn install
```

> ⏱ Tests take approximately 30 minutes to complete. We recommend only running them if you are planning to actively contribute to the project.

All dependent services run in Docker. See the [getting started guide](https://namazustudios.com/docs/getting-started/) for prerequisites.

---

## Contributing

Contributions are welcome and deeply appreciated. Please submit via pull request. All contributions should remain open and available to the community.

If you want to work on the Elements core codebase, see **[HACKING.md](./HACKING.md)** for build instructions, module architecture, and internals documentation.

### 🙏 Immediate help wanted

- **GitHub Actions migration:** we're currently on Bitbucket Pipelines and actively working to migrate to GitHub Actions. If you can figure this out, you will be a friend of this project forever. Seriously.
- **UI enhancements and fixes:** improvements to the CMS and admin dashboard are always appreciated.
- **Bug reports:** if you find something broken, [open an issue](https://github.com/NamazuStudios/elements/issues). Clear reproduction steps go a long way.

### Other ways to contribute

- **Write or improve documentation:** good docs are the #1 thing that helps new developers succeed with Elements.
- **Build and share example projects:** a working game using Elements is worth more than any marketing copy. Share what you build.
- **Publish open-source Elements to Maven Central:** starting with 3.7, you can publish add-ons directly. The more quality add-ons available, the stronger the ecosystem for everyone.
- **Answer questions on Discord:** helping other developers get unstuck is one of the most valuable contributions you can make.
- **Improve test coverage:** the test suite is always a work in progress. New tests for edge cases and untested modules are welcome.
- **Engine SDKs:** GameMaker and Godot SDK contributions are on the roadmap. If you're familiar with either engine, we'd love to collaborate.

---

## Links

- 🌐 [Website](https://namazustudios.com/elements/)
- 📖 [Documentation](https://namazustudios.com/docs/)
- 💬 [Discord](https://discord.gg/n4ZeG7g6)
- 📧 [contact@namazustudios.com](mailto:info@namazustudios.com)
