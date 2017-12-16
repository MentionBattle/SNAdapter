package org.mentionbattle.snadapter.impl

import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.mentionbattle.snadapter.impl.eventsystem.MentionEvent
import org.mentionbattle.snadapter.impl.startup.StartUpManager
import org.mentionbattle.snadapter.impl.startup.configuration.ConfigurationParser
import java.nio.file.Paths
import java.util.*

fun main(args : Array<String>) {

    val configuration = ConfigurationParser().parse(Paths.get("sna.config"))
    StartUpManager(configuration, listOf("org.mentionbattle")).use {
        runBlocking {
            it.run()
        }
        while (true) {
            val result = ""
            if (result.equals("exit")) {
                break
            }
        }
    }
}