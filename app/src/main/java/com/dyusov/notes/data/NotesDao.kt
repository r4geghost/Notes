package com.dyusov.notes.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {
    /* Room будет автоматически оповещать о каждом изменении в БД, если используется Flow */

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteDbModel>>

    @Query("SELECT * FROM notes " +
            "WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'" +
            "ORDER BY updatedAt DESC") // '||' - конкатенация строк
    fun searchNotes(query: String): Flow<List<NoteDbModel>>

    @Query("DELETE FROM notes WHERE id == :noteId")
    suspend fun deleteNote(noteId: Int)

    @Query("UPDATE notes SET isPinned = NOT isPinned WHERE id == :noteId")
    suspend fun switchPinnedStatus(noteId: Int)

    // для вставки запрос писать не нужно, при конфликте - обновление записи
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNote(note: NoteDbModel)
}