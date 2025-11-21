@file:OptIn(ExperimentalMaterial3Api::class)

package com.dyusov.notes.presentation.screens.creation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dyusov.notes.presentation.utils.DateFormatter

@Composable
fun CreateNoteScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateNoteViewModel = viewModel(),
    onFinished: () -> Unit
) {

    val state by viewModel.state.collectAsState() // используем делегат

    when (val currentState = state) {
        is CreateNoteState.Creation -> {
            // У Scaffold есть slot (место) под top bar с кнопками навигации и заголовком,
            // поэтому лучше использовать его, чем самому располагать элементы
            Scaffold(
                modifier = modifier,
                // верхняя строка экрана
                topBar = {
                    TopAppBar(
                        // заголовок
                        title = {
                            Text(
                                text = "Create note",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        // кнопка "назад"
                        navigationIcon = {
                            Icon(
                                modifier = Modifier
                                    .padding(start = 16.dp, end = 8.dp)
                                    .clickable {
                                        viewModel.processCommand(CreateNoteCommand.Back)
                                    },
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack, // стрелка назад
                                contentDescription = "Back to main screen",
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    // заголовок заметки
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        value = currentState.title,
                        onValueChange = { title ->
                            viewModel.processCommand(CreateNoteCommand.InputTitle(title))
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        textStyle = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        placeholder = {
                            Text(
                                text = "Title",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                                    .copy(alpha = 0.2f), // изменяем прозрачность

                            )
                        }
                    )
                    // текущая дата
                    Text(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = DateFormatter.formatCurrentDate(),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // контент заметки
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .weight(1f),
                        value = currentState.content,
                        onValueChange = { content ->
                            viewModel.processCommand(CreateNoteCommand.InputContent(content = content))
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        placeholder = {
                            Text(
                                text = "Note something down...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                                    .copy(alpha = 0.2f), // изменяем прозрачность, ставим 20%

                            )
                        }
                    )
                    // кнопка "сохранить"
                    Button(
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.padding(24.dp),
                        onClick = {
                            viewModel.processCommand(CreateNoteCommand.Save)
                        },
                        enabled = currentState.isSavedEnabled, // активна или нет
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary
                                .copy(alpha = 0.1f), // прозрачность 10%
                            contentColor = MaterialTheme.colorScheme.onPrimary, // цвет текста кнопки
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary,
                        )
                    ) {
                        Text(
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            text = "Save note",
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        CreateNoteState.Finished -> {
        /*
            @Composable функции должны выполнять только отрисовку и реагировать на действия
            пользователя. Поэтому в случае, когда необходимо выполнить какие-то действия за
            пределами ответственности @Composable функции, нужно использовать функцию SideEffect()

            Она в качестве параметра принимает callback, но без аннотации @Composable!
            Callback будет вызываться каждый раз при рекомпозиции
            Если нужно вызвать callback только один раз при первой отрисовке,
            используется функция LaunchedEffect().

            Ей нужно передать key (ключ) - при рекомпозиции будет проверка ключа на соответствие
            - в случае изменения будет вызван callback
        */
            LaunchedEffect(key1 = Unit) {
                onFinished()
            }
        }
    }
}