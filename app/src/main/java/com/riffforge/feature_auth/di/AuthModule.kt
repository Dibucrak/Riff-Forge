package com.riffforge.feature_auth.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.riffforge.feature_auth.data.repository.AuthRepositoryImpl
import com.riffforge.feature_auth.domain.repository.AuthRepository
import com.riffforge.feature_auth.domain.use_case.AuthUseCases
import com.riffforge.feature_auth.domain.use_case.DeleteAccountUseCase
import com.riffforge.feature_auth.domain.use_case.GetCurrentUserUseCase
import com.riffforge.feature_auth.domain.use_case.LinkWithGoogleUseCase
import com.riffforge.feature_auth.domain.use_case.SendPasswordResetUseCase
import com.riffforge.feature_auth.domain.use_case.SignInUseCase
import com.riffforge.feature_auth.domain.use_case.SignInWithGoogleUseCase
import com.riffforge.feature_auth.domain.use_case.SignOutUseCase
import com.riffforge.feature_auth.domain.use_case.SignUpUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthRepository(firebaseAuth: FirebaseAuth, firestore: FirebaseFirestore): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth, firestore)
    }

    @Provides
    @Singleton
    fun provideAuthUseCases(repository: AuthRepository): AuthUseCases {
        return AuthUseCases(
            signIn = SignInUseCase(repository),
            signUp = SignUpUseCase(repository),
            signInWithGoogle = SignInWithGoogleUseCase(repository),
            linkWithGoogle = LinkWithGoogleUseCase(repository),
            sendPasswordReset = SendPasswordResetUseCase(repository),
            deleteAccount = DeleteAccountUseCase(repository),
            signOut = SignOutUseCase(repository),
            getCurrentUser = GetCurrentUserUseCase(repository)
        )
    }
}
