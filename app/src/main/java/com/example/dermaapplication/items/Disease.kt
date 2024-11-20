package com.example.dermaapplication.items

/**
 * Klasa reprezentująca schorzenie skórne
 *
 * @property name Nazwa schorzenia skórnego.
 * @property description Opis schorzenia skórnego.
 * @property symptoms Symptomy schorzenia skórnego.
 * @property images Lista zdjęć schorzenia skórnego.
 */
data class Disease(
    val name: String,
    val description: String,
    val symptoms: List<String>,
    val images: List<String>
)