package com.example.wallet.PayInfo.RecordManager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.wallet.CustomManager.AppDatabase
import com.example.wallet.CustomManager.Record
import com.example.wallet.CustomManager.RecordDao
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RecordRepository(private val recordDao: RecordDao) {
    fun insert(record: Record) {
        GlobalScope.launch {
            recordDao.insert(record)
        }
    }

    fun getRecordsByTime(from: Long, to: Long): LiveData<List<Record>> {
        return recordDao.getRecordsByTime(from, to)
    }

    suspend fun delete(id: Int) {
        recordDao.delete(id)
    }
}

class RecordViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RecordRepository
    private val _records = MutableLiveData<List<Record>>()
    val records: LiveData<List<Record>> get() = _records

    init {
        val recordDao = AppDatabase.getDatabase(application).recordDao()
        repository = RecordRepository(recordDao)
    }

    fun insert(record: Record) = repository.insert(record)

    fun getRecordsByTime(from: Long, to: Long) {
        viewModelScope.launch {
            _records.value = repository.getRecordsByTime(from, to).value
        }
    }

    fun delete(id: Int) = viewModelScope.launch {
        repository.delete(id)
    }
}

