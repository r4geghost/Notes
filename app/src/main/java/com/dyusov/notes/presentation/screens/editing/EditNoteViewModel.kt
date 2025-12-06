package com.dyusov.notes.presentation.screens.editing

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyusov.notes.domain.ContentItem.Image
import com.dyusov.notes.domain.ContentItem.Text
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
                val content = if (note.content.lastOrNull() !is Text) {
                    note.content + Text("")
                } else {
                    note.content
                }
                // устанавливаем состояние редактирования заметки
                EditNoteState.Editing(note.copy(content = content))
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
                            val newText = Text(content = command.content)
                            val newNote = currentState.note.copy(
                                content = currentState.note.content.toMutableList().apply {
                                    this[command.index] = newText
                                }
                            )
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
                            val content = note.content.filter {
                                // сохраняем либо изображения, либо непустой текст
                                it !is Text || it.content.isNotBlank()
                            }
                            editNoteUseCase(note.copy(content = content))
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

                is EditNoteCommand.DeleteImage -> {
                    _state.update { currentState ->
                        if (currentState is EditNoteState.Editing) {
                            val newContent = currentState.note.content.toMutableList().apply {
                                removeAt(command.index) // удаляем элемент по индексу
                            }
                            val newNote = currentState.note.copy(content = newContent)
                            currentState.copy(note = newNote)
                        } else {
                            currentState
                        }
                    }
                }

                is EditNoteCommand.AddImage -> {
                    _state.update { currentState ->
                        if (currentState is EditNoteState.Editing) {
                            // работаем со текущим списком контента
                            val newContent = currentState.note.content.toMutableList().apply {
                                val lastContentItem = last()
                                // удаляем последний элемент, если это пустая строка текста
                                if (lastContentItem is Text && lastContentItem.content.isBlank()) {
                                    removeAt(lastIndex)
                                }
                                // добавляем изображение
                                add(Image(command.uri.toString()))
                                // добавляем пустую строку (чтобы пользователь мог продолжать)
                                add(Text(""))
                            }
                            val newNote = currentState.note.copy(content = newContent)
                            currentState.copy(note = newNote)
                        } else {
                            currentState
                        }
                    }
                }
            }
        }
    }
}