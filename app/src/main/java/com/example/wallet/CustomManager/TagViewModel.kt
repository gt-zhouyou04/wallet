package com.example.wallet.CustomManager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TagViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TagRepository
    val allTags: LiveData<List<Tag>>

    init {
        val tagDao = AppDatabase.getDatabase(application).tagDao()
        repository = TagRepository(tagDao)
        allTags = repository.allTags
    }

    fun insert(tag: Tag) = viewModelScope.launch {
        repository.insert(tag)
    }

    fun delete(id: Int) = viewModelScope.launch {
        repository.delete(id)
    }
}