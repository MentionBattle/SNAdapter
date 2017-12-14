package org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.eventhandlers

import com.vk.api.sdk.streaming.objects.StreamingCallbackMessage
import org.apache.logging.log4j.LogManager

class LogHandler : VkMsgHandler {
    private val logger = LogManager.getLogger()

    override fun handle(message: StreamingCallbackMessage,
                        hashedTags: HashedTagToContentendIdWithTag) {
        val tags: List<String> = message.event.tags.filter { hashedTags.containsKey(it) }
        logger.info("vkEvent{tag: ${tags.map { hashedTags.get(it)!!.second }}, " +
                "eventUrl: ${message.event.eventUrl}, " +
                "text: ${message.event.text}}")
    }
}