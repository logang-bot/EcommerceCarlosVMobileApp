package com.restrusher.ecomercecarlosv.fakes

import com.restrusher.ecomercecarlosv.domain.model.Umbrales
import com.restrusher.ecomercecarlosv.domain.repository.UmbralesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeUmbralesRepository(umbrales: Umbrales = Umbrales()) : UmbralesRepository {

    private val stored = MutableStateFlow(umbrales)

    var refreshResult = true

    fun given(umbrales: Umbrales) {
        stored.value = umbrales
    }

    override val isSyncing: Flow<Boolean> = flowOf(false)

    override fun getUmbrales(): Flow<Umbrales> = stored

    override suspend fun save(umbrales: Umbrales) {
        stored.value = umbrales
    }

    override suspend fun refresh(): Boolean = refreshResult
}
