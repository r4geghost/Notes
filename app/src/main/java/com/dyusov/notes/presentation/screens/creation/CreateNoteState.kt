package com.dyusov.notes.presentation.screens.creation

import com.dyusov.notes.domain.ContentItem

sealed interface CreateNoteState {

    data class Creation(
        val title: String = "",
        val content: List<ContentItem> = listOf(ContentItem.Text(""))
    ) : CreateNoteState {

        val isSaveEnabled: Boolean
            get() {
                return when {
                    title.isBlank() -> false
                    content.isEmpty() -> false
                    else -> {
                        content.any {
                            // если только тексты и хотя бы один из них не пустой
                            it !is ContentItem.Text || it.content.isNotBlank()
                        }
                    }
                }
            }
    }

    data object Finished : CreateNoteState
}