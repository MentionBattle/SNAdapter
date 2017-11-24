package org.mentionbattle.snadapter.impl.socialnetworks.handlers

import org.mentionbattle.snadapter.api.core.SocialNetwork
import org.mentionbattle.snadapter.api.core.eventsystem.Event
import org.mentionbattle.snadapter.api.core.eventsystem.EventHandler
import org.mentionbattle.snadapter.api.core.eventsystem.EventQueue
import org.mentionbattle.snadapter.api.core.socialnetworks.SocialNetworkHandler
import org.mentionbattle.snadapter.impl.eventsystem.ExitEvent
import org.mentionbattle.snadapter.impl.eventsystem.PrimitiveEventQueue
import org.mentionbattle.snadapter.impl.eventsystem.StringEvent
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.Tags
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.VkServiceToken

@SocialNetwork("VK")
internal class VkHandler(token: VkServiceToken, tags : Tags, eventQueue : PrimitiveEventQueue)
    : SocialNetworkHandler, EventHandler {

    var isWorking = true
    val eventQueue = eventQueue
    val vk = VkStreamingServiceOfficial(token.accessToken)

    override fun handleEvent(event: Event) {
        when (event) {
            is ExitEvent -> {
                isWorking = false
                eventQueue.removeHandler(this)
            }
        }
    }

    override fun processData() {
        eventQueue.addHandler(this)
        vk.addHandler("logToConsole", vk.defaultHandler)
        vk.startListenEvents()
    }
}