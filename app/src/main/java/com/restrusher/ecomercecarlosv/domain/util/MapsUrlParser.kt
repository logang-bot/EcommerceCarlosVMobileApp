package com.restrusher.ecomercecarlosv.domain.util

/**
 * Extracts (latitude, longitude) from common Google Maps URL formats:
 * - /maps/place/.../@lat,lng,zoom
 * - ?q=lat,lng or &q=lat,lng
 * - ?ll=lat,lng
 * - ?query=lat,lng (Embed API)
 * Returns null if no coordinate pair is found (e.g. named-place URLs).
 */
fun extractMapsCoordinates(url: String): Pair<Double, Double>? {
    val patterns = listOf(
        Regex("@(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)"),
        Regex("[?&]q=(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)"),
        Regex("[?&]ll=(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)"),
        Regex("[?&]query=(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)"),
    )
    for (pattern in patterns) {
        val match = pattern.find(url) ?: continue
        val lat = match.groupValues[1].toDoubleOrNull() ?: continue
        val lng = match.groupValues[2].toDoubleOrNull() ?: continue
        if (lat in -90.0..90.0 && lng in -180.0..180.0) return Pair(lat, lng)
    }
    return null
}
