package org.mentionbattle.snadapter.api.core.eventsystem

interface EventHandler {
    fun handleEvent(event : Event)
}