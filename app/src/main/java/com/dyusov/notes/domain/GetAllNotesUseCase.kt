package com.dyusov.notes.domain

import kotlinx.coroutines.flow.Flow

class GetAllNotesUseCase {
    /* избегаем дублирования, если добавить operator, можем вызывать эту функцию без указания имени:

        val getAllNotesUseCase = GetAllNotesUseCase()
        getAllNotesUseCase() -> тут вызов метода invoke()
    */
    operator fun invoke(): Flow<List<Note>> {
        TODO()
    }
}