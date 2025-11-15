package com.dyusov.notes.presentation.screens.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState() // делегат
    // androidx.compose.runtime.getValue

    val currentState = state

    Column(
        modifier = Modifier
            .padding(top = 40.dp)
            .verticalScroll(rememberScrollState()), // запоминание состояния скролла
        // под капотом функция rememberSavable, которая сохраняет значение не только при
        // рекомпозиции @Composable функции, но и при пересоздании Activity
        // обычная функция remember {} - только при рекомпозиции
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        currentState.otherNotes.forEach { note ->
            Text(
                modifier = Modifier.clickable {
                    viewModel.processCommand(NotesCommand.EditNote(note))
                },
                text = "${note.title} - ${note.content}",
                fontSize = 24.sp
            )
        }
    }
}