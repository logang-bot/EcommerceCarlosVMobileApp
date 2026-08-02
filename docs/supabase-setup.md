# Supabase Setup Guide

Steps to wire a Supabase project (staging or production) to the app. Run them **once per
environment, in order** — sections 1–6 below are the complete recipe for standing up a
brand-new environment from scratch. You don't need both projects ready at the same time.

> **Quick order for a new environment:**
> 1. Create the project → 2. Run the SQL scripts → 3. Deploy the admin Edge Functions →
> 4. Add keys to `local.properties` (and CI secrets) → 5. Create the first SUPERUSUARIO →
> 6. Select the build variant.
>
> Sections 7–9 are day-to-day operations and background, not part of first-time setup.

---

## 1. Create the Supabase project

Go to [supabase.com](https://supabase.com), create a new project, and note (Dashboard →
**Project Settings** → **API**):

- **Project URL** — "Project URL", looks like `https://abcdefgh.supabase.co`. The subdomain
  (`abcdefgh`) is the **project ref** you'll need in step 3.
- **Publishable key** — under "API Keys", the `sb_publishable_...` key (safe to ship in the app).

> The **secret / service-role key** is **not** used by the app — do not put it in
> `local.properties` or CI. Privileged operations run in Edge Functions, which get the service
> role injected automatically by Supabase at runtime (see steps 3 and section 9).

---

## 2. Run the SQL scripts

In the Supabase dashboard → **SQL Editor**, run the following files **in order**:

| Order | File | What it does |
|-------|------|--------------|
| 1 | `docs/sql/schema.sql` | Creates all tables, indexes, triggers, and the `set_updated_at_ms` function |
| 2 | `docs/sql/rls.sql` | Enables Row Level Security and adds per-table policies |
| 3 | `docs/sql/storage.sql` | Creates the four photo storage buckets and their access policies |

> `schema.sql` already includes the `updated_at` column, the auto-update trigger, and the
> delta-sync indexes on all four business tables. No extra SQL is needed for a fresh environment.

---

## 3. Deploy the admin Edge Functions

Privileged user-management operations (create / role change / activate-deactivate / password
reset of another user / delete) run **server-side** in Edge Functions so the service-role key
never ships in the APK. They live in `supabase/functions/` and must be deployed to every
environment.

Deploy them with the Supabase CLI, from the repo root. If the CLI isn't installed,
`npx supabase@latest ...` runs it on demand (needs Node). `--use-api` bundles server-side, so
**no local Docker is required**:

```bash
# One-time: authorize the CLI (opens a browser)
npx supabase@latest login

# Deploy all five functions to the target project.
# <PROJECT_REF> is the project URL subdomain from step 1 (e.g. https://abcd.supabase.co → abcd).
npx supabase@latest functions deploy \
  create-user update-user-role set-user-active reset-user-password delete-user \
  --project-ref <PROJECT_REF> --use-api
```

- **No secrets to configure.** `SUPABASE_URL`, `SUPABASE_SERVICE_ROLE_KEY` and
  `SUPABASE_ANON_KEY` are injected into every function automatically — you never handle the
  service-role key yourself.
- If your CLI version rejects multiple names in one call, deploy them one at a time.
- Verify afterwards in Dashboard → **Edge Functions**: all five should be listed.
- The Dashboard → Edge Functions editor can also create them by hand, but the functions share
  `supabase/functions/_shared/auth.ts`, so the CLI is much easier.

Each function verifies the caller's JWT and confirms they are an active `SUPERUSUARIO` before
acting — see section 9 for the full picture.

---

## 4. Fill in `local.properties`

Open `local.properties` at the project root and set the two keys for the environment:

```properties
# Staging
STAGING_SUPABASE_URL=https://YOUR_STAGING_PROJECT.supabase.co
STAGING_SUPABASE_PUBLISHABLE_KEY=your-staging-publishable-key

# Production
PRODUCTION_SUPABASE_URL=https://YOUR_PRODUCTION_PROJECT.supabase.co
PRODUCTION_SUPABASE_PUBLISHABLE_KEY=your-production-publishable-key
```

`local.properties` is gitignored — never commit these values. The **secret / service-role key
is intentionally absent**; the app no longer uses it (see section 9).

> **CI builds don't see this file.** The build resolves each value from an environment variable
> first and only falls back to `local.properties`, so GitHub Actions injects the same four keys
> from repository secrets (`STAGING_SUPABASE_URL`, `STAGING_SUPABASE_PUBLISHABLE_KEY`, and the
> production pair). Do **not** add a `*_SECRET_KEY` secret. If you add or rotate a value here,
> update the matching repository secret too — see `docs/release-distribution.md`.

---

## 5. Create the first SUPERUSUARIO

The first superuser can't be created from within the app because no one is logged in yet (and
the `create-user` Edge Function requires an existing SUPERUSUARIO caller). Bootstrap it manually:

**Step A** — Create the auth account in Supabase dashboard → Authentication → Users → "Add user":
- Email: `carlos@comercializadora.ve` (or whatever the real address is)
- Password: choose a temporary password
- Auto Confirm User: ✅

**Step B** — Copy the generated UUID from the Users list (it will be the Auth UID).

**Step C** — Insert the profile row via SQL Editor (replace `<AUTH_UID>` and other values):

```sql
INSERT INTO users (id, email, name, role, is_active, created_at)
VALUES (
    '<AUTH_UID>',
    'carlos@comercializadora.ve',
    'Carlos Villarroel',
    'SUPERUSUARIO',
    true,
    EXTRACT(EPOCH FROM NOW())::bigint * 1000   -- epoch milliseconds
);
```

After this, the app's Login screen will accept those credentials and the user will have full
SUPERUSUARIO access — and can create further users in-app (which routes through the
`create-user` Edge Function from step 3).

---

## 6. Select the build variant

In Android Studio → **Build Variants** panel (bottom-left), choose:

| Variant | Use for |
|---------|---------|
| `stagingDebug` | Day-to-day development |
| `stagingRelease` | QA / internal testing against staging data |
| `productionRelease` | App Store / production build |

The staging app installs with package name `com.restrusher.ecomercecarlosv.staging`, so both
flavors can be installed on the same device at the same time.

---

# Operations & reference

The sections below are not part of first-time setup.

## 7. Reset a user's password

Three options — prefer Option C for day-to-day admin use since it requires no dashboard access.

### Option C — In-app (superuser only, no dashboard required)

Any `SUPERUSUARIO` can navigate to **Mi Perfil → Seguridad → Cambiar contraseña** (to change
their own) or open **Gestión de Usuarios → [user] → Seguridad → Cambiar contraseña** (to reset
another user's password without knowing their current one). Changing *your own* password uses
the regular authenticated client; resetting *another* user's password calls the
`reset-user-password` Edge Function (server-side, service role) — see sections 3 and 9.

### Option A — Dashboard (sends a recovery email)

1. Supabase dashboard → **Authentication** → **Users**
2. Locate the user by email
3. Click the **⋮** menu on their row → **Send password recovery**
4. Open the recovery email and follow the link to set a new password

### Option B — SQL Editor (immediate, no email required)

In **SQL Editor**, run:

```sql
UPDATE auth.users
SET encrypted_password = crypt('new_password_here', gen_salt('bf'))
WHERE email = 'user@example.com';
```

Replace `new_password_here` and `user@example.com` with the actual values.
`pgcrypto` (`crypt` / `gen_salt`) is enabled by default on all Supabase projects.

---

## 8. Troubleshooting Storage uploads

### Photos not uploading — `NullPointerException` at `NoArgKt.Settings`

If logcat shows something like:

```
java.lang.NullPointerException: appContext must not be null
    at com.russhwolf.settings.NoArgKt.Settings(NoArg.kt:32)
    at io.github.jan.supabase.storage.resumable.SettingsResumableCache.<init>(...)
```

the cause is that `androidx.startup.InitializationProvider` was removed entirely from the merged `AndroidManifest.xml`. supabase-kt Storage creates a `SettingsResumableCache` (backed by `multiplatform-settings-no-arg`) the moment `storage.from(bucket)` is called. That cache needs `com.russhwolf.settings.SettingsInitializer` to have run first — which only happens if `InitializationProvider` is alive in the manifest.

This app disables WorkManager auto-init so Hilt's `HiltWorkerFactory` is used instead. The correct way to do that is to keep the provider with `tools:node="merge"` and remove only the `WorkManagerInitializer` `<meta-data>` entry:

```xml
<provider
    android:name="androidx.startup.InitializationProvider"
    android:authorities="${applicationId}.androidx-startup"
    android:exported="false"
    tools:node="merge">
    <meta-data
        android:name="androidx.work.WorkManagerInitializer"
        android:value="androidx.startup"
        tools:node="remove" />
</provider>
```

Do **not** use `tools:node="remove"` on the `<provider>` element itself — that silently kills every library that registers initializers through `androidx.startup`.

### StorageService uses the regular authenticated client

`StorageService` uploads photos with the **regular authenticated client** (publishable key).
The four photo buckets are public and their RLS policies (`docs/sql/storage.sql`) already allow
any `authenticated` user to insert/select/update/delete, so no service role is needed. If you
ever make a bucket private or tighten its RLS, re-check those INSERT policies.

---

## 9. Security — the secret / service-role key is out of the APK

Previously the secret / service-role key was compiled into the APK via `BuildConfig` and was
readable by anyone who decompiled it — and that key bypasses Row Level Security entirely.

**This is now fixed.** The privileged Auth-Admin operations run in the Edge Functions deployed
in step 3, one per operation:

| Function | Replaces |
|----------|----------|
| `create-user` | `auth.admin.createUser` + `users` insert |
| `update-user-role` | `updateUserById{role}` + `users` update |
| `set-user-active` | `updateUserById{ban_duration}` (activate/deactivate) + `users` update |
| `reset-user-password` | `updateUserById{password}` (reset another user) |
| `delete-user` | `auth.admin.deleteUser` + `users` delete |

Each function verifies the caller's JWT and confirms they are an active `SUPERUSUARIO` before
acting (`supabase/functions/_shared/auth.ts`). The Android side calls them through
`data/remote/AdminUserService`, using the regular client's `functions.invoke` (which forwards
the session JWT). The app ships **only the publishable key**; the service-role key never leaves
the server (Supabase injects it into each function at runtime).

Each function reports failures as `{"error": "<Spanish text>"}`; the app shows that text and
nothing else. See `docs/features/usuarios.md` → "Error contract" for how the client side handles
it — in short, `functions.invoke` throws before the app can inspect the status, and the raw
exception message carries the caller's bearer token, so it never reaches the UI.

Everything else that used to go through the old admin client now uses the regular authenticated
client, allowed by existing RLS: photo uploads (`StorageService`), self password change and
self profile edits (`CambiarContrasenaViewModel` self path, `EditarPerfilViewModel`).

### Migrating an existing environment (one-time cleanup)

If an environment previously ran a build that shipped the secret key, after deploying the
functions (step 3):

1. **Revoke the exposed secret key** — Dashboard → **Project Settings → API → API Keys**, revoke
   the old `sb_secret_...` key and generate a fresh one. The Edge Functions are unaffected (they
   use the separately injected `SUPABASE_SERVICE_ROLE_KEY`, which was never in the app).
2. **Remove it from `local.properties`** — delete any `*_SUPABASE_SECRET_KEY` line.
3. **Remove the CI secrets** — GitHub → Settings → Secrets and variables → Actions → delete
   `STAGING_SUPABASE_SECRET_KEY` and `PRODUCTION_SUPABASE_SECRET_KEY`.

### Behaviour note — editing your own email

Editing your *own* email now goes through the standard `auth.updateUser` flow instead of the
admin API. If the project has **"Secure email change"** enabled (Authentication → Providers →
Email), a self email change may require confirmation. Name, phone and photo are unaffected.

---

## Checklist

### Staging
- [x] Supabase project created
- [x] `schema.sql` executed *(v1 — no `updated_at`)*
- [x] `rls.sql` executed
- [x] `storage.sql` executed
- [x] Admin Edge Functions deployed (`create-user`, `update-user-role`, `set-user-active`, `reset-user-password`, `delete-user`)
- [x] `local.properties` updated with staging values (URL + publishable key only)
- [x] First SUPERUSUARIO created (auth user + `users` table row)
- [x] `stagingDebug` variant selected — login works end-to-end
- [x] **Secret-key cleanup** — old `sb_secret_...` key revoked; removed from `local.properties` + CI (see section 9)
- [ ] **Phase 10b migration** — run the ALTER TABLE statements from `docs/db-schema.md` → "Staging environment changes" section to add `updated_at` + triggers + indexes to the existing staging DB
- [ ] **Phase 12 migration** — run `docs/db-schema.md` → "Staging environment changes" → section 6 to create the `umbrales` table (was local-only `SharedPreferences`, now synced)

### Production
- [ ] Supabase project created
- [ ] `schema.sql` executed
- [ ] `rls.sql` executed
- [ ] `storage.sql` executed
- [ ] Admin Edge Functions deployed
- [ ] `local.properties` updated with production values (URL + publishable key only)
- [ ] First SUPERUSUARIO created (auth user + `users` table row)
- [ ] `productionRelease` build verified
