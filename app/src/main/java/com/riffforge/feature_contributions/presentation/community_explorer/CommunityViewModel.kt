package com.riffforge.feature_contributions.presentation.community_explorer

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.riffforge.feature_contributions.domain.model.Contribution
import com.riffforge.feature_contributions.domain.repository.CommunityRepository
import com.riffforge.feature_songs.domain.model.Song
import com.riffforge.feature_songs.domain.use_case.SongUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val songUseCases: SongUseCases,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = mutableStateOf(CommunityState())
    val state: State<CommunityState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var getContributionsJob: Job? = null
    private var getMyContributionsJob: Job? = null
    private var getMySongsJob: Job? = null

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }

    init {
        getContributions()
        observeMyContributions()
        observeMySongs()
    }

    fun onEvent(event: CommunityEvent) {
        when (event) {
            is CommunityEvent.SearchQueryChanged -> {
                _state.value = state.value.copy(searchQuery = event.query)
            }
            is CommunityEvent.SelectTab -> {
                _state.value = state.value.copy(selectedTab = event.index)
            }
            is CommunityEvent.DownloadContribution -> {
                downloadToLocalLibrary(event.contribution)
            }
            is CommunityEvent.DeleteMyContribution -> {
                deleteMyContribution(event.id)
            }
            is CommunityEvent.ToggleExpandContribution -> {
                val current = state.value.expandedIds
                val updated = if (event.id in current) current - event.id else current + event.id
                _state.value = state.value.copy(expandedIds = updated)
            }
            is CommunityEvent.ShowShareSongDialog -> {
                _state.value = state.value.copy(isShareDialogVisible = true)
            }
            is CommunityEvent.HideShareSongDialog -> {
                _state.value = state.value.copy(isShareDialogVisible = false)
            }
            is CommunityEvent.PublishLocalSong -> {
                publishLocalSongToCommunity(event.song)
            }
        }
    }

    private fun getContributions() {
        getContributionsJob?.cancel()
        getContributionsJob = communityRepository.getApprovedContributions()
            .onEach { contributionsList ->
                _state.value = state.value.copy(
                    contributions = contributionsList,
                    isLoading = false,
                    error = ""
                )
                loadAuthorAvatars(contributionsList)
            }
            .catch { e ->
                _state.value = state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar la comunidad"
                )
            }
            .launchIn(viewModelScope)
    }

    private fun observeMyContributions() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        getMyContributionsJob?.cancel()
        getMyContributionsJob = communityRepository.getContributionsByAuthor(uid)
            .onEach { list ->
                _state.value = state.value.copy(myContributions = list)
                loadAuthorAvatars(list)
            }
            .catch { }
            .launchIn(viewModelScope)
    }

    private fun loadAuthorAvatars(contributions: List<Contribution>) {
        val currentUser = firebaseAuth.currentUser
        val currentUid = currentUser?.uid.orEmpty()
        val distinctAuthorIds = contributions.map { it.authorId.trim() }
            .filter { it.isNotBlank() }
            .distinct()
        val existing = state.value.authorAvatars
        val targetIds = distinctAuthorIds.filter { uid ->
            uid !in existing || uid == currentUid
        }
        if (targetIds.isEmpty()) return

        viewModelScope.launch {
            val fetched = coroutineScope {
                targetIds.map { uid ->
                    async {
                        uid to fetchPublicAvatar(uid, currentUser)
                    }
                }.awaitAll()
            }
            val merged = state.value.authorAvatars.toMutableMap()
            fetched.forEach { (uid, pair) ->
                if (pair != null) {
                    merged[uid] = pair
                }
            }
            _state.value = state.value.copy(authorAvatars = merged)
        }
    }

    private suspend fun fetchPublicAvatar(
        uid: String,
        currentUser: com.google.firebase.auth.FirebaseUser?
    ): Pair<String, String>? {
        val pubSnap = runCatching {
            firestore.collection("public_profiles").document(uid).get().await()
        }.getOrNull()

        var photoUrl = pubSnap?.getString("photoUrl").orEmpty()
        var photoData = pubSnap?.getString("photoData").orEmpty()

        if (currentUser != null && uid == currentUser.uid && (photoUrl.isBlank() || pubSnap == null || !pubSnap.exists())) {
            val authPhoto = currentUser.providerData
                .firstOrNull { it.providerId == "google.com" && it.photoUrl != null }
                ?.photoUrl?.toString()
                ?: currentUser.photoUrl?.toString()
                ?: ""
            val cleanAuthPhoto = authPhoto.replace("=s96-c", "=s400-c")
            val userSnap = runCatching {
                firestore.collection("users").document(uid).get().await()
            }.getOrNull()
            val storedPhotoUrl = userSnap?.getString("photoUrl").orEmpty()
            val storedPhotoData = userSnap?.getString("photoData").orEmpty()
            if (photoUrl.isBlank()) {
                photoUrl = storedPhotoUrl.ifBlank { cleanAuthPhoto }
            }
            if (photoData.isBlank()) {
                photoData = storedPhotoData
            }
            val payload = hashMapOf<String, Any>(
                "displayName" to (userSnap?.getString("displayName") ?: currentUser.displayName ?: "Guitarrista"),
                "username" to (userSnap?.getString("username") ?: currentUser.displayName?.lowercase()?.replace(" ", "_") ?: "musico"),
                "photoUrl" to photoUrl,
                "photoData" to photoData
            )
            runCatching {
                firestore.collection("public_profiles").document(uid)
                    .set(payload, SetOptions.merge()).await()
            }
        }

        return if (photoUrl.isNotBlank() || photoData.isNotBlank()) {
            photoUrl to photoData
        } else {
            null
        }
    }

    private fun observeMySongs() {
        getMySongsJob?.cancel()
        getMySongsJob = songUseCases.getSongs()
            .onEach { songs ->
                _state.value = state.value.copy(mySongs = songs)
            }
            .catch { }
            .launchIn(viewModelScope)
    }

    private fun downloadToLocalLibrary(contribution: Contribution) {
        viewModelScope.launch {
            try {
                val alreadyExists = state.value.mySongs.any {
                    it.title.trim().equals(contribution.title.trim(), ignoreCase = true) &&
                        it.artist.trim().equals(contribution.artist.ifBlank { "Artista de la Comunidad" }.trim(), ignoreCase = true)
                }
                if (alreadyExists) {
                    _state.value = state.value.copy(
                        downloadedIds = state.value.downloadedIds + contribution.id
                    )
                    _eventFlow.emit(UiEvent.ShowSnackbar("\"${contribution.title}\" ya está en tu repertorio local"))
                    return@launch
                }

                val localSong = Song(
                    title = contribution.title,
                    artist = contribution.artist.ifBlank { "Artista de la Comunidad" },
                    key = contribution.key,
                    tuning = contribution.tuning,
                    bpm = contribution.bpm,
                    content = contribution.content,
                    isDraft = false
                )

                songUseCases.addSong(localSong)
                _state.value = state.value.copy(
                    downloadedIds = state.value.downloadedIds + contribution.id
                )
                _eventFlow.emit(UiEvent.ShowSnackbar("\"${contribution.title}\" guardada en tu repertorio local"))
            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Error al descargar: ${e.message}"))
            }
        }
    }

    private fun deleteMyContribution(id: String) {
        viewModelScope.launch {
            val result = communityRepository.deleteContribution(id)
            if (result.isSuccess) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Aporte eliminado."))
            } else {
                _eventFlow.emit(UiEvent.ShowSnackbar("Error al eliminar aporte: ${result.exceptionOrNull()?.message ?: ""}"))
            }
        }
    }

    private fun publishLocalSongToCommunity(song: Song) {
        viewModelScope.launch {
            _state.value = state.value.copy(isShareDialogVisible = false)
            val user = firebaseAuth.currentUser
            if (user == null) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Debes iniciar sesión para compartir canciones."))
                return@launch
            }
            try {
                val authorName = user.displayName?.takeIf { it.isNotBlank() }
                    ?: user.email?.substringBefore("@")
                    ?: "Músico Riff Forge"
                val newContribution = Contribution(
                    id = "",
                    title = song.title,
                    artist = song.artist.ifBlank { "Original" },
                    key = song.key.ifBlank { "C" },
                    tuning = song.tuning.ifBlank { "E Standard" },
                    bpm = song.bpm,
                    content = song.content,
                    authorId = user.uid,
                    authorName = authorName,
                    isApproved = false,
                    timestamp = System.currentTimeMillis()
                )

                val result = communityRepository.publishContribution(newContribution)
                if (result.isSuccess) {
                    _state.value = state.value.copy(selectedTab = 1)
                    _eventFlow.emit(
                        UiEvent.ShowSnackbar("\"${song.title}\" enviada a revisión. Aparecerá en Comunidad cuando un administrador la apruebe.")
                    )
                } else {
                    _eventFlow.emit(
                        UiEvent.ShowSnackbar("Error al enviar aporte: ${result.exceptionOrNull()?.message ?: "Intenta de nuevo"}")
                    )
                }
            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Error al compartir: ${e.message}"))
            }
        }
    }
}
