package com.example.wallet.CustomManager

import androidx.lifecycle.LiveData

class TagRepository(private val tagDao: TagDao) {
    val allTags: LiveData<List<Tag>> = tagDao.getAllTags()

    suspend fun insert(tag: Tag) {
        tagDao.insert(tag)
    }

    suspend fun delete(id: Int) {
        tagDao.delete(id)
    }
}