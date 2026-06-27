package com.riffforge.feature_contributions.domain.repository

import com.riffforge.feature_contributions.domain.model.Contribution
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    fun getApprovedContributions(): Flow<List<Contribution>>
    suspend fun publishContribution(contribution: Contribution): Result<Unit>
}