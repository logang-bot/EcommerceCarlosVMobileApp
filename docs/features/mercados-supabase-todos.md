# Mercados — Supabase Integration TODOs

All Mercados data currently lives exclusively in Room (local SQLite). This document tracks every wiring point needed when connecting to Supabase (Phase 9).

---

## 1. Supabase Table Schema

```sql
CREATE TABLE mercados (
  id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name        text NOT NULL,
  address     text NOT NULL DEFAULT '',
  photo_url   text,
  created_at  bigint NOT NULL  -- epoch millis, set by the client on insert
);
```

Row-Level Security: All authenticated users can SELECT/INSERT/UPDATE/DELETE (shared resource — no per-user ownership).

---

## 2. DTO

`data/remote/dto/MercadoDto.kt` — already created. Maps `snake_case` Supabase columns to Kotlin fields via `@SerialName`.

```kotlin
@Serializable
data class MercadoDto(
    val id: String,
    val name: String,
    val address: String,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("created_at") val createdAt: Long,
)
```

---

## 3. Remote Data Source

Create `data/remote/source/MercadoRemoteDataSource.kt` (interface) and `impl/MercadoRemoteDataSourceImpl.kt`:

```kotlin
interface MercadoRemoteDataSource {
    suspend fun getAll(): List<MercadoDto>
    suspend fun upsert(dto: MercadoDto)
    suspend fun delete(id: String)
}

class MercadoRemoteDataSourceImpl @Inject constructor(
    private val supabase: SupabaseClient,
) : MercadoRemoteDataSource {

    override suspend fun getAll(): List<MercadoDto> =
        supabase.from("mercados").select().decodeList()

    override suspend fun upsert(dto: MercadoDto) =
        supabase.from("mercados").upsert(dto)

    override suspend fun delete(id: String) =
        supabase.from("mercados").delete { filter { eq("id", id) } }
}
```

---

## 4. Mapper additions

Add `MercadoMapper.toDto` and `MercadoMapper.fromDto` conversions:

```kotlin
fun toDto(domain: Mercado) = MercadoDto(
    id = domain.id,
    name = domain.name,
    address = domain.address,
    photoUrl = domain.photoUrl,
    createdAt = domain.createdAt,
)

fun fromDto(dto: MercadoDto) = Mercado(
    id = dto.id,
    name = dto.name,
    address = dto.address,
    photoUrl = dto.photoUrl,
    createdAt = dto.createdAt,
)
```

---

## 5. SyncOperation wiring

`MercadoRepositoryImpl.save()` and `.delete()` must enqueue a `SyncOperationEntity` (entityType = `"MERCADO"`) after every local write:

```kotlin
override suspend fun save(mercado: Mercado) {
    dao.insert(MercadoMapper.toEntity(mercado))
    syncScheduler.enqueue("MERCADO", mercado.id, if (exists) "UPDATE" else "CREATE")
}

override suspend fun delete(id: String) {
    dao.deleteById(id)
    syncScheduler.enqueue("MERCADO", id, "DELETE")
}
```

---

## 6. SyncManager / Syncer

Create `data/sync/MercadoSyncer.kt` extending the `EntitySyncer<MercadoEntity, MercadoDto>` base class. Register it in `SyncerRegistry`.

Merge logic (upsert by UUID):
- Fetch all remote mercados → `remoteDataSource.getAll()`
- For each remote DTO: upsert into Room if newer (`created_at` is the only timestamp; use remote as source of truth on first sync)
- Process queued `SyncOperationEntity` items for `MERCADO`: push CREATE/UPDATE/DELETE to remote

---

## 7. Photo upload (Phase 2 extension)

When `Mercado.photoUrl` is set from a local file URI:
1. Upload to Supabase Storage: `mercado-images/mercados/{mercadoId}/photo.jpg`
2. Update `photoUrl` in Room with the public URL after successful upload
3. Enqueue UPDATE sync operation

`MercadoRepositoryImpl` will call `StorageRemoteDataSource.uploadMercadoPhoto(mercadoId, localUri)` before saving.

---

## 8. DI changes

- Bind `MercadoRemoteDataSource` → `MercadoRemoteDataSourceImpl` in `SupabaseModule`
- Inject `MercadoRemoteDataSource` into `MercadoRepositoryImpl`
- Add `MercadoSyncer` to `SyncerRegistry`

---

## 9. Auth / RLS

Supabase calls require an authenticated session. `SupabaseModule` must initialise the `Auth` plugin and attach the JWT to every request automatically. No per-call token injection needed if the Supabase client is configured with `Auth` plugin.

---

## Checklist

- [ ] Create Supabase `mercados` table with RLS policy
- [ ] Create `MercadoRemoteDataSource` interface + impl
- [ ] Add `toDto` / `fromDto` to `MercadoMapper`
- [ ] Wire `SyncScheduler.enqueue()` calls inside `MercadoRepositoryImpl`
- [ ] Create `MercadoSyncer` and register in `SyncerRegistry`
- [ ] Handle photo upload in `save()` (Phase 2 extension)
- [ ] Update `SupabaseModule` to bind new data source
- [ ] Test sync round-trip (create on device A → appears on device B)
