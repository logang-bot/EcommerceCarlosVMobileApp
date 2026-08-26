# Code conventions

The engineering conventions for this project are **not stored in this repo**. They live in a
plugin marketplace so they apply across projects rather than only here:

<https://github.com/logang-bot/claude-marketplace>

`.claude/skills/` used to hold them and no longer exists. Edit the marketplace repo instead —
a copy made here would silently drift.

## What comes from where

| Plugin | Covers |
|---|---|
| `general-code-style` | File and class size (~200 lines), function size (~10 lines), max 3 parameters, self-describing names |
| `android-compose` | Composables, previews, screen routes, `strings.xml`, when logic belongs in a UseCase |
| `dev-workflow` | Changelog and release procedure, keeping `docs/` current, git write policy |

They are enabled for this project in `.claude/settings.json`, so anyone cloning the repo gets
them automatically.

The `dev-workflow` plugin's release skill is deliberately generic and looks up the
project-specific values at release time. For this project they are in
**`docs/shipping-a-build.md`**: `APP_VERSION_NAME` in `gradle.properties` as the version
constant, Spanish business-owner-facing notes, the ~800-character Telegram cap, and the
blocking Edge Functions deploy (`docs/supabase-setup.md` §3). The headers of `CHANGELOG.md`
and `CHANGELOG.unreleased.md` restate the same rules.

## Enforced automatically

Three hooks run without being asked:

- a write to an oversized file or function prints a size warning
- `git commit`, `push`, `tag`, and similar are **blocked** — the developer runs all git writes
- ending a turn with code changed but nothing in `docs/` prints a reminder

## Referenced elsewhere

`docs/progress.md` and `docs/features/testing.md` cite skills by name
(`identifying-use-cases`, `writing-a-string-variable-or-text-in-a-code-file`). Those names are
unchanged; they now resolve from the plugins.
