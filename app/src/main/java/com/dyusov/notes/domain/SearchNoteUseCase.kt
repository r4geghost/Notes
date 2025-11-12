package com.dyusov.notes.domain

import kotlinx.coroutines.flow.Flow

class SearchNoteUseCase {
    operator fun invoke(query: String): Flow<List<Note>> {
        TODO()
    }
}