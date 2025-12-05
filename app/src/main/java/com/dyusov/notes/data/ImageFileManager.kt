package com.dyusov.notes.data

import android.content.Context
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

class ImageFileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val imagesDir: File = context.filesDir // папка, куда сохраняются изображения

    suspend fun copyImageToInternStorage(url: String): String {
        val fileName = "IMG_${UUID.randomUUID()}.jpg" // генерируем имя файла
        val file = File(imagesDir, fileName)

        // переключаем контекст на Dispatchers.IO
        withContext(Dispatchers.IO) {
            // создаем поток ввода через функцию Use (под капотом try/catch/finally
            context.contentResolver.openInputStream(url.toUri()).use { inputStream ->
                // создаем поток вывода через функцию Use
                file.outputStream().use { outputStream ->
                    // записываем данные из потока ввода в поток вывода
                    inputStream?.copyTo(outputStream)
                }
            }
        }

        return file.absolutePath
    }

    suspend fun deleteImage(url: String) {
        withContext(Dispatchers.IO) {
            val file = File(url)
            // удаляем файл, если он существует и он внутренний
            if (file.exists() && isFileInternal(url)) {
                file.delete()
            }
        }
    }

    fun isFileInternal(url: String): Boolean {
        // файл внутренний, если его путь начинается с адреса внутреннего хранилища
        return url.startsWith(imagesDir.absolutePath)
    }
}