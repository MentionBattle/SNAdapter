package org.mentionbattle.snadapter.impl.eventsystem

import com.sun.jmx.remote.internal.ArrayQueue
import org.mentionbattle.snadapter.api.core.Component
import org.mentionbattle.snadapter.api.core.eventsystem.Event
import org.mentionbattle.snadapter.api.core.eventsystem.EventHandler
import org.mentionbattle.snadapter.api.core.eventsystem.EventQueue
import java.util.*

@Component
class PrimitiveEventQueue : EventQueue {
    val handlers: MutableList<EventHandler> = mutableListOf()

    private val toRemove : MutableList<EventHandler> = mutableListOf()
    override fun addHandler(eh : EventHandler) {
        synchronized(this) {
            handlers.add(eh)
        }
    }

    override fun removeHandler(eh : EventHandler) {
        toRemove.add(eh)
    }

    override fun addEvent(event : Event) {
        synchronized(this) {
            toRemove.forEach({t -> handlers.remove(t)})
            toRemove.clear()
            handlers.forEach({h -> h.handleEvent(event)})
        }
    }
 }
