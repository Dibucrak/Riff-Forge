package com.riffforge.feature_contributions.domain.repository

import com.riffforge.feature_contributions.domain.model.Contribution
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    fun getApprovedContributions(): Flow<List<Contribution>>
    fun getContributionsByAuthor(authorId: String): Flow<List<Contribution>>
    fun getAllContributions(): Flow<List<Contribution>>
    suspend fun publishContribution(contribution: Contribution): Result<Unit>

    fun getPendingContributions(): Flow<List<Contribution>>
    suspend fun approveContribution(id: String): Result<Unit>
    suspend fun rejectContribution(id: String): Result<Unit>
    suspend fun deleteContribution(id: String): Result<Unit>
}
