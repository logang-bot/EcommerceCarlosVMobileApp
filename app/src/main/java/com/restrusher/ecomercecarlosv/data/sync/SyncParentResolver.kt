package com.restrusher.ecomercecarlosv.data.sync

import android.util.Log
import com.restrusher.ecomercecarlosv.data.local.dao.ClienteDao
import com.restrusher.ecomercecarlosv.data.local.dao.MercadoDao
import com.restrusher.ecomercecarlosv.data.mapper.ClienteMapper
import com.restrusher.ecomercecarlosv.data.mapper.MercadoMapper
import com.restrusher.ecomercecarlosv.data.remote.dto.ClienteDto
import com.restrusher.ecomercecarlosv.data.remote.dto.MercadoDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject

/**
 * Recovers foreign-key parents a syncer is about to need but does not have locally.
 *
 * Room enforces `mercados -> clientes -> pedidos`, while each entity syncs on its own schedule and
 * every full fetch filters out `is_deleted` rows. A child can therefore arrive with no local parent
 * — a soft-deleted mercado whose clientes are still live, or a cliente created after the last
 * cliente delta — and the insert fails the whole pull with a FOREIGN KEY constraint error.
 *
 * Both methods take the ids the caller is about to write and return the subset that is now safe to
 * insert. Ids left out could not be found server-side and their children must be skipped.
 * Parents are fetched *without* the `is_deleted` filter: a tombstone satisfies the constraint and
 * stays invisible to the UI, since every read query filters `isDeleted = 0`.
 */
class SyncParentResolver @Inject constructor(
    private val mercadoDao: MercadoDao,
    private val clienteDao: ClienteDao,
    private val supabase: SupabaseClient,
) {

    suspend fun ensureMercadosExist(mercadoIds: Set<String>): Set<String> {
        if (mercadoIds.isEmpty()) return emptySet()
        val missing = missingFrom(mercadoIds, mercadoDao.existingIds(mercadoIds.toList()))
        if (missing.isEmpty()) return mercadoIds
        Log.d(TAG, "recovering ${missing.size} missing mercado parent(s)")
        val dtos = fetchMercados(missing)
        dtos.forEach { mercadoDao.insert(MercadoMapper.fromDto(it)) }
        return keepResolvable(mercadoIds, missing - dtos.mapTo(mutableSetOf()) { it.id }, "mercado")
    }

    /** Resolves each recovered cliente's own mercado first, so it cannot trip the very foreign key
     *  it is here to prevent. */
    suspend fun ensureClientesExist(clienteIds: Set<String>): Set<String> {
        if (clienteIds.isEmpty()) return emptySet()
        val missing = missingFrom(clienteIds, clienteDao.existingIds(clienteIds.toList()))
        if (missing.isEmpty()) return clienteIds
        Log.d(TAG, "recovering ${missing.size} missing cliente parent(s)")
        val dtos = fetchClientes(missing)
        val mercados = ensureMercadosExist(dtos.mapTo(mutableSetOf()) { it.mercadoId })
        val insertable = dtos.filter { it.mercadoId in mercados }
        insertable.forEach { clienteDao.insert(ClienteMapper.fromDto(it)) }
        return keepResolvable(clienteIds, missing - insertable.mapTo(mutableSetOf()) { it.id }, "cliente")
    }

    private suspend fun fetchMercados(ids: Set<String>): List<MercadoDto> =
        supabase.from("mercados").select { filter { isIn("id", ids.toList()) } }.decodeList()

    private suspend fun fetchClientes(ids: Set<String>): List<ClienteDto> =
        supabase.from("clientes").select { filter { isIn("id", ids.toList()) } }.decodeList()

    private fun missingFrom(wanted: Set<String>, present: List<String>): Set<String> =
        if (wanted.isEmpty()) emptySet() else wanted - present.toSet()

    private fun keepResolvable(wanted: Set<String>, unresolved: Set<String>, label: String): Set<String> {
        if (unresolved.isNotEmpty()) {
            Log.w(TAG, "$label parents absent server-side, skipping their children: $unresolved")
        }
        return wanted - unresolved
    }

    companion object {
        private const val TAG = "SyncParentResolver"
    }
}
