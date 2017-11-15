package org.mentionbattle.snadapter.impl.eventsystem

import org.mentionbattle.snadapter.api.core.eventsystem.Event

class StringEvent (text : String) : Event {
    val text = text
}