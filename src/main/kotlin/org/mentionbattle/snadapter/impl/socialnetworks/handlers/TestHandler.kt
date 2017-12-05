package org.mentionbattle.snadapter.impl.socialnetworks.handlers

import org.mentionbattle.snadapter.api.core.SocialNetwork
import org.mentionbattle.snadapter.api.core.eventsystem.Event
import org.mentionbattle.snadapter.api.core.eventsystem.EventHandler
import org.mentionbattle.snadapter.api.core.socialnetworks.SocialNetworkHandler
import org.mentionbattle.snadapter.impl.eventsystem.ExitEvent
import org.mentionbattle.snadapter.impl.eventsystem.MentionEvent
import org.mentionbattle.snadapter.impl.eventsystem.PrimitiveEventQueue
import org.mentionbattle.snadapter.impl.eventsystem.StringEvent
import java.util.*

@SocialNetwork("Test")
internal class TestHandler(eventQueue : PrimitiveEventQueue) : SocialNetworkHandler, EventHandler {

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
        val rnd = Random()
        while (isWorking) {
            eventQueue.addEvent(MentionEvent(rnd.nextInt(2) + 1, if (rnd.nextInt(2) == 0)  "vk" else "twitter",
                    "https://vk.com/ct_year2014?w=wall-75415835_2769%2Fall", "Artem Zholus", "Test event from sna",
                    "https://pp.userapi.com/c840137/v840137533/26e78/jHGnZTiL_zs.jpg", Date()))
            println("Test send message")
            Thread.sleep(3000)
        }
        println("Test job cancelled")
    }

}