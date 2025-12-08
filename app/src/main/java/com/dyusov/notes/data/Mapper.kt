package com.dyusov.notes.data

import com.dyusov.notes.domain.ContentItem
import com.dyusov.notes.domain.Note

fun Note.toDbModel(): NoteDbModel {
    return NoteDbModel(
        id = id,
        title = title,
        updatedAt = updatedAt,
        isPinned = isPinned
    )
}

// extension-функция на класс заметки с контентом
fun NoteWithContentDbModel.toEntity(): Note {
    return Note(
        id = noteDbModel.id,
        title = noteDbModel.title,
        content = content.toContentItems(),
        updatedAt = noteDbModel.updatedAt,
        isPinned = noteDbModel.isPinned
    )
}

fun List<NoteWithContentDbModel>.toEntities(): List<Note> {
    return map { it.toEntity() }
}

fun List<ContentItem>.toContentItemDbModels(noteId: Int): List<ContentItemDbModel> {
    return mapIndexed { index, item ->
        when (item) {
            is ContentItem.Image -> {
                ContentItemDbModel(
                    noteId = noteId,
                    contentType = ContentType.IMAGE,
                    content = item.url, // для картинок храним просто url
                    order = index // порядковый номер
                )
            }

            is ContentItem.Text -> {
                ContentItemDbModel(
                    noteId = noteId,
                    contentType = ContentType.TEXT,
                    content = item.content,
                    order = index // порядковый номер
                )
            }
        }
    }
}

fun List<ContentItemDbModel>.toContentItems(): List<ContentItem> {
    return map { item ->
        when (item.contentType) {
            ContentType.TEXT -> {
                ContentItem.Text(content = item.content)
            }

            ContentType.IMAGE -> {
                ContentItem.Image(url = item.content)
            }
        }
    }
}