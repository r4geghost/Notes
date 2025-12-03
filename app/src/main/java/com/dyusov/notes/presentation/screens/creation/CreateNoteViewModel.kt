package com.dyusov.notes.presentation.screens.creation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyusov.notes.domain.AddNoteUseCase
import com.dyusov.notes.domain.ContentItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateNoteViewModel @Inject constructor(
    private val addNoteUseCase: AddNoteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<CreateNoteState>(
        CreateNoteState.Creation()
    )
    val state = _state.asStateFlow()

    fun processCommand(command: CreateNoteCommand) {
        viewModelScope.launch {
            when (command) {
                is CreateNoteCommand.InputTitle -> {
                    _state.update { currentState ->
                        if (currentState is CreateNoteState.Creation) {
                            currentState.copy(title = command.title)
                        } else {
                            currentState // сюда мы вообще не должны попасть
                        }
                    }
                }

                is CreateNoteCommand.InputContent -> {
                    _state.update { currentState ->
                        if (currentState is CreateNoteState.Creation) {
                            val newContent = currentState.content
                                .mapIndexed { index, item ->
                                    // меняем контент только там, где были изменения
                                    if (index == command.index && item is ContentItem.Text) {
                                        item.copy(content = command.content)
                                    } else {
                                        item
                                    }
                                }
                            currentState.copy(content = newContent)
                        } else {
                            currentState // сюда мы вообще не должны попасть
                        }
                    }
                }

                CreateNoteCommand.Save -> {
                    _state.update { currentState ->
                        // если состояние создания, вызываем метод UseCase и сохраняем заметку
                        if (currentState is CreateNoteState.Creation) {
                            val content = currentState.content.filter {
                                // сохраняем либо изображения, либо непустой текст
                                it !is ContentItem.Text || it.content.isNotBlank()
                            }
                            addNoteUseCase(
                                title = currentState.title,
                                content = content
                            )
                            CreateNoteState.Finished // устанавливаем состояние завершения
                        } else {
                            currentState // если не в состоянии создания, возвращаем его же
                        }
                    }
                }

                CreateNoteCommand.Back -> {
                    _state.update { CreateNoteState.Finished }
                }

                is CreateNoteCommand.AddImage -> {
                    _state.update { currentState ->
                        if (currentState is CreateNoteState.Creation) {
                            // работаем со текущим списком контента
                            currentState.content.toMutableList().apply {
                                val lastContentItem = last()
                                // удаляем последний элемент, если это пустая строка текста
                                if (lastContentItem is ContentItem.Text && lastContentItem.content.isBlank()) {
                                    removeAt(lastIndex)
                                }
                                // добавляем изображение
                                add(ContentItem.Image(command.uri.toString()))
                                // добавляем пустую строку (чтобы пользователь мог продолжать)
                                add(ContentItem.Text(""))
                            }.let {
                                // обновляем состояние экрана, применяя изменения в контенте
                                currentState.copy(content = it)
                            }
                        } else {
                            currentState
                        }
                    }
                }
            }
        }
    }
}