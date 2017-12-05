package org.mentionbattle.snadapter.impl.socialnetworks.handlers

import org.mentionbattle.snadapter.api.core.SocialNetwork
import org.mentionbattle.snadapter.api.core.eventsystem.Event
import org.mentionbattle.snadapter.api.core.eventsystem.EventHandler
import org.mentionbattle.snadapter.api.core.socialnetworks.SocialNetworkHandler
import org.mentionbattle.snadapter.impl.eventsystem.ExitEvent
import org.mentionbattle.snadapter.impl.eventsystem.PrimitiveEventQueue
import org.mentionbattle.snadapter.impl.eventsystem.LogEvent


@SocialNetwork("Twitter")
internal class TwitterHandler(eventQueue : PrimitiveEventQueue) : SocialNetworkHandler, EventHandler {

    val eventQueue = eventQueue
    var isWorking = true
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
        while (isWorking) {
            eventQueue.addEvent(LogEvent("twitter adds event"))
            Thread.sleep(3000)
        }
        println("Twitter job cancelled")
    }

}