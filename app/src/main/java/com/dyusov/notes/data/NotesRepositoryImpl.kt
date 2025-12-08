package com.dyusov.notes.data

import com.dyusov.notes.domain.ContentItem
import com.dyusov.notes.domain.Note
import com.dyusov.notes.domain.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotesRepositoryImpl @Inject constructor(
    private val notesDao: NotesDao,
    private val imageFileManager: ImageFileManager
) : NotesRepository {

    override suspend fun addNote(
        title: String,
        content: List<ContentItem>,
        updatedAt: Long,
        isPinned: Boolean
    ) {
        val processedContent = content.processForStorage()
        val noteDbModel = NoteDbModel(
            id = 0, // если id = 0, БД сама сгенерирует id
            title = title,
            updatedAt = updatedAt,
            isPinned = isPinned
        )
        val noteId = notesDao.addNote(noteDbModel).toInt() // добавляем заметку и получаем id

        val contentItems = processedContent.toContentItemDbModels(noteId)
        notesDao.addNoteContent(contentItems) // добавляем контент
    }

    override suspend fun deleteNote(noteId: Int) {
        val note = notesDao.getNote(noteId).toEntity()
        notesDao.deleteNote(noteId)

        note.content
            .filterIsInstance<ContentItem.Image>()
            .map { it.url }
            .forEach {
                imageFileManager.deleteImage(it)
            }
    }

    override suspend fun editNote(note: Note) {
        val oldNote = notesDao.getNote(note.id).toEntity()

        getRemovedUrls(oldNote = oldNote, newNote = note).forEach { url ->
            imageFileManager.deleteImage(url)
        }

        val processedContent = note.content.processForStorage()
        val processedNote = note.copy(content = processedContent)

        notesDao.addNote(processedNote.toDbModel()) // используем ранее созданный маппер
        // сначала удаляем весь прошлый контент, а затем добавляем новый
        notesDao.deleteNoteContent(note.id)
        notesDao.addNoteContent(processedContent.toContentItemDbModels(note.id))
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

    private suspend fun List<ContentItem>.processForStorage(): List<ContentItem> {
        return map { contentItem ->
            when (contentItem) {
                is ContentItem.Image -> {
                    if (imageFileManager.isFileInternal(contentItem.url)) {
                        contentItem
                    } else {
                        // переопределяем путь к внутреннему хранилищу
                        ContentItem.Image(imageFileManager.copyImageToInternStorage(contentItem.url))
                    }
                }

                is ContentItem.Text -> contentItem
            }
        }
    }

    private fun getRemovedUrls(oldNote: Note, newNote: Note): List<String> {
        return getContentUrlsFromNote(oldNote) - getContentUrlsFromNote(newNote).toSet()
    }

    private fun getContentUrlsFromNote(note: Note): List<String> {
        return note.content.filterIsInstance<ContentItem.Image>().map { it.url }
    }
}