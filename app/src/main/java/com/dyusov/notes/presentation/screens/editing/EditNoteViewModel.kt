package com.dyusov.notes.presentation.screens.editing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyusov.notes.domain.DeleteNoteUseCase
import com.dyusov.notes.domain.EditNoteUseCase
import com.dyusov.notes.domain.GetNoteUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// нужно обязательно передавать id заметки, которую редактируем
@HiltViewModel(assistedFactory = EditNoteViewModel.Factory::class)
class EditNoteViewModel @AssistedInject constructor(
    private val editNoteUseCase: EditNoteUseCase,
    private val getNoteUseCase: GetNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    @Assisted("noteId") private val noteId: Int
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("noteId") noteId: Int): EditNoteViewModel
    }

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