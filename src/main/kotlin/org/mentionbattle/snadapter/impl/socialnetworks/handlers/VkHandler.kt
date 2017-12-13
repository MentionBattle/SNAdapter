package org.mentionbattle.snadapter.impl.socialnetworks.handlers

import org.mentionbattle.snadapter.api.core.SocialNetwork
import org.mentionbattle.snadapter.api.core.eventsystem.Event
import org.mentionbattle.snadapter.api.core.eventsystem.EventHandler
import org.mentionbattle.snadapter.api.core.socialnetworks.SocialNetworkHandler
import org.mentionbattle.snadapter.impl.eventsystem.ExitEvent
import org.mentionbattle.snadapter.impl.eventsystem.PrimitiveEventQueue
import org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.VkStreamingServiceOfficial
import org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.eventhandlers.EventQueueHandler
import org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.eventhandlers.LogHandler
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.Tags
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.VkServiceAuth

@SocialNetwork("VK")
internal class VkHandler(auth: VkServiceAuth, tags: Tags, eventQueue: PrimitiveEventQueue)
    : SocialNetworkHandler, EventHandler {

    var isWorking = true
    val eventQueue = eventQueue
    val vk = VkStreamingServiceOfficial(auth, tags, eventQueue)

    override fun handleEvent(event: Event) {
        when (event) {
            is ExitEvent -> {
                isWorking = false
                eventQueue.removeHandler(this)
                vk.close()
            }
        }
    }

    override fun processData() {
            eventQueue.addHandler(this)

            vk.addHandler(LogHandler())
            vk.addHandler(EventQueueHandler(eventQueue))

            vk.startListenEvents()
    }
}