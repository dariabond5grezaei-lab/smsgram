package com.example.domain.rules

import android.util.Log
import androidx.room.withTransaction
import com.example.SmsGramApp
import com.example.data.local.ConversationMetadataEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ApplyResult(
    val processed: Int,
    val changed: Int,
    val failed: Boolean = false,
    val errorMessage: String? = null
)

object RuleApplier {
    private const val TAG = "RuleApplier"

    /**
     * Applies enabled rules to older conversations.
     * Conversations with an existing customFolderId are skipped.
     * Evaluates in batches with suspend calls and withTransaction on Dispatchers.IO.
     */
    suspend fun applyRules(app: SmsGramApp): ApplyResult = withContext(Dispatchers.IO) {
        var processedCount = 0
        var changedCount = 0

        try {
            val db = app.database
            val repo = app.smsRepository
            val enabledRules = db.senderRuleDao().getEnabledRules()
            if (enabledRules.isEmpty()) {
                Log.i(TAG, "No enabled rules found. Skipping applyRules.")
                return@withContext ApplyResult(0, 0, failed = false)
            }

            val conversations = repo.getConversations(forceRefresh = true)
            val metadataList = db.conversationMetadataDao().getAllMetadataList()
            val metadataMap = metadataList.associateBy { it.threadId }.toMutableMap()

            // Skip conversations whose metadata customFolderId is not null
            val candidates = conversations.filter { conv ->
                val meta = metadataMap[conv.threadId]
                meta?.customFolderId == null
            }

            // 1. Read recent message bodies and compute matches in plain code BEFORE transaction
            val updatesToApply = mutableListOf<ConversationMetadataEntity>()

            for (conv in candidates) {
                processedCount++
                val isContact = repo.getContactInfo(conv.address).first != null

                // Suspend call on IO
                val recentBodies = repo.getRecentMessageBodies(conv.threadId, 20)

                val combinedBody = if (recentBodies.isNotEmpty()) {
                    recentBodies.joinToString("\n")
                } else {
                    conv.snippet
                }

                val matchedFolder = RuleEngine.firstMatchingFolder(
                    rules = enabledRules,
                    sender = conv.address,
                    body = combinedBody,
                    isContact = isContact
                )

                if (matchedFolder != null) {
                    val existingMeta = metadataMap[conv.threadId]
                    val updatedMeta = if (existingMeta != null) {
                        existingMeta.copy(customFolderId = matchedFolder)
                    } else {
                        ConversationMetadataEntity(
                            threadId = conv.threadId,
                            address = conv.address,
                            customFolderId = matchedFolder,
                            isArchived = false
                        )
                    }
                    metadataMap[conv.threadId] = updatedMeta
                    updatesToApply.add(updatedMeta)
                    changedCount++
                }
            }

            // 2. Write results in batches of 100 with Room withTransaction
            val batchSize = 100
            for (chunk in updatesToApply.chunked(batchSize)) {
                db.withTransaction {
                    for (meta in chunk) {
                        db.conversationMetadataDao().saveMetadata(meta)
                    }
                }
                Log.i(TAG, "Batch saved. Current changed: $changedCount")
            }

            repo.invalidateConversationsCache()
            Log.i(TAG, "applyRules completed: processed=$processedCount, changed=$changedCount")
            ApplyResult(processed = processedCount, changed = changedCount, failed = false)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply rules to older messages", e)
            ApplyResult(
                processed = processedCount,
                changed = changedCount,
                failed = true,
                errorMessage = e.message
            )
        }
    }
}
