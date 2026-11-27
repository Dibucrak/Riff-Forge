package com.riffforge.feature_auth.domain.use_case

import android.app.Activity
import com.riffforge.feature_auth.domain.model.AuthUser
import com.riffforge.feature_auth.domain.repository.AuthRepository

class LinkWithGoogleUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(activity: Activity): Result<AuthUser> {
        return repository.linkWithGoogle(activity)
    }
}
