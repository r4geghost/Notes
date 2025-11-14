@file:OptIn(ExperimentalCoroutinesApi::class)

package com.dyusov.notes.presentation.screens.notes

import androidx.lifecycle.ViewModel
import com.dyusov.notes.data.TestNotesRepositoryImpl
import com.dyusov.notes.domain.AddNoteUseCase
import com.dyusov.notes.domain.DeleteNoteUseCase
import com.dyusov.notes.domain.EditNoteUseCase
import com.dyusov.notes.domain.GetAllNotesUseCase
import com.dyusov.notes.domain.GetNoteUseCase
import com.dyusov.notes.domain.Note
import com.dyusov.notes.domain.SearchNoteUseCase
import com.dyusov.notes.domain.SwitchPinnedStatusUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

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


    // todo: temp
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        query
            // произойдет переключение на другой тип данных flow (список заметок)
            // latest - отменяет предыдущие подписки при изменении объекта flow
            .flatMapLatest {
                if (it.isBlank()) {
                    searchNoteUseCase(it)
                } else {
                    getAllNotesUseCase()
                }
            }
            .onEach {
                val pinned = it.filter { note -> note.isPinned }
                val other = it.filter { note -> !note.isPinned }
                _state.update {
                    it.copy(pinnedNotes = pinned, otherNotes = other)
                }
            }
            .launchIn(scope)
    }

    fun processCommand(command: NotesCommand) {
        when (command) {
            is NotesCommand.DeleteNote -> {
                deleteNoteUseCase(command.noteId)
            }

            is NotesCommand.EditNote -> {
                val title = command.note.title
                editNoteUseCase(note = command.note.copy(title = "$title edited"))
            }

            is NotesCommand.InputSearchQuery -> {

            }

            is NotesCommand.SwitchPinnedStatus -> {
                switchPinnedStatusUseCase(command.noteId)
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