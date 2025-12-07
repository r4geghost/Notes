package com.dyusov.notes.data

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "content",
    primaryKeys = ["noteId", "order"], // двойной PK
    foreignKeys = [
        // FK на таблицу notes
        ForeignKey(
            entity = NoteDbModel::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE // если удаляется заметка, то и контент тоже
        )
    ]
)
data class ContentItemDbModel(
    val noteId: Int,
    val contentType: ContentType,
    val content: String,
    val order: Int
)

enum class ContentType {
    TEXT, IMAGE
}