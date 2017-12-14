package org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.handlers

import com.vk.api.sdk.streaming.objects.StreamingCallbackMessage
import org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.beautifiers.MsgBeautifier

typealias HashedTagToContentendIdWithTag = MutableMap<String, Pair<Int, String>>

interface VkMsgHandler {
    fun handle(message: StreamingCallbackMessage,
               hashedTags: HashedTagToContentendIdWithTag,
               msgBeautifier: MsgBeautifier)
}