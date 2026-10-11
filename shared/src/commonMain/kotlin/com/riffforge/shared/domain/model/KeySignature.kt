package com.riffforge.shared.domain.model

data class KeySignature(
    val id: Int,
    val majorName: String,
    val minorName: String,
    val accidentals: String,
    val chordsInKey: List<String>
)
