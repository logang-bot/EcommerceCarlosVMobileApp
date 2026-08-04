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

- **Fix (marcar todo como pagado):** settling a client's debts when taking them off the blacklist
  only happened on the device — the server never found out, so the pedidos stayed unpaid for
  everyone else and a later sync could bring the old debt back. Odd, because the saldo extra created
  by the same action *did* sync, so the two halves disagreed. Every settled pedido is now sent.
- **Change (mercados):** the coloured dot on a mercado now means the same thing as the client's own
  badge. Before, it counted every unpaid pedido, so a normal order placed that morning turned the
  mercado amber, and a mercado could show red while every client inside it showed "al día". It now
  counts only partially-paid regular pedidos, and it finally respects the montos and días configured
  in Ajustes, which it used to ignore in favour of fixed values. **Expect fewer coloured dots.**

- **Fix (usuarios):** changing a role, deactivating, activating or deleting a user looked like it
  had worked even when the server rejected it — the app kept the change locally and went back to
  the list, so the app and the server disagreed from then on, with nothing to retry it. It now
  stays on the screen and says what failed, and the local data is only updated once the server
  confirms.
- **Fix (mensajes de error):** errors from user administration showed a raw technical block that
  included the URL, the request headers and the session token. They now show only the server's
  short Spanish explanation ("Ya existe un usuario con ese correo"), or a generic message when the
  server did not send one. Creating a user also finally shows its error — it was silently
  discarded before.

- **Fix (trabajar sin conexión):** after a long spell offline the app lost its connection to the
  server, and the first sync on reconnect failed — showing a red sync icon, a "sync failed"
  notification and an error message, even though nothing was actually wrong. The app now waits for
  the connection to be re-established and sends the pending changes as soon as it is, without any
  false alarms. Pulling to refresh right after reconnecting also waits instead of showing an error.
  Affected every user, not only those using the fingerprint.
- **New (sesión caducada):** if the session can no longer be renewed — the account was deactivated,
  or the password was changed on another device — the app now says so and takes the user back to the
  login screen, instead of leaving them in an app where nothing saves.
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
- **Fix (sesión caducada):** when the session expired, a user with the fingerprint enabled was sent
  back to the fingerprint screen — which cannot renew an expired session. Tapping it just failed
  before finally offering the password. The login screen now detects this on arrival and shows the
  password field straight away, with the fingerprint option hidden until it can work again. The
  fingerprint keeps working normally after the password login.
- **Fix (sesión caducada):** an expiry detected in the background sent the user back to the login
  screen with no message at all. The "Tu sesión expiró" notice now always accompanies it.
- **New (cambio de usuario):** signing in as a different user on a device that still holds unsent
  changes now asks for confirmation first, naming the previous user and how many changes would be
  lost, and suggesting they sign in and sync instead. Previously those changes were silently sent to
  the server under the new user's account — recorded against the wrong person. Cancelling keeps them.
  A different user signing in with nothing pending simply gets a clean device instead of inheriting
  the previous user's mercados, clientes and pedidos.
- **Fix (data loss):** signing out while offline on a device without the fingerprint destroyed every
  unsent change. They are now kept and sent on the next sign-in.
- **Fix (sincronización):** a reconnect could start two sync passes at once, uploading the same
  pedido and the same photo twice. Only one runs at a time now.
- **Fix (cuenta desactivada):** deactivated users sometimes saw a generic authentication error
  instead of the screen telling them to contact a superuser. Deleted accounts also reported
  "Contraseña incorrecta" when the password was not the problem.

- **Security (Phase 15):** the Supabase service-role/secret key was removed from the APK;
  user-management admin operations (create, edit, role change, activate/deactivate, delete,
  reset another user's password) now run server-side in Edge Functions. No visible change
  for the app user. See `docs/supabase-setup.md` §9.
