package com.example.dermaapplication.items

import java.io.Serializable

/**
 * Klasa danych reprezentująca pinezkę.
 *
 * @param x współrzędna X pinezki na mapie ciała.
 * @param y współrzędna Y pinezki na mapie ciała.
 */
data class Pin(
    val x: Float,
    val y: Float
) : Serializable
