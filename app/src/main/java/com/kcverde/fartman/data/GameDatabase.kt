package com.kcverde.fartman.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [GameRecord::class], version = 1, exportSchema = false)
abstract class GameDatabase : RoomDatabase() {
  abstract fun gameDao(): GameDao

  companion object {
    @Volatile private var instance: GameDatabase? = null

    fun getDatabase(context: Context): GameDatabase =
      instance
        ?: synchronized(this) {
          instance
            ?: Room.databaseBuilder(
                context.applicationContext,
                GameDatabase::class.java,
                DATABASE_NAME,
              )
              // Match history is disposable: losing it on a schema change beats
              // shipping a migration for a scoreboard.
              .fallbackToDestructiveMigration(dropAllTables = true)
              .build()
              .also { instance = it }
        }

    private const val DATABASE_NAME = "fartman_database"
  }
}
