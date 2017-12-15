package org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.handlers

import com.vk.api.sdk.streaming.objects.StreamingCallbackMessage
import org.apache.logging.log4j.LogManager
import org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.beautifiers.MsgBeautifier

class LogHandler : VkMsgHandler {
    private val logger = LogManager.getLogger()

    override fun handle(message: StreamingCallbackMessage,
                        hashedTags: HashedTagToContentendIdWithTag,
                        msgBeautifier: MsgBeautifier) {
        val tags: List<String> = message.event.tags.filter { hashedTags.containsKey(it) }
        logger.info("vkEvent{tag: ${tags.map { hashedTags.get(it)!!.second }}, " +
                "eventUrl: ${message.event.eventUrl}, " +
                "text: ${message.event.text}}")
    }
}