package org.mentionbattle.snadapter.api.core.eventsystem


interface EventQueue {
    fun addHandler(h : EventHandler)

    fun removeHandler(h : EventHandler)

    fun addEvent(event : Event)
}