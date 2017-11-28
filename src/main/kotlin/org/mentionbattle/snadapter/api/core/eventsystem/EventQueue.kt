package org.mentionbattle.snadapter.api.core.eventsystem


interface EventQueue {
    fun addHandler(eh : EventHandler)

    fun removeHandler(eh : EventHandler)

    fun addEvent(event : Event)
}