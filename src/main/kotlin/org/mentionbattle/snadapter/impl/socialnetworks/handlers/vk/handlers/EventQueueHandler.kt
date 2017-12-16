package org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.handlers

import com.vk.api.sdk.streaming.objects.StreamingCallbackMessage
import org.mentionbattle.snadapter.api.core.SocialNetwork
import org.mentionbattle.snadapter.api.core.eventsystem.Event
import org.mentionbattle.snadapter.impl.eventsystem.MentionEvent
import org.mentionbattle.snadapter.impl.eventsystem.PrimitiveEventQueue
import org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.beautifiers.MsgBeautifier
import java.util.*

@SocialNetwork("vk_queue_handler")
class EventQueueHandler(val eventQueue: PrimitiveEventQueue) : VkMsgHandler {
    override fun handle(message: StreamingCallbackMessage,
                        hashedTags: HashedTagToContentendIdWithTag,
                        msgBeautifier: MsgBeautifier) {
        val contanders = MutableList(2, { false })
        val msgTags = message.event.tags.filter { hashedTags.containsKey(it) }
        msgTags.forEach { tagHash ->
            contanders[hashedTags.get(tagHash)!!.first - 1] = true
        }

        for ((id, shouldSend) in contanders.withIndex()) {
            if (shouldSend) eventQueue.addEvent(buildMentionEvent(id + 1, message, msgBeautifier))
        }
    }

    private fun buildMentionEvent(contenderId: Int, message: StreamingCallbackMessage,
                                  msgBeautifier: MsgBeautifier): Event {
        val vkAccount = msgBeautifier.findNameAndAvatarUrl(message.event.author.id)
        var cleanedText = msgBeautifier.cleanUpNamesInText(message.event.text)
        cleanedText = msgBeautifier.shieldExclamationPoint(cleanedText)
        return MentionEvent(
                contenderId,
                "vk",
                message.event.eventUrl,
                vkAccount.name,
                cleanedText,
                vkAccount.avatarUrl,
                Date(message.event.creationTime * 1000L))
    }
}