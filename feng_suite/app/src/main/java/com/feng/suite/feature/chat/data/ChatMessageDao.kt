package com.feng.suite.feature.chat.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT 1")
    fun getLatestMessage(): Flow<ChatMessageEntity?>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessagesAsc(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    fun getAllMessagesDesc(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY sender ASC, timestamp ASC")
    fun getAllMessagesBySender(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: String): ChatMessageEntity?

    @Query("SELECT * FROM chat_messages WHERE isFromMe = 1 AND (deliveryStatus = :statusFailed OR deliveryStatus = :statusSending) ORDER BY timestamp ASC")
    suspend fun getUnsentMessages(
        statusFailed: MessageDeliveryStatus = MessageDeliveryStatus.FAILED,
        statusSending: MessageDeliveryStatus = MessageDeliveryStatus.SENDING
    ): List<ChatMessageEntity>

    @Query("UPDATE chat_messages SET deliveryStatus = :status WHERE id = :id")
    suspend fun updateDeliveryStatus(id: String, status: MessageDeliveryStatus)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteMessage(id: String)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAll()
}
