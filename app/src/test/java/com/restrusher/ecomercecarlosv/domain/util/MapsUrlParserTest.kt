package com.restrusher.ecomercecarlosv.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MapsUrlParserTest {

    @Test
    fun `at-sign form with zoom — returns lat and lng ignoring the zoom segment`() {
        val result = extractMapsCoordinates("https://www.google.com/maps/place/Tienda/@19.4326,-99.1332,17z")

        assertEquals(19.4326, result!!.first, 0.0)
        assertEquals(-99.1332, result.second, 0.0)
    }

    @Test
    fun `q param after question mark — returns the pair`() {
        val result = extractMapsCoordinates("https://maps.google.com/?q=19.4326,-99.1332")

        assertEquals(19.4326, result!!.first, 0.0)
        assertEquals(-99.1332, result.second, 0.0)
    }

    @Test
    fun `q param after ampersand — returns the pair`() {
        val result = extractMapsCoordinates("https://maps.google.com/?hl=es&q=19.4326,-99.1332")

        assertEquals(19.4326, result!!.first, 0.0)
        assertEquals(-99.1332, result.second, 0.0)
    }

    @Test
    fun `ll param — returns the pair`() {
        val result = extractMapsCoordinates("https://maps.google.com/?ll=19.4326,-99.1332&z=17")

        assertEquals(19.4326, result!!.first, 0.0)
        assertEquals(-99.1332, result.second, 0.0)
    }

    @Test
    fun `query param from the embed api — returns the pair`() {
        val result = extractMapsCoordinates("https://www.google.com/maps/embed/v1/place?key=abc&query=19.4326,-99.1332")

        assertEquals(19.4326, result!!.first, 0.0)
        assertEquals(-99.1332, result.second, 0.0)
    }

    @Test
    fun `url with both at-sign and q forms — at-sign wins by pattern order`() {
        val result = extractMapsCoordinates("https://maps.google.com/maps/@1.5,2.5,17z?q=40.5,-3.5")

        assertEquals(1.5, result!!.first, 0.0)
        assertEquals(2.5, result.second, 0.0)
    }

    @Test
    fun `named place without coordinates — returns null`() {
        assertNull(extractMapsCoordinates("https://www.google.com/maps/place/Mercado+Central"))
    }

    @Test
    fun `integer coordinates without a decimal point — returns null`() {
        // The patterns require `\.\d+` on both values, so whole-number pairs are not recognised.
        assertNull(extractMapsCoordinates("https://maps.google.com/?q=19,-99"))
    }

    @Test
    fun `latitude above ninety — returns null`() {
        assertNull(extractMapsCoordinates("https://maps.google.com/maps/@91.0,0.0,17z"))
    }

    @Test
    fun `longitude above one hundred eighty — returns null`() {
        assertNull(extractMapsCoordinates("https://maps.google.com/maps/@0.0,181.0,17z"))
    }

    @Test
    fun `coordinates at the positive boundary — are accepted`() {
        val result = extractMapsCoordinates("https://maps.google.com/maps/@90.0,180.0,17z")

        assertEquals(90.0, result!!.first, 0.0)
        assertEquals(180.0, result.second, 0.0)
    }

    @Test
    fun `coordinates at the negative boundary — are accepted`() {
        val result = extractMapsCoordinates("https://maps.google.com/maps/@-90.0,-180.0,17z")

        assertEquals(-90.0, result!!.first, 0.0)
        assertEquals(-180.0, result.second, 0.0)
    }

    @Test
    fun `both values negative — signs are preserved`() {
        val result = extractMapsCoordinates("https://maps.google.com/?q=-16.5,-68.15")

        assertEquals(-16.5, result!!.first, 0.0)
        assertEquals(-68.15, result.second, 0.0)
    }

    @Test
    fun `empty url — returns null`() {
        assertNull(extractMapsCoordinates(""))
    }
}
