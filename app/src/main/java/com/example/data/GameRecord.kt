package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_records")
data class GameRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val creatorName: String,
    val guesserName: String,
    val secretWord: String,
    val isWin: Boolean,
    val incorrectGuesses: Int,
    val hintString: String,
    val timestamp: Long = System.currentTimeMillis()
)
