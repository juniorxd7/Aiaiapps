package com.example.ui

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.example.data.SettingsRepository
import com.example.domain.WordListParser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

data class SorteiaUiState(
    val words: List<String> = emptyList(),
    val drawnHistory: List<String> = emptyList(),
    val currentWord: String? = null,
    val rollingWord: String? = null,
    val isDrawing: Boolean = false,
    val avoidRepeats: Boolean = false,
    val canUndoClear: Boolean = false,
) {
    val availableWords: List<String>
        get() = if (avoidRepeats) {
            words.filterNot { word -> drawnHistory.any { it.equals(word, ignoreCase = true) } }
        } else {
            words
        }

    val allDrawn: Boolean
        get() = words.isNotEmpty() && avoidRepeats && availableWords.isEmpty()

    val canDraw: Boolean
        get() = !isDrawing && availableWords.isNotEmpty()
}

sealed interface SorteiaEvent {
    data class DuplicateWord(val word: String) : SorteiaEvent
    data class ImportedWords(val count: Int) : SorteiaEvent
    data object ListCleared : SorteiaEvent
}

class SorteiaViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val wordsFlow = MutableStateFlow(
        savedStateHandle.get<ArrayList<String>>(KEY_WORDS)?.toList() ?: emptyList()
    )
    private val drawnHistoryFlow = MutableStateFlow(
        savedStateHandle.get<ArrayList<String>>(KEY_HISTORY)?.toList() ?: emptyList()
    )
    private val currentWordFlow = MutableStateFlow(
        savedStateHandle.get<String>(KEY_CURRENT_WORD)
    )
    private val rollingWordFlow = MutableStateFlow<String?>(null)
    private val isDrawingFlow = MutableStateFlow(false)
    private val lastClearedBackupFlow = MutableStateFlow<List<String>?>(null)

    private val _events = MutableSharedFlow<SorteiaEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<SorteiaEvent> = _events.asSharedFlow()

    private var drawJob: Job? = null

    val uiState: StateFlow<SorteiaUiState> = combine(
        wordsFlow,
        drawnHistoryFlow,
        currentWordFlow,
        rollingWordFlow,
        isDrawingFlow,
        settingsRepository.avoidRepeats,
        lastClearedBackupFlow,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        SorteiaUiState(
            words = values[0] as List<String>,
            drawnHistory = values[1] as List<String>,
            currentWord = values[2] as String?,
            rollingWord = values[3] as String?,
            isDrawing = values[4] as Boolean,
            avoidRepeats = values[5] as Boolean,
            canUndoClear = values[6] != null,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SorteiaUiState(
            words = wordsFlow.value,
            drawnHistory = drawnHistoryFlow.value,
            currentWord = currentWordFlow.value,
        ),
    )

    fun addWord(raw: String): Boolean {
        val cleaned = WordListParser.normalizeWord(raw)
        if (cleaned.isEmpty()) return false

        val existing = wordsFlow.value
        if (existing.any { it.equals(cleaned, ignoreCase = true) }) {
            _events.tryEmit(SorteiaEvent.DuplicateWord(cleaned))
            return false
        }

        val updated = existing + cleaned
        updateWords(updated)
        lastClearedBackupFlow.value = null
        return true
    }

    fun importList(rawText: String, replaceExisting: Boolean): Int {
        val parsed = WordListParser.parseWords(rawText)
        if (parsed.isEmpty()) return 0

        val current = if (replaceExisting) emptyList() else wordsFlow.value
        val existingKeys = current.map { it.lowercase() }.toHashSet()
        val toAdd = parsed.filter { existingKeys.add(it.lowercase()) }

        val updated = current + toAdd
        updateWords(updated)
        if (replaceExisting) {
            updateHistory(emptyList())
            updateCurrentWord(null)
        }
        lastClearedBackupFlow.value = null

        if (toAdd.isNotEmpty()) {
            _events.tryEmit(SorteiaEvent.ImportedWords(toAdd.size))
        }
        return toAdd.size
    }

    fun removeWord(word: String) {
        val updated = wordsFlow.value.filterNot { it.equals(word, ignoreCase = true) }
        updateWords(updated)
        val updatedHistory = drawnHistoryFlow.value.filterNot { it.equals(word, ignoreCase = true) }
        updateHistory(updatedHistory)
        if (currentWordFlow.value?.equals(word, ignoreCase = true) == true) {
            updateCurrentWord(updatedHistory.lastOrNull())
        }
    }

    fun clearAll() {
        val snapshot = wordsFlow.value
        if (snapshot.isEmpty()) return
        drawJob?.cancel()
        isDrawingFlow.value = false
        rollingWordFlow.value = null
        lastClearedBackupFlow.value = snapshot
        updateWords(emptyList())
        updateHistory(emptyList())
        updateCurrentWord(null)
        _events.tryEmit(SorteiaEvent.ListCleared)
    }

    fun undoClear() {
        val backup = lastClearedBackupFlow.value ?: return
        updateWords(backup)
        lastClearedBackupFlow.value = null
    }

    fun resetRound() {
        drawJob?.cancel()
        isDrawingFlow.value = false
        rollingWordFlow.value = null
        updateHistory(emptyList())
        updateCurrentWord(null)
    }

    fun toggleAvoidRepeats(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAvoidRepeats(enabled)
        }
    }

    fun drawRandomWord(random: Random = Random.Default, animate: Boolean = true) {
        val state = uiState.value
        if (state.isDrawing) return
        val pool = state.availableWords
        if (pool.isEmpty()) return

        val winner = pool[random.nextInt(pool.size)]

        if (!animate || pool.size == 1) {
            updateCurrentWord(winner)
            updateHistory(drawnHistoryFlow.value + winner)
            return
        }

        drawJob?.cancel()
        drawJob = viewModelScope.launch {
            isDrawingFlow.value = true
            val steps = 8
            for (i in 0 until steps) {
                val preview = pool[random.nextInt(pool.size)]
                rollingWordFlow.value = preview
                val stepDelay = 45L + (i * 12L)
                delay(stepDelay)
            }
            rollingWordFlow.value = null
            updateCurrentWord(winner)
            updateHistory(drawnHistoryFlow.value + winner)
            isDrawingFlow.value = false
        }
    }

    private fun updateWords(words: List<String>) {
        wordsFlow.value = words
        savedStateHandle[KEY_WORDS] = ArrayList(words)
    }

    private fun updateHistory(history: List<String>) {
        drawnHistoryFlow.value = history
        savedStateHandle[KEY_HISTORY] = ArrayList(history)
    }

    private fun updateCurrentWord(word: String?) {
        currentWordFlow.value = word
        savedStateHandle[KEY_CURRENT_WORD] = word
    }

    companion object {
        private const val KEY_WORDS = "key_words"
        private const val KEY_HISTORY = "key_history"
        private const val KEY_CURRENT_WORD = "key_current_word"

        fun provideFactory(
            owner: SavedStateRegistryOwner,
            settingsRepository: SettingsRepository,
        ): AbstractSavedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory(owner, null) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle,
                ): T {
                    return SorteiaViewModel(handle, settingsRepository) as T
                }
            }
    }
}
