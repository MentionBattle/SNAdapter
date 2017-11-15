package org.mentionbattle.snadapter.impl

import kotlinx.coroutines.experimental.launch
import org.mentionbattle.snadapter.api.core.Component
import org.mentionbattle.snadapter.api.core.eventsystem.Event
import org.mentionbattle.snadapter.api.core.eventsystem.EventHandler
import org.mentionbattle.snadapter.impl.eventsystem.ExitEvent
import org.mentionbattle.snadapter.impl.eventsystem.PrimitiveEventQueue
import org.mentionbattle.snadapter.impl.eventsystem.StringEvent
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

@Component
class CoreListener(eventQueue: PrimitiveEventQueue) : EventHandler {

    val eventQueue = eventQueue
    val clients : MutableList<Socket> = mutableListOf<Socket>()
    lateinit var server : ServerSocket

    override fun handleEvent(event : Event) {
        when (event) {
            is ExitEvent -> {
                println("server shutdown start...")
                server.close()
            }
            is StringEvent -> {
                println(event.text)
            }
        }
    }

    suspend fun run(port : Int) {
        eventQueue.addHandler(this)
        server = ServerSocket(port)
        try {
            while (true) {
                val result = server.accept();
                onAccept(result)
            }
        } catch (e : SocketException) {
            println("server shutdown end")
        }
        eventQueue.removeHandler(this)
        println("server job cancelled")
    }

    fun onAccept(client : Socket) {
        clients.add(client)
        println("new client has been connected")
    }

}