package org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.eventhandlers

import com.vk.api.sdk.streaming.objects.StreamingCallbackMessage
import org.mentionbattle.snadapter.api.core.eventsystem.Event
import org.mentionbattle.snadapter.impl.eventsystem.MentionEvent
import org.mentionbattle.snadapter.impl.eventsystem.PrimitiveEventQueue
import org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.objects.VkAccount
import java.util.*

class EventQueueHandler(val eventQueue: PrimitiveEventQueue) : VkMsgHandler {
    override fun handle(message: StreamingCallbackMessage,
                        hashedTags: HashedTagToContentendIdWithTag) {
        val contanders = MutableList(2, { false })
        val msgTags = message.event.tags.filter { hashedTags.containsKey(it) }
        msgTags.forEach { tagHash ->
            run { contanders[hashedTags.get(tagHash)!!.first - 1] = true }
        }

        for ((id, shouldSend) in contanders.withIndex()) {
            if (shouldSend) eventQueue.addEvent(buildMentionEvent(id - 1, message))
        }
    }

    private fun buildMentionEvent(contenderId: Int, message: StreamingCallbackMessage): Event {
        val vkAccount = VkAccount(message.event.author.id)
        return MentionEvent(
                contenderId,
                "vk",
                message.event.eventUrl,
                vkAccount.name,
                message.event.text,
                vkAccount.avatarUrl,
                Date(message.event.creationTime.toLong()))
    }
}