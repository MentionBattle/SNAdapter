package org.mentionbattle.snadapter.impl.socialnetworks.handlers

import com.vk.api.sdk.streaming.objects.StreamingCallbackMessage
import com.vk.api.sdk.streaming.objects.StreamingRule

typealias VkEventHandler = (StreamingCallbackMessage) -> Unit

interface VkStreamingService {
    fun addHandler(handlerName: String, handler: VkEventHandler)

    fun getAllRules(): List<StreamingRule>

    fun deleteAllRules()

    fun addNewRule(value: String): Boolean

    fun deleteRule(tag: String): Boolean

    fun startListenEvents()
}