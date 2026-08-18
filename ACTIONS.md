# Release Process

This document describes how Elements is built, versioned, and released via GitHub Actions. It's for maintainers cutting releases, not for plugin/game developers — if you're building on Elements, see the [getting started guide](https://namazustudios.com/docs/getting-started/) instead. If you're contributing to the core codebase, see [HACKING.md](./HACKING.md).

---

## Overview

Elements follows a release-branch model:

- **`main`** always sits at the next unreleased `-SNAPSHOT` version (e.g. `3.10.0-SNAPSHOT`). Every push builds, tests, and publishes a snapshot to GitHub Packages.
- **`release/X.Y`** branches are cut from `main` when a release line is ready to stabilize. Each one lives at its own version and is where release candidates and formal releases for that line are tagged.
- **Tags** (`X.Y.Z` or `X.Y.Z-rc-N`) are what actually trigger a real publish: Maven Central + GitHub Packages deploy, Docker images, Javadoc/OpenAPI docs, and (for non-RC tags) a GitHub Release.

All of the workflows below live in [`.github/workflows`](./.github/workflows) and are manual (`workflow_dispatch`), run from the **Actions** tab against the appropriate branch.

---

## Starting a new release line

Run **[Cut Release Branch](./.github/workflows/cut-release-branch.yml)** with the first release-candidate version for the new line, e.g. `3.9.0-rc-1`.

This creates `release/3.9` from `main` and sets its version directly to `3.9.0-rc-1` — no tag, no publish yet, just the branch. `main` is left untouched; bump it forward to the next `-SNAPSHOT` yourself (via **Set Version**, below) once you're ready to start developing the next line.

---

## Iterating release candidates

Once a release branch exists, every ordinary push to it (bug fixes, cherry-picks) is auto-bumped and tagged by **[release-branch-push.yml](./.github/workflows/release-branch-push.yml)**:

- If the branch's current version already has an `-rc-N` suffix, the push triggers an automatic bump to `-rc-(N+1)`, which gets tagged and pushed. That tag triggers **[tag-publish.yml](./.github/workflows/tag-publish.yml)**, which builds, tests, and publishes the RC (Maven Central + GitHub Packages, Docker images, Javadoc/OpenAPI) — RCs don't get a GitHub Release.
- If the branch is still sitting at a plain `-SNAPSHOT` (shouldn't normally happen once **Cut Release Branch** is used, but can if a branch is reset), the push just builds and tests as a sanity check, with no tag.

If a release branch ever does end up at a plain `-SNAPSHOT` and needs its first RC cut, use **[Cut Release Candidate](./.github/workflows/cut-rc.yml)** to promote it to `-rc-1` and tag it.

---

## Making a formal release

Run **[Release](./.github/workflows/release.yml)** against the release branch. This only works when the branch is currently sitting on a release candidate (`X.Y.Z-rc-N`) — a release can only be made from an RC.

It:

1. Drops the `-rc-N` suffix (`X.Y.Z-rc-N` → `X.Y.Z`), tags it, and pushes. That tag triggers `tag-publish.yml`, which publishes the release for real and creates a GitHub Release.
2. Immediately bumps the branch to the next patch's first RC (`X.Y.Z` → `X.Y.(Z+1)-rc-1`), tags that too, and pushes — so the branch is ready for the next round of bug-fix RCs without a separate step.

---

## Ad-hoc version changes

**[Set Version](./.github/workflows/set-version.yml)** sets an arbitrary version on any branch — no build, no tag, no publish. This is how `main` gets bumped to the next `-SNAPSHOT` when starting a new release line, and generally how any branch's version gets corrected on demand.

---

## Reference: what triggers what

| Trigger | Workflow | Does |
|---|---|---|
| Push to any branch except `release/*` | `main-build.yml` → `complete-build.yml` | Build, test, Docker images, Javadoc/OpenAPI snapshot publish. Real GitHub Packages publish only from `main`. |
| Push to `release/*` | `release-branch-push.yml` | Auto-bumps and tags the next RC if already on an RC; otherwise just builds/tests. |
| Tag push matching `X.Y.Z(-rc-N)?` (≥ 3.9.0) | `tag-publish.yml` | Real build, test, and publish: Maven Central + GitHub Packages, Docker images, Javadoc/OpenAPI, and a GitHub Release for non-RC tags. |
| Manual: Cut Release Branch | `cut-release-branch.yml` | Creates `release/X.Y` from `main` at a given RC version. |
| Manual: Cut Release Candidate | `cut-rc.yml` | Promotes a release branch from plain `-SNAPSHOT` to `-rc-1`, tags it. |
| Manual: Release | `release.yml` | Drops the RC suffix, tags, publishes, then re-enters RC mode for the next patch. |
| Manual: Set Version | `set-version.yml` | Sets an arbitrary version on any branch, no tag or publish. |
