# Supabase Setup Guide

Steps to wire each Supabase project (staging and/or production) to the app.
Run these once per environment. You do not need both projects ready at the same time —
the staging setup alone is enough to develop and test Phase 9.

---

## 1. Create the Supabase project

Go to [supabase.com](https://supabase.com), create a new project, and note:

- **Project URL** — found in Settings → API → "Project URL", looks like `https://abcdefgh.supabase.co`
- **Publishable key** — found in Settings → API → "Publishable" (safe to include in the app)
- **Secret key** — found in Settings → API → "Secret" (never commit or expose this)

---

## 2. Run the SQL scripts

In the Supabase dashboard → **SQL Editor**, run the following files **in order**:

| Order | File | What it does |
|-------|------|--------------|
| 1 | `docs/sql/schema.sql` | Creates all tables, indexes, triggers, and the `set_updated_at_ms` function |
| 2 | `docs/sql/rls.sql` | Enables Row Level Security and adds per-table policies |
| 3 | `docs/sql/storage.sql` | Creates photo storage buckets and their access policies |

> `schema.sql` already includes the `updated_at` column, the auto-update trigger, and the delta-sync indexes on all four business tables. No extra SQL is needed for a fresh environment.

---

## 3. Fill in `local.properties`

Open `local.properties` at the project root and replace the placeholders:

```properties
# Staging
STAGING_SUPABASE_URL=https://YOUR_STAGING_PROJECT.supabase.co
STAGING_SUPABASE_PUBLISHABLE_KEY=your-staging-publishable-key
STAGING_SUPABASE_SECRET_KEY=your-staging-secret-key

# Production (fill in when the project is ready)
PRODUCTION_SUPABASE_URL=https://YOUR_PRODUCTION_PROJECT.supabase.co
PRODUCTION_SUPABASE_PUBLISHABLE_KEY=your-production-publishable-key
PRODUCTION_SUPABASE_SECRET_KEY=your-production-secret-key
```

`local.properties` is gitignored — never commit these keys.

> **CI builds don't see this file.** The build resolves each value from an environment
> variable first and only falls back to `local.properties`, so GitHub Actions injects
> the same six keys from repository secrets. If you add or rotate a value here, update
> the matching secret too — see `docs/release-distribution.md`.

---

## 4. Create the first SUPERUSUARIO

The first superuser cannot be created from within the app because no one is logged in yet.
Do it manually:

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

After this, the app's Login screen will accept those credentials and the user will have full SUPERUSUARIO access.

---

## 5. Reset a user's password

Three options — prefer Option C for day-to-day admin use since it requires no dashboard access.

### Option C — In-app (superuser only, no dashboard required)

Any `SUPERUSUARIO` can navigate to **Mi Perfil → Seguridad → Cambiar contraseña** (to change their own) or open **Gestión de Usuarios → [user] → Seguridad → Cambiar contraseña** (to reset another user's password without knowing their current one). The app calls `adminClient.auth.admin.updateUserById(userId)` with the new password.

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

## 7. Select the build variant

In Android Studio → **Build Variants** panel (bottom-left), choose:

| Variant | Use for |
|---------|---------|
| `stagingDebug` | Day-to-day development |
| `stagingRelease` | QA / internal testing against staging data |
| `productionRelease` | App Store / production build |

The staging app installs with package name `com.restrusher.ecomercecarlosv.staging`, so both flavors can be installed on the same device at the same time.

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

### StorageService uses the admin (service role) client

`StorageService` uploads photos using `@AdminClient` (the Supabase service role key), not the regular authenticated client. This bypasses Storage RLS INSERT policies entirely. If you tighten the service role key's permissions or move uploads to a different client, re-check the bucket RLS INSERT policies in `docs/sql/storage.sql`.

---

## 9. ⚠️ Security note — service role key in APK

The `SUPABASE_SERVICE_ROLE_KEY` is compiled into the APK via `BuildConfig` and is readable by anyone who decompiles the APK. For this internal-use app that is acceptable, but if you ever distribute the app to untrusted users, move the admin operations (`createUser`, `updateUser`, `deleteUser`, `banUser`) to a **Supabase Edge Function** so the key never leaves the server.

---

## Checklist

### Staging
- [x] Supabase project created
- [x] `schema.sql` executed *(v1 — no `updated_at`)*
- [x] `rls.sql` executed
- [x] `storage.sql` executed
- [x] `local.properties` updated with staging values
- [x] First SUPERUSUARIO created (auth user + `users` table row)
- [x] `stagingDebug` variant selected — login works end-to-end
- [ ] **Phase 10b migration** — run the ALTER TABLE statements from `docs/db-schema.md` → "Staging environment changes" section to add `updated_at` + triggers + indexes to the existing staging DB
- [ ] **Phase 12 migration** — run `docs/db-schema.md` → "Staging environment changes" → section 6 to create the `umbrales` table (was local-only `SharedPreferences`, now synced)

### Production
- [ ] Supabase project created
- [ ] `schema.sql` executed
- [ ] `rls.sql` executed
- [ ] `storage.sql` executed
- [ ] `local.properties` updated with production values
- [ ] First SUPERUSUARIO created (auth user + `users` table row)
- [ ] `productionRelease` build verified
