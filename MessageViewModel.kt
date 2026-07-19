package com.msgsafe.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgsafe.app.data.AppDatabase
import com.msgsafe.app.data.MessageEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.app.Application
import androidx.lifecycle.AndroidViewModel

class MessageViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).messageDao()

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    init {
        viewModelScope.launch {
            dao.getAll().collectLatest { _messages.value = it }
        }
    }

    fun onSearchChanged(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.isBlank()) {
                dao.getAll().collectLatest { _messages.value = it }
            } else {
                dao.search(query).collectLatest { _messages.value = it }
            }
        }
    }
}
