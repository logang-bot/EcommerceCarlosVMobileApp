# Shipping a Build

Step-by-step for getting a new build into the Telegram channel. One-time setup
(keystore, bot, secrets) lives in `docs/release-distribution.md` — this document
assumes that's already done.

| | Staging | Production |
|---|---|---|
| Trigger | Manual, from the Actions tab | Push a `v*` git tag |
| Flavor built | `staging` | `production` |
| App on device | Installs alongside production (`.staging` suffix) | The real app |
| Version shown | `APP_VERSION_NAME` + `-staging` | `APP_VERSION_NAME`, e.g. `1.2.0` |
| Filename in channel | `CarlosV-1.2.0-a1b2c3d-staging.apk` | `CarlosV-1.2.0.apk` |
| Who it's for | You, to verify before a real release | The customer |

---

## Before you ship

1. Everything you want in the build is **committed and pushed to `main`**. The
   workflow builds from the remote, not from your working tree — uncommitted changes
   are invisible to it.
2. The app builds and runs locally.
3. For production: `APP_VERSION_NAME` is bumped to a version that has never shipped,
   and you've already run the same code as staging and installed it.

---

## Shipping a staging build

Use this to prove a build works before the customer ever sees it.

There are **no git commands here** — no tag, no version bump. Staging just builds
whatever is currently on `main`. The only thing you do beforehand is push your code.

1. Go to the **Actions** tab of the repo.
2. In the left sidebar, click the **Release APK** workflow.
3. Click the **Run workflow** dropdown on the right.
4. Leave the branch as `main`, set **Build flavor** to `staging`.
5. Click the green **Run workflow** button.
6. Refresh the page. A new run appears — click it to watch the log. ~5 minutes.

When it goes green, the APK is in the Telegram channel with `Entorno: staging` in the
caption.

### Worked example — checking a fix before releasing 1.0.1

You've shipped `1.0.0` to the customer. You then fix a crash, and you want to see the
fix on a real phone before they get it.

**1. Bump the version and push the fix together.**

```bash
# gradle.properties: APP_VERSION_NAME=1.0.0  ->  APP_VERSION_NAME=1.0.1

git add -A
git commit -m "Fix crash on pedido save; bump version to 1.0.1"
git push origin main
```

Bumping *now*, before the staging build, is what makes the staging APK say
`1.0.1-staging` — it's the 1.0.1 candidate, and it says so. Nothing has shipped to the
customer yet; the property is just a declaration.

**2. Run the staging build.** Actions → **Release APK** → **Run workflow** → flavor
`staging` → **Run workflow**. Wait ~5 minutes.

**3. Install from Telegram.** `CarlosV-1.0.1-a1b2c3d-staging.apk` arrives in the
channel. Tap it. It installs *next to* the production app — two icons — so the
customer's `1.0.0` install is untouched and you can compare them side by side.

**4. Verify the fix.** The staging app points at the staging Supabase, so you can
create and delete test data freely without touching real customer data.

**5. Happy? Ship it for real.** The property is already at `1.0.1`, so all that's left
is the tag:

```bash
git tag v1.0.1
git push origin v1.0.1
```

That's the whole cycle: **bump → push → staging → verify → tag**. Staging is a
rehearsal of an exact version; the tag is what actually ships it.

Staging installs **alongside** production rather than replacing it, because the flavor
adds a `.staging` suffix to the application ID. You'll see two app icons. That's
intended — it lets you compare against the customer's build on one phone.

### What version does a staging build get?

The same `APP_VERSION_NAME` as production, plus a `-staging` suffix: `1.0.0-staging`.

Staging deliberately does **not** get a fake version like `99.9.9`. A staging build
exists to rehearse a specific production release, so it should say which one — `1.0.0`
staged is the `1.0.0` candidate. It's already impossible to mistake for production: it
installs as a separate app, the version says `-staging`, the filename says `-staging`,
and the caption says `Entorno: staging`.

Two staging builds of the same version read identically *on the device*. Tell them
apart by the commit SHA in the filename, or the build number in the caption
(`build 47` — that's the `versionCode`, unique to every run).

---

## Shipping a production build

Every release number lives in **two places that must agree**: `APP_VERSION_NAME` in
`gradle.properties`, and the git tag. Two rules follow from that:

- **Within one release, repeat the number.** Property `1.2.0`, tag `v1.2.0`. The
  workflow refuses to build if they differ.
- **Across releases, never repeat it.** Every release gets a fresh number, bumped in
  both places together. A version that has shipped is spent forever.

### The steps

1. Pick the next version. Semantic versioning, based on what changed since the last
   release:

   | Change | Bump | Example |
   |---|---|---|
   | Bug fixes only | patch | `1.2.0` → `1.2.1` |
   | New features, nothing broken | minor | `1.2.1` → `1.3.0` |
   | Major rework | major | `1.3.0` → `2.0.0` |

2. Edit `gradle.properties`:

   ```properties
   APP_VERSION_NAME=1.2.1
   ```

3. Commit and push to `main`. **Before tagging** — the tag must point at a commit that
   already contains the bumped property, or the match check fails.

   ```bash
   git add gradle.properties
   git commit -m "Bump version to 1.2.1"
   git push origin main
   ```

4. Tag that commit and push the tag:

   ```bash
   git tag v1.2.1
   git push origin v1.2.1
   ```

5. Go to the **Actions** tab — a run started automatically. Watch it.

The tag must start with `v`; the workflow ignores anything else.

### Worked example — shipping the release after 1.0.0

You shipped `1.0.0`. Since then you fixed a crash. To ship that fix:

```bash
# 1. gradle.properties: APP_VERSION_NAME=1.0.0  ->  APP_VERSION_NAME=1.0.1

git add gradle.properties
git commit -m "Bump version to 1.0.1"
git push origin main

git tag v1.0.1
git push origin v1.0.1
```

The customer gets `CarlosV-1.0.1.apk` in the channel, installs it over `1.0.0`, and
keeps all their data.

Note what you **don't** touch: `versionCode`. It comes from the GitHub run number
automatically and increases on its own. It's the number Android actually uses to
decide whether an install is an upgrade, and hand-managing it is how people ship
builds their customers can't install.

### Why both?

The tag doesn't *set* the version — `APP_VERSION_NAME` does. The tag just marks which
commit ships. The workflow compares the two and refuses to build if they disagree:

```
Tag v1.3.0 does not match APP_VERSION_NAME=1.2.0 in gradle.properties.
Bump the property and re-tag.
```

That check costs seconds and catches the most common release mistake — tagging a
version you forgot to bump — before it reaches the customer as an APK whose version
number lies about what's inside it.

If you hit that error: delete the local tag (`git tag -d v1.3.0`), fix the property,
commit, and tag again. If you already pushed the bad tag, `git push origin :v1.3.0`
deletes it from the remote. A tag that never produced a build is safe to delete —
that rule about never reusing tags only applies once a build has shipped from it.

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

**You cannot reuse a version.** Git tags are meant to be permanent, and re-pushing one
that already exists is refused. If `1.2.0` shipped broken, fix the bug and ship
`1.2.1` — bump `APP_VERSION_NAME`, commit, push, tag `v1.2.1`, exactly like any other
release. Don't delete and re-push the tag: the customer may already have the bad
build, and a higher version number is the only thing that moves them off it.

(The exception is a tag that never built — e.g. you tagged before bumping the
property and the run failed the match check. Nothing shipped, so `git tag -d v1.2.0`
and `git push origin :v1.2.0` are safe there.)

### What stops you shipping a version twice

Three independent layers, so you'd have to work at it:

1. **Git blocks re-tagging.** `git tag v1.0.0` fails locally with *"tag 'v1.0.0'
   already exists"*. Force the local tag and the remote still rejects the push:
   *"Updates were rejected because the tag already exists"*.
2. **The workflow blocks a tag/property mismatch.** Tagging `v1.0.1` while the
   property still says `1.0.0` fails in seconds, before the build.
3. **The workflow blocks a re-dispatch.** A manual production run whose
   `APP_VERSION_NAME` already has a matching tag is refused — this is the one git
   can't catch, since no tag is being pushed.

Staging is exempt from all of this. Staging builds are meant to be repeated, and the
`.staging` app ID keeps them away from the customer's install.

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
| *Resolve build metadata*, "does not match APP_VERSION_NAME" | You tagged without bumping the property (or vice versa). Fix and re-tag — nothing shipped |
| *Resolve build metadata*, "was already released" | A manual production dispatch of a version that already has a tag. Bump `APP_VERSION_NAME` |
| *Decode release keystore* | `RELEASE_KEYSTORE_BASE64` missing or misnamed |
| *Build signed APK*, "keystore was tampered with" | Wrong keystore password, or the base64 got truncated when pasted |
| *Build signed APK*, "No key with alias" | `RELEASE_KEY_ALIAS` doesn't match the keystore (this project's is `key0`) |
| *Locate APK*, "only an unsigned APK" | Keystore didn't apply. An unsigned APK can't be installed, so it's never delivered |
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
