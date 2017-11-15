package org.mentionbattle.snadapter.impl

import kotlinx.coroutines.experimental.runBlocking
import org.mentionbattle.snadapter.impl.startup.StartUpManager

fun main(args : Array<String>) {
    StartUpManager().use {
        it.initialize(listOf("org.mentionbattle"))
        runBlocking {
            it.run()
        }
    }
}