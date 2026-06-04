# App Blueprint — Inventory & Sales Manager

A reference document for bootstrapping a new Android app with the same architecture, wiring, and feature set as this project. Covers structure, libraries, navigation, and feature list. Styling (colors, fonts, themes) is omitted.

---

## 1. Tech Stack & Libraries

### Build

| Tool | Version |
|------|---------|
| Kotlin | 2.3.10 |
| AGP | 9.1.0 |
| KSP | 2.3.10-1.0.32 |
| Min SDK | 24 |
| Target SDK | 36 |
| Compose BOM | 2024.09.00 |

### Core Dependencies (`libs.versions.toml`)

```toml
[versions]
kotlin = "2.3.10"
ksp = "2.3.10-1.0.32"
agp = "9.1.0"
compose-bom = "2024.09.00"
navigation-compose = "2.8.1"
room = "2.8.4"
hilt = "2.59.2"
hilt-navigation-compose = "1.2.0"
hilt-work = "1.2.0"
kotlinx-serialization = "1.6.3"
supabase = "3.1.4"
ktor = "3.1.2"
coil = "3.0.4"
work-manager = "2.9.0"
datastore = "1.1.1"
compose-animation = "1.7.2"

[libraries]
# Compose
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-animation = { group = "androidx.compose.animation", name = "animation", version.ref = "compose-animation" }
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation-compose" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hilt-navigation-compose" }
hilt-work = { group = "androidx.hilt", name = "hilt-work", version.ref = "hilt-work" }
hilt-work-compiler = { group = "androidx.hilt", name = "hilt-compiler", version.ref = "hilt-work" }

# Serialization
kotlinx-serialization = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinx-serialization" }

# Supabase
supabase-postgrest = { group = "io.github.jan-tennert.supabase", name = "postgrest-kt", version.ref = "supabase" }
supabase-storage = { group = "io.github.jan-tennert.supabase", name = "storage-kt", version.ref = "supabase" }
ktor-okhttp = { group = "io.ktor", name = "ktor-client-okhttp", version.ref = "ktor" }

# Image loading
coil-compose = { group = "io.coil-kt.coil3", name = "coil-compose", version.ref = "coil" }
coil-network-okhttp = { group = "io.coil-kt.coil3", name = "coil-network-okhttp", version.ref = "coil" }

# Background work
work-runtime = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "work-manager" }

# DataStore
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

### `app/build.gradle.kts` plugins block

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}
```

---

## 2. AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<application
    android:name=".YourApp"
    ...>

    <!-- Disable auto-init so WorkManager uses Hilt's configuration -->
    <provider
        android:name="androidx.startup.InitializationProvider"
        android:authorities="${applicationId}.androidx-startup"
        tools:node="remove" />

    <!-- FileProvider for camera / file access -->
    <provider
        android:name="androidx.core.content.FileProvider"
        android:authorities="${applicationId}.provider"
        android:exported="false"
        android:grantUriPermissions="true">
        <meta-data
            android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/file_paths" />
    </provider>

</application>
```

---

## 3. Project Structure

```
app/src/main/java/com/example/yourapp/
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── dao/
│   │   │   ├── StoreDao.kt
│   │   │   ├── ProductDao.kt
│   │   │   ├── SaleDao.kt
│   │   │   ├── InventoryLogDao.kt
│   │   │   └── SyncOperationDao.kt
│   │   └── entity/
│   │       ├── StoreEntity.kt
│   │       ├── ProductEntity.kt
│   │       ├── SaleEntity.kt
│   │       ├── InventoryLogEntity.kt
│   │       └── SyncOperationEntity.kt
│   ├── mapper/
│   │   ├── StoreMapper.kt
│   │   ├── ProductMapper.kt
│   │   ├── SaleMapper.kt
│   │   └── InventoryLogMapper.kt
│   ├── remote/
│   │   ├── dto/
│   │   │   ├── StoreDto.kt
│   │   │   ├── ProductDto.kt
│   │   │   └── SaleDto.kt
│   │   └── source/
│   │       ├── StoreRemoteDataSource.kt
│   │       ├── ProductRemoteDataSource.kt
│   │       ├── SaleRemoteDataSource.kt
│   │       └── impl/
│   │           ├── StoreRemoteDataSourceImpl.kt
│   │           ├── ProductRemoteDataSourceImpl.kt
│   │           └── SaleRemoteDataSourceImpl.kt
│   ├── repository/
│   │   └── impl/
│   │       ├── StoreRepositoryImpl.kt
│   │       ├── ProductRepositoryImpl.kt
│   │       ├── SaleRepositoryImpl.kt
│   │       ├── InventoryLogRepositoryImpl.kt
│   │       └── UserSettingsRepositoryImpl.kt
│   └── sync/
│       ├── DeviceIdProvider.kt
│       ├── EntitySyncer.kt          ← abstract base
│       ├── StoreSyncer.kt
│       ├── ProductSyncer.kt
│       ├── SaleSyncer.kt
│       ├── SyncerRegistry.kt
│       ├── SyncManager.kt
│       ├── SyncNotifier.kt
│       ├── SyncScheduler.kt
│       ├── SyncWorker.kt
│       └── RemoteErrorHandler.kt
├── domain/
│   ├── model/
│   │   ├── Store.kt
│   │   ├── Product.kt
│   │   ├── Sale.kt
│   │   ├── InventoryLog.kt
│   │   ├── Currency.kt             ← enum
│   │   ├── SaleType.kt             ← enum
│   │   └── ProfitOutcome.kt        ← enum
│   ├── repository/                 ← interfaces only
│   │   ├── StoreRepository.kt
│   │   ├── ProductRepository.kt
│   │   ├── SaleRepository.kt
│   │   ├── InventoryLogRepository.kt
│   │   └── UserSettingsRepository.kt
│   └── usecase/
│       ├── RecordSaleUseCase.kt
│       ├── RestockProductUseCase.kt
│       ├── GetSalesHistoryUseCase.kt
│       └── GetSalesSummaryUseCase.kt
├── presentation/
│   ├── navigation/
│   │   ├── AppNavigation.kt        ← NavHost composable
│   │   ├── SyncViewModel.kt        ← global sync state
│   │   └── NotificationPermissionHandler.kt
│   └── screens/
│       └── AppRoutes.kt            ← @Serializable route objects
├── ui/
│   ├── common/                     ← shared composables
│   │   ├── AppBottomNavBar.kt
│   │   ├── DateRangeFilter.kt
│   │   ├── LoadingButton.kt
│   │   ├── NotificationPermissionDialog.kt
│   │   ├── PermissionDialog.kt
│   │   ├── ProductDropdown.kt
│   │   └── StoreCard.kt
│   ├── screen/
│   │   ├── home/
│   │   │   ├── HomeScreen.kt
│   │   │   ├── HomeViewModel.kt
│   │   │   └── HomeUiState.kt
│   │   ├── store/
│   │   │   ├── StoreListScreen.kt / StoreListViewModel.kt / StoreListUiState.kt
│   │   │   ├── StoreDetailScreen.kt / StoreDetailViewModel.kt / StoreDetailUiState.kt
│   │   │   └── CreateStoreScreen.kt / CreateStoreViewModel.kt / CreateStoreFormState.kt
│   │   ├── product/
│   │   │   ├── ProductListScreen.kt / ProductListViewModel.kt / ProductListUiState.kt
│   │   │   ├── CreateProductScreen.kt / CreateProductViewModel.kt / CreateProductFormState.kt
│   │   │   └── InventoryHistoryScreen.kt / InventoryHistoryViewModel.kt / InventoryHistoryUiState.kt
│   │   └── sale/
│   │       ├── RecordSaleScreen.kt / RecordSaleViewModel.kt / RecordSaleFormState.kt
│   │       ├── SalesListScreen.kt / SalesListViewModel.kt / SalesListUiState.kt
│   │       ├── SaleDetailScreen.kt / SaleDetailViewModel.kt / SaleDetailUiState.kt
│   │       ├── SalesReportScreen.kt / SalesReportViewModel.kt / SalesReportUiState.kt
│   │       └── CreditSalesListScreen.kt / CreditSalesListViewModel.kt / CreditSalesListUiState.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── di/
│   ├── DatabaseModule.kt
│   ├── SupabaseModule.kt
│   └── WorkerModule.kt
├── YourApp.kt                      ← Application class
└── MainActivity.kt
```

---

## 4. Architecture

The app follows **Clean Architecture** with three layers. Dependencies only point inward.

```
UI (Compose) → ViewModel → UseCase → Repository (interface) → Repository (impl) → DAO / Remote
```

### Layer rules

| Layer | Allowed dependencies |
|-------|---------------------|
| `domain/` | Nothing outside domain |
| `data/` | `domain/` interfaces only |
| `ui/` + `presentation/` | `domain/` models, `data/` indirectly via Hilt |

### Three-model pattern per entity

```
Room Entity  ←→  Mapper  ←→  Domain Model  ←→  Mapper  ←→  DTO (Supabase)
```

Each entity has its own mapper object. ViewModels only see domain models.

### Data flow (read)

```
Room (Flow<Entity>) → Mapper → Flow<DomainModel> → ViewModel → UiState → Composable
```

### Data flow (write)

```
Composable → ViewModel → UseCase (optional) → RepositoryImpl
    → DAO (local write, immediate)
    → SyncScheduler (enqueue SyncOperationEntity for later push)
```

---

## 5. Dependency Injection (Hilt)

### Application class

```kotlin
@HiltAndroidApp
class YourApp : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var syncManager: SyncManager
    @Inject lateinit var workManager: WorkManager
    @Inject lateinit var syncNotifier: SyncNotifier

    override val workManagerConfiguration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        syncNotifier.createNotificationChannel()
        SyncWorker.schedule(workManager)
        syncManager.runInitialSyncIfNeeded()
    }
}
```

### Main Activity

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YourAppTheme {
                AppNavigation()
            }
        }
    }
}
```

### Hilt modules

**DatabaseModule** — provides Room DB and all DAOs; binds repository interfaces to implementations.

**SupabaseModule** — provides a `SupabaseClient` singleton (Postgrest + Storage plugins); binds remote data source interfaces to implementations. Credentials stored in `BuildConfig` via `local.properties`.

**WorkerModule** — provides a `WorkManager` singleton.

---

## 6. Room Database

### Setup

```kotlin
@Database(
    entities = [
        StoreEntity::class,
        ProductEntity::class,
        SaleEntity::class,
        InventoryLogEntity::class,
        SyncOperationEntity::class
    ],
    version = 1,           // start at 1 for a new project
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDao
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun inventoryLogDao(): InventoryLogDao
    abstract fun syncOperationDao(): SyncOperationDao
}
```

### Entity schemas

**StoreEntity**
```
id: String (PK, UUID)
name: String
description: String
currency: String           ← enum name stored as String
logoUrl: String?
photoUrl: String?
createdAt: Long            ← epoch millis
lastAccessedAt: Long?
deviceId: String
```

**ProductEntity**
```
id: String (PK, UUID)
storeId: String (FK → stores, CASCADE delete)
name: String
description: String
price: Double
costPrice: Double
stock: Int
imageUrl: String?
createdAt: Long
deviceId: String
[Index: storeId]
```

**SaleEntity**
```
id: String (PK, UUID)
storeId: String (FK → stores, CASCADE delete)
productId: String?         ← nullable: free-text sales have no product
productName: String
quantity: Int
unitPrice: Double
unitCost: Double
totalAmount: Double
saleType: String           ← SaleType enum name
profitOutcome: String      ← ProfitOutcome enum name
notes: String?
onCredit: Boolean
creditPersonName: String?
soldAt: Long               ← user-selected date, epoch millis
createdAt: Long
deviceId: String
[Index: storeId]
```

**InventoryLogEntity**
```
id: Long (PK, autoGenerate)
storeId: String
productId: String
productName: String
previousStock: Int
newStock: Int
loggedAt: Long
[Index: storeId]
```

**SyncOperationEntity**
```
id: Long (PK, autoGenerate)
entityType: String         ← "STORE" | "PRODUCT" | "SALE"
entityId: String           ← UUID of the local entity
operation: String          ← "CREATE" | "UPDATE" | "DELETE"
createdAt: Long
```

---

## 7. Domain Models & Enums

```kotlin
data class Store(
    val id: String,
    val name: String,
    val description: String,
    val currency: Currency,
    val logoUrl: String?,
    val photoUrl: String?,
    val createdAt: Long,
    val lastAccessedAt: Long?,
    val deviceId: String
)

data class Product(
    val id: String,
    val storeId: String,
    val name: String,
    val description: String,
    val price: Double,
    val costPrice: Double,
    val stock: Int,
    val imageUrl: String?,
    val createdAt: Long,
    val deviceId: String
)

data class Sale(
    val id: String,
    val storeId: String,
    val productId: String?,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val unitCost: Double,
    val totalAmount: Double,
    val saleType: SaleType,
    val profitOutcome: ProfitOutcome,
    val notes: String?,
    val onCredit: Boolean,
    val creditPersonName: String?,
    val soldAt: Long,
    val createdAt: Long,
    val deviceId: String
)

data class InventoryLog(
    val id: Long,
    val storeId: String,
    val productId: String,
    val productName: String,
    val previousStock: Int,
    val newStock: Int,
    val loggedAt: Long
)

enum class Currency { USD, EUR, GBP, /* ... */ }
enum class SaleType  { STANDARD, /* ... */ }
enum class ProfitOutcome { NORMAL_PROFIT, BREAK_EVEN, LOSS, /* ... */ }
```

---

## 8. Repository Interfaces

```kotlin
interface StoreRepository {
    suspend fun create(store: Store): String
    suspend fun update(store: Store)
    fun getAll(): Flow<List<Store>>
    suspend fun getById(id: String): Store?
    suspend fun delete(id: String)
    suspend fun markAccessed(id: String)
}

interface ProductRepository {
    suspend fun create(product: Product): String
    suspend fun update(product: Product)
    fun getByStore(storeId: String): Flow<List<Product>>
    suspend fun getById(id: String): Product?
    suspend fun delete(id: String)
}

interface SaleRepository {
    suspend fun create(sale: Sale): String
    suspend fun update(sale: Sale)
    fun getByStore(storeId: String): Flow<List<Sale>>
    fun getOnCreditByStore(storeId: String): Flow<List<Sale>>
    suspend fun getById(id: String): Sale?
    suspend fun delete(id: String)
}

interface InventoryLogRepository {
    suspend fun log(entry: InventoryLog)
    fun getByStore(storeId: String): Flow<List<InventoryLog>>
    suspend fun getByProduct(productId: String): List<InventoryLog>
    suspend fun getByDateRange(storeId: String, from: Long, to: Long): List<InventoryLog>
}

interface UserSettingsRepository {
    suspend fun setLastAccessedStore(storeId: String)
    fun getLastAccessedStore(): Flow<String?>
}
```

---

## 9. Use Cases

| Use Case | Inputs | Side effects |
|----------|--------|-------------|
| `RecordSaleUseCase` | `Sale` | Inserts sale, decrements product stock, logs inventory change |
| `RestockProductUseCase` | `productId`, `quantity` | Updates product stock, logs inventory change |
| `GetSalesHistoryUseCase` | `storeId`, `fromDate?`, `toDate?`, `productId?` | Returns filtered `Flow<List<Sale>>` |
| `GetSalesSummaryUseCase` | `storeId`, `from`, `to` | Returns aggregated summary: revenue, profit, credit totals, breakdowns per day/product/outcome |

---

## 10. Navigation

### Route definitions (`AppRoutes.kt`)

```kotlin
@Serializable object HomeRoute
@Serializable object StoreListRoute

@Serializable data class StoreDetailRoute(val storeId: String)
@Serializable data class CreateStoreRoute(val storeId: String? = null)   // null = create

@Serializable data class ProductListRoute(val storeId: String)
@Serializable data class CreateProductRoute(val storeId: String, val productId: String? = null)

@Serializable data class RecordSaleRoute(val storeId: String)
@Serializable data class SalesListRoute(val storeId: String)
@Serializable data class SaleDetailRoute(val saleId: String)
@Serializable data class SalesReportRoute(
    val storeId: String,
    val fromDate: Long,
    val toDate: Long,
    val productId: String? = null
)
@Serializable data class CreditSalesListRoute(val storeId: String)
@Serializable data class InventoryHistoryRoute(val storeId: String)
```

### Navigation flow

```
HomeRoute ──────────────────────────────────────────────── bottom nav
     │
     └─► StoreListRoute ──────────────────────────────────── bottom nav
               │
               └─► StoreDetailRoute(storeId)
                         ├─► CreateStoreRoute(storeId)        ← edit existing
                         ├─► ProductListRoute(storeId)
                         │       ├─► CreateProductRoute(storeId)
                         │       └─► CreateProductRoute(storeId, productId)  ← edit
                         ├─► RecordSaleRoute(storeId)
                         ├─► SalesListRoute(storeId)
                         │       ├─► SaleDetailRoute(saleId)
                         │       └─► SalesReportRoute(...)
                         ├─► CreditSalesListRoute(storeId)
                         └─► InventoryHistoryRoute(storeId)
```

### `AppNavigation.kt` skeleton

```kotlin
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val syncViewModel: SyncViewModel = hiltViewModel()
    val syncUiState by syncViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        snackbarHost = { /* global snackbar */ },
        bottomBar = {
            // Show only on HomeRoute and StoreListRoute
            AppBottomNavBar(navController)
        }
    ) { padding ->
        // Optional: blur overlay when syncing
        NavHost(
            navController = navController,
            startDestination = HomeRoute
        ) {
            composable<HomeRoute> { HomeScreen(navController) }
            composable<StoreListRoute> { StoreListScreen(navController) }
            composable<StoreDetailRoute> { backStack ->
                val route = backStack.toRoute<StoreDetailRoute>()
                StoreDetailScreen(storeId = route.storeId, navController)
            }
            composable<CreateStoreRoute> { backStack ->
                val route = backStack.toRoute<CreateStoreRoute>()
                CreateStoreScreen(storeId = route.storeId, navController)
            }
            // ... remaining routes
        }
    }
}
```

### Back stack conventions

- `popUpTo` + `launchSingleTop = true` for bottom nav tabs (avoids duplicate stack entries)
- After create/edit success → `navController.popBackStack()`
- After delete → `navController.popBackStack()` to the parent list

---

## 11. Features

### Home

- Summary card: total revenue, profit, and credit totals across all stores
- Quick-access to the last accessed store
- Entry point to store list

### Store management

- List of stores as cards (name, currency, last accessed date)
- Create / edit store: name, description, currency picker, logo image, store photo
- Delete store (with confirmation dialog — cascades to all products/sales)
- Store detail: overview card (revenue, profit, credit), analytics card (date-filtered), shortcut buttons to all sub-features

### Product management

- List of products per store: name, stock level, price, cost price, image thumbnail
- Create / edit product: name, description, sale price, cost price, initial stock, optional image (camera or gallery)
- Delete product (with confirmation dialog)
- Inline stock update dialog (restock action, logs the change)

### Inventory history

- Chronological log of all stock changes per store
- Each entry: product name, previous stock, new stock, date/time
- Filterable by product and date range

### Record sale

Two product selection modes:

1. **Grid picker** — select from existing products, tap to pick, shows stock level
2. **Custom entry** — free-text product name (for items not in catalogue)

Form fields:
- Product (grid or custom name)
- Quantity
- Sale price per unit (pre-filled from product, editable)
- Cost price per unit (pre-filled, editable)
- Sale date (date picker, defaults to today)
- Sale type
- Notes (optional)
- On credit toggle → credit person name field

Confirm dialog shows calculated totals before saving.

Result dialog shows profit/loss outcome after saving.

### Sales list

- Chronological list of all sales per store
- Each card: product name, quantity, total amount, profit outcome badge, credit badge
- Filter by date range and product
- Search by product name

### Sale detail

- Full detail view: product, quantity, prices, totals, date, notes
- Credit status with person name
- Delete sale (with confirmation dialog)

### Sales report

- Date-range filter
- Optional product filter
- Summary totals: revenue, profit, losses, credit
- Charts: revenue by day, sales breakdown by product
- Profit outcome breakdown

### Credit sales list

- Filtered list of all sales marked as on-credit per store
- Shows credit person name and amount owed
- Tap to navigate to sale detail (where credit status can be updated)

---

## 12. Offline-first Sync

### Write path

Every write goes local-first:

1. RepositoryImpl writes to Room immediately
2. RepositoryImpl calls `SyncScheduler.enqueue(entityType, entityId, operation)`
3. `SyncScheduler` inserts a `SyncOperationEntity` into Room

### Background worker

```kotlin
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncManager: SyncManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        syncManager.runSync()
        return Result.success()
    }

    companion object {
        fun schedule(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build())
                .build()
            workManager.enqueueUniquePeriodicWork(
                "sync_worker",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
```

### Sync manager responsibilities

- Fetch all remote stores / products / sales
- Merge with local DB (upsert by UUID)
- Process pending `SyncOperationEntity` queue (push local changes to remote)
- Repair image URLs after upload
- Emit sync status as `StateFlow<Boolean>` consumed by `SyncViewModel`

### Device identification

Each device generates a UUID stored in DataStore on first launch (`DeviceIdProvider`). All created entities carry this `deviceId` field for conflict resolution.

---

## 13. Permission Handling

### Runtime permissions requested at relevant moments

| Permission | When requested |
|-----------|---------------|
| `POST_NOTIFICATIONS` | On first app launch (via dialog) |
| `CAMERA` | When user taps "Take photo" in product/store form |

### Pattern

```kotlin
// PermissionDialog composable:
// - Shows rationale text
// - "Allow" → ActivityCompat.requestPermissions
// - "Deny" / dismiss → no-op

// ViewModel exposes:
var showPermissionDialog: Boolean
var permissionGranted: Boolean

// After ActivityResult callback updates ViewModel,
// the composable reacts via collectAsStateWithLifecycle()
```

---

## 14. Global Sync UI

`SyncViewModel` is scoped to the `AppNavigation` composable and exposes:

```kotlin
data class SyncUiState(
    val isSyncing: Boolean = false,
    val syncError: String? = null
)
```

`AppNavigation` displays:
- A loading overlay (with optional blur) when `isSyncing = true`
- A Snackbar with the error message when `syncError != null`

Both `HomeRoute` and `StoreListRoute` show the bottom navigation bar; all other routes hide it.

---

## 15. UserSettings (DataStore)

```kotlin
interface UserSettingsRepository {
    suspend fun setLastAccessedStore(storeId: String)
    fun getLastAccessedStore(): Flow<String?>
}
```

Backed by `DataStore<Preferences>`. Used by `HomeViewModel` to pre-load the last store the user had open.

---

## 16. Supabase Backend Schema

### Tables

```sql
stores (
  id uuid PRIMARY KEY,
  name text,
  description text,
  currency text,
  logo_url text,
  photo_url text,
  created_at timestamptz,
  device_id text
)

products (
  id uuid PRIMARY KEY,
  store_id uuid REFERENCES stores(id) ON DELETE CASCADE,
  name text,
  description text,
  price numeric,
  cost_price numeric,
  stock integer,
  image_url text,
  created_at timestamptz,
  device_id text
)

sales (
  id uuid PRIMARY KEY,
  store_id uuid REFERENCES stores(id) ON DELETE CASCADE,
  product_id uuid,          -- nullable
  product_name text,
  quantity integer,
  unit_price numeric,
  unit_cost numeric,
  total_amount numeric,
  sale_type text,
  profit_outcome text,
  notes text,
  on_credit boolean,
  credit_person_name text,
  sold_at timestamptz,
  created_at timestamptz,
  device_id text
)
```

### Storage bucket

Single bucket (e.g., `store-images`) with folders per entity:
```
store-images/
├── stores/{storeId}/logo.jpg
├── stores/{storeId}/photo.jpg
└── products/{productId}/image.jpg
```

---

## 17. ViewModels — Naming Convention

Each screen has three files:

| File | Responsibility |
|------|---------------|
| `XxxScreen.kt` | Composable UI, collects `UiState` via `collectAsStateWithLifecycle` |
| `XxxViewModel.kt` | `@HiltViewModel`, exposes `StateFlow<XxxUiState>`, handles events |
| `XxxUiState.kt` / `XxxFormState.kt` | Immutable data class for UI state |

`SyncViewModel` is an exception: it is not tied to a screen and is injected at the `AppNavigation` level.

---

## 18. Wiring Checklist for a New Project

- [ ] Create version catalog (`libs.versions.toml`) with all versions above
- [ ] Apply all Gradle plugins in `app/build.gradle.kts`
- [ ] Add Supabase URL + anon key to `local.properties`, expose via `BuildConfig`
- [ ] Create `Application` class with `@HiltAndroidApp` and WorkManager `Configuration.Provider`
- [ ] Create `DatabaseModule`, `SupabaseModule`, `WorkerModule`
- [ ] Define all 5 Room entities and DAOs
- [ ] Define all 5 domain repository interfaces
- [ ] Implement all 5 repositories (local DAO + `SyncScheduler` call on every write)
- [ ] Implement `SyncWorker`, `SyncManager`, `SyncScheduler`, `DeviceIdProvider`
- [ ] Define all `@Serializable` route objects in `AppRoutes.kt`
- [ ] Wire `NavHost` in `AppNavigation.kt` with bottom bar visibility logic
- [ ] Create `SyncViewModel` scoped to `AppNavigation`
- [ ] Create one `Screen + ViewModel + UiState` triplet per route
- [ ] Implement the 4 use cases
- [ ] Add manifest permissions and FileProvider
- [ ] Disable WorkManager auto-init in manifest
