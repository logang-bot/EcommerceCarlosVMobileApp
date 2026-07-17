# Shipping a Build

Step-by-step for getting a new build into the Telegram channel. One-time setup
(keystore, bot, secrets) lives in `docs/release-distribution.md` — this document
assumes that's already done.

| | Staging | Production |
|---|---|---|
| Trigger | Manual, from the Actions tab | Push a `v*` git tag |
| Flavor built | `staging` | `production` |
| App on device | Installs alongside production (`.staging` suffix) | The real app |
| Version shown | `0.0.0-a1b2c3d` | The tag, e.g. `1.2.0` |
| Filename in channel | `CarlosV-0.0.0-a1b2c3d-staging.apk` | `CarlosV-1.2.0.apk` |
| Who it's for | You, to verify before a real release | The customer |

---

## Before you ship

1. Everything you want in the build is **committed and pushed to `main`**. The
   workflow builds from the remote, not from your working tree — uncommitted changes
   are invisible to it.
2. The app builds and runs locally.
3. For production: you've already shipped the same code as staging and installed it.

---

## Shipping a staging build

Use this to prove a build works before the customer ever sees it.

1. Go to the **Actions** tab of the repo.
2. In the left sidebar, click the **Release APK** workflow.
3. Click the **Run workflow** dropdown on the right.
4. Leave the branch as `main`, set **Build flavor** to `staging`.
5. Click the green **Run workflow** button.
6. Refresh. A new run appears — click it to watch the log. It takes ~5 minutes.

When it goes green, the APK is in the Telegram channel with `Entorno: staging` in the
caption.

Staging installs **alongside** production rather than replacing it, because the flavor
adds a `.staging` suffix to the application ID. You'll see two app icons. That's
intended — it lets you compare against the customer's build on one phone.

---

## Shipping a production build

Production ships by tagging. The tag name becomes the version the customer sees.

1. Make sure `main` is pushed and green.
2. Pick the version number. Use semantic versioning:
   - `v1.0.1` — bug fixes only
   - `v1.1.0` — new features, nothing broken
   - `v2.0.0` — major rework
3. Tag and push:

   ```bash
   git tag v1.2.0
   git push origin v1.2.0
   ```

4. Go to the **Actions** tab — a run started automatically. Watch it.

The tag must start with `v`; the workflow ignores anything else. The `v` is stripped
for the version name, so `v1.2.0` ships as `1.2.0`.

---

## Verifying it worked

The Telegram post carries everything you need to confirm you shipped what you meant to:

```
📦 Nueva versión disponible

Versión: 1.2.0 (build 47)
Entorno: production
Tamaño: 18 MB
Commit: a1b2c3d
```

Check **Commit** matches the commit you intended, and **Entorno** says `production`
before telling the customer to update.

### Filenames

AGP names every APK `app-<flavor>-release.apk`, which would make every build in the
channel look identical. The delivery step renames the file on upload:

- Production → `CarlosV-1.2.0.apk`
- Staging → `CarlosV-0.0.0-a1b2c3d-staging.apk`

Only the transmitted name changes (via curl's `;filename=`); the APK itself is
untouched, so the signature stays valid. If you change the scheme, it's the
`FILENAME` variable in the *Deliver to Telegram* step of
`.github/workflows/release.yml`.

Every run also attaches the APK to the workflow run itself (Actions → the run →
*Artifacts*), kept for 30 days. Useful if the Telegram upload fails but the build
succeeded — you can download it there and send it manually.

---

## Re-shipping after a mistake

**You cannot reuse a tag.** Git tags are meant to be permanent, and re-pushing one
that already exists is refused. If `v1.2.0` shipped broken, fix the bug and ship
`v1.2.1`. Don't try to delete and re-push the tag — the customer may already have the
bad build, and a new version number is the only way to move them off it.

There's no "unship". Once the APK is in the channel the customer can install it, so
if a build is bad, post a message in the channel telling them to wait, then ship the
fix.

---

## ⚠️ Staging builds notify the customer too

Both flavors post to the same channel, because there's a single `TELEGRAM_CHAT_ID`
secret. Your customer gets a notification for every staging build you run, even though
that build isn't for them.

They can't install it over their app by mistake — the `.staging` suffix makes it a
separate install — so the risk is confusion, not breakage. But it's noisy.

If it becomes a problem, make a second private channel for staging and split the
secret in two (`TELEGRAM_CHAT_ID_STAGING` / `TELEGRAM_CHAT_ID_PRODUCTION`), then pick
between them in the delivery step of `.github/workflows/release.yml` based on the
flavor.

---

## When the run fails

Open the failed run in the Actions tab and find the red step — it names the problem
directly.

| Step that failed | Meaning |
|---|---|
| *Decode release keystore* | `RELEASE_KEYSTORE_BASE64` missing or misnamed |
| *Build signed APK*, "keystore was tampered with" | Wrong keystore password, or the base64 got truncated when pasted |
| *Build signed APK*, "No key with alias" | `RELEASE_KEY_ALIAS` doesn't match the keystore (this project's is `key0`) |
| *Locate APK*, "over Telegram's 50 MB limit" | APK outgrew the Bot API limit — enable `isMinifyEnabled` for release |
| *Deliver to Telegram*, `chat not found` | Wrong `TELEGRAM_CHAT_ID`, or the bot was removed as channel admin |
| *Deliver to Telegram*, `not enough rights` | The bot is a member but not an admin with "Post messages" |

A failed run ships nothing — the delivery step only runs after a successful build, so
a broken build can never reach the customer.

Secrets are masked in logs as `***`. That's GitHub protecting you, not a bug.

---

## What the customer does

Nothing, on their end, beyond tapping the APK in the channel and confirming the
install. Android asks them to allow installs from Telegram the **first time only**.

Updates install over the top with all their data preserved, as long as every build is
signed with the same keystore. See `docs/release-distribution.md` for why that
keystore must never be lost.
