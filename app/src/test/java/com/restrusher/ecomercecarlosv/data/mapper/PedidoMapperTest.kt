package com.restrusher.ecomercecarlosv.data.mapper

import com.restrusher.ecomercecarlosv.data.local.entity.PedidoWithLines
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.fixtures.detallePedidoEntity
import com.restrusher.ecomercecarlosv.fixtures.pedidoDto
import com.restrusher.ecomercecarlosv.fixtures.pedidoEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class PedidoMapperTest {

    @Test
    fun `toDomain then toEntity — round-trips every shared field`() {
        val entity = pedidoEntity()

        val result = PedidoMapper.toEntity(PedidoMapper.toDomain(entity))

        assertEquals(entity.copy(updatedAt = 0L), result)
    }

    @Test
    fun `toDomain — status column — is parsed into the enum`() {
        assertEquals(PedidoStatus.PAID, PedidoMapper.toDomain(pedidoEntity(status = "PAID")).status)
    }

    @Test
    fun `toEntity — enum status — is stored as its name`() {
        val domain = PedidoMapper.toDomain(pedidoEntity(status = "PENDING"))

        assertEquals("PENDING", PedidoMapper.toEntity(domain).status)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain — unknown status column — throws rather than defaulting`() {
        PedidoMapper.toDomain(pedidoEntity(status = "CANCELLED"))
    }

    @Test
    fun `toDomain — no lines given — yields an empty line list`() {
        assertEquals(emptyList<Any>(), PedidoMapper.toDomain(pedidoEntity()).lines)
    }

    @Test
    fun `toDomain — with lines — projects only the name and quantity`() {
        val lines = listOf(
            detallePedidoEntity(id = "d1", productName = "Arroz", quantity = 2),
            detallePedidoEntity(id = "d2", productName = "Azúcar", quantity = 5),
        )

        val domain = PedidoMapper.toDomain(pedidoEntity(), lines)

        assertEquals(listOf("Arroz", "Azúcar"), domain.lines.map { it.productName })
        assertEquals(listOf(2, 5), domain.lines.map { it.quantity })
    }

    @Test
    fun `toDomain — from PedidoWithLines — matches the two-argument overload`() {
        val entity = pedidoEntity()
        val lines = listOf(detallePedidoEntity())

        assertEquals(PedidoMapper.toDomain(entity, lines), PedidoMapper.toDomain(PedidoWithLines(entity, lines)))
    }

    @Test
    fun `fromDto — itemCount is not carried by the server payload — resets to zero`() {
        assertEquals(0, PedidoMapper.fromDto(pedidoDto()).itemCount)
    }

    @Test
    fun `fromDto — keeps the sync bookkeeping columns`() {
        val entity = PedidoMapper.fromDto(pedidoDto())

        assertEquals(1_700_000_100_000L, entity.updatedAt)
    }

    @Test
    fun `toDto then fromDto — round-trips every remote field but itemCount`() {
        val entity = pedidoEntity(itemCount = 3)

        val result = PedidoMapper.fromDto(PedidoMapper.toDto(entity))

        assertEquals(entity.copy(itemCount = 0, updatedAt = 0L), result)
    }
}
