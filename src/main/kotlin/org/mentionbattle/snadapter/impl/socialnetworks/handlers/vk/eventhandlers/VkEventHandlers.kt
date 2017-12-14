package org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.eventhandlers

import com.vk.api.sdk.streaming.objects.StreamingCallbackMessage

typealias HashedTagToContentendIdWithTag = MutableMap<String, Pair<Int, String>>

interface VkMsgHandler {
    fun handle(message: StreamingCallbackMessage,
               hashedTags: HashedTagToContentendIdWithTag)
}