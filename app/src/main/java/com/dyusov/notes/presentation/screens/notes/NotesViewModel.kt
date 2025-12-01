@file:OptIn(ExperimentalCoroutinesApi::class)

package com.dyusov.notes.presentation.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyusov.notes.domain.GetAllNotesUseCase
import com.dyusov.notes.domain.Note
import com.dyusov.notes.domain.SearchNoteUseCase
import com.dyusov.notes.domain.SwitchPinnedStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val getAllNotesUseCase: GetAllNotesUseCase,
    private val searchNoteUseCase: SearchNoteUseCase,
    private val switchPinnedStatusUseCase: SwitchPinnedStatusUseCase
) : ViewModel() {

    private val query = MutableStateFlow("")

    private val _state = MutableStateFlow<NotesScreenState>(
        NotesScreenState() // значение по умолчанию
    )
    val state = _state.asStateFlow()

    init {
        query
            // обновление состояния экрана
            .onEach { input ->
                _state.update { it.copy(query = input) }
            }
            // произойдет переключение на другой тип данных flow (список заметок)
            // latest - отменяет предыдущие подписки при изменении объекта flow
            .flatMapLatest { input ->
                if (input.isBlank()) {
                    getAllNotesUseCase()
                } else {
                    searchNoteUseCase(input)
                }
            }
            .onEach { notes ->
                val pinned = notes.filter { note -> note.isPinned }
                val other = notes.filter { note -> !note.isPinned }
                _state.update {
                    it.copy(pinnedNotes = pinned, otherNotes = other)
                }
            }
            .launchIn(viewModelScope) // особый скоуп viewModel
        // уничтожается вместе со всеми корутинами при уничтожении ViewModel
    }

    fun processCommand(command: NotesCommand) {
        viewModelScope.launch {
            when (command) {
                is NotesCommand.InputSearchQuery -> {
                    query.update {
                        command.query.trim()
                    }
                }

                is NotesCommand.SwitchPinnedStatus -> {
                    switchPinnedStatusUseCase(command.noteId)
                }
            }
        }
    }
}


// класс для хранения состояния экрана (поискового запроса, закрепленных заметок и остальных)
data class NotesScreenState(
    val query: String = "",
    val pinnedNotes: List<Note> = listOf(),
    val otherNotes: List<Note> = listOf()
)