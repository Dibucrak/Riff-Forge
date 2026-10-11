package com.riffforge.shared.domain.repository

import com.riffforge.shared.domain.model.Contribution
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    fun getApprovedContributions(): Flow<List<Contribution>>
    suspend fun publishContribution(contribution: Contribution): Result<Unit>

    fun getPendingContributions(): Flow<List<Contribution>>
    suspend fun approveContribution(id: String): Result<Unit>
    suspend fun rejectContribution(id: String): Result<Unit>
}
