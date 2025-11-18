@file:OptIn(ExperimentalCoroutinesApi::class)

package com.dyusov.notes.presentation.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyusov.notes.data.TestNotesRepositoryImpl
import com.dyusov.notes.domain.AddNoteUseCase
import com.dyusov.notes.domain.DeleteNoteUseCase
import com.dyusov.notes.domain.EditNoteUseCase
import com.dyusov.notes.domain.GetAllNotesUseCase
import com.dyusov.notes.domain.GetNoteUseCase
import com.dyusov.notes.domain.Note
import com.dyusov.notes.domain.SearchNoteUseCase
import com.dyusov.notes.domain.SwitchPinnedStatusUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotesViewModel : ViewModel() {
    private val repository = TestNotesRepositoryImpl // временно нарушаем принцип чистой архитектуры

    private val addNoteUseCase = AddNoteUseCase(repository)
    private val editNoteUseCase = EditNoteUseCase(repository)
    private val deleteNoteUseCase = DeleteNoteUseCase(repository)
    private val getAllNotesUseCase = GetAllNotesUseCase(repository)
    private val getNoteUseCase = GetNoteUseCase(repository)
    private val searchNoteUseCase = SearchNoteUseCase(repository)
    private val switchPinnedStatusUseCase = SwitchPinnedStatusUseCase(repository)

    private val query = MutableStateFlow("")

    private val _state = MutableStateFlow<NotesScreenState>(
        NotesScreenState() // значение по умолчанию
    )
    val state = _state.asStateFlow()

    init {
        addSomeNotes()
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

    // todo: temp
    private fun addSomeNotes() {
        viewModelScope.launch {
            repeat(50000) {
                addNoteUseCase(title = "Title №$it", content = "Content №$it")
            }
        }

    }

    fun processCommand(command: NotesCommand) {
        viewModelScope.launch {
            when (command) {
                is NotesCommand.DeleteNote -> {
                    deleteNoteUseCase(command.noteId)
                }

                is NotesCommand.EditNote -> {
                    val note = getNoteUseCase(command.note.id) // todo: temp
                    val title = note.title
                    editNoteUseCase(note = command.note.copy(title = "$title edited"))
                }

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