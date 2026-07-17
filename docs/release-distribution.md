# Release & Distribution

How builds reach the customer: GitHub Actions builds a signed APK, then a Telegram
bot posts it to a private channel the customer is subscribed to.

Cost: $0. Private repos get 2,000 free Actions minutes/month; a build here takes
~5 minutes, so roughly 400 builds/month fit in the free tier.

> This document covers **setup and reference**. For the day-to-day steps of shipping a
> build, see `docs/shipping-a-build.md`.

## How a release is triggered

**Tagged release (normal path).** The tag name becomes the version name:

```bash
git tag v1.2.0
git push origin v1.2.0
```

`v1.2.0` produces `versionName = 1.2.0`, always the `production` flavor.

**Manual build.** Actions → *Release APK* → *Run workflow*, pick `production` or
`staging`. Version name is `0.0.0-<short-sha>`, so it's obvious it isn't a real release.

`versionCode` always comes from the GitHub run number, which increases forever and
never repeats. This matters: Android refuses to install an APK whose `versionCode`
is the same as or lower than the installed one.

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
| `PRODUCTION_SUPABASE_SECRET_KEY` | See the security note below |
| `STAGING_SUPABASE_URL` | |
| `STAGING_SUPABASE_PUBLISHABLE_KEY` | |
| `STAGING_SUPABASE_SECRET_KEY` | |

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

## Security note: `SUPABASE_SECRET_KEY`

`SUPABASE_SECRET_KEY` is compiled into `BuildConfig`, which means it ships inside the
APK. Anyone who receives a build can unzip it and recover the key — and a Supabase
secret/service-role key bypasses Row Level Security entirely. Telegram delivery
widens the blast radius slightly, since the APK now sits in a chat that can be
forwarded.

This is pre-existing, not something the pipeline introduced, and it is left as-is
here so the build keeps working. The fix is to drop the secret key from the client
and reach privileged operations through an Edge Function using the user's JWT,
leaving only the publishable key in the app. Worth doing before this goes to real
customers.

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
