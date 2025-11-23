package com.dyusov.notes.presentation.screens.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dyusov.notes.presentation.screens.creation.CreateNoteScreen
import com.dyusov.notes.presentation.screens.editing.EditNoteScreen
import com.dyusov.notes.presentation.screens.notes.NotesScreen

// реализация с использованием библиотеки Jetpack Compose Navigation
@Composable
fun NavGraph() {
    /*
        NavController содержит граф навигации и предоставляет методы, которые
        позволяют приложению перемещаться между пунктами назначения в графе

        rememberNavController позволяет переживать рекомпозицию
     */
    val navController = rememberNavController()

    /*
     NavHost отвечает за переключение экранов приложения
     */
    NavHost(
        navController = navController,
        startDestination = Screen.Notes.route // указываем начальное состояние экрана
    ) {
        // строим граф навигации - новый экран в граф добавляется с помощью функции composable
        composable(Screen.Notes.route) {
            NotesScreen(
                // для перехода на другой экран вызываем функцию navigate у navController
                onNoteClick = {
                    // edit_note/{note_id}, где {note_id} - параметр (id заметки)
                    navController.navigate(Screen.EditNote.createRoute(noteId = it.id))
                },
                onAddNoteClick = {
                    navController.navigate(Screen.CreateNote.route)
                }
            )
        }
        composable(Screen.CreateNote.route) {
            CreateNoteScreen(
                // при получении callback на завершение, возвращаемся назад к предыдущему экрану
                onFinished = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.EditNote.route) {
            // получаем id заметки из аргументов
            val noteId = Screen.EditNote.getNoteId(it.arguments)
            EditNoteScreen(
                onFinished = {
                    navController.popBackStack()
                },
                noteId = noteId
            )
        }
    }
}

/*------------------------------------------------------------------------------------------------*/

// кастомная реализация без библиотеки
@Composable
fun CustomNavGraph() {

    val customScreen = remember {
        mutableStateOf<CustomScreen>(CustomScreen.Notes)
    }

    when (val currentScreen = customScreen.value) {
        CustomScreen.CreateNote -> {
            CreateNoteScreen(
                onFinished = {
                    // при завершении создания заметки - открыть главный экран
                    customScreen.value = CustomScreen.Notes
                }
            )
        }

        is CustomScreen.EditNote -> {
            EditNoteScreen(
                onFinished = {
                    // при завершении редактирования - открыть главный экран
                    customScreen.value = CustomScreen.Notes
                },
                noteId = currentScreen.noteId // передаем id заметки из состояния (smart cast)
            )
        }

        CustomScreen.Notes -> {
            NotesScreen(
                onNoteClick = { note ->
                    // при нажатии на заметку - открыть экран редактирования
                    customScreen.value = CustomScreen.EditNote(note.id)
                },
                onAddNoteClick = {
                    // при нажатии на кнопку добавления заметки - открыть экран добавления
                    customScreen.value = CustomScreen.CreateNote
                }
            )
        }
    }
}