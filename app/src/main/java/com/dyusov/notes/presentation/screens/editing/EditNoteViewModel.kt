package com.dyusov.notes.presentation.screens.editing

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyusov.notes.data.NotesRepositoryImpl
import com.dyusov.notes.domain.DeleteNoteUseCase
import com.dyusov.notes.domain.EditNoteUseCase
import com.dyusov.notes.domain.GetNoteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// нужно обязательно передавать id заметки, которую редактируем
class EditNoteViewModel(private val noteId: Int, context: Context) : ViewModel() {

    private val repository = NotesRepositoryImpl.getInstance(context)

    private val editNoteUseCase = EditNoteUseCase(repository)
    private val getNoteUseCase = GetNoteUseCase(repository)
    private val deleteNoteUseCase = DeleteNoteUseCase(repository)

    private val _state = MutableStateFlow<EditNoteState>(
        EditNoteState.Initial
    )
    val state = _state.asStateFlow()

    // при инициализации вью модели нужно загрузить заметку из репозитория
    init {
        viewModelScope.launch {
            _state.update {
                val note = getNoteUseCase(noteId) // noteId из конструктора!
                // устанавливаем состояние редактирования заметки
                EditNoteState.Editing(note)
            }
        }
    }

    fun processCommand(command: EditNoteCommand) {
        viewModelScope.launch {
            when (command) {
                is EditNoteCommand.InputTitle -> {
                    _state.update { currentState ->
                        if (currentState is EditNoteState.Editing) {
                            val newNote = currentState.note.copy(title = command.title)
                            currentState.copy(note = newNote)
                        } else {
                            currentState
                        }
                    }
                }

                is EditNoteCommand.InputContent -> {
                    _state.update { currentState ->
                        if (currentState is EditNoteState.Editing) {
                            val newNote = currentState.note.copy(content = command.content)
                            currentState.copy(note = newNote)
                        } else {
                            currentState
                        }
                    }
                }

                EditNoteCommand.Save -> {
                    _state.update { currentState ->
                        if (currentState is EditNoteState.Editing) {
                            val note = currentState.note
                            editNoteUseCase(note)
                            EditNoteState.Finished // устанавливаем состояние завершения
                        } else {
                            currentState // если не в состоянии создания, возвращаем его же
                        }
                    }
                }

                EditNoteCommand.Back -> {
                    _state.update { EditNoteState.Finished }
                }

                EditNoteCommand.Delete -> {
                    _state.update { currentState ->
                        if (currentState is EditNoteState.Editing) {
                            val note = currentState.note
                            deleteNoteUseCase(note.id)
                            EditNoteState.Finished // устанавливаем состояние завершения
                        } else {
                            currentState // если не в состоянии создания, возвращаем его же
                        }
                    }
                }
            }
        }
    }
}