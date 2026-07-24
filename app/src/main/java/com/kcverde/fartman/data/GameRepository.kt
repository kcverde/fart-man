package com.kcverde.fartman.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {
    val history: Flow<List<GameRecord>> = gameDao.getAllHistory()

    suspend fun insert(record: GameRecord) {
        gameDao.insertRecord(record)
    }

    suspend fun clearAll() {
        gameDao.deleteAllHistory()
    }
}
