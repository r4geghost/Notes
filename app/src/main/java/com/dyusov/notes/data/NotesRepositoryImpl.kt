package com.dyusov.notes.data

import com.dyusov.notes.domain.ContentItem
import com.dyusov.notes.domain.Note
import com.dyusov.notes.domain.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotesRepositoryImpl @Inject constructor(
    private val notesDao: NotesDao
) : NotesRepository {

    override suspend fun addNote(
        title: String,
        content: List<ContentItem>,
        updatedAt: Long,
        isPinned: Boolean
    ) {
        val note = Note(
            id = 0, // если id = 0, БД сама сгенерирует id
            title = title,
            content = content,
            updatedAt = updatedAt,
            isPinned = isPinned
        )
        val noteDbModel = note.toDbModel()
        notesDao.addNote(noteDbModel)
    }

    override suspend fun deleteNote(noteId: Int) {
        notesDao.deleteNote(noteId)
    }

    override suspend fun editNote(note: Note) {
        notesDao.addNote(note.toDbModel()) // используем ранее созданный маппер
    }

    override fun getAllNotes(): Flow<List<Note>> {
        return notesDao.getAllNotes().map { it.toEntities() } // NotesDbModel -> Note
    }

    override suspend fun getNote(noteId: Int): Note {
        return notesDao.getNote(noteId).toEntity()
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        return notesDao.searchNotes(query).map { it.toEntities() }
    }

    override suspend fun switchPinStatus(noteId: Int) {
        notesDao.switchPinnedStatus(noteId)
    }
}