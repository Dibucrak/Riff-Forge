package com.riffforge.feature_setlists.domain.use_case

import com.riffforge.feature_setlists.domain.model.SetlistDetail
import com.riffforge.feature_setlists.domain.repository.SetlistRepository
import kotlinx.coroutines.flow.firstOrNull

class GetSetlistByIdUseCase(
    private val repository: SetlistRepository
) {
    suspend operator fun invoke(id: Int): SetlistDetail? {
        return repository.getSetlistById(id).firstOrNull()
    }
}