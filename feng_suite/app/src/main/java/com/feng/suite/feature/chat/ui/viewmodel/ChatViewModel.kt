package com.feng.suite.feature.chat.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.feng.suite.core.mqtt.MqttManager
import com.feng.suite.feature.chat.data.ChatDatabase
import com.feng.suite.feature.chat.data.ChatMessageEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = ChatDatabase.getInstance(application).chatMessageDao()

    val latestMessage: StateFlow<ChatMessageEntity?> = dao.getLatestMessage().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val messages: StateFlow<List<ChatMessageEntity>> = dao.getAllMessagesAsc().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun sendMessage(content: String, target: String = "全部设备") {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return

        val myDevice = MqttManager.config.value.deviceName
        val entity = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            sender = myDevice,
            target = target,
            content = trimmed,
            timestamp = System.currentTimeMillis(),
            isFromMe = true
        )

        viewModelScope.launch {
            dao.insertMessage(entity)
            MqttManager.publishChatMessage(entity)
        }
    }

    fun deleteMessage(id: String) {
        viewModelScope.launch {
            dao.deleteMessage(id)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            dao.clearAll()
        }
    }
}
