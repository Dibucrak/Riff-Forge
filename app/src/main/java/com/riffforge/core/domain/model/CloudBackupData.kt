package com.riffforge.core.domain.model

import com.riffforge.feature_setlists.domain.model.SetlistDetail
import com.riffforge.feature_songs.domain.model.Song

data class CloudBackupData(
    val songs: List<Song> = emptyList(),
    val setlists: List<SetlistDetail> = emptyList()
)