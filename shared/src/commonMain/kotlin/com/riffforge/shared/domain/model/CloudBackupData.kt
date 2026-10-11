package com.riffforge.shared.domain.model

data class CloudBackupData(
    val songs: List<Song> = emptyList(),
    val setlists: List<SetlistDetail> = emptyList()
)
