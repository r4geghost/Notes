package com.dyusov.notes.data

import kotlinx.serialization.Serializable

@Serializable
sealed interface ContentItemDbModel {
    data class Text(val content: String) : ContentItemDbModel

    data class Image(val url: String) : ContentItemDbModel
}