package com.dyusov.notes.presentation.screens.creation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dyusov.notes.domain.AddNoteUseCase
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
                            currentState.copy(
                                title = command.title,
                                // если заголовок и контент не пустой, делаем кнопку "save" активной
                                isSavedEnabled = currentState.title.isNotBlank() && currentState.content.isNotBlank()
                            )
                        } else {
                            CreateNoteState.Creation(title = command.title)
                        }
                    }
                }

                is CreateNoteCommand.InputContent -> {
                    _state.update { currentState ->
                        if (currentState is CreateNoteState.Creation) {
                            currentState.copy(
                                content = command.content,
                                // если заголовок и контент не пустой, делаем кнопку "save" активной
                                isSavedEnabled = currentState.title.isNotBlank() && currentState.content.isNotBlank()
                            )
                        } else {
                            CreateNoteState.Creation(content = command.content)
                        }
                    }
                }

                CreateNoteCommand.Save -> {
                    _state.update { currentState ->
                        // если состояние создания, вызываем метод UseCase и сохраняем заметку
                        if (currentState is CreateNoteState.Creation) {
                            addNoteUseCase(
                                title = currentState.title,
                                content = currentState.content
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
            }
        }
    }
}