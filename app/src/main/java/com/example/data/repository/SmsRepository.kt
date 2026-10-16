package com.example.data.repository

import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.util.Log
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat
import com.example.data.local.AppDatabase
import com.example.data.local.ConversationMetadataEntity
import com.example.data.local.TrashEntity
import com.example.data.model.ConversationItem
import com.example.data.model.DualSimInfo
import com.example.data.model.SmsMessageItem
import com.example.domain.util.SenderNormalizer
import com.example.receiver.SmsStatusReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SmsRepository(
    private val context: Context,
    private val database: AppDatabase
) {

    private val contentResolver = context.contentResolver

    @Volatile
    private var inMemoryContactsCache: Map<String, Pair<String?, String?>>? = null

    @Volatile
    private var inMemoryConversationsCache: List<ConversationItem>? = null

    private val singleContactCache = java.util.Collections.synchronizedMap(
        object : java.util.LinkedHashMap<String, Pair<String?, String?>>(MAX_CONTACT_LRU_SIZE, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Pair<String?, String?>>?): Boolean {
                return size > MAX_CONTACT_LRU_SIZE
            }
        }
    )

    companion object {
        private const val MAX_CONTACT_LRU_SIZE = 500
    }

    fun invalidateContactsCache() {
        inMemoryContactsCache = null
        singleContactCache.clear()
    }

    /**
     * Pre-fetches all contacts in a single ContentResolver query (15ms total).
     * Maps raw numbers, normalized numbers, and last 10 digits to (Name, PhotoUri).
     */
    private fun getAllContactsMap(forceRefresh: Boolean = false): Map<String, Pair<String?, String?>> {
        val cachedMap = inMemoryContactsCache
        if (!forceRefresh && cachedMap != null && cachedMap.isNotEmpty()) {
            return cachedMap
        }

        val map = mutableMapOf<String, Pair<String?, String?>>()
        try {
            val hasContactsPerm = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasContactsPerm) return emptyMap()

            val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
            )

            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                val numberIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val photoIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)

                if (numberIdx != -1 && nameIdx != -1) {
                    while (cursor.moveToNext()) {
                        val rawNumber = cursor.getString(numberIdx) ?: continue
                        val name = cursor.getString(nameIdx)
                        val photo = if (photoIdx != -1) cursor.getString(photoIdx) else null

                        if (!name.isNullOrBlank()) {
                            val pair = Pair(name, photo)
                            map[rawNumber] = pair
                            val norm = SenderNormalizer.normalize(rawNumber)
                            if (norm.isNotBlank()) {
                                map[norm] = pair
                            }
                            if (rawNumber.length >= 10) {
                                map[rawNumber.takeLast(10)] = pair
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        inMemoryContactsCache = map
        return map
    }

    /**
     * Get distinct contacts list on device for Contact Picker / New Message
     */
    fun getDeviceContacts(): List<com.example.data.model.ContactEntry> {
        val list = mutableListOf<com.example.data.model.ContactEntry>()
        val seen = mutableSetOf<String>()
        try {
            val hasContactsPerm = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasContactsPerm) return emptyList()

            val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
            )

            contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} COLLATE LOCALIZED ASC"
            )?.use { cursor ->
                val numberIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val photoIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)

                if (numberIdx != -1 && nameIdx != -1) {
                    while (cursor.moveToNext()) {
                        val rawNumber = cursor.getString(numberIdx) ?: continue
                        val name = cursor.getString(nameIdx) ?: continue
                        val photo = if (photoIdx != -1) cursor.getString(photoIdx) else null

                        val clean = rawNumber.replace(" ", "").replace("-", "")
                        if (clean.isNotBlank() && seen.add(clean)) {
                            list.add(com.example.data.model.ContactEntry(name = name, number = rawNumber, photoUri = photo))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun resolveContactFromMap(
        rawAddress: String,
        contactsMap: Map<String, Pair<String?, String?>>
    ): Pair<String?, String?> {
        if (rawAddress.isBlank()) return Pair(null, null)

        // 1. Check in-memory batch contacts map
        contactsMap[rawAddress]?.let { return it }

        val norm = SenderNormalizer.normalize(rawAddress)
        contactsMap[norm]?.let { return it }

        if (rawAddress.length >= 10) {
            contactsMap[rawAddress.takeLast(10)]?.let { return it }
        }

        // 2. Check single contact cache
        singleContactCache[rawAddress]?.let { return it }

        // 3. Fallback single query (only if not found in batch map)
        val info = getContactInfo(rawAddress)
        singleContactCache[rawAddress] = info
        return info
    }

    /**
     * Check if required SMS permissions are granted
     */
    fun hasSmsPermissions(): Boolean {
        val readSms = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        val sendSms = ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        return readSms && sendSms
    }

    /**
     * Load all conversations grouped by threadId (Optimized batch contact matching)
     */
    suspend fun getConversations(forceRefresh: Boolean = false): List<ConversationItem> = withContext(Dispatchers.IO) {
        if (!hasSmsPermissions()) return@withContext emptyList()

        val cachedList = inMemoryConversationsCache
        if (!forceRefresh && cachedList != null && cachedList.isNotEmpty()) {
            return@withContext cachedList
        }

        val contactsMap = getAllContactsMap(forceRefresh)
        val conversations = LinkedHashMap<Long, ConversationItem>(128)

        // 1. Efficiently query all unread SMS counts in a single fast query
        val unreadCounts = mutableMapOf<Long, Int>()
        try {
            contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                arrayOf(Telephony.Sms.THREAD_ID),
                "read = 0",
                null,
                null
            )?.use { cursor ->
                val threadIdIdx = cursor.getColumnIndex(Telephony.Sms.THREAD_ID)
                if (threadIdIdx != -1) {
                    while (cursor.moveToNext()) {
                        val tId = cursor.getLong(threadIdIdx)
                        if (tId > 0) {
                            unreadCounts[tId] = (unreadCounts[tId] ?: 0) + 1
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Query conversations using the optimized Telephony.Sms.Conversations.CONTENT_URI (1 row per thread)
        var cursor: Cursor? = null
        var fallbackNeeded = false
        try {
            val uri = Telephony.Sms.Conversations.CONTENT_URI
            val projection = arrayOf(
                Telephony.Sms.Conversations.THREAD_ID,
                Telephony.Sms.Conversations.ADDRESS,
                Telephony.Sms.Conversations.BODY,
                Telephony.Sms.Conversations.DATE,
                Telephony.Sms.Conversations.READ,
                Telephony.Sms.Conversations.TYPE,
                Telephony.Sms.Conversations.STATUS
            )

            cursor = contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${Telephony.Sms.Conversations.DATE} DESC"
            )

            if (cursor != null && cursor.moveToFirst()) {
                val threadIdIdx = cursor.getColumnIndex(Telephony.Sms.Conversations.THREAD_ID)
                val addressIdx = cursor.getColumnIndex(Telephony.Sms.Conversations.ADDRESS)
                val bodyIdx = cursor.getColumnIndex(Telephony.Sms.Conversations.BODY)
                val dateIdx = cursor.getColumnIndex(Telephony.Sms.Conversations.DATE)
                val readIdx = cursor.getColumnIndex(Telephony.Sms.Conversations.READ)
                val typeIdx = cursor.getColumnIndex(Telephony.Sms.Conversations.TYPE)
                val statusIdx = cursor.getColumnIndex(Telephony.Sms.Conversations.STATUS)

                do {
                    val threadId = if (threadIdIdx != -1) cursor.getLong(threadIdIdx) else 0L
                    if (threadId <= 0L) continue

                    val rawAddress = if (addressIdx != -1) cursor.getString(addressIdx) ?: "" else ""
                    val body = if (bodyIdx != -1) cursor.getString(bodyIdx) ?: "" else ""
                    val date = if (dateIdx != -1) cursor.getLong(dateIdx) else 0L
                    val read = if (readIdx != -1) cursor.getInt(readIdx) else 1
                    val type = if (typeIdx != -1) cursor.getInt(typeIdx) else 1
                    val status = if (statusIdx != -1) cursor.getInt(statusIdx) else 0

                    val isSentByUser = (type == Telephony.Sms.MESSAGE_TYPE_SENT)
                    val unreadCount = unreadCounts[threadId] ?: 0

                    val contactInfo = resolveContactFromMap(rawAddress, contactsMap)

                    conversations[threadId] = ConversationItem(
                        threadId = threadId,
                        address = rawAddress,
                        contactName = contactInfo.first,
                        photoUri = contactInfo.second,
                        snippet = body,
                        dateEpoch = date,
                        unreadCount = unreadCount,
                        isLastMessageSentByUser = isSentByUser,
                        lastMessageStatus = status,
                        totalMessageCount = 1
                    )
                } while (cursor.moveToNext())
            } else if (cursor == null) {
                fallbackNeeded = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            fallbackNeeded = true
        } finally {
            cursor?.close()
        }

        // 3. Graceful fallback to full-table query if optimized query fails or is empty on some custom Android builds
        if (fallbackNeeded || (conversations.isEmpty() && hasAnySmsOnDevice())) {
            val uri = Telephony.Sms.CONTENT_URI
            val projection = arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.THREAD_ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.READ,
                Telephony.Sms.TYPE,
                Telephony.Sms.STATUS
            )

            try {
                cursor = contentResolver.query(
                    uri,
                    projection,
                    null,
                    null,
                    "${Telephony.Sms.DATE} DESC"
                )

                if (cursor != null && cursor.moveToFirst()) {
                    val threadIdIdx = cursor.getColumnIndex(Telephony.Sms.THREAD_ID)
                    val addressIdx = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
                    val bodyIdx = cursor.getColumnIndex(Telephony.Sms.BODY)
                    val dateIdx = cursor.getColumnIndex(Telephony.Sms.DATE)
                    val readIdx = cursor.getColumnIndex(Telephony.Sms.READ)
                    val typeIdx = cursor.getColumnIndex(Telephony.Sms.TYPE)
                    val statusIdx = cursor.getColumnIndex(Telephony.Sms.STATUS)

                    do {
                        val threadId = if (threadIdIdx != -1) cursor.getLong(threadIdIdx) else 0L
                        if (threadId <= 0L) continue

                        val read = if (readIdx != -1) cursor.getInt(readIdx) else 1
                        val type = if (typeIdx != -1) cursor.getInt(typeIdx) else 1
                        val isSentByUser = (type == Telephony.Sms.MESSAGE_TYPE_SENT)
                        val additionalUnread = if (read == 0 && !isSentByUser) 1 else 0

                        val existing = conversations[threadId]
                        if (existing == null) {
                            val rawAddress = if (addressIdx != -1) cursor.getString(addressIdx) ?: "" else ""
                            val body = if (bodyIdx != -1) cursor.getString(bodyIdx) ?: "" else ""
                            val date = if (dateIdx != -1) cursor.getLong(dateIdx) else 0L
                            val status = if (statusIdx != -1) cursor.getInt(statusIdx) else 0
                            val contactInfo = resolveContactFromMap(rawAddress, contactsMap)

                            conversations[threadId] = ConversationItem(
                                threadId = threadId,
                                address = rawAddress,
                                contactName = contactInfo.first,
                                photoUri = contactInfo.second,
                                snippet = body,
                                dateEpoch = date,
                                unreadCount = additionalUnread,
                                isLastMessageSentByUser = isSentByUser,
                                lastMessageStatus = status,
                                totalMessageCount = 1
                            )
                        } else {
                            conversations[threadId] = existing.copy(
                                unreadCount = existing.unreadCount + additionalUnread,
                                totalMessageCount = existing.totalMessageCount + 1
                            )
                        }
                    } while (cursor.moveToNext())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
        }

        val result = ArrayList(conversations.values)
        inMemoryConversationsCache = result
        result
    }

    /**
     * Invalidate cached conversations so next load reads fresh data
     */
    fun invalidateConversationsCache() {
        inMemoryConversationsCache = null
    }

    private fun hasAnySmsOnDevice(): Boolean {
        return try {
            contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                arrayOf(Telephony.Sms._ID),
                null,
                null,
                "${Telephony.Sms._ID} LIMIT 1"
            )?.use { it.moveToFirst() } ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get the last [limit] message bodies for a specific conversation thread without loading the whole thread.
     */
    suspend fun getRecentMessageBodies(threadId: Long, limit: Int = 20): List<String> = withContext(Dispatchers.IO) {
        if (!hasSmsPermissions() || threadId <= 0L) return@withContext emptyList()
        val bodies = ArrayList<String>(limit)
        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                arrayOf(Telephony.Sms.BODY),
                "${Telephony.Sms.THREAD_ID} = ?",
                arrayOf(threadId.toString()),
                "${Telephony.Sms.DATE} DESC LIMIT $limit"
            )
            if (cursor != null && cursor.moveToFirst()) {
                val bodyIdx = cursor.getColumnIndex(Telephony.Sms.BODY)
                do {
                    val body = if (bodyIdx != -1) cursor.getString(bodyIdx) ?: "" else ""
                    if (body.isNotEmpty()) {
                        bodies.add(body)
                    }
                } while (cursor.moveToNext() && bodies.size < limit)
            }
        } catch (e: Exception) {
            android.util.Log.e("SmsRepository", "Error getting recent message bodies for thread $threadId", e)
        } finally {
            cursor?.close()
        }
        bodies
    }

    /**
     * Get all messages for a specific conversation thread
     */
    suspend fun getMessagesForThread(threadId: Long): List<SmsMessageItem> = withContext(Dispatchers.IO) {
        if (!hasSmsPermissions() || threadId <= 0L) return@withContext emptyList()

        val list = ArrayList<SmsMessageItem>(128)
        val uri = Telephony.Sms.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.READ,
            Telephony.Sms.TYPE,
            Telephony.Sms.STATUS,
            Telephony.Sms.SUBSCRIPTION_ID
        )

        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                uri,
                projection,
                "${Telephony.Sms.THREAD_ID} = ?",
                arrayOf(threadId.toString()),
                "${Telephony.Sms.DATE} ASC"
            )

            if (cursor != null && cursor.moveToFirst()) {
                val idIdx = cursor.getColumnIndex(Telephony.Sms._ID)
                val addressIdx = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = cursor.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = cursor.getColumnIndex(Telephony.Sms.DATE)
                val readIdx = cursor.getColumnIndex(Telephony.Sms.READ)
                val typeIdx = cursor.getColumnIndex(Telephony.Sms.TYPE)
                val statusIdx = cursor.getColumnIndex(Telephony.Sms.STATUS)
                val subIdIdx = cursor.getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID)

                do {
                    val id = if (idIdx != -1) cursor.getLong(idIdx) else 0L
                    val address = if (addressIdx != -1) cursor.getString(addressIdx) ?: "" else ""
                    val body = if (bodyIdx != -1) cursor.getString(bodyIdx) ?: "" else ""
                    val date = if (dateIdx != -1) cursor.getLong(dateIdx) else 0L
                    val read = if (readIdx != -1) cursor.getInt(readIdx) == 1 else true
                    val type = if (typeIdx != -1) cursor.getInt(typeIdx) else 1
                    val status = if (statusIdx != -1) cursor.getInt(statusIdx) else 0
                    val subId = if (subIdIdx != -1) cursor.getInt(subIdIdx) else -1

                    val isSent = (type == Telephony.Sms.MESSAGE_TYPE_SENT)
                    val effectiveStatus = if (isSent && status == 32 && (System.currentTimeMillis() - date > 60_000L)) {
                        64
                    } else {
                        status
                    }

                    list.add(
                        SmsMessageItem(
                            id = id,
                            threadId = threadId,
                            address = address,
                            body = body,
                            dateEpoch = date,
                            isSentByUser = isSent,
                            isRead = read,
                            status = effectiveStatus,
                            subId = subId
                        )
                    )
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        list
    }

    /**
     * Mark all messages in a conversation thread as read
     */
    suspend fun markThreadAsRead(threadId: Long) = withContext(Dispatchers.IO) {
        if (!hasSmsPermissions()) return@withContext
        try {
            val values = ContentValues().apply {
                put(Telephony.Sms.READ, 1)
                put(Telephony.Sms.SEEN, 1)
            }
            contentResolver.update(
                Telephony.Sms.CONTENT_URI,
                values,
                "${Telephony.Sms.THREAD_ID} = ? AND ${Telephony.Sms.READ} = 0",
                arrayOf(threadId.toString())
            )
            // Hotspot #6 Fix: Update in-memory cached thread read state in-place instead of invalidating the entire cache
            updateThreadReadLocally(threadId, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Updates an individual thread unread count in the in-memory cache without discarding all items
     */
    fun updateThreadReadLocally(threadId: Long, unreadCount: Int) {
        val current = inMemoryConversationsCache ?: return
        inMemoryConversationsCache = current.map { conv ->
            if (conv.threadId == threadId) conv.copy(unreadCount = unreadCount) else conv
        }
    }

    /**
     * Sends an SMS message directly using Android Telephony SmsManager
     */
    suspend fun sendSms(
        recipient: String,
        body: String,
        subId: Int = -1,
        deliveryReport: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        if (!hasSmsPermissions() || recipient.isBlank() || body.isBlank()) return@withContext false

        var messageUri: Uri? = null
        try {
            val smsManager: SmsManager = if (subId != -1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java).createForSubscriptionId(subId)
            } else if (subId != -1) {
                @Suppress("DEPRECATION")
                SmsManager.getSmsManagerForSubscriptionId(subId)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            // Write sent message into system SMS provider first to get URI/ID for tracking
            messageUri = insertSentMessage(recipient, body, subId)
            val messageId = messageUri?.lastPathSegment?.toLongOrNull() ?: -1L

            val parts = smsManager.divideMessage(body)
            val sentIntents = ArrayList<PendingIntent>()
            val deliveryIntents = ArrayList<PendingIntent>()

            for (i in 0 until parts.size) {
                val sentIntent = Intent(context, SmsStatusReceiver::class.java).apply {
                    action = SmsStatusReceiver.ACTION_SMS_SENT
                    putExtra(SmsStatusReceiver.EXTRA_RECIPIENT, recipient)
                    putExtra(SmsStatusReceiver.EXTRA_PART_INDEX, i)
                    putExtra(SmsStatusReceiver.EXTRA_PART_COUNT, parts.size)
                    if (messageUri != null) {
                        putExtra(SmsStatusReceiver.EXTRA_MESSAGE_URI, messageUri.toString())
                    }
                    if (messageId != -1L) {
                        putExtra(SmsStatusReceiver.EXTRA_MESSAGE_ID, messageId)
                    }
                }
                val sentPI = PendingIntent.getBroadcast(
                    context,
                    System.currentTimeMillis().toInt() + i,
                    sentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                sentIntents.add(sentPI)

                if (deliveryReport) {
                    val deliveryIntent = Intent(context, SmsStatusReceiver::class.java).apply {
                        action = SmsStatusReceiver.ACTION_SMS_DELIVERED
                        putExtra(SmsStatusReceiver.EXTRA_RECIPIENT, recipient)
                        putExtra(SmsStatusReceiver.EXTRA_PART_INDEX, i)
                        putExtra(SmsStatusReceiver.EXTRA_PART_COUNT, parts.size)
                        if (messageUri != null) {
                            putExtra(SmsStatusReceiver.EXTRA_MESSAGE_URI, messageUri.toString())
                        }
                        if (messageId != -1L) {
                            putExtra(SmsStatusReceiver.EXTRA_MESSAGE_ID, messageId)
                        }
                    }
                    val deliveryPI = PendingIntent.getBroadcast(
                        context,
                        System.currentTimeMillis().toInt() + 1000 + i,
                        deliveryIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    deliveryIntents.add(deliveryPI)
                }
            }

            // Send via multi-part
            smsManager.sendMultipartTextMessage(
                recipient,
                null,
                parts,
                sentIntents,
                if (deliveryReport) deliveryIntents else null
            )

            true
        } catch (e: Exception) {
            e.printStackTrace()
            if (messageUri != null) {
                try {
                    val failValues = ContentValues().apply {
                        put(Telephony.Sms.STATUS, Telephony.Sms.STATUS_FAILED)
                        put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_FAILED)
                    }
                    context.contentResolver.update(messageUri, failValues, null, null)
                } catch (_: Exception) {}
            }
            false
        }
    }

    /**
     * Insert sent message into system SMS provider content://sms/sent
     */
    private fun insertSentMessage(address: String, body: String, subId: Int): Uri? {
        return try {
            val values = ContentValues().apply {
                put(Telephony.Sms.ADDRESS, address)
                put(Telephony.Sms.BODY, body)
                put(Telephony.Sms.DATE, System.currentTimeMillis())
                put(Telephony.Sms.READ, 1)
                put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_OUTBOX)
                put(Telephony.Sms.STATUS, Telephony.Sms.STATUS_PENDING)
                if (subId != -1) {
                    put(Telephony.Sms.SUBSCRIPTION_ID, subId)
                }
            }
            contentResolver.insert(Telephony.Sms.Sent.CONTENT_URI, values)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Mark an entire thread as unread
     */
    suspend fun markThreadAsUnread(threadId: Long) = withContext(Dispatchers.IO) {
        try {
            val values = ContentValues().apply {
                put(Telephony.Sms.READ, 0)
            }
            contentResolver.update(
                Telephony.Sms.CONTENT_URI,
                values,
                "${Telephony.Sms.THREAD_ID} = ?",
                arrayOf(threadId.toString())
            )
            invalidateConversationsCache()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun removeSystemSms(id: Long): Int = withContext(Dispatchers.IO) {
        try {
            contentResolver.delete(
                Telephony.Sms.CONTENT_URI,
                "${Telephony.Sms._ID} = ?",
                arrayOf(id.toString())
            )
        } catch (e: Exception) {
            Log.e("SmsRepository", "Error removing system SMS $id", e)
            0
        }
    }

    /**
     * Soft-deletes a message by moving it to the internal Room Trash and removing from system SMS
     */
    suspend fun deleteMessage(message: SmsMessageItem) = withContext(Dispatchers.IO) {
        try {
            // Save to trash
            database.trashDao().insertToTrash(
                TrashEntity(
                    originalSmsId = message.id,
                    threadId = message.threadId,
                    address = message.address,
                    contactName = message.contactName,
                    body = message.body,
                    originalDateEpoch = message.dateEpoch,
                    isSentByUser = message.isSentByUser
                )
            )

            // Delete from system SMS
            contentResolver.delete(
                Telephony.Sms.CONTENT_URI,
                "${Telephony.Sms._ID} = ?",
                arrayOf(message.id.toString())
            )
            invalidateConversationsCache()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Deletes an entire thread
     */
    suspend fun deleteThread(threadId: Long) = withContext(Dispatchers.IO) {
        try {
            val messages = getMessagesForThread(threadId)
            for (msg in messages) {
                database.trashDao().insertToTrash(
                    TrashEntity(
                        originalSmsId = msg.id,
                        threadId = msg.threadId,
                        address = msg.address,
                        body = msg.body,
                        originalDateEpoch = msg.dateEpoch,
                        isSentByUser = msg.isSentByUser
                    )
                )
            }

            contentResolver.delete(
                Telephony.Sms.CONTENT_URI,
                "${Telephony.Sms.THREAD_ID} = ?",
                arrayOf(threadId.toString())
            )
            invalidateConversationsCache()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Restores a message from Trash back to system SMS
     */
    suspend fun restoreFromTrash(trashId: Long) = withContext(Dispatchers.IO) {
        val item = database.trashDao().getTrashById(trashId) ?: return@withContext
        try {
            val values = ContentValues().apply {
                put(Telephony.Sms.ADDRESS, item.address)
                put(Telephony.Sms.BODY, item.body)
                put(Telephony.Sms.DATE, item.originalDateEpoch)
                put(Telephony.Sms.READ, 1)
                put(Telephony.Sms.TYPE, if (item.isSentByUser) Telephony.Sms.MESSAGE_TYPE_SENT else Telephony.Sms.MESSAGE_TYPE_INBOX)
            }
            contentResolver.insert(Telephony.Sms.CONTENT_URI, values)
            database.trashDao().deleteFromTrash(trashId)
            invalidateConversationsCache()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Automatically purge expired OTP messages older than the given hours.
     * Only purges service/commercial senders (not personal numbers starting with 09...),
     * matches with precise regex, and moves to Trash before deleting.
     */
    suspend fun purgeExpiredOtps(olderThanHours: Int) = withContext(Dispatchers.IO) {
        if (olderThanHours <= 0 || !hasSmsPermissions()) return@withContext
        val cutoff = System.currentTimeMillis() - (olderThanHours * 3600 * 1000L)
        val otpRegex = Regex("""(کد|رمز|code|otp).{0,15}\d{4,8}""", RegexOption.IGNORE_CASE)

        try {
            val uri = Telephony.Sms.Inbox.CONTENT_URI
            val projection = arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.THREAD_ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE
            )
            val cursor = contentResolver.query(
                uri,
                projection,
                "${Telephony.Sms.DATE} < ? AND (${Telephony.Sms.BODY} LIKE ? OR ${Telephony.Sms.BODY} LIKE ? OR ${Telephony.Sms.BODY} LIKE ? OR ${Telephony.Sms.BODY} LIKE ?)",
                arrayOf(cutoff.toString(), "%کد%", "%رمز%", "%code%", "%otp%"),
                null
            )

            cursor?.use {
                val idIdx = it.getColumnIndex(Telephony.Sms._ID)
                val threadIdIdx = it.getColumnIndex(Telephony.Sms.THREAD_ID)
                val addressIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)

                while (it.moveToNext()) {
                    val id = if (idIdx != -1) it.getLong(idIdx) else 0L
                    val threadId = if (threadIdIdx != -1) it.getLong(threadIdIdx) else 0L
                    val address = if (addressIdx != -1) it.getString(addressIdx) ?: "" else ""
                    val body = if (bodyIdx != -1) it.getString(bodyIdx) ?: "" else ""
                    val date = if (dateIdx != -1) it.getLong(dateIdx) else 0L

                    // Never delete OTP from personal contacts (09xxxxxxxxx)
                    val norm = SenderNormalizer.normalize(address)
                    val isPersonal = (norm.startsWith("09") && norm.length == 11)
                    if (isPersonal) continue

                    // Check if message matches OTP pattern
                    val isOtp = otpRegex.containsMatchIn(body) ||
                            body.contains("کد تایید") ||
                            body.contains("رمز یکبار مصرف") ||
                            body.contains("رمز پویا") ||
                            body.contains("فعالسازی")

                    if (isOtp) {
                        // 1. Move to Trash first so it's not permanently lost
                        database.trashDao().insertToTrash(
                            TrashEntity(
                                originalSmsId = id,
                                threadId = threadId,
                                address = address,
                                body = body,
                                originalDateEpoch = date,
                                isSentByUser = false
                            )
                        )
                        // 2. Delete from system SMS provider
                        contentResolver.delete(
                            Telephony.Sms.CONTENT_URI,
                            "${Telephony.Sms._ID} = ?",
                            arrayOf(id.toString())
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Purge messages from Trash that have been deleted for more than olderThanDays (default 30 days)
     */
    suspend fun purgeExpiredTrash(olderThanDays: Int = 30) = withContext(Dispatchers.IO) {
        val cutoff = System.currentTimeMillis() - (olderThanDays * 24 * 3600 * 1000L)
        try {
            database.trashDao().deleteExpiredTrash(cutoff)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Get active Dual SIM subscriptions
     */
    fun getActiveSimCards(): List<DualSimInfo> {
        val list = mutableListOf<DualSimInfo>()
        try {
            val hasPhoneState = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPhoneState) {
                list.add(DualSimInfo(0, -1, "سیم‌کارت پیش‌فرض", "سیم ۱"))
                return list
            }

            val subManager = context.getSystemService(SubscriptionManager::class.java)
            val subList: List<SubscriptionInfo>? = subManager?.activeSubscriptionInfoList
            if (!subList.isNullOrEmpty()) {
                for (sub in subList) {
                    list.add(
                        DualSimInfo(
                            slotIndex = sub.simSlotIndex,
                            subscriptionId = sub.subscriptionId,
                            displayName = sub.displayName?.toString() ?: "سیم‌کارت ${sub.simSlotIndex + 1}",
                            carrierName = sub.carrierName?.toString() ?: "اپراتور",
                            iccId = sub.iccId ?: ""
                        )
                    )
                }
                list.sortBy { it.slotIndex }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (list.isEmpty()) {
            list.add(DualSimInfo(0, -1, "سیم‌کارت پیش‌فرض", "سیم ۱"))
        }
        return list
    }

    /**
     * Query contact name & avatar photo URI by phone number
     */
    fun getContactInfo(phoneNumber: String): Pair<String?, String?> {
        if (phoneNumber.isBlank()) return Pair(null, null)
        try {
            val hasContactsPerm = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasContactsPerm) return Pair(null, null)

            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )
            val projection = arrayOf(
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI
            )

            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val name = cursor.getString(0)
                    val photo = cursor.getString(1)
                    return Pair(name, photo)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Pair(null, null)
    }

    /**
     * Resolve threadId for a recipient phone number
     */
    fun getOrCreateThreadId(recipient: String): Long {
        return try {
            Telephony.Threads.getOrCreateThreadId(context, recipient)
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    // --- Conversation Metadata DAO Wrappers (G1) ---

    suspend fun setPinned(threadId: Long, pinned: Boolean, pinnedAt: Long = if (pinned) System.currentTimeMillis() else 0L) = withContext(Dispatchers.IO) {
        val existing = database.conversationMetadataDao().getMetadata(threadId)
        if (existing != null) {
            database.conversationMetadataDao().setPinned(threadId, pinned, pinnedAt)
        } else {
            database.conversationMetadataDao().saveMetadata(
                ConversationMetadataEntity(threadId = threadId, isPinned = pinned, pinnedAt = pinnedAt)
            )
        }
    }

    suspend fun setMuted(threadId: Long, muted: Boolean, untilEpoch: Long?) = withContext(Dispatchers.IO) {
        val existing = database.conversationMetadataDao().getMetadata(threadId)
        if (existing != null) {
            database.conversationMetadataDao().setMuted(threadId, muted, untilEpoch)
        } else {
            database.conversationMetadataDao().saveMetadata(
                ConversationMetadataEntity(threadId = threadId, isMuted = muted, muteUntilEpoch = untilEpoch)
            )
        }
    }

    suspend fun setArchived(threadId: Long, archived: Boolean) = withContext(Dispatchers.IO) {
        val existing = database.conversationMetadataDao().getMetadata(threadId)
        if (existing != null) {
            database.conversationMetadataDao().setArchived(threadId, archived)
        } else {
            database.conversationMetadataDao().saveMetadata(
                ConversationMetadataEntity(threadId = threadId, isArchived = archived)
            )
        }
    }

    suspend fun setDraft(threadId: Long, text: String?, now: Long = System.currentTimeMillis()) = withContext(Dispatchers.IO) {
        val existing = database.conversationMetadataDao().getMetadata(threadId)
        if (existing != null) {
            database.conversationMetadataDao().setDraft(threadId, text, now)
        } else {
            database.conversationMetadataDao().saveMetadata(
                ConversationMetadataEntity(threadId = threadId, draftText = text, draftUpdatedAt = now)
            )
        }
    }

    fun observeMetadata(threadId: Long): Flow<ConversationMetadataEntity?> =
        database.conversationMetadataDao().observeMetadata(threadId)

    fun observeAllMetadata(): Flow<List<ConversationMetadataEntity>> =
        database.conversationMetadataDao().observeAllMetadata()
}
