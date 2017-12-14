package org.mentionbattle.snadapter.impl.socialnetworks.handlers

import org.mentionbattle.snadapter.api.core.SocialNetwork
import org.mentionbattle.snadapter.api.core.eventsystem.Event
import org.mentionbattle.snadapter.api.core.eventsystem.EventHandler
import org.mentionbattle.snadapter.api.core.socialnetworks.SocialNetworkHandler
import org.mentionbattle.snadapter.impl.eventsystem.ExitEvent
import org.mentionbattle.snadapter.impl.eventsystem.PrimitiveEventQueue
import org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.VkStreamingService
import org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.handlers.EventQueueHandler
import org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.handlers.LogHandler
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.Tags
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.VkServiceAuth

@SocialNetwork("vk")
internal class VkHandler(auth: VkServiceAuth, tags: Tags, eventQueue: PrimitiveEventQueue)
    : SocialNetworkHandler, EventHandler {

    var isWorking = true
    val eventQueue = eventQueue
    val vk = VkStreamingService(auth, tags, eventQueue)

    override fun handleEvent(event: Event) {
        when (event) {
            is ExitEvent -> {
                eventQueue.removeHandler(this)
                vk.close()
                isWorking = false
            }
        }
    }

    override fun processData() {
        eventQueue.addHandler(this)

        vk.addMsgHandler(LogHandler())
        vk.addMsgHandler(EventQueueHandler(eventQueue))

        vk.ensureRules()
        vk.startListenMsgStream()
    }
}