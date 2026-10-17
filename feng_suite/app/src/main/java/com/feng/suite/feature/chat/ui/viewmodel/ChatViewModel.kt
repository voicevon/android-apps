package com.feng.suite.feature.chat.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.feng.suite.core.mqtt.MqttManager
import com.feng.suite.feature.chat.data.ChatDatabase
import com.feng.suite.feature.chat.data.ChatMessageEntity
import com.feng.suite.feature.chat.data.MessageDeliveryStatus
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
            isFromMe = true,
            deliveryStatus = MessageDeliveryStatus.SENDING
        )

        viewModelScope.launch {
            dao.insertMessage(entity)
            val success = MqttManager.publishChatMessage(entity)
            val finalStatus = if (success) MessageDeliveryStatus.SENT else MessageDeliveryStatus.FAILED
            dao.updateDeliveryStatus(entity.id, finalStatus)
        }
    }

    fun retryMessage(id: String) {
        viewModelScope.launch {
            val entity = dao.getMessageById(id) ?: return@launch
            if (!entity.isFromMe) return@launch

            dao.updateDeliveryStatus(id, MessageDeliveryStatus.SENDING)
            val success = MqttManager.publishChatMessage(entity)
            val finalStatus = if (success) MessageDeliveryStatus.SENT else MessageDeliveryStatus.FAILED
            dao.updateDeliveryStatus(id, finalStatus)
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
