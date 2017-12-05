package org.mentionbattle.snadapter.impl.eventsystem

import org.mentionbattle.snadapter.api.core.eventsystem.Event

class LogEvent(text : String) : Event {
    val text = text
}