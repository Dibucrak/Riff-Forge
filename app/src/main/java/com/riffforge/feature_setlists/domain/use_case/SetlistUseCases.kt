package com.riffforge.feature_setlists.domain.use_case

import com.riffforge.feature_setlists.domain.model.Setlist
import com.riffforge.feature_setlists.domain.model.SetlistDetail
import com.riffforge.feature_setlists.domain.repository.SetlistRepository
import kotlinx.coroutines.flow.Flow

class GetSetlistsUseCase(private val repository: SetlistRepository) {
    operator fun invoke(): Flow<List<SetlistDetail>> = repository.getSetlists()
}

class AddSetlistUseCase(private val repository: SetlistRepository) {
    suspend operator fun invoke(setlist: Setlist): Long {
        if (setlist.name.isBlank()) {
            throw IllegalArgumentException("El nombre del Setlist no puede estar vacío.")
        }
        return repository.insertSetlist(setlist)
    }
}

class AddSongToSetlistUseCase(private val repository: SetlistRepository) {
    suspend operator fun invoke(setId: Int, songId: Int) {
        repository.addSongToSetlist(setId, songId)
    }
}

class RemoveSongFromSetlistUseCase(private val repository: SetlistRepository) {
    suspend operator fun invoke(setId: Int, songId: Int) {
        repository.removeSongFromSetlist(setId, songId)
    }
}

class UpdateSetlistUseCase(private val repository: SetlistRepository) {
    suspend operator fun invoke(setId: Int, name: String, description: String) {
        if (name.isBlank()) throw IllegalArgumentException("El nombre del Setlist no puede estar vacío.")
        repository.updateSetlist(setId, name, description)
    }
}

class DeleteSetlistUseCase(private val repository: SetlistRepository) {
    suspend operator fun invoke(setId: Int) {
        repository.deleteSetlist(setId)
    }
}

data class SetlistUseCases(
    val getSetlists: GetSetlistsUseCase,
    val getSetlistById: GetSetlistByIdUseCase,
    val addSetlist: AddSetlistUseCase,
    val updateSetlist: UpdateSetlistUseCase,
    val deleteSetlist: DeleteSetlistUseCase,
    val addSongToSetlist: AddSongToSetlistUseCase,
    val removeSongFromSetlist: RemoveSongFromSetlistUseCase
)