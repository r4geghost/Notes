package com.dyusov.notes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Room создаст за нас БД, поэтому класс абстрактный
@Database(
    entities = [NoteDbModel::class, ContentItemDbModel::class], // таблицы в БД, требуется список Kotlin классов
    version = 3,
    exportSchema = false // не нужна история версий БД (false)
)
abstract class NotesDatabase : RoomDatabase() {

    abstract fun notesDao(): NotesDao

    companion object {
        private val LOCK = Any()
        private var instance: NotesDatabase? = null

        fun getInstance(context: Context): NotesDatabase {
            instance?.let { return it }

            synchronized(LOCK) {
                instance?.let { return it }
                return Room.databaseBuilder(
                    context = context,
                    klass = NotesDatabase::class.java, // здесь требуется Java класс
                    name = "notes.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                    .also {
                        instance = it // кладем созданный экземпляр БД в переменную instance
                    }
            }
        }
    }
}