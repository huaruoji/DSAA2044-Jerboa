package com.jerboa.db.repository

import androidx.annotation.WorkerThread
import com.jerboa.db.dao.SearchHistoryDao
import com.jerboa.db.entity.SearchHistory

class SearchHistoryRepository(
    private val searchHistoryDao: SearchHistoryDao,
) {
    val recentSearches = searchHistoryDao.getRecentSearches()

    @WorkerThread
    suspend fun insertTerm(term: String) {
        searchHistoryDao.insertSearch(
            SearchHistory(searchTerm = term, timestamp = System.currentTimeMillis()),
        )
    }

    @WorkerThread
    suspend fun clearAll() {
        searchHistoryDao.clearAll()
    }
}


