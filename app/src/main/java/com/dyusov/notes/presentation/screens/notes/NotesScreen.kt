package com.dyusov.notes.presentation.screens.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dyusov.notes.domain.Note
import com.dyusov.notes.presentation.ui.theme.Green
import com.dyusov.notes.presentation.ui.theme.Yellow200

@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState() // делегат
    // androidx.compose.runtime.getValue

    // Создаем ленивый список (важно не использовать vertical/horizontal scroll!)
    LazyColumn(
        modifier = modifier
            .padding(top = 40.dp),
        // под капотом функция rememberSavable, которая сохраняет значение не только при
        // рекомпозиции @Composable функции, но и при пересоздании Activity
        // обычная функция remember {} - только при рекомпозиции
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // каждый элемент ленивого списка должен быть вызван внутри функции item
        item {
            Title(text = "All Notes")
        }

        item {
            SearchBar(
                query = state.query,
            ) {
                viewModel.processCommand(NotesCommand.InputSearchQuery(it))
            }
        }

        item {
            Subtitle(text = "Pinned")
        }

        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.pinnedNotes.forEach { pinnedNote ->
                    // передаем ключ для связи item и заметки
                    item(key = pinnedNote.id) {
                        NoteCard(
                            note = pinnedNote,
                            onNoteClick = {
                                viewModel.processCommand(
                                    NotesCommand.EditNote(pinnedNote)
                                )
                            },
                            onLongNoteClick = {
                                viewModel.processCommand(
                                    NotesCommand.SwitchPinnedStatus(pinnedNote.id)
                                )
                            },
                            onDoubleNoteClick = {
                                viewModel.processCommand(
                                    NotesCommand.DeleteNote(pinnedNote.id)
                                )
                            },
                            backgroundColor = Yellow200 // цвет заметки
                        )
                    }
                }
            }
        }

        item {
            Subtitle(text = "Others")
        }

        // также можно использовать функцию items для передачи всей коллекции
        items(
            items = state.otherNotes,
            /*
                функция, которая в качестве параметра принимает заметку
                и возвращает ключ в зависимости от этой заметки
            */
            key = { it.id }
        ) { otherNote ->
            NoteCard(
                note = otherNote,
                onNoteClick = {
                    viewModel.processCommand(
                        NotesCommand.EditNote(otherNote)
                    )
                },
                onLongNoteClick = {
                    viewModel.processCommand(
                        NotesCommand.SwitchPinnedStatus(otherNote.id)
                    )
                },
                onDoubleNoteClick = {
                    viewModel.processCommand(
                        NotesCommand.DeleteNote(otherNote.id)
                    )
                },
                backgroundColor = Green // цвет заметки
            )
        }
    }
}

@Composable
private fun Title(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        modifier = modifier,
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun SearchBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        modifier = modifier.fillMaxWidth(),
        value = query,
        onValueChange = onQueryChange, // можно и { onQueryChange(it) }
        // placeholder: @Composable (() -> Unit)? -> подход Slot API
        placeholder = {
            Text(
                text = "Search...",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        // аналогично для иконки поиска
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search, // иконка поиска по умолчанию
                contentDescription = "Search notes" // обязательный параметр описания элемента
            )
        },
        // радиус скругления
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
private fun Subtitle(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        modifier = modifier,
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun NoteCard(
    modifier: Modifier = Modifier,
    note: Note,
    backgroundColor: Color,
    // используем callback
    onNoteClick: (Note) -> Unit,
    onLongNoteClick: (Note) -> Unit,
    onDoubleNoteClick: (Note) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            // для реагирования на разные виды нажатий/кликов
            .combinedClickable(
                onClick = {
                    onNoteClick(note)
                },
                onLongClick = {
                    onLongNoteClick(note)
                },
                onDoubleClick = {
                    onDoubleNoteClick(note)
                }
            )
    ) {
        // Заголовок заметки
        Text(
            text = note.title,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        // Время последнего изменения заметки
        Text(
            text = note.updatedAt.toString(),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // Контент заметки
        Text(
            text = note.content,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}