package com.riffforge.shared.domain.use_case

import com.riffforge.shared.domain.model.Setlist
import com.riffforge.shared.domain.model.SetlistDetail
import com.riffforge.shared.domain.repository.SetlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GetSetlistsUseCase(private val repository: SetlistRepository) {
    operator fun invoke(): Flow<List<SetlistDetail>> = repository.getSetlists()
}

class GetSetlistByIdUseCase(
    private val repository: SetlistRepository
) {
    suspend operator fun invoke(id: Int): SetlistDetail? {
        return repository.getSetlistById(id).firstOrNull()
    }
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

data class SetlistUseCases(
    val getSetlists: GetSetlistsUseCase,
    val getSetlistById: GetSetlistByIdUseCase,
    val addSetlist: AddSetlistUseCase,
    val addSongToSetlist: AddSongToSetlistUseCase
)
