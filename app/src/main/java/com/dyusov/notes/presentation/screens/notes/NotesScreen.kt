package com.dyusov.notes.presentation.screens.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.dyusov.notes.R
import com.dyusov.notes.domain.ContentItem
import com.dyusov.notes.domain.Note
import com.dyusov.notes.presentation.ui.theme.OtherNotesColors
import com.dyusov.notes.presentation.ui.theme.PinnedNotesColors
import com.dyusov.notes.presentation.utils.DateFormatter

@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel(),
    onNoteClick: (Note) -> Unit,
    onAddNoteClick: () -> Unit
) {
    val state by viewModel.state.collectAsState() // делегат
    // androidx.compose.runtime.getValue

    // корневой элемент = Scaffold

    Scaffold(
        modifier = modifier,
        // кнопка добавления заметки
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNoteClick,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                // иконка
                content = {
                    Icon(
                        // добавляем локальную иконку
                        painter = painterResource(R.drawable.ic_add_note),
                        contentDescription = "Button add note"
                    )
                }
            )
        }
    ) { innerPadding ->

        // Добавляем текст, когда нет ни одной заметки
        if (state.otherNotes.isEmpty() && state.pinnedNotes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    fontWeight = FontWeight.Normal,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    text = "No notes found"
                )
            }
        }

        // Создаем ленивый список (важно не использовать vertical/horizontal scroll!)
        LazyColumn(
            contentPadding = innerPadding
        ) {
            // каждый элемент ленивого списка должен быть вызван внутри функции item
            item {
                Title(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    text = "All Notes"
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                SearchBar(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    query = state.query,
                ) {
                    viewModel.processCommand(NotesCommand.InputSearchQuery(it))
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (!state.pinnedNotes.isEmpty()) {
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = "Pinned"
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        // отступ пропадает при скролле элементов
                        contentPadding = PaddingValues(horizontal = 24.dp)
                    ) {
                        state.pinnedNotes.forEachIndexed { index, pinnedNote ->
                            // передаем ключ для связи item и заметки
                            item(key = pinnedNote.id) {
                                NoteCard(
                                    // устанавливаем максимальную ширину
                                    // (если места нужно меньше, элемент займет столько, сколько нужно)
                                    modifier = Modifier.widthIn(max = 160.dp),
                                    note = pinnedNote,
                                    // по клику будет переход на другой экран
                                    // за навигацию будет отвечать другая @Composable функция
                                    // поэтому передает callback
                                    onNoteClick = onNoteClick,
                                    onLongNoteClick = {
                                        viewModel.processCommand(
                                            NotesCommand.SwitchPinnedStatus(pinnedNote.id)
                                        )
                                    },
                                    backgroundColor = PinnedNotesColors[index % PinnedNotesColors.size] // цвет заметки
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = "Others"
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // также можно использовать функцию itemsIndexed для передачи всей коллекции + индесы
            itemsIndexed(
                items = state.otherNotes,
                /*
                    функция, которая в качестве параметра принимает заметку
                    и возвращает ключ в зависимости от этой заметки
                */
                key = { _, note -> note.id }
            ) { index, otherNote ->
                val imageUrl = otherNote.content
                    .filterIsInstance<ContentItem.Image>()
                    .map { it.url }
                    .firstOrNull()

                if (imageUrl == null) {
                    NoteCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        note = otherNote,
                        // по клику будет переход на другой экран
                        // за навигацию будет отвечать другая @Composable функция
                        // поэтому передает callback
                        onNoteClick = onNoteClick,
                        onLongNoteClick = {
                            viewModel.processCommand(
                                NotesCommand.SwitchPinnedStatus(otherNote.id)
                            )
                        },
                        backgroundColor = OtherNotesColors[index % OtherNotesColors.size] // цвет заметки
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    NoteCardWithImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        note = otherNote,
                        imageUrl = imageUrl,
                        // по клику будет переход на другой экран
                        // за навигацию будет отвечать другая @Composable функция
                        // поэтому передает callback
                        onNoteClick = onNoteClick,
                        onLongNoteClick = {
                            viewModel.processCommand(
                                NotesCommand.SwitchPinnedStatus(otherNote.id)
                            )
                        },
                        backgroundColor = OtherNotesColors[index % OtherNotesColors.size] // цвет заметки
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }


            }
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
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(10.dp)
            ),
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
                tint = MaterialTheme.colorScheme.onSurface,
                imageVector = Icons.Default.Search, // иконка поиска по умолчанию
                contentDescription = "Search notes" // обязательный параметр описания элемента
            )
        },
        // радиус скругления
        shape = RoundedCornerShape(10.dp),
        // цвета
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent, // убирает полоску у поиска в фокусе
            unfocusedIndicatorColor = Color.Transparent, // убирает полоску у поиска вне фокуса

        )
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
    onLongNoteClick: (Note) -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            // для реагирования на разные виды нажатий/кликов
            .combinedClickable(
                onClick = {
                    onNoteClick(note)
                },
                onLongClick = {
                    onLongNoteClick(note)
                }
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Заголовок заметки
            Text(
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                text = note.title,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            // Отступ между элементами
            Spacer(modifier = Modifier.height(4.dp))

            // Время последнего изменения заметки
            Text(
                text = DateFormatter.formatDateToString(note.updatedAt),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Текстовый контент заметки
            note.content
                .filterIsInstance<ContentItem.Text>()
                .filter { it.content.isNotBlank() }
                .joinToString("\n") { it.content }
                .takeIf { it.isNotEmpty() }
                ?.let {
                    // Отступ между элементами
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        maxLines = 3, // ограничение на кол-во строк
                        overflow = TextOverflow.Ellipsis, // стратегия если текст вылез за пределы контейнера (многоточие)
                        text = it,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
        }
    }
}

@Composable
fun NoteCardWithImage(
    modifier: Modifier = Modifier,
    note: Note,
    imageUrl: String,
    backgroundColor: Color,
    // используем callback
    onNoteClick: (Note) -> Unit,
    onLongNoteClick: (Note) -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            // для реагирования на разные виды нажатий/кликов
            .combinedClickable(
                onClick = {
                    onNoteClick(note)
                },
                onLongClick = {
                    onLongNoteClick(note)
                }
            )
    ) {
        Box() {
            // Картинки в заметке (берем первую)
            AsyncImage(
                modifier = Modifier
                    .heightIn(max = 120.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                model = imageUrl, // ссылка на картинку
                contentDescription = "First image from note",
                contentScale = ContentScale.FillWidth
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                // Заголовок заметки
                Text(
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    text = note.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = LocalTextStyle.current.copy(
                        shadow = Shadow(
                            color = Color.Black, // Color of the shadow
                            blurRadius = 100f    // Blur radius of the shadow
                        )
                    )
                )

                // Время последнего изменения заметки
                Text(
                    text = DateFormatter.formatDateToString(note.updatedAt),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = LocalTextStyle.current.copy(
                        shadow = Shadow(
                            color = Color.Black, // Color of the shadow
                            blurRadius = 100f      // Blur radius of the shadow
                        )
                    )
                )
            }
        }
        // Текстовый контент заметки
        note.content
            .filterIsInstance<ContentItem.Text>()
            .filter { it.content.isNotBlank() }
            .joinToString("\n") { it.content }
            .takeIf { it.isNotEmpty() }
            ?.let {
                Text(
                    modifier = Modifier.padding(16.dp),
                    maxLines = 3, // ограничение на кол-во строк
                    overflow = TextOverflow.Ellipsis, // стратегия если текст вылез за пределы контейнера (многоточие)
                    text = it,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
    }
}