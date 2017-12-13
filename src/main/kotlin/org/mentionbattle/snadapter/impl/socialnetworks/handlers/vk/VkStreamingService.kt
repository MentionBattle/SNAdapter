package org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk

import com.vk.api.sdk.streaming.objects.StreamingRule
import org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.eventhandlers.VkEventHandler
import org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.objects.RuleInfo

interface VkStreamingService {
    fun addHandler(handler: VkEventHandler)

    fun getAllRules(): List<StreamingRule>

    fun deleteAllRules()

    fun addNewRule(ruleInfo: RuleInfo): Boolean

    fun deleteRule(tag: String): Boolean

    fun startListenEvents()
}