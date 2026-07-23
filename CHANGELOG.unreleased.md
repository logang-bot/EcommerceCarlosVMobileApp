# Unreleased changes

Running list of changes that have landed on `main` but are **not yet shipped** in a
production release. Add a bullet here whenever you make a change that matters to whoever
uses the app (a feature, a fix, a security or behaviour change).

At release time these get **distilled into a single Spanish, business-owner-facing
`## [VERSION]` section in `CHANGELOG.md`**, and this file is reset to just the `## Pending`
heading. See the `updating-changelog-or-release-notes` skill for the full workflow.

Notes here can be terse and developer-facing — the customer-friendly Spanish wording is
written at release time. Skip purely internal changes with no user or security impact
(refactors, docs, tests, tooling).

---

## Pending

- **Security (Phase 15):** the Supabase service-role/secret key was removed from the APK;
  user-management admin operations (create, edit, role change, activate/deactivate, delete,
  reset another user's password) now run server-side in Edge Functions. No visible change
  for the app user. See `docs/supabase-setup.md` §9.
