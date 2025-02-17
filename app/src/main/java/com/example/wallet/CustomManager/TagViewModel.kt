package com.example.wallet.CustomManager

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
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

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }

    fun getAllTags() = viewModelScope.launch {
        repository.getAllTags()
    }

    fun addDefaultTags() = viewModelScope.launch {
        repository.addDefaultTags()
    }

    fun getTagNameById(tagId: Int): LiveData<String?> {
        val tagNameLiveData = MutableLiveData<String?>()

        // 观察 allTags 的变化，并更新 tagNameLiveData
        allTags.observeForever { tags ->
            val tagName = tags.firstOrNull { it.id == tagId }?.name
            tagNameLiveData.value = tagName
        }

        return tagNameLiveData
    }

    fun logTags(tags: List<Tag>) {
        tags.forEach { tag ->
            Log.d("TagViewModel", "Tag: ${tag.id}, Name: ${tag.name}")
        }
    }
}
