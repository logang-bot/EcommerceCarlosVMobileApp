---
name: updating-changelog-or-release-notes
description: When adding an entry to CHANGELOG.md, recording a user-facing change, bumping APP_VERSION_NAME, or preparing/shipping a release — always read CHANGELOG.unreleased.md first.
---

There are **two files** and they serve different purposes:

- `CHANGELOG.unreleased.md` — a running, developer-facing scratchpad of changes that have
  landed but are **not yet shipped**. Append here as work happens.
- `CHANGELOG.md` — the **customer-facing** production release notes. One `## [VERSION]`
  section per shipped version, in Spanish, read by the business owner. The release workflow
  extracts the section whose header matches `APP_VERSION_NAME` in `gradle.properties` and
  sends it to Telegram; a production build **fails** if that section is missing.

## When a user-facing change lands (during normal development)

Append a bullet to the `## Pending` list in `CHANGELOG.unreleased.md`. Keep it short; it can
be developer-facing English. **Do NOT edit `CHANGELOG.md`** for individual changes — that file
only changes at release time. Skip changes with no user or security impact (internal refactors,
docs, tests, tooling).

## When preparing a release (bumping the version / writing release notes)

1. **Read `CHANGELOG.unreleased.md`** — it holds everything to announce.
2. Decide the new version and set `APP_VERSION_NAME` in `gradle.properties`.
3. **Distill all `## Pending` bullets into ONE new `## [VERSION]` section at the top of the
   version list in `CHANGELOG.md`**, following that file's house style:
   - **Spanish**, aimed at the business owner (not a developer).
   - Short phrases describing **what changes for the person using the app** — never file names,
     class names, or technical internals.
   - The section header **must exactly match** `APP_VERSION_NAME` (e.g. `## [1.0.1]`).
   - Whole section **≤ 800 characters** (Telegram caption limit; the workflow rejects longer).
   - If a pending change has no user-visible effect (e.g. an internal security fix), say so
     plainly rather than inventing a feature ("Mejoras internas de seguridad… El funcionamiento
     de la app no cambia.").
4. **Reset `CHANGELOG.unreleased.md`** — remove the distilled bullets, leaving only the header
   and the empty `## Pending` heading.

## Boundaries

- Do not run git commands (commit/tag/push) — the user handles all git themselves. Stop after
  the file edits and tell them the version + tag to push.
- Before a **production** release, remind the user that the Edge Functions must be deployed to
  the production Supabase project (see `docs/supabase-setup.md` §3) or admin actions 404 in the
  shipped build.
