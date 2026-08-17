# Release & Distribution

How builds reach the customer: GitHub Actions builds a signed APK, then a Telegram
bot posts it to a private channel the customer is subscribed to.

Cost: $0. Private repos get 2,000 free Actions minutes/month; a build here takes
~5 minutes, so roughly 400 builds/month fit in the free tier.

> This document covers **setup and reference**. For the day-to-day steps of shipping a
> build, see `docs/shipping-a-build.md`.

## How a release is triggered

**Tagged release (normal path).** Bump `APP_VERSION_NAME` in `gradle.properties`,
commit, then tag with a matching `v` prefix:

```bash
git tag v1.2.0
git push origin v1.2.0
```

Always builds the `production` flavor.

**Manual build.** Actions → *Release APK* → *Run workflow*, pick `production` or
`staging`.

### Versioning

`versionName` comes from **`APP_VERSION_NAME` in `gradle.properties`** — the single
source of truth. A tag doesn't set the version; it only says "ship what's declared."
If a tag and the property disagree, the workflow fails in seconds, before building,
because there's no way to tell which one is the mistake.

`versionCode` is unrelated to that and comes from the GitHub run number, which only
ever increases. Android refuses to install an APK whose `versionCode` is the same as
or lower than the installed one, so this must never be hand-managed.

## One-time setup

### 1. Release keystore

Generate it once. **If this file is lost, you can never ship an update to an
already-installed app** — the signature won't match and the customer would have to
uninstall and lose local data. Back it up somewhere outside this repo.

```bash
keytool -genkeypair -v -keystore release.jks -alias upload \
  -keyalg RSA -keysize 2048 -validity 10000
```

Encode it for GitHub:

```bash
base64 -w 0 release.jks
```

`.gitignore` already excludes `*.jks` and `*.keystore`, but keep the file outside the
repo anyway.

### 2. Telegram bot and channel

1. Message [@BotFather](https://t.me/BotFather) → `/newbot` → copy the token.
2. Create a **private channel** (not a group — channels give you post history and
   the customer can't reply into it).
3. Add the bot as an **administrator** with "Post messages" permission. A plain
   member cannot upload.
4. Add your customer as a subscriber.
5. Get the channel ID: post any message in the channel, then

   ```bash
   curl "https://api.telegram.org/bot<TOKEN>/getUpdates"
   ```

   Look for `"chat":{"id":-100...}`. Channel IDs are negative and start with `-100`.
   Include the minus sign.

### 3. GitHub Secrets

Settings → Secrets and variables → Actions → *New repository secret*:

| Secret | Value |
| --- | --- |
| `RELEASE_KEYSTORE_BASE64` | Output of the `base64 -w 0` command |
| `RELEASE_KEYSTORE_PASSWORD` | Keystore password |
| `RELEASE_KEY_ALIAS` | `upload` (or your chosen alias) |
| `RELEASE_KEY_PASSWORD` | Key password |
| `TELEGRAM_BOT_TOKEN` | Token from @BotFather |
| `TELEGRAM_CHAT_ID` | Channel ID, including the leading `-100` |
| `PRODUCTION_SUPABASE_URL` | Same values as your `local.properties` |
| `PRODUCTION_SUPABASE_PUBLISHABLE_KEY` | |
| `STAGING_SUPABASE_URL` | |
| `STAGING_SUPABASE_PUBLISHABLE_KEY` | |

> The Supabase **secret / service-role key** is no longer used by the app and must
> **not** be added as a CI secret — privileged operations run in Edge Functions
> (see the security note below).

## Customer-side install

Telegram notifies the channel, the customer taps the APK, and Android asks them to
allow installs from Telegram (Settings → Install unknown apps). That prompt appears
once, on the first install only.

Updates install straight over the top with data preserved, as long as every build
is signed with the same keystore.

## How the build reads secrets

`app/build.gradle.kts` resolves config through a `secret()` helper that checks
environment variables first, then `local.properties`. So CI injects everything as
env vars and never writes `local.properties`, while local development keeps working
unchanged with no keystore present (release builds are simply left unsigned locally).

## Security note: the secret / service-role key (resolved)

The Supabase **secret / service-role key used to be compiled into `BuildConfig`** and
shipped inside the APK, where anyone who unzipped a build could recover it — and that
key bypasses Row Level Security entirely.

This has been fixed. The key is no longer referenced anywhere in the app: the
privileged operations (creating, updating, deleting auth users) now run in
server-side **Supabase Edge Functions**, invoked with the signed-in SUPERUSUARIO's
JWT (`data/remote/AdminUserService`). The app now ships **only the publishable key**.

See `docs/supabase-setup.md` → "Deploying the admin Edge Functions" for how to deploy
them. When rotating keys, remember the old secret key was previously distributed in
APKs and should be regenerated in the Supabase dashboard.

## Troubleshooting

**`RELEASE_KEYSTORE_BASE64 is not set`** — secret missing or misnamed.

**Telegram HTTP 400, `chat not found`** — wrong `TELEGRAM_CHAT_ID`, or the bot isn't
an admin of the channel.

**Telegram HTTP 413 / `Request Entity Too Large`** — APK over the Bot API's 50 MB
limit. The workflow checks size before uploading and fails with a clear message.
Current builds are ~18 MB. If it ever gets close, enable `isMinifyEnabled` for
release.

**Customer sees "App not installed"** — either the APK is signed with a different
keystore than the installed build (uninstall first, or use the original keystore),
or they have the other flavor installed. `staging` uses the applicationId suffix
`.staging`, so both flavors can coexist.

> ⚠️ **Uninstalling erases all local data**, and nothing is backed up — that is deliberate
> (see `docs/features/auth.md` → "Backup and restore"). Have the user open the app and let it
> sync before uninstalling, or any pending changes are lost. Installing *over* the existing
> build with the right keystore is an update, not a reinstall, and keeps their data.
