package com.dyusov.notes.data

import com.dyusov.notes.domain.ContentItem
import com.dyusov.notes.domain.Note
import kotlinx.serialization.json.Json

fun Note.toDbModel(): NoteDbModel {
    return NoteDbModel(
        id = id,
        title = title,
        content = Json.encodeToString(content.toContentItemDbModels()),
        updatedAt = updatedAt,
        isPinned = isPinned
    )
}

fun NoteDbModel.toEntity(): Note {
    return Note(
        id = id,
        title = title,
        content = Json.decodeFromString<List<ContentItemDbModel>>(content).toContentItems(),
        updatedAt = updatedAt,
        isPinned = isPinned
    )
}

fun List<NoteDbModel>.toEntities(): List<Note> {
    return map { it.toEntity() }
}

fun List<ContentItem>.toContentItemDbModels(): List<ContentItemDbModel> {
    return map {item ->
        when(item) {
            is ContentItem.Image -> {
                ContentItemDbModel.Image(url = item.url)
            }
            is ContentItem.Text -> {
                ContentItemDbModel.Text(content = item.context)
            }
        }
    }
}

fun List<ContentItemDbModel>.toContentItems(): List<ContentItem> {
    return map {item ->
        when(item) {
            is ContentItemDbModel.Image -> {
                ContentItem.Image(url = item.url)
            }
            is ContentItemDbModel.Text -> {
                ContentItem.Text(context = item.content)
            }
        }
    }
}