package org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.eventhandlers

import com.vk.api.sdk.streaming.objects.StreamingCallbackMessage

class LogHandler : VkMsgHandler {
    override fun handle(message: StreamingCallbackMessage,
                        hashedTags: HashedTagToContentendIdWithTag) {
        println("vk event")
        val msgTags = message.event.tags.filter { hashedTags.containsKey(it) }
        msgTags.forEach { tagHash ->
            println("""
                    |tag: ${hashedTags.get(tagHash)!!.second}
                    |eventUrl: ${message.event.eventUrl}
                    |text: ${message.event.text}
                    |""".trimMargin())
        }
    }
}