package com.dyusov.notes.domain

sealed interface ContentItem {
    data class Text(val context: String) : ContentItem

    data class Image(val url: String) : ContentItem
}