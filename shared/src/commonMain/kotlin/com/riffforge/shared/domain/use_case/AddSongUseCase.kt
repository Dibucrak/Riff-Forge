package com.riffforge.shared.domain.use_case

import com.riffforge.shared.domain.model.Song
import com.riffforge.shared.domain.repository.SongRepository

class AddSongUseCase(
    private val repository: SongRepository
) {
    suspend operator fun invoke(song: Song) {
        if (song.title.isBlank()) {
            throw IllegalArgumentException("El título de la canción no puede estar vacío.")
        }
        if (song.artist.isBlank()) {
            throw IllegalArgumentException("El nombre del artista no puede estar vacío.")
        }

        repository.insertSong(song)
    }
}
