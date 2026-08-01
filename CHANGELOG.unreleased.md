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

- **Fix (fingerprint login):** logging in with the fingerprint could drop the user into the app with
  no Supabase session — data looked stale, uploaded photos were missing, and admin edits were
  rejected. The fingerprint now mints a genuinely fresh access token from the stored refresh token
  *before* opening the app (blocking, no password needed), and re-reads the profile so role changes
  and deactivations apply just like a password login. Offline it still logs in from the local cache.
- **Change (sign out):** on a device with the fingerprint enabled, signing out no longer revokes the
  stored refresh token, so the next fingerprint tap gets straight back in without a password. Devices
  without the fingerprint behave as before (full revoke + local data wipe). Use the new "Olvidar"
  option for a full sign-out that revokes everything.
- **New (login screen):** "¿No eres X? Entrar con otra cuenta" now opens a confirmation dialog that
  deletes the saved fingerprint, the stored session and the cached data from this device. Previously
  it only hid the fingerprint card until the next restart. Cached data is kept if writes are still
  pending sync, so nothing unsynced is lost.
- **Fix (error messages):** sync and upload failures showed a raw technical toast
  (`Failed to push PEDIDO(<id>) to Supabase`). They now show a short Spanish message in a snackbar,
  one per failed sync instead of one per record.
- **Fix (data loss):** a rejected session mid-use could silently wipe all locally cached data,
  including changes not yet synced. Only an explicit sign-out clears local data now.

- **Security (Phase 15):** the Supabase service-role/secret key was removed from the APK;
  user-management admin operations (create, edit, role change, activate/deactivate, delete,
  reset another user's password) now run server-side in Edge Functions. No visible change
  for the app user. See `docs/supabase-setup.md` §9.
