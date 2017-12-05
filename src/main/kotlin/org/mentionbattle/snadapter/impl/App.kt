package org.mentionbattle.snadapter.impl

import kotlinx.coroutines.experimental.runBlocking
import org.mentionbattle.snadapter.impl.eventsystem.MentionEvent
import org.mentionbattle.snadapter.impl.startup.StartUpManager
import java.util.*

fun main(args : Array<String>) {

    StartUpManager().use {
        it.initialize(listOf("org.mentionbattle"))
        runBlocking {
            it.run()
        }
    }
}