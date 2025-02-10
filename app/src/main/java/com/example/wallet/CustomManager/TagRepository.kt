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

    suspend fun deleteAll() {
        tagDao.deleteAll()
    }

    suspend fun getAllTags() {
        tagDao.getAllTags()
    }

    suspend fun addDefaultTags() {
        tagDao.insert(Tag(name = "衣服"))
        tagDao.insert(Tag(name = "食物"))
        tagDao.insert(Tag(name = "交通"))
        tagDao.insert(Tag(name = "住房"))
    }
}