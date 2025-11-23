package com.dyusov.notes.presentation.screens.navigation

import android.os.Bundle

// библиотека Jetpack Compose Navigation использует строки для навигации (route - направление)
sealed class Screen(val route: String) {

    data object Notes : Screen("notes")

    data object CreateNote : Screen("create_note")

    data object EditNote : Screen("edit_note/{note_id}") { // Bundle("note_id" -> 5)
        fun createRoute(noteId: Int): String { // edit_note/5
            return "edit_note/$noteId"
        }

        fun getNoteId(args: Bundle?): Int {
            return args?.getString("note_id")?.toInt() ?: 0
        }
    }
}