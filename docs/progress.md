# Project Progress

High-level phase tracker. Details for each feature live in `docs/features/`.

---

## Phases

| Phase | Scope | Status |
|-------|-------|--------|
| 1 | Navigation scaffold, theme, Login screen | ✅ Done |
| 2 | Mercados list, Detalle de Mercado, Create Mercado | ✅ Done |
| 2b | Perfil y Seguridad, Gestión de Usuarios (roles, biometric toggle, crear usuario) | ✅ Done |
| 2c | Biometric fully functional, empty states redesign, UI polish | ✅ Done |
| 2d | Login two-state (enrolled user screen), bottom navigation, Editar Perfil screen | ✅ Done |
| 2e | User management screens redesign: Gestión de Usuarios, UsuarioDetalle, CrearUsuario | ✅ Done |
| 2f | Mercados: long-press selection + edit, contextual action bar, Búsqueda Global screen stub, MercadoDto | ✅ Done |
| 3 | Detalle de Cliente, Crear Cliente, Saldo Extra | ✅ Done |
| 4 | Creación de Pedido (cart flow) | ✅ Done |
| 5 | Detalle de Pedido, Historial de Pagos | ✅ Done |
| 6 | Catálogo de Productos, Crear/Editar Producto | ✅ Done |
| 7 | Lista Negra, Agregar a Lista Negra | ✅ Done |
| 2h | Splash screen, app icon, app rename, real logo in Login, biometric screen redesign, file splits | ✅ Done |
| 8 | Reportes tab (Diario + Por cliente modes, PDF export, Reporte de Pedidos) | ✅ Done |
| 9 | Supabase auth + sync layer, DataStore session persistence | 🔄 In Progress |
| 9b | NetworkMonitor, centralized error manager, data synchronizer, network request queue | ✅ Done |
| 9c | WorkManager background queue, immediate flush on enqueue, reliable trigger via MAX(id) | ✅ Done |
| 9d | Always require login on app start — session wiped on startup, Room cache preserved | ✅ Done |
| 9e | Lazy per-screen data sync with staleness thresholds (2h master / 30 min business) | ✅ Done |
| 9f | In-app theme switcher (Claro / Oscuro / Sistema) with system bar support | ✅ Done |
| 10 | Client-side pagination (Load More button, 20/batch) + pull-to-refresh on all list screens | ✅ Done |
| 10b | Delta sync: `updated_at` cursor, Room v16 migration, pull-to-refresh error toast | ✅ Done |
| 10c | Remove client-side pagination (LoadMoreButton), server-side range fetch for >1000 rows | ✅ Done |
| 11 | Soft-delete: `isDeleted` on all 4 entities, Room v17, QueueProcessor pushes UPDATE instead of DELETE, syncers hard-delete locally on delta when `is_deleted = true` | ✅ Done |
| 12 | Depuración / Mantenimiento: Superusuario-only two-phase cleanup — export pedidos to CSV/XLSX then hard-delete from Supabase + Room | ✅ Done |
| 13 | Cambiar contraseña (superuser-only) — self via Perfil + others via UsuarioDetalleScreen | ✅ Done |
| 14 | Build distribution: GitHub Actions builds a signed APK on `v*` tags, Telegram bot delivers it to a private channel | ✅ Done |

---

## ✅ Phase 14 — Build distribution via GitHub Actions + Telegram

Replaces paid distribution services (Firebase App Distribution / Azure Pipelines) with
the free tier of GitHub Actions plus a Telegram bot. Full details in
`docs/release-distribution.md`.

### How it works

Pushing a `v*` tag builds a signed `productionRelease` APK and posts it to the private
Telegram channel the customer is subscribed to. A manual `workflow_dispatch` run can
also target the `staging` flavor. Delivery is a single `sendDocument` call to the Bot
API; the workflow fails early if the APK exceeds the API's 50 MB document limit
(current builds are ~18 MB). The APK is renamed on upload (`CarlosV-1.2.0.apk`)
since AGP's default `app-production-release.apk` makes every build indistinguishable
in the channel.

### Signing

`app/build.gradle.kts` gained a `signingConfigs { release { ... } }` block. CI decodes
`RELEASE_KEYSTORE_BASE64` into a `.jks` on the runner and points `RELEASE_KEYSTORE_FILE`
at it. When no keystore is configured the `signingConfig` is simply not applied, so
local `assembleRelease` still works (unsigned) and no developer needs the keystore.

### Secret resolution

New top-level `secret(key)` helper resolves **environment variables first, then
`local.properties`**. This let CI inject Supabase values as env vars without
synthesising a `local.properties` file, and left local development untouched. All six
`buildConfigField` calls in the two flavors now route through it.

### Versioning

`versionCode` was hardcoded to `1`, which silently blocks installing any update over
an existing build. It now reads `BUILD_NUMBER` (the GitHub run number, monotonic and
never reused), and `versionName` reads `BUILD_VERSION_NAME` (derived from the tag).
Both fall back to the old values for local builds.

### ⚠️ Known issue — `SUPABASE_SECRET_KEY` ships in the APK

`SUPABASE_SECRET_KEY` is a `buildConfigField`, so it is embedded in every APK and can
be extracted by anyone who receives one. It bypasses RLS entirely. Pre-existing (not
introduced by this phase) and left working as-is, but Telegram delivery widens the
exposure. Fix: move privileged operations behind an Edge Function and ship only the
publishable key.

### New files

| File | Description |
|------|-------------|
| `.github/workflows/release.yml` | Build + Telegram delivery; tag-triggered or manual with flavor picker |
| `docs/release-distribution.md` | Setup reference: keystore, bot/channel, GitHub Secrets |
| `docs/shipping-a-build.md` | Runbook: step-by-step for shipping staging and production |

### Modified files

| File | Change |
|------|--------|
| `app/build.gradle.kts` | `secret()` helper; `signingConfigs.release`; env-driven `versionCode`/`versionName`; flavors read via `secret()` |
| `docs/supabase-setup.md` | Note that CI reads the same values from GitHub Secrets |

---

## ✅ Phase 13 — Cambiar contraseña (superuser-only)

Admin password change flow wired to Supabase Auth. Full details in `docs/features/perfil.md → Cambiar contraseña screen`.

### Two entry points

- **PerfilScreen → Seguridad → Cambiar contraseña** (`isSelf = true`): The logged-in superuser changes their own password. Requires entering the current password (verified via `supabase.auth.signInWith(Email)`) before `supabase.auth.updateUser { password = ... }`.
- **UsuarioDetalleScreen → Seguridad → Cambiar contraseña** (`isSelf = false`): Superuser resets another user's password without needing their current password. Uses `adminClient.auth.admin.updateUserById(userId) { password = ... }`.

### New files

| File | Description |
|------|-------------|
| `ui/screen/perfil/CambiarContrasenaScreen.kt` | Full screen with PwTargetCard, scope banner, password fields with eye toggles, requirements checklist, success overlay, bottom bar |
| `ui/screen/perfil/CambiarContrasenaViewModel.kt` | `@HiltViewModel`; dispatches to self or admin API based on `isSelf`; reads route args via `SavedStateHandle.toRoute<CambiarContrasenaRoute>()` |
| `ui/screen/perfil/CambiarContrasenaUiState.kt` | Data class with inline validation computed properties (`meetsLength`, `meetsNumber`, `meetsCasing`, `passwordMismatch`, `isValid`) |

### Modified files

| File | Change |
|------|--------|
| `presentation/screens/AppRoutes.kt` | Added `CambiarContrasenaRoute(userId: String, isSelf: Boolean)` |
| `presentation/navigation/AppNavigation.kt` | Added `composable<CambiarContrasenaRoute>` entry |
| `ui/screen/perfil/PerfilScreen.kt` | `Cambiar contraseña` row gated to `SUPERUSUARIO`; navigates to `CambiarContrasenaRoute(userId, isSelf = true)` |
| `ui/screen/perfil/PerfilViewModel.kt` | Added `userId` field to `PerfilUiState` (threaded from `sessionManager.currentUser`) |
| `ui/screen/usuario/UsuarioDetalleScreen.kt` | New "Seguridad" section with `Cambiar contraseña` row; navigates to `CambiarContrasenaRoute(userId, isSelf = false)` |
| `res/values/strings.xml` | Added `cambiar_contrasena_*` and `usuario_detalle_seguridad` / `usuario_detalle_cambiar_contrasena*` strings |

---

## ✅ Resolved post-Phase 13b

### ⚡ Create/Edit ViewModels blocking the UI on save (6 s freeze)

`CreateMercadoViewModel`, `CreateClienteViewModel`, `CreateProductoViewModel` all called `resolvePhotoUrl()` from `onSave()`. That method called `storageService.uploadPhoto()` — a real Supabase Storage network request — **before** writing to Room. When online the upload took ~6 seconds, blocking navigation entirely.

**Fix** (all three ViewModels): removed `StorageService` from the constructor and deleted `resolvePhotoUrl()`. `onSave()` now stores `s.photoUri?.toString()` directly — a Room write that completes in ~1 ms — and calls `onSuccess()` immediately. Since every `repository.save()` already enqueues a `SyncOp.UPSERT`, `QueueProcessor` handles the photo upload + Supabase push in the background without any UI involvement.

Side-effect: edit operations are now properly offline-first too. Previously a failed upload would crash the save flow; now edits always write to Room first and sync when online.

**Modified files**: `ui/screen/mercado/CreateMercadoViewModel.kt`, `ui/screen/cliente/CreateClienteViewModel.kt`, `ui/screen/producto/CreateProductoViewModel.kt`.

---

### 📸 Offline image not shown after create; post-reconnect upload missing

When creating a mercado/cliente/producto offline the photo was not visible after save, and after reconnecting the photo was never uploaded to Supabase Storage.

**Root cause**: `resolvePhotoUrl()` called `.getOrNull()` on upload failure → stored `null` as `photoUrl`. No image shown. When the entity was later synced by `QueueProcessor`, it pushed the `null` photoUrl to Supabase and never attempted a Storage upload.

**Fix — ViewModels**: `resolvePhotoUrl` removed entirely (see above). `s.photoUri?.toString()` is stored, so a local `content://` URI is saved to Room on offline creates. Coil renders the local image immediately.

**Fix — `QueueProcessor.kt`**: Added `StorageService` injection. In the `upsert()` branch for `MERCADO`, `CLIENTE`, and `PRODUCTO`: if `entity.photoUrl?.startsWith("content://") == true`, the processor uploads the photo to Supabase Storage first, updates the Room entity with the resulting `https://` URL, then pushes the entity DTO to Supabase. If the upload throws (network failure), the outer `runCatching` catches it and the op remains in the queue for retry.

**Modified files**: `data/queue/QueueProcessor.kt` (+ `StorageService` constructor param + `content://` detection + upload in upsert branches for MERCADO / CLIENTE / PRODUCTO).

---

### 📱 Camera causes automatic navigation back when creating mercado/cliente

Tapping the camera shutter in `TakePicture` caused the Create screen to disappear and the mercado/cliente to never be created.

**Root cause**: Android killed the app process while the camera was in the foreground (low memory). On camera return the Activity was recreated, `AppViewModel` re-initialized, `isLoaded` flipped false→true, and `AppNavigation`'s `LaunchedEffect(isLoaded)` unconditionally ran `navController.navigate(HomeRoute) { popUpTo(LoginRoute) }`, popping the entire back stack.

**Fix — `AppNavigation.kt`**: added `navController.currentDestination?.hasRoute<LoginRoute>() == true` guard so the auto-login navigation only fires when the back stack actually shows the Login screen.

**Fix — `CreateMercadoScreen.kt`, `CreateClienteScreen.kt`**: changed `pendingCameraUri` from `remember { mutableStateOf<Uri?>(null) }` to `rememberSaveable { ... }` so the pending URI survives process death / Activity recreation.

**Modified files**: `presentation/navigation/AppNavigation.kt`, `ui/screen/mercado/CreateMercadoScreen.kt`, `ui/screen/cliente/CreateClienteScreen.kt`.

---

### 🍞 Pull-to-refresh error toast behind FAB + missing dismiss button

`RefreshErrorToast` was rendered inside the Scaffold content `Box`, placing it behind the FAB in Z-order. It also had no way to dismiss it.

**Fix — `RefreshErrorToast.kt`**: added `onDismiss: () -> Unit` parameter and an `IconButton` with a `Close` icon at the end of the Row.

**Fix — all four list screens** (`MercadosScreen`, `ClientesScreen`, `CatalogoScreen`, `ListaNegraScreen`): moved the `RefreshErrorToast` call from inside the content `Box` to the Scaffold's `snackbarHost` parameter. Material3 Scaffold renders the snackbar host above the FAB in Z-order, matching the expected visual hierarchy.

**Modified files**: `ui/common/RefreshErrorToast.kt`, `ui/screen/mercado/MercadosScreen.kt`, `ui/screen/cliente/ClientesScreen.kt`, `ui/screen/producto/CatalogoScreen.kt`, `ui/screen/lista_negra/ListaNegraScreen.kt`.

---

### 🔵 Sync badge position misaligned

The pending-ops badge dot on `SyncBarIcon` was placed too far from the cloud icon's top-right corner.

**Root cause**: `BadgedBox` from Material3 treats any badge taller than 6 dp as a "large badge" and applies `(-6 dp, -6 dp)` inward offsets designed for text-content badges. Our 8 dp dot was hitting this path and being pushed well away from the corner.

**Fix — `SyncBarIcon.kt`**: replaced `BadgedBox` with a direct `Box` overlay. The dot is positioned with `Modifier.align(Alignment.TopEnd).offset(x = (-6).dp, y = 6.dp)`, which places its center at ≈ (30 dp, 10 dp) in the 40 dp container — imperceptibly close to the 21 dp icon's top-right corner at (30.5 dp, 9.5 dp).

**Modified files**: `ui/common/SyncBarIcon.kt`.

---

## ✅ Resolved post-Phase 13

### 📸 StorageService — photo upload silently failing (no image after create)

After creating a mercado, cliente, or producto with a photo, no image appeared in the list or detail screens. The Supabase Storage bucket showed no new objects.

**Root cause**: `AndroidManifest.xml` had `tools:node="remove"` on the entire `androidx.startup.InitializationProvider` (added in Phase 9c to disable WorkManager auto-init). Removing the whole provider kills every `androidx.startup` initializer registered by any library — including `com.russhwolf.settings.SettingsInitializer` from `multiplatform-settings-no-arg`. That initializer is responsible for capturing the application `Context` so the no-arg `Settings()` constructor works on Android. supabase-kt Storage uses `Settings()` (via `SettingsResumableCache`) the moment `storage.from(bucket)` is called, so without the initializer every upload NPE'd immediately.

The NPE was previously swallowed by `.getOrElse { uriStr }` (which fell back to storing the local `content://` URI). After that fallback was removed in favor of `.getOrNull()` (see "Resolved post-Phase 12 → Cross-device photo not showing"), the failure became visible as `photoUrl = null`.

**Fix — `AndroidManifest.xml`**: Changed `tools:node="remove"` on the provider to `tools:node="merge"`, and moved the removal down to only the `WorkManagerInitializer` `<meta-data>` entry. The `InitializationProvider` now survives in the merged manifest, all third-party initializers run normally, and only WorkManager's auto-init is suppressed so Hilt's factory takes over.

```xml
<!-- Before (kills ALL androidx.startup initializers): -->
<provider
    android:name="androidx.startup.InitializationProvider"
    android:authorities="${applicationId}.androidx-startup"
    tools:node="remove" />

<!-- After (removes only WorkManager auto-init): -->
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

**Fix — `StorageService`**: Switched from the regular `SupabaseClient` to `@AdminClient` (service role key). The admin client bypasses Storage RLS INSERT policies entirely, so uploads work regardless of how the bucket policies are configured.

**Fix — `SupabaseModule`**: Added `install(Storage)` to `provideAdminSupabaseClient()` so the admin client has the Storage plugin available.

**Fix — `gradle/libs.versions.toml` + `app/build.gradle.kts`**: Added `multiplatform-settings-no-arg:1.1.1` as an explicit dependency. supabase-kt pulls it transitively, but declaring it explicitly ensures the Android AAR variant (with its manifest) is always resolved and appears in the merged manifest for verification.

### 🪵 Upload failure logging in create ViewModels

`resolvePhotoUrl` in all three create ViewModels previously swallowed upload exceptions silently. Added structured logging so failures surface immediately in Logcat:

- `Log.d(TAG, "resolvePhotoUrl: uri=$uriStr")` — emitted at the start of every `resolvePhotoUrl` call so the URI is visible before any network call.
- `.onFailure { Log.e(TAG, "Photo upload failed [$bucket/$entityId]: ${it.javaClass.simpleName}: ${it.message}", it) }` — logs the full exception with stack trace.

**Modified files**: `CreateMercadoViewModel.kt` (tag `"CreateMercadoVM"`), `CreateClienteViewModel.kt` (tag `"CreateClienteVM"`), `CreateProductoViewModel.kt` (tag `"CreateProductoVM"`).

### Modified files (all fixes)

| File | Change |
|------|--------|
| `app/src/main/AndroidManifest.xml` | `InitializationProvider` kept with `tools:node="merge"`; only `WorkManagerInitializer` meta-data entry removed |
| `di/SupabaseModule.kt` | `install(Storage)` added to `provideAdminSupabaseClient()` |
| `data/remote/StorageService.kt` | Injection changed from regular `SupabaseClient` to `@AdminClient`; `require(bytes.isNotEmpty())` guard added |
| `gradle/libs.versions.toml` | `multiplatformSettings = "1.1.1"` + `multiplatform-settings-no-arg` library entry added |
| `app/build.gradle.kts` | `implementation(libs.multiplatform.settings.no.arg)` added |
| `ui/screen/mercado/CreateMercadoViewModel.kt` | `Log.d`/`Log.e` added to `resolvePhotoUrl` |
| `ui/screen/cliente/CreateClienteViewModel.kt` | `Log.e` added to `resolvePhotoUrl` |
| `ui/screen/producto/CreateProductoViewModel.kt` | `Log.e` added to `resolvePhotoUrl` |

---

## ✅ Resolved post-Phase 12

### 🖼️ DetalleClienteScreen — cliente photo not displayed

`DetalleClienteHeader` called `ClienteAvatar(name = cliente.name, size = 76.dp)` without passing `photoUrl`. Photo was loaded into Room/Supabase but never reached the avatar composable.

**Fix** (`DetalleClienteHeader.kt`): `ClienteAvatar(name = cliente.name, photoUrl = cliente.photoUrl, size = 76.dp)`.

---

### 🌐 Cross-device photo not showing

When `StorageService.uploadPhoto()` failed silently, `resolvePhotoUrl` in the three create/edit ViewModels used `.getOrElse { uriStr }`, which stored the local `content://` URI in Room and Supabase. That URI is scoped to the originating device's MediaStore and is unreachable on any other device.

**Fix** (all three ViewModels — `CreateClienteViewModel`, `CreateMercadoViewModel`, `CreateProductoViewModel`): changed `.getOrElse { uriStr }` to `.getOrNull()`. Upload failure now results in `photoUrl = null` (honest and consistent across devices) rather than a dangling device-local URI.

**Root cause of upload failures**: the Supabase Storage bucket must be set to **Public** in the Supabase dashboard, and the RLS policy for INSERT must allow authenticated users without a restrictive path filter. See `docs/sql/storage.sql` and `docs/supabase-setup.md`.

---

### 🔄 Transient "Failed to sync: CLIENTES" toast on first login

After login, `DataSynchronizer.resetStaleness()` clears all `lastSyncedAt` timestamps, causing all entity types to trigger a full cold-fetch burst simultaneously. Under low connectivity the first attempt could time out or fail before the network fully settled — but `lastSyncedAt` is restored to the previous value on failure, so the next navigation retried automatically (self-healing).

**Fix** (`DataSynchronizer.kt`): added a silent single retry before emitting the error toast. If the first `withTimeoutOrNull` call returns `null` or `SyncResult.Failure`, the syncer runs once more before surfacing the error to the user.

---

### 🔄 Pull-to-refresh indicator hidden behind top bar (MercadosScreen)

`PullToRefreshBox` renders its indicator at `y = 0` of its own layout. When the box filled the full scaffold content area (which starts at `y = 0` below the status bar, before the top app bar), the indicator appeared under the top bar.

**Fix** (`MercadosScreen.kt`): added `.padding(top = innerPadding.calculateTopPadding())` to the `PullToRefreshBox` modifier and removed the duplicate top padding from the inner `LazyColumn` content padding.

---

### 🔄 Pull-to-refresh not working in empty state (ClientesScreen)

`PullToRefreshBox` detects the pull gesture via `NestedScrollConnection`. A static `Column` (the previous empty state container) does not participate in nested scrolling, so no pull gesture was ever forwarded.

**Fix** (`ClientesScreen.kt`): wrapped the empty state in a `LazyColumn` with the `EmptyState` composable as an `item { ... }` using `Modifier.fillParentMaxSize()`. `LazyColumn` provides the required scrollable surface so the pull gesture is detected even when there is only one item. Applied the same `padding(top = innerPadding.calculateTopPadding())` fix to the `PullToRefreshBox` modifier as in MercadosScreen.

---

## ✅ Phase 10 — Pull-to-Refresh (client-side pagination later removed in Phase 10c)

Applied to all four primary list screens: **Mercados**, **Clientes**, **Lista Negra**, **Catálogo**.

> **Note:** The client-side `LoadMoreButton` / `visibleCount` pagination added in this phase was removed in **Phase 10c**. The pull-to-refresh machinery (`PullToRefreshBox`, `_isRefreshing`, `onRefresh`, `repository.refresh()`) is still in place. The syncer's >1000-row edge case is now handled at the server level with `range()` batching (see Phase 10c).

### 📄 Client-side Pagination *(removed in Phase 10c)*

~~All list data is already in Room (synced from Supabase). Pagination is applied in the UI layer.~~
~~`LoadMoreButton` / `visibleCount` / `PAGE_SIZE = 20` / `hasMore` — all deleted. See Phase 10c.~~

### 🔄 Pull-to-Refresh

**DataSynchronizer** — new `suspend fun forceSync(entityType: String)`:
- If a sync for that entity is already in-flight, waits for it to complete (no duplicate network calls).
- Otherwise clears the staleness stamp (both in-memory `lastSyncedAt` and `SharedPreferences`) and calls `syncIfStale(entityType, 0L)`, which always runs because `elapsed >= 0 >= 0` → threshold `0` is never exceeded.
- Suspends until the sync finishes (success, failure, or timeout).

**Repository interfaces & impls** — new `suspend fun refresh()`:
- `MercadoRepository`, `ClienteRepository`, `ProductoRepository`, `PedidoRepository`.
- Each impl delegates to `dataSynchronizer.forceSync(EntityType.XXX)`.

**Entities refreshed per screen on pull-to-refresh:**
| Screen | Entities refreshed |
|---|---|
| MercadosScreen | MERCADO + CLIENTE + PEDIDO |
| ClientesScreen | CLIENTE + PEDIDO |
| ListaNegraScreen | CLIENTE |
| CatalogoScreen | PRODUCTO |

**Files changed:**
`data/sync/DataSynchronizer.kt` (+`forceSync`, +`first` import),
`domain/repository/MercadoRepository.kt`, `ClienteRepository.kt`, `ProductoRepository.kt`, `PedidoRepository.kt` (+`refresh`),
`data/repository/impl/MercadoRepositoryImpl.kt`, `ClienteRepositoryImpl.kt`, `ProductoRepositoryImpl.kt`, `PedidoRepositoryImpl.kt` (+`refresh`),
`ui/common/LoadMoreButton.kt` (rewritten — 4 states, design-matched),
`ui/common/SkeletonRow.kt` (new — `SkeletonClienteRow`),
`res/values/strings.xml` (+`load_more_*` strings for all 4 states),
`ui/screen/cliente/ClientesUiState.kt`, `ClientesViewModel.kt`, `ClientesScreen.kt`,
`ui/screen/mercado/MercadosUiState.kt`, `MercadosViewModel.kt`, `MercadosScreen.kt`,
`ui/screen/lista_negra/ListaNegraUiState.kt`, `ListaNegraViewModel.kt`, `ListaNegraScreen.kt`,
`ui/screen/producto/CatalogoUiState.kt`, `CatalogoViewModel.kt`, `CatalogoScreen.kt`,
`docs/progress.md`

---

## ✅ Phase 10b — Delta Sync + Pull-to-Refresh Error Toast

### 🔄 Delta Sync

Replaces full-table re-downloads with incremental fetches keyed on `updated_at`.

**How it works:**
- Each entity syncer (`MercadoSyncer`, `ClienteSyncer`, `ProductoSyncer`, `PedidoSyncer`) now accepts a `since: Long` parameter (epoch ms).
- If `since == 0L` (first run, Room is empty): full fetch with no filter.
- Otherwise: `WHERE updated_at > since` via supabase-kt filter DSL (`gt("updated_at", since)`).
- `DataSynchronizer.syncIfStale()` passes `previous = lastSyncedAt[entity] ?: 0L` to the syncer and updates `lastSyncedAt` only on success (rolls back on timeout or failure).
- `forceSync()` (called by pull-to-refresh) no longer clears `lastSyncedAt` — pull-to-refresh also uses the delta path.
- `detalle_pedido` has no `updated_at`: in delta mode, lines for changed pedidos are deleted and re-fetched via `isIn("pedido_id", changedIds)`. If no pedidos changed, the syncer returns `Success` immediately.

**`ClienteSyncer` local-only field preservation**: fetches the existing Room row by ID before mapping, so `isBlacklisted`, `blacklistReason`, `blacklistBalance`, `blacklistedAt`, `blacklistIsManualAmount`, and `primaryPhoneIndex` (fields absent in Supabase) are preserved on delta upsert.

**Room migration (v15 → v16):** `updatedAt INTEGER NOT NULL DEFAULT 0` added to `mercados`, `clientes`, `productos`, `pedidos`. Backfilled with `createdAt`. See `AppDatabase.MIGRATION_15_16`.

**Supabase changes required:** See *Staging environment changes* section in `docs/db-schema.md` for the exact SQL (add column, trigger function, indexes).

### 🍞 Pull-to-Refresh Error Toast

**Signal chain:** `syncIfStale()` / `forceSync()` → `Boolean` → `repository.refresh(): Boolean` → `_refreshFailed: MutableStateFlow<Boolean>` in ViewModel → `refreshFailed: Boolean` in UiState → `RefreshErrorToast` composable.

**`RefreshErrorToast`** (`ui/common/RefreshErrorToast.kt`):
- `surface2` bg, `border2` 1dp border, 14dp corner radius.
- Warning icon (20dp, `redText`) + "No se pudo actualizar" (semibold, `onSurface`) / "Sin conexión con el servidor" (`text3`) + `TextButton` with Refresh icon + "Reintentar" (`redText`).
- Positioned at `Alignment.BottomCenter` inside a `Box` wrapping the Scaffold content; padding = `navBar + 12dp`.
- Tapping "Reintentar" calls `onRefresh()`, which resets `_refreshFailed = false` before the new attempt.

**ViewModel pattern** (all 4 screens):
- `_refreshFailed: MutableStateFlow<Boolean>` chained via `.combine()` before `.stateIn()`.
- `onRefresh()` delegates to a use case (see below) or calls `repo.refresh()` directly for single-repo screens. All must succeed for the result to be `true`.
- `onRefreshErrorDismissed()` resets `_refreshFailed`.

**Use cases extracted:**
- `RefreshMercadoDataUseCase(mercadoRepo, clienteRepo, pedidoRepo)` — refreshes all three in parallel via `coroutineScope { async { } }`; used by `MercadosViewModel`. The "mercado dashboard requires all three current" is a domain rule, not ViewModel glue.
- `RefreshClienteDataUseCase(clienteRepo, pedidoRepo)` — refreshes the pair in parallel; used by `ClientesViewModel`. Client status is computed from unpaid pedidos, so both must be in sync.
- `ListaNegraViewModel` and `CatalogoViewModel` call a single `repo.refresh()` — no use case warranted for a one-liner.

**Files changed:**
`data/remote/dto/{Cliente,Mercado,Producto,Pedido}Dto.kt` (+`updatedAt`),
`data/local/entity/{Cliente,Mercado,Producto,Pedido}Entity.kt` (+`updatedAt`),
`data/mapper/{Cliente,Mercado,Producto,Pedido}Mapper.kt` (+`updatedAt` in `fromDto`),
`data/local/AppDatabase.kt` (v16, `MIGRATION_15_16`),
`di/DatabaseModule.kt` (+`MIGRATION_15_16`),
`data/sync/EntitySyncer.kt` (interface: `sync(since: Long)`),
`data/sync/impl/{Mercado,Cliente,Producto,Pedido}Syncer.kt` (delta logic),
`data/sync/DataSynchronizer.kt` (`syncIfStale` + `forceSync` return `Boolean`),
`domain/repository/{Cliente,Mercado,Producto,Pedido}Repository.kt` (`refresh(): Boolean`),
`data/repository/impl/*RepositoryImpl.kt` (`refresh(): Boolean`),
`domain/usecase/RefreshMercadoDataUseCase.kt` (new — parallel refresh of mercado+cliente+pedido),
`domain/usecase/RefreshClienteDataUseCase.kt` (new — parallel refresh of cliente+pedido),
`ui/screen/{cliente,mercado,lista_negra,producto}/***UiState.kt` (+`refreshFailed`),
`ui/screen/{cliente,mercado,lista_negra,producto}/***ViewModel.kt` (+`_refreshFailed`, `onRefreshErrorDismissed`; Mercados+Clientes delegate to use cases),
`ui/screen/{cliente,mercado,lista_negra,producto}/***Screen.kt` (+`RefreshErrorToast` overlay),
`ui/common/RefreshErrorToast.kt` (new),
`res/values/strings.xml` (+`refresh_error_{title,subtitle,retry}`),
`docs/db-schema.md` (v16, `updatedAt` columns, staging SQL),
`docs/progress.md`

---

## ✅ Phase 10c — Remove Client-Side Pagination; Server-Side Range Fetch

Replaced the UI load-more button with proper server-side batched fetching and LazyColumn unbounded rendering.

**Why**: `LoadMoreButton` was slicing a list that was already in Room. LazyColumn renders only visible items regardless of list size, so client-side pagination had no performance benefit. The real concern — Supabase PostgREST's 1 000-row cap on a single `select()` — is now solved at the syncer layer.

**Server-side range fetch (all 4 syncers)**:
- Full-fetch path (`since == 0L`) replaced with a `while(true)` loop calling `supabase.from(...).select { range(offset, offset + BATCH_SIZE - 1) }`.
- `BATCH_SIZE = 1000`. Loop breaks when `page.size < BATCH_SIZE`.
- Results accumulated via `buildList { addAll(page) }`.
- `PedidoSyncer` has two methods: `fetchAllPedidoPages()` and `fetchAllDetallPages()`.
- Delta path (`since > 0L`) unchanged — PostgREST's row cap is not a concern there.

**UI removal**:
- `ui/common/LoadMoreButton.kt` — deleted.
- All 4 UiStates — `visibleCount`, `visibleXxx`, `hasMore`, `PAGE_SIZE` removed.
- All 4 ViewModels — `_visibleCount`, `onLoadMore()`, and related resets removed; use case extractions (`RefreshMercadoDataUseCase`, `RefreshClienteDataUseCase`) unchanged.
- All 4 Screens — `onLoadMore` param removed; `items(state.visibleXxx)` → `items(state.xxx)`; `LoadMoreButton` item blocks removed.
- `res/values/strings.xml` — `load_more_*` and `pagination_load_more_label` strings removed.

**Files changed:**
`data/sync/impl/{Mercado,Cliente,Producto,Pedido}Syncer.kt` (+`fetchAllPages()` / `fetchAllDetallPages()`),
`ui/screen/{cliente,mercado,lista_negra,producto}/***UiState.kt` (removed pagination fields),
`ui/screen/{cliente,mercado,lista_negra,producto}/***ViewModel.kt` (removed `_visibleCount`, `onLoadMore`),
`ui/screen/{cliente,mercado,lista_negra,producto}/***Screen.kt` (removed `LoadMoreButton`),
`ui/common/LoadMoreButton.kt` (deleted),
`res/values/strings.xml` (removed `load_more_*`),
`docs/progress.md`

---

## ✅ Phase 9b — Infrastructure (NetworkMonitor, Error Manager, Data Sync, Queue)

Three inter-related infrastructure features added. All details in `docs/features/infrastructure.md`.

### 🌐 NetworkMonitor
`NetworkMonitorImpl` uses `ConnectivityManager.NetworkCallback` to emit `Flow<Boolean>` as connectivity changes. Checks `NET_CAPABILITY_VALIDATED` (captive portals without internet → offline). Provided via `di/NetworkModule.kt`.

### ⚠️ Centralized Error Manager
- `AppError` (sealed class): `Network`, `Database`, `Sync`, `Queue`, `Unknown`.
- `AppErrorLogger`: routes to `Log.e`/`Log.w` with structured tags.
- `GlobalErrorHandler`: `@Singleton` `SharedFlow<AppError>` event bus. Injected into syncers and `QueueProcessor`.
- `AppNavigation` collects errors → `Toast.LENGTH_LONG`. Screens with own error UI (e.g. Login's inline banner) do not re-emit through this handler.

### 🔄 Data Synchronizer (read path)
`DataSynchronizer` runs on app start and on connectivity restore. Fetches all records from Supabase with a 10s timeout; on timeout/failure, Room data continues to serve the UI. Syncers: `MercadoSyncer`, `ClienteSyncer` (merges local-only fields), `ProductoSyncer`, `PedidoSyncer` (+ detalles). Room v14 schema unchanged (no new tables for syncers).

### 📋 Network Request Queue (write path)
New Room table `sync_operations` (Room v14, migration 13→14). Every repository write enqueues a `SyncOperationEntity` (`UPSERT` or `DELETE`) after the local Room write. `QueueProcessor` flushes the queue when the device is online:
- Deduplicates entries per entity (`DELETE` wins over `UPSERT`)
- For `UPSERT`: reads entity from Room → pushes DTO to Supabase
- For `DELETE`: calls `supabase.from(table).delete().eq("id", ...)`
- Retries up to 3 times; abandoned entries remain for debugging

**Files changed (Phase 9b):**
`data/network/NetworkMonitor.kt`, `data/network/NetworkMonitorImpl.kt`, `di/NetworkModule.kt`,
`domain/error/AppError.kt`, `data/error/AppErrorLogger.kt`, `data/error/GlobalErrorHandler.kt`,
`data/local/entity/SyncOperationEntity.kt`, `data/local/dao/SyncOperationDao.kt`,
`data/local/AppDatabase.kt` (v13→v14 + migration), `di/DatabaseModule.kt`,
`data/mapper/MercadoMapper.kt`, `data/mapper/ClienteMapper.kt`, `data/mapper/ProductoMapper.kt`,
`data/mapper/PedidoMapper.kt`, `data/mapper/DetallePedidoMapper.kt`,
`data/sync/EntitySyncer.kt`, `data/sync/DataSynchronizer.kt`,
`data/sync/impl/MercadoSyncer.kt`, `data/sync/impl/ClienteSyncer.kt`,
`data/sync/impl/ProductoSyncer.kt`, `data/sync/impl/PedidoSyncer.kt`,
`data/queue/QueueProcessor.kt`,
`data/repository/impl/MercadoRepositoryImpl.kt`, `data/repository/impl/ClienteRepositoryImpl.kt`,
`data/repository/impl/ProductoRepositoryImpl.kt`, `data/repository/impl/PedidoRepositoryImpl.kt`,
`PedidosApp.kt`, `presentation/navigation/AppViewModel.kt`, `presentation/navigation/AppNavigation.kt`,
`ui/screen/cliente/DetalleClienteActions.kt` (fixed pre-existing preview bug),
`docs/features/infrastructure.md`, `docs/progress.md`

---

## ✅ Phase 9f — In-app theme switcher (Claro / Oscuro / Sistema)

Three-way theme selector added to **Mi Perfil → Ajustes** (visible to all users). Full details in `docs/features/perfil.md → Theme system`.

### What changed

**New files:**
- `domain/model/ThemeMode.kt` — `LIGHT`, `DARK`, `SYSTEM` enum
- `data/prefs/ThemeManager.kt` — `@Singleton`, `SharedPreferences("theme_prefs")`, `StateFlow<ThemeMode>` + `setTheme()`

**Updated files:**
- `ui/theme/Theme.kt` — `EcomerceCarlosVTheme` now accepts `themeMode: ThemeMode`; resolves `darkTheme` from it; `SideEffect` drives `isAppearanceLightStatusBars` / `isAppearanceLightNavigationBars` via `WindowCompat.getInsetsController` so the device status bar and navigation bar icons follow the theme
- `MainActivity.kt` — field-injects `ThemeManager`; collects `themeMode` flow inside `setContent`; passes it to `EcomerceCarlosVTheme`
- `ui/screen/perfil/PerfilUiState.kt` — added `themeMode: ThemeMode`
- `ui/screen/perfil/PerfilViewModel.kt` — injects `ThemeManager`; collects theme flow → `state.themeMode`; exposes `setTheme()`
- `ui/screen/perfil/PerfilScreen.kt` — new `AppearanceCard` composable (rounded card, moon icon header, 3-button segment selector); "Ajustes" section now shown for **all users** (was superuser-only); Umbrales row remains inside Ajustes but superuser-gated
- `res/values/strings.xml` — added `perfil_apariencia_title`, `perfil_apariencia_subtitle`, `perfil_theme_light`, `perfil_theme_dark`, `perfil_theme_system`
- `docs/features/perfil.md`, `docs/progress.md`

---

## ✅ Phase 9e — Lazy per-screen data sync with staleness thresholds

Data is no longer fetched eagerly on app start. Each repository now triggers `DataSynchronizer.triggerSyncIfStale(entityType, thresholdMs)` from its primary read Flow methods. Full details in `docs/features/infrastructure.md → Data Synchronizer`.

### How it works

`DataSynchronizer` keeps an in-memory `ConcurrentHashMap<String, Long>` (`lastSyncedAt`) that records when each entity type was last successfully synced. On every `triggerSyncIfStale` call:
1. If offline → no-op
2. If `now - lastSyncedAt[entityType] < threshold` → no-op (data is fresh)
3. Otherwise → stamp the entity as "syncing now" (prevents duplicate concurrent syncs), launch the corresponding `EntitySyncer`, update Room. On failure/timeout → remove stamp so next navigation retries.

The map is **in-memory only** — resets on every app start, so the first navigation after login always fetches fresh data. On connectivity restore, the map is cleared entirely.

### Thresholds

| Entity | Threshold | Rationale |
|---|---|---|
| `MERCADO` | 2 hours | Master data, rarely changes mid-day |
| `PRODUCTO` | 2 hours | Catalog data, rarely changes mid-day |
| `CLIENTE` | 30 minutes | Active business data |
| `PEDIDO` | 30 minutes | High-frequency transaction data |

### Where sync is triggered (repositories)

| Repository method | Entity synced |
|---|---|
| `MercadoRepositoryImpl.getAll()` | MERCADO |
| `ClienteRepositoryImpl.getAll/getAllIncludingBlacklisted/getByMercado/getBlacklisted()` | CLIENTE |
| `ProductoRepositoryImpl.getAll()` | PRODUCTO |
| `PedidoRepositoryImpl.getByCliente/getByClienteWithLines/getAll/getAllUnpaid()` | PEDIDO |

`getByIdFlow()` methods do not trigger sync — by the time a detail screen opens, the list screen has already triggered it.

**Files changed (Phase 9e):**
`data/sync/DataSynchronizer.kt` (full rewrite — lazy threshold model, `triggerSyncIfStale`, connectivity-restore clears map),
`data/repository/impl/MercadoRepositoryImpl.kt`,
`data/repository/impl/ClienteRepositoryImpl.kt`,
`data/repository/impl/ProductoRepositoryImpl.kt`,
`data/repository/impl/PedidoRepositoryImpl.kt`,
`docs/features/infrastructure.md`, `docs/progress.md`

---

## ✅ Phase 9d — Always require login on app start

The user is always required to log in every time the app starts. A persisted JWT from a previous session no longer auto-restores the user. Full details in `docs/features/auth.md → Session behaviour on app restart`.

### How it works

`DataStoreGoTrueSessionManager.loadSession()` uses a `@Volatile firstLoad` flag. On the first call (always startup), it deletes the stored JWT and returns `null`. supabase-kt immediately emits `NotAuthenticated` — the `AppNavigation` auto-navigate `LaunchedEffect` never fires, and the user sees Login.

After login, `saveSession()` stores the new JWT. Subsequent `loadSession()` calls (`firstLoad = false`) return the JWT normally so token refresh works during the active session.

`SessionManagerImpl` uses a `startupDone` flag to distinguish the startup clear from an explicit logout. `wipeLocalDataIfNeeded()` (which calls `database.clearAllTables()`) is skipped during the startup clear so Room cache (mercados, clientes, pedidos, sync queue) survives restarts. It still runs on explicit logout.

**Files changed (Phase 9d):**
`data/session/DataStoreGoTrueSessionManager.kt` (+ `firstLoad` flag, wipe-on-first-load in `loadSession()`),
`data/session/SessionManagerImpl.kt` (+ `startupDone` flag, skip wipe on startup `NotAuthenticated`),
`docs/features/auth.md`, `docs/progress.md`

---

## ✅ Phase 9c — Queue improvements (WorkManager + immediate flush)

Three improvements to the network request queue introduced in Phase 9b. Full details in `docs/features/infrastructure.md`.

### Background processing survives app kills (WorkManager)
`SyncWorker` (`@HiltWorker`, `CoroutineWorker`) runs every 15 minutes whenever `NetworkType.CONNECTED`. On each run it calls `syncOperationDao.resetAllRetryCount()` then `queueProcessor.flush()`. This means pending queue entries are never permanently abandoned — they get a fresh set of retry attempts on every worker run, even after the app was killed.

`PedidosApp` now implements `Configuration.Provider` and supplies a `HiltWorkerFactory` so Hilt can inject dependencies into the worker. WorkManager auto-init is disabled in the manifest so Hilt's factory takes over.

### Immediate flush when a new entry is enqueued while already online
Previously `QueueProcessor` only flushed on connectivity-restore events. A write made while already online would sit in the queue for up to 15 minutes. Now a second coroutine observes `observeLatestEnqueuedId(): Flow<Long>` (returns `MAX(id)` from `sync_operations`). When a new row is inserted, the max ID increases, triggering an immediate `flush()` if online.

### Reliable trigger: MAX(id) instead of COUNT(*)
`COUNT(*)` is fragile as a change trigger: if a deletion and an insertion happen at the same time, Room may coalesce both into one notification and the count may appear unchanged, causing `distinctUntilChanged` to suppress the emission. `MAX(id)` always increases on a new insert (auto-increment), so no new row can ever be missed.

**Files changed (Phase 9c):**
`gradle/libs.versions.toml` (+ `workManager`, `hiltWork` versions + library entries),
`app/build.gradle.kts` (+ `work-runtime`, `hilt-work`, `ksp(hilt-work-compiler)`),
`AndroidManifest.xml` (+ `RECEIVE_BOOT_COMPLETED` permission, disable WorkManager auto-init provider),
`data/queue/SyncWorker.kt` (new),
`di/WorkerModule.kt` (new),
`data/local/dao/SyncOperationDao.kt` (+ `observeLatestEnqueuedId()`, + `resetAllRetryCount()`),
`data/queue/QueueProcessor.kt` (+ second flush-trigger coroutine),
`PedidosApp.kt` (implements `Configuration.Provider`, injects `HiltWorkerFactory`, schedules `SyncWorker`),
`docs/features/infrastructure.md`, `docs/progress.md`

---

## ✅ Phase 9 — Auth + Supabase wiring (completed)

### 🔐 Supabase Authentication — real auth implemented
`LoginViewModel` now calls `supabase.auth.signInWith(Email)`. Three error paths:
- **Invalid credentials** → inline red error banner above fields (bold title + body text); both fields show red border + error hint below password.
- **Account disabled/banned** → blocking card replaces form (ban icon, "Tu cuenta está desactivada", "Contactar al administrador" + "Entrar con otra cuenta" buttons). Detected via `"banned"` in `RestException.message`.
- **No connection** → generic error message in the banner.

### 💾 Session persistence — DataStore-backed
`SessionManagerImpl` persists the `userId` in DataStore and the JWT in `DataStoreGoTrueSessionManager`.
On app restart, supabase-kt auto-loads the JWT from DataStore; `SessionManagerImpl` listens to
`auth.sessionStatus` and restores `currentUser` from Room (or fetches from Supabase on first device).
- `SessionStatus.LoadingFromStorage` → renamed `Initializing` in supabase-kt 3.1.4
- `SessionStatus.NetworkError` → renamed `RefreshFailure` in supabase-kt 3.1.4

### 🔗 Create user — wired to Supabase admin API
`CrearUsuarioViewModel` calls `adminClient.auth.admin.createUserWithEmail(...)` (renamed in supabase-kt 3.1.4), inserts a row in the `users` table, and upserts into local Room. Requires `STAGING/PRODUCTION_SECRET_KEY` in `local.properties`.

Admin operations use `banDuration`:
- Deactivate user → `banDuration = "876000h"` (~100 years; Supabase has no permanent-ban boolean)
- Reactivate user → `banDuration = "none"`

### 👥 Three-role schema (client requirement)
| Role | Access |
|------|--------|
| SUPERUSUARIO | Full CRUD on all tables, manage users and roles |
| USUARIO | Full CRUD on business tables (mercados/clientes/productos/pedidos); read/edit own profile only |
| INVITADO | Read-only on all tables; read own profile only |

**App changes:** `UserRole.INVITADO` enum added. `RoleBadge` updated (accent-tint/primary for USUARIO, blue-tint/blueText/eye icon for INVITADO). Role picker shows 3 cards; only the selected one expands its permissions list. `GestionUsuariosScreen` now groups users into 3 sections (Super usuarios / Usuarios / Invitados). Activate/Deactivate button toggles based on `user.isActive`.

### 🔏 Role-gated UI (canWrite pattern)

All business screens enforce the three-tier role matrix at the UI level via a `canWrite: Boolean` field in each UiState (computed as `user?.role != UserRole.INVITADO`).

**INVITADO users see all data but cannot:**
- Create mercados (FAB hidden in MercadosScreen)
- Edit or delete mercados (edit icon + danger zone hidden in DetalleMercadoScreen)
- Create clients (FAB + empty-state action hidden in ClientesScreen)
- Edit clients, create pedidos, blacklist/unblacklist, or add saldo extra (all hidden in DetalleClienteScreen)
- Edit pedidos or record payments (edit icon + bottom bar hidden in DetallePedidoScreen)
- Create or edit products; product rows are non-clickable with no chevron (CatalogoScreen)

User management (`GestionUsuariosScreen`) is gated separately in `PerfilScreen` by checking `state.role == UserRole.SUPERUSUARIO` — unchanged from Phase 9 auth wiring.

**Files changed (role-gating):**
`ui/screen/mercado/MercadosUiState.kt`, `MercadosViewModel.kt`, `MercadosScreen.kt`,
`ui/screen/mercado/DetalleMercadoUiState.kt`, `DetalleMercadoViewModel.kt`, `DetalleMercadoScreen.kt`,
`ui/screen/cliente/ClientesUiState.kt`, `ClientesViewModel.kt`, `ClientesScreen.kt`,
`ui/screen/cliente/DetalleClienteUiState.kt`, `DetalleClienteViewModel.kt`, `DetalleClienteScreen.kt`, `DetalleClienteActions.kt`,
`ui/screen/pedido/DetallePedidoUiState.kt`, `DetallePedidoViewModel.kt`, `DetallePedidoScreen.kt`,
`ui/screen/producto/CatalogoUiState.kt`, `CatalogoViewModel.kt`, `CatalogoScreen.kt`

See `docs/features/usuarios.md → canWrite pattern` for the full element table.

### 🌍 Environments
Two product flavors: **staging** and **production**. Each reads its own Supabase URL + keys from
`local.properties`. Build variants: `stagingDebug`, `stagingRelease`, `productionDebug`, `productionRelease`.
Staging fully wired and tested. Production keys TBD.

### 📋 SQL docs
`docs/sql/schema.sql` — all CREATE TABLE statements with inline `ENABLE ROW LEVEL SECURITY`.
`docs/sql/rls.sql` — 3-tier RLS policies (SUPERUSUARIO / USUARIO / INVITADO).
`docs/sql/storage.sql` — Storage bucket creation + policies.

**Files changed (Phase 9 + this session):**
`gradle/libs.versions.toml`, `app/build.gradle.kts`, `local.properties`,
`di/AppQualifiers.kt`, `di/DataStoreModule.kt`, `di/SupabaseModule.kt`,
`data/session/DataStoreGoTrueSessionManager.kt`, `data/session/SessionManagerImpl.kt`,
`domain/session/SessionManager.kt`, `domain/model/UserRole.kt`,
`domain/repository/UserRepository.kt`, `data/remote/dto/UserDto.kt`, `data/mapper/UserMapper.kt`,
`data/repository/impl/UserRepositoryImpl.kt`,
`presentation/navigation/AppNavigation.kt`, `presentation/navigation/AppViewModel.kt`,
`MainActivity.kt`,
`ui/screen/auth/LoginUiState.kt`, `ui/screen/auth/LoginViewModel.kt`,
`ui/screen/auth/LoginScreen.kt`, `ui/screen/auth/LoginContent.kt`, `ui/screen/auth/LoginComponents.kt`,
`ui/screen/usuario/CrearUsuarioViewModel.kt`, `ui/screen/usuario/UsuarioDetalleViewModel.kt`,
`ui/screen/usuario/GestionUsuariosViewModel.kt`, `ui/screen/usuario/GestionUsuariosUiState.kt`,
`ui/screen/usuario/GestionUsuariosScreen.kt`, `ui/screen/usuario/UsuarioDetalleScreen.kt`,
`ui/screen/usuario/CrearUsuarioScreen.kt`, `ui/screen/usuario/UserUiModel.kt`,
`ui/screen/perfil/PerfilViewModel.kt`, `ui/screen/perfil/EditarPerfilViewModel.kt`,
`ui/common/RoleBadge.kt`,
`docs/sql/schema.sql`, `docs/sql/rls.sql`, `docs/supabase-setup.md`,
`docs/features/auth.md`, `docs/progress.md`

---

## ✅ Post-Phase 8 improvements

### 📱 Primary phone for clients (DB v13)

Users can now mark one phone number as **primary** in the Create/Edit Cliente form. The primary phone is the one displayed in `DetalleClienteScreen`.

**PhoneListField redesign** (`CreateClienteComponents.kt`):
- Each `PhoneRow` is a custom card (52dp min height, 14dp corners, `surface2` bg). Left to right: radio circle (24dp, filled `primary`+Check when primary, outlined `text3` when not), phone icon, `BasicTextField` (15.5sp Monospace), "PRINCIPAL" badge (primary text, `accentTint` bg, only on primary row), call button (edit mode + non-empty only), delete button (dimmed when only one phone).
- Primary row has a 1.5dp `primary` border; non-primary has 1dp `border2`.
- Info hint below the label: "El teléfono **principal** es el que aparece en el detalle del cliente."
- "Agregar otro teléfono" is a bordered pill row.

**Tap-to-call:**
- `DetalleClienteScreen` — primary phone chip launches `Intent(ACTION_DIAL)` (already existed; now uses primary phone instead of `firstOrNull()`).
- `EditarClienteScreen` — each phone row shows a `Call` icon button that dials that specific number.

**DB:** `MIGRATION_12_13` adds `primaryPhoneIndex INTEGER NOT NULL DEFAULT 0` to `clientes`. Room bumped to **v13**.

**Files changed:** `Cliente.kt`, `ClienteEntity.kt`, `ClienteMapper.kt`, `AppDatabase.kt`, `DatabaseModule.kt`, `CreateClienteFormState.kt`, `CreateClienteViewModel.kt`, `CreateClienteComponents.kt`, `CreateClienteScreen.kt`, `DetalleClienteHeader.kt`, `docs/db-schema.md`, `docs/features/clientes.md`.

---

### 📥 Reports saved to Downloads folder

Report export no longer opens the print dialog. Files are now written directly to the device's **Downloads** folder and a `Toast` confirms the result.

**`ReporteSaver.kt`** (`ui/screen/reporte/`):
- `saveReportToDownloads(context, html, fileName): SaveResult` — coroutine (Dispatchers.IO).
  - **API 29+**: `MediaStore.Downloads` — no storage permission required.
  - **API 24–28**: `Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)` + `File.writeText`.
- `SaveResult`: `Success(fileName)` / `NoSpace` / `Error(cause)`.
- `showSaveToast(context, result)` — shows `Toast.LENGTH_LONG` in Spanish.
- Error detection: IOException message checked for `ENOSPC`/`No space left` → `NoSpace`; all others → `Error`.

**Filenames:** `Reporte_Diario_YYYYMMDD_HHmm.html`, `Reporte_PorCliente_YYYYMMDD_HHmm.html`, `Reporte_{ClientName}_YYYYMMDD_HHmm.html`.

**Files changed:** `ReporteScreen.kt`, `ReporteClienteScreen.kt`, `ReporteSaver.kt` (new), `strings.xml` (3 new toast strings).

---

### 🔄 Report generation status screen (`ReporteStatusScreen`)

Tapping the export action now navigates to a dedicated status screen instead of saving directly.

**Generating state:**
- Shimmer skeleton document thumbnail (150×197dp, infinite `Brush.linearGradient` animation).
- "Creando tu reporte…" title + item-count description (e.g. "Reuniendo 23 pedidos").
- `LinearProgressIndicator` animated in three steps (0→12%→62%→95% over ~950ms total).
- Three `GenStep` rows: step 0 "Pedidos/Movimientos reunidos" → step 1 "Generando el documento" → step 2 "Listo para descargar". Each step has a circle badge (active: spinning sync icon on `accentTint`; done: check on `greenTint`; todo: dot on `surface3`).
- AppBar: "Generando reporte" + "Cancelar" text button.

**Ready state:**
- Green check badge (56dp, 18dp corners).
- File meta card: red-tint doc icon + filename + "HTML · N KB".
- AppBar: "Reporte listo".
- Bottom bar:
  - **Compartir** → `Intent.ACTION_SEND` with cached file via `FileProvider` (Android native share sheet).
  - **Descargar** → `saveReportToDownloads` → toast. Turns green ("Descargado" + Check) after first tap.

**`ReporteExportHolder`** (singleton): calling screen sets `pending = PendingExport(html, fileName, itemCount, isMovimientosVariant)` before navigating to `ReporteStatusRoute`.

**`file_paths.xml`**: added `<cache-path name="reports" path="reports/" />` for `FileProvider`.

**Files changed:** `ReporteStatusScreen.kt` (new), `ReporteExportHolder.kt` (new), `AppRoutes.kt` (+ `ReporteStatusRoute`), `AppNavigation.kt` (+ composable), `HomeScreen.kt` (pass navController to ReporteScreen), `ReporteScreen.kt` (navigate instead of direct save), `ReporteClienteScreen.kt` (navigate instead of direct save), `file_paths.xml`.

---

### 🎨 Reportes UI polish (same session as Phase 8)

- **Mode toggle**: selected tab now uses `primary` bg + `onPrimary` text (was `surface`/`onSurface`); unselected uses `text2` (was `text3`).
- **Chip rows**: `DiarioDateChips`, `ClienteDateChips`, `PresetChipsRow` changed from `Row + horizontalScroll` → `FlowRow` so chips wrap instead of being cut off.
- **Resolved date bar** (`ReporteResolvedDateBar`): shown below chip rows for non-Personalizado presets. 42dp pill, `surface2`+`border`, calendar icon + formatted date text. `formatDiarioBarText` / `formatClienteBarText` helpers in `ReporteDateChips.kt`.
- **`ReporteStatCard` redesign**: added 34×34dp icon container (10dp corners, tinted bg + matching icon) above the value; value bumped to 22sp SemiBold Monospace; removed `bgColor` param, added `icon` + `iconBgColor`.

---

## ✅ Phase 8 — Reportes tab

Full **Reportes** screen (tab 3 of bottom nav) with two modes and PDF export:

**Diario mode:** segmented toggle → date chip bar (Hoy/Ayer/Semana/Personalizado) → "Cobrado hoy" hero card (green gradient, 38sp mono amount) → two stat cards (Pedidos creados / Pendiente del día) → Movimientos list (8dp dot, name, type·mercado·time, amount).

**Por cliente mode:** date chip bar (Este mes/Trimestre/Año/Personalizado) → client selector card with "Cambiar" → `ClienteSelectorSheet` (ModalBottomSheet, alphabetical client list) → three stat cards (Facturado/Pagado/Saldo) → Historial list (36dp icon tile, title, status subtext, amount).

**"Personalizado":** both modes show two `DateField` composables (Desde/Hasta) that open Material3 `DatePickerDialog`.

**Report export:** top-bar `Description` icon (Reportes tab) and "Generar PDF" button (`ReporteClienteScreen`) → saves `.html` file to **Downloads** via `ReporteSaver.kt` (MediaStore on API 29+, File API on 24–28). Toast on success/error. *(Originally WebView+PrintManager; replaced post-Phase 8 — see "Post-Phase 8 improvements" above.)*

**Data layer:** `PedidoRepository.getAll(): Flow<List<Pedido>>` added (no DB migration — reads existing `pedidos` table).

**Home logo + Búsqueda Global (also Phase 8):**
- `LogoMark()` composable added as `leading` in `MercadosScreen` top bar (34dp circle, white bg, border2, `img_logo.png`).
- `BusquedaScreen` now has three sections: Clientes / Lista Negra / Mercados. `BlacklistResultRow` shows a 18dp red ban badge overlay on the avatar + "En Lista Negra" red pill chip.

**Reporte de Pedidos (also Phase 8):** full PDF-preview screen accessible from the "Generar reporte" menu in `DetalleClienteScreen`. Shows client info, resume summary cards, and full pedidos list. Exports via the same `WebView + PrintManager` pattern.

See `docs/features/reporte.md`, `docs/features/busqueda.md`, `docs/features/clientes.md`.

---

## ✅ Resolved post-Phase 7

### ⚖️ Client status decoupled from saldo-extra balance

`computeStatus()` now uses a separate `statusBalance` (only `PARTIAL && !isSaldoExtra` pedidos) instead of the full display balance. The displayed "Saldo pendiente total" still includes saldo-extra entries, but the red/amber/green badge and row gradient are driven exclusively by real unpaid orders.

**Before:** a client with only saldo-extra debt would show as ADVERTENCIA/CRITICO.  
**After:** that client shows as AL_DIA in color/badge; the extra amount is still visible in the balance block.

Applied identically in both `DetalleClienteViewModel` and `ClientesViewModel`. See **`docs/features/clientes.md → Client Status Thresholds`** for the full rule.

---

### 📦 Expandable `PedidoRow` with product lines

Regular pedido rows now display a **chevron button** (42×42dp, 12dp radius) in place of the left icon tile. Tapping the chevron expands an inline product-list panel below the row with a 180° animated caret rotation. Saldo-extra rows are unchanged (amber Tag tile, amber bg tint).

**Domain:** `PedidoLineItem(productName, quantity)` added. `Pedido.lines: List<PedidoLineItem>` field added (default empty).

**Data layer:** `PedidoWithLines` Room POJO (`@Embedded PedidoEntity` + `@Relation List<DetallePedidoEntity>`). `PedidoDao.getByClienteWithLines()` (`@Transaction` query). `PedidoMapper.toDomain(PedidoWithLines)` overload maps `product_name` + `quantity` into `PedidoLineItem` list. `PedidoRepository.getByClienteWithLines()` added; `PedidoRepositoryImpl` implements it.

**ViewModel:** `DetalleClienteViewModel` now uses `getByClienteWithLines` instead of `getByCliente`, so every emission carries line items.

**No DB migration required** — `product_name` was already present in `detalle_pedido` since Room v9 (Phase 4). `CreatePedidoUseCase` was already saving it from `CartItem.productName`.

**`PedidoRow.kt` redesign:** `CaretButton` composable (accentSoft bg + primary border when expanded, surface3 + border when collapsed; `animateFloatAsState` 180° rotation). `PedidoLinesPanel` composable (surface bg card, border inset, 10.5sp uppercase "N PRODUCTOS" header, `HorizontalDivider`-separated rows with product name + mono `×qty`). `AnimatedVisibility` with `expandVertically + fadeIn/Out(tween 180ms)` controls panel visibility. Panel left-padding aligns under row content (75dp = 20dp horizontal + 42dp tile + 13dp gap).

---

### 🚫 Lista Negra — row navigation, balance redesign, unblacklist resolution sheet, filter chips

Four improvements applied to the lista negra / detalle cliente flow:

1. **Row navigation in `ListaNegraScreen`** — `BlacklistRow` is now clickable and navigates to `DetalleClienteRoute(clienteId)`, allowing users to view the client detail and remove them from the blacklist.

2. **`DetalleClienteScreen` balance redesign** — `BalanceBlock` is now a unified component with three states:
   - *Normal*: status-based gradient, "Saldo pendiente total", 32sp, `ClienteStatusBadge`
   - *AUTO blacklisted*: same as normal + full-width "Lista Negra" `BalanceCard` in the breakdown
   - *MANUAL blacklisted*: red gradient, "Saldo en Lista Negra", 29sp `redText`, circular "⊘ Manual" badge + `BalanceCaption` info line + breakdown cards shown as frozen/inactive
   - `BalanceBreakdown` always rendered: side-by-side "Pedidos" (blue) + "Saldo extra" (amber) `BalanceCard`s; plus full-width LN card when AUTO blacklisted

3. **Unblacklist resolution sheet (`QuitarListaNegraSheet`)** — When `blacklistIsManualAmount == true`, tapping "Quitar de Lista Negra" opens a `ModalBottomSheet` (mockup-faithful design: centered green header icon, radio-style option cards with icon tile + radio circle, amber info banner). Two options:
   - *Restaurar pedidos y saldos* — clears the blacklist; pedidos are unchanged. Pre-selected by default.
   - *Marcar todo como pagado* — marks all non-PAID pedidos as PAID. If `blacklistBalance > (pedidosBalance + extraBalance)`, a saldo extra for the difference is created via `CreateSaldoExtraUseCase` before the client is unblacklisted. Option is disabled when `blacklistIsManualAmount == false`.
   When `blacklistIsManualAmount == false` (AUTO), the client is unblacklisted immediately with no sheet.

4. **"Cuenta" section with filter chips** — The pedidos section is renamed "Cuenta". A three-dot `PedidosMenuButton` in the section header lets the user filter by Pendiente / Parcial / Pagado (toggle, multi-select). Active filters appear as colored status chips. Section header shows "N de M" count. ViewModel computes `pedidosBalance`, `extraBalance`, `unpaidPedidosCount`, `unpaidExtraCount` from the unfiltered list.

**DB:** Room migration 11→12 adds `blacklistIsManualAmount INTEGER NOT NULL DEFAULT 0` to `clientes`. Existing blacklisted rows default to `false` (treated as AUTO). DB version bumped to 12.

### 💥 FK cascade data loss on mercado/cliente save — fixed

**Root cause:** Room 2.7+ enables `PRAGMA foreign_keys = ON` by default. All parent-table DAOs (`MercadoDao`, `ClienteDao`) used `@Insert(onConflict = REPLACE)`, which internally DELETEs the old row before inserting the replacement — firing `ON DELETE CASCADE` and wiping all child rows (clientes, pedidos, detalle_pedido).

**Symptom:** Editing a mercado's location field deleted all clientes and pedidos belonging to that mercado.

**Fix:** All parent-table DAOs switched to `@Insert(onConflict = IGNORE)` returning `Long`. Repositories now fall through to `@Update` when `insert()` returns `-1`. `PedidoDao` and `DetallePedidoDao` also switched to `IGNORE` as a precaution. See `docs/db-schema.md → Data integrity` for the canonical pattern.

### 💾 `fallbackToDestructiveMigration` removed

Removed `.fallbackToDestructiveMigration(dropAllTables = true)` from `DatabaseModule`. Room now throws on a missing migration instead of silently dropping all tables.

### 🖼️ Profile photo not rendering — fixed

`ProfileAvatar` composable updated to accept an optional `photoUrl` parameter and delegate to `PhotoThumbnail` (initials as fallback). Wired through `PerfilUiState`, `PerfilViewModel`, `LoginFormState`, and `LoginViewModel` so both the profile screen and the biometric login welcome card show the user's photo when set.

### 📍 DetalleMercadoScreen — UBICACIÓN section always visible

Removed the `if (!mercado.mapsUrl.isNullOrBlank())` guard around the UBICACIÓN section. `MapsLinkField` already handles blank values gracefully (shows placeholder, hides "Abrir" chip).

### 🔄 DetalleMercadoScreen — stale data after editing

`DetalleMercadoViewModel` was a one-shot `init` loader. Replaced with a reactive `stateIn` over `MercadoRepository.getByIdFlow(mercadoId)`. Added `getByIdFlow(id)` to `MercadoDao` (Flow-returning query), `MercadoRepository` interface, and `MercadoRepositoryImpl`. The screen now updates automatically when any edit is saved.

### 👤 Profile state not reflecting edits immediately

`PerfilViewModel.loadProfile()` was reading `sessionManager.currentUser.value` once in an `init` coroutine. Changed to `sessionManager.currentUser.collect { … }` so the UI reacts to session updates without a navigate-back/navigate-in cycle.

### 🏠 Home screen avatar not showing profile photo

`MercadosUiState` and `MercadosViewModel` now thread `currentUserPhotoUrl` from `SessionManager`. `MercadosScreen` passes it to `ProfileAvatar` in the top-bar action slot.

### 🧾 DetallePedidoScreen — overflow menu, button color, payment validation

Three UI fixes applied to `DetallePedidoScreen`:

1. **Three-dot overflow menu** — `PedidoOverflowMenu` composable added (matches `ClientesFilterMenu` design: `DropdownMenu` with `elevated` container, `border2`, `RoundedCornerShape(16.dp)`). Two actions:
   - *Modificar fecha* → Material3 `DatePickerDialog` pre-filled with the pedido's current `createdAt`.
   - *Eliminar pedido* → `AlertDialog` confirmation → calls `pedidoRepository.delete()` and pops back.
   - Supporting: `PedidoDao.updateDate()` query, `PedidoRepository.updateDate()`, `PedidoRepositoryImpl` impl, `showDeleteConfirm` + `showDatePicker` flags in `DetallePedidoUiState`, and corresponding ViewModel handlers.

2. **"Marcar pagado" button color** — changed from `MaterialTheme.extendedColors.greenText` to `MaterialTheme.colorScheme.primary` to match the standard button color pattern.

3. **Partial payment validation** — `PagoParcialSheetContent` now has the same guards as `PagoSheet`: `showError` flag, `LaunchedEffect(amountText)` reset, `isAmountEmpty`/`isAmountTooHigh`/`canConfirm` checks, `isError` on the text field, error text below it, and alpha-dimmed button. Reuses existing `pedidos_pago_parcial_error_vacio` and `pedidos_pago_parcial_error_maximo` strings.

### 📅 Date picker off-by-one when modifying pedido date

`DatePicker` returns UTC midnight for the selected date. `SimpleDateFormat` renders it in the device's local timezone, showing the previous day for UTC-negative zones. Fix: `selectedDateMillis - TimeZone.getDefault().getOffset(selectedDateMillis)` converts UTC midnight to local midnight before saving.

### 💰 Partial payment showing pedido creation date instead of payment date

`onRegistrarPago` only set `paidAt = System.currentTimeMillis()` when the pedido became fully `PAID`; for `PARTIAL` it passed `null`, causing `PagosSection` to fall back to `createdAt`. Fixed: `paidAt` is now always set to `System.currentTimeMillis()` regardless of resulting status.

---

## ✅ Resolved in Phase 2c

### 👆 Biometric Authentication — now fully functional
`BiometricPrompt` is wired to the device's native authentication dialog. Enrollment date is persisted to Room (`biometricEnabledAt` column on `users` table). The login screen hides the biometric button unless the feature is enabled. See `docs/features/usuarios.md → Biometric`.

**Root cause of original failure**: `MainActivity` extended `ComponentActivity`, which does not extend `FragmentActivity`. `BiometricPrompt` requires a `FragmentActivity`. Fix: `MainActivity` now extends `AppCompatActivity` (which IS a `FragmentActivity`).

---

## Other open action items

- **Fonts**: ✅ Geist variable fonts added (`geist_variable.ttf`, `geist_mono_variable.ttf`)
- **Icons**: `ic_shield_check.xml` replaced by `ic_admin_panel.xml` (imported). `ic_users.xml` imported. Both used via `painterResource(R.drawable.*)` at all call sites. `PedidosIcons.kt` removed.

---

## Phase 3 (completion) — implemented

- `SaldoExtraScreen` + `SaldoExtraViewModel` + `SaldoExtraUiState` — form with locked category, description, amount, `DatePickerDialog`
- `SaldoExtraRoute(clienteId)` wired in `AppRoutes` + `AppNavigation`
- `CreateSaldoExtraUseCase` — creates a `Pedido` with `isSaldoExtra=true`, no line items, `notes` = description
- `isSaldoExtra: Boolean` flag added to `PedidoEntity` / `Pedido` / `PedidoMapper` / `PedidoDto`
- DB migration 9→10 (adds `isSaldoExtra` column); Room version bumped to 10
- `PedidoRow` updated: amber Tag icon + "Manual" badge for saldo-extra rows

## Phase 4 — implemented

- `PedidoEntity`, `DetallePedidoEntity` — Room tables with FK cascade from `clientes`
- `PedidoDao`, `DetallePedidoDao` — full CRUD
- `PedidoMapper`, `DetallePedidoMapper` — entity ↔ domain
- `PedidoDto`, `DetallePedidoDto` — Supabase-ready (Phase 9)
- `PedidoRepository` interface + `PedidoRepositoryImpl`
- `CreatePedidoUseCase` — creates pedido + line items atomically
- `CreacionPedidoScreen` — 3-column product grid, active search bar, CartPanel, LineEditSheet, PagoSheet
- `DetalleClienteScreen` — now shows live pedido list; balance/status computed from real pedido data
- `PayChip` — shared composable for PAID/PARTIAL/PENDING status
- DB migration 8→9

## Phase 5 — implemented

- `DetallePedidoScreen` — Scaffold with date subtitle + `PayChip` in top bar; saldo-extra branch (shows notes) vs normal branch (`LineItemsSection`)
- `TotalBlock` — total / paid (green) / saldo restante (amber) rows with divider
- `DetallePedidoBottomBar` — "Registrar pago parcial" + "Marcar como pagado" (hidden when PAID/isSaving)
- `PagoParacialSheet` — `ModalBottomSheet` with decimal amount input; amount clamped to remaining balance
- `DetallePedidoViewModel` — 4-flow `combine`; `onMarcarPagado` / `onRegistrarPago` with PARTIAL/PAID status logic
- `DetallePedidoLineItem` — `LineItemRow` with strikethrough catalog price when overridden, `PriceModifiedHint`
- `DetalleClienteScreen` `onPedidoClick` wired to `DetallePedidoRoute(pedidoId)` (TODO resolved)

## MercadosScreen live stats — implemented

- `PedidoRepository.getAllUnpaid()` — new DAO + repo query for all non-PAID pedidos
- `MercadosViewModel` now combines mercados + all clients + unpaid pedidos to compute `MercadoStat` per mercado
- `MercadoStat(activeClientCount, hasWarning, hasCritical)` drives `MercadoStatRow` in each mercado row
- Status dot: 6dp amber circle (ADVERTENCIA), red circle (CRITICO), hidden when AL_DIA; text color follows status

---

## Phase 2h — implemented

- **Splash screen** — `androidx.core:core-splashscreen 1.0.1`; `ic_splash.xml` (`<layer-list>` + `<bitmap android:src="@drawable/img_logo">`, 30dp insets); `Theme.EcomerceCarlosV.Splash` in `themes.xml`; `installSplashScreen()` called before `enableEdgeToEdge()` in `MainActivity`
- **App icon** — Image Asset Studio with `img_logo.png` as foreground; adaptive icon XMLs in `mipmap-anydpi-v26/`; background color `#FFFFFF` in `values/ic_launcher_background.xml`
- **App name** — renamed from "Pedidos & Cuentas" to "CarlosVCommerce" in `strings.xml`
- **BrandMark** — replaced placeholder gradient box with `Image(painterResource(R.drawable.img_logo))` (80dp default, 64dp compact)
- **Biometric screen redesign** — removed "Usar contraseña" sub-state; enrolled-user screen now always shows password field + "Iniciar sesión" + "Entrar con huella" row; `showPasswordLogin` removed from `LoginFormState`; `onBiometricPasswordLogin()` added to `LoginViewModel`
- **LoginScreen split** — `LoginScreen.kt` (thin router) · `LoginContent.kt` (regular state) · `LoginBiometricoContent.kt` (enrolled-user state) · `LoginComponents.kt` (shared: `BrandMark`, `LoginTextField`, `PrimaryLoginButton`, `DividerOr`)
- **PerfilScreen split** — `BiometricCard` + `BiometricToggle` extracted to `BiometricCard.kt`
- **UmbralesScreen** — `UmbralesScreen.kt` wired to `UmbralesRoute` in `AppNavigation`; `PerfilScreen` Ajustes section navigates to it

---

### 🏠 Home logo + Búsqueda Global — Lista Negra section

**Home screen (MercadosScreen):**
- `PedidosTopBar` gained a `leading: @Composable (() -> Unit)?` parameter (placed in the actions row left of the `Spacer`, only when `onBack == null`).
- `LogoMark()` composable added to `MercadosScreen.kt`: 34dp circle, white bg, 1dp `border2` inset, `img_logo.png` `ContentScale.Crop`.
- `MercadosScreen` passes `leading = { LogoMark() }` to show the Carlos V logo at the top-left of the home screen.

**Búsqueda Global — new "Lista Negra" section:**
- `BlacklistSearchResult(clienteId, name, photoUrl, mercadoName, balance)` added to `BusquedaUiState.kt`.
- `BusquedaUiState.blacklistResults` field added; `hasResults` updated to include it.
- `BusquedaViewModel` splits `clienteRepository.getAll()` into active (`!isBlacklisted`) → `clienteResults` and blacklisted (`isBlacklisted`) → `blacklistResults`. Balance comes from `cliente.blacklistBalance`.
- `BlacklistResultRow` composable: avatar with 18dp red ban badge (redText bg, white icon, 2dp bg-color ring) + name + mercado + "En Lista Negra" red pill chip + balance.
- Section inserted between Clientes and Mercados in the `LazyColumn`, with `Block` icon and `redText` label color.
- `SearchGroupLabel` accepts optional `labelColor: Color?` for the red variant.

---

### 📄 Reporte de Pedidos — PDF export from Detalle Cliente

Full "Reporte de pedidos" screen accessible from the "Generar reporte" menu item in `PedidosMenuButton`.

**Navigation**: `ReporteClienteRoute(clienteId)` added to `AppRoutes.kt` and wired in `AppNavigation.kt`. `PedidosMenuButton` gains `onGenerarReporte: () -> Unit` parameter. `DetalleClienteScreen` passes `onGenerarReporte = { state.cliente?.let { navController.navigate(ReporteClienteRoute(it.id)) } }`.

**Screen** (`ReporteClienteScreen.kt`, `ReporteClienteViewModel.kt`, `ReporteClienteUiState.kt`):
- Top bar: "Reporte de pedidos" + client name subtitle + back arrow.
- Four scrollable sections: **Encabezado** (accentSoft card, company name + date), **Cliente** (2×2 info grid), **Resumen** (3 stat cards — "Sin pagar" blue, "Saldo pendiente" amber, "Total pedidos" green), **Pedidos** (list of `ReportePedidoRow` composables with date, product names summary, amounts, `PayChip`).
- Bottom export bar: full-width "Exportar PDF" `Button`.

**HTML generation** (zero new dependencies):
1. `buildReporteHtml(state, date)` / `buildReporteClienteHtml(state, period, date)` generate self-contained HTML strings with inline CSS (tables, color chips, status badges).
2. On button tap: `saveReportToDownloads(context, html, fileName)` writes to the **Downloads** folder + `showSaveToast` confirms. *(See "Post-Phase 8 improvements" for the full saver implementation.)*

**Menu item redesign**: `ReporteMenuItem` now has `accentSoft` bg, `Description` icon in `accentTint` tile, full-contrast label, and primary-tinted chevron. Previously non-interactive.

---

### 🧹 DepuracionScreen split + count-freshness fix; PerfilScreen Ajustes/Mantenimiento split

**Bug fix**: `CleanupRepositoryImpl.countPedidosOlderThan` read the local Room `pedidos` table directly, without ever triggering a sync — unlike every other pedido-reading path (`PedidoRepositoryImpl.getAll()`/`getByCliente()`, which call `DataSynchronizer.triggerSyncIfStale`). Opening Perfil → Mantenimiento before visiting any other pedido screen in the session left the local table empty, so the count pill showed `0` for any cutoff. Fixed by injecting `PedidoRepository` into `CleanupRepositoryImpl` and calling `.refresh()` before counting. See `docs/features/depuracion.md → Count freshness`.

**DepuracionScreen split** (was a single 880-line file) — now `DepuracionScreen.kt` (`DepuracionScreen` ViewModel wiring + `DepuracionContent` state-driven phase dispatch, previewable) plus one file per phase: `DepuracionConfigContent.kt`, `DepuracionProgressContent.kt`, `DepuracionErrorContent.kt`, `DepuracionDoneContent.kt`, `DepuracionConfirmDialog.kt`, `DepuracionDatePickerDialog.kt`, `PhaseSteps.kt`. Mirrors the `ui/screen/reporte/` split pattern; every phase file has its own light/dark previews.

**PerfilScreen split** — `AjustesSection.kt` (Apariencia card + Umbrales row) and `MantenimientoSection.kt` (Depuración row) extracted with their own previews, since they sat below the fold in `PerfilContent`'s single long scrolling preview. `PerfilDarkPreview` (superuser variant) also given `heightDp = 1500` so the full scroll content renders without clipping.

---

## Build config snapshots

| Tool | Version |
|------|---------|
| Kotlin | 2.2.10 |
| AGP | 9.2.1 |
| KSP | 2.2.10-2.0.2 |
| Compose BOM | 2026.02.01 |
| Hilt | 2.59.2 |
| Navigation Compose | 2.8.4 |
| AppCompat | 1.7.0 |
| Biometric | 1.1.0 |
| Room | 2.8.4 |
| WorkManager | 2.9.0 |
| Hilt Work | 1.2.0 |
| SplashScreen | 1.0.1 |
