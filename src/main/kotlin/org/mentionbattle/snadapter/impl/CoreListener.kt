package org.mentionbattle.snadapter.impl

import org.mentionbattle.snadapter.api.core.Component
import org.mentionbattle.snadapter.api.core.eventsystem.Event
import org.mentionbattle.snadapter.api.core.eventsystem.EventHandler
import org.mentionbattle.snadapter.impl.eventsystem.ExitEvent
import org.mentionbattle.snadapter.impl.eventsystem.MentionEvent
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
                for (c in clients)
                    c.close()
                server.close()
            }
            is StringEvent -> {
                println(event.text)
            }
            is MentionEvent -> {
                synchronized(clients) {
                    println("clients sise: ")
                    println(clients.size)
                    val toRemove = mutableListOf<Socket>()
                    for (c in clients) {
                        try {
                            if (!c.isClosed) {
                                sendString(c, "mention|" + event.packToJson())
                            } else {
                                println("NB : socket was closed")
                                toRemove.add(c)
                            }
                        } catch (e : SocketException) {
                            toRemove.add(c)
                        }
                    }
                    for (c in toRemove) {
                        if (!c.isClosed) {
                            c.close()
                        }
                        clients.remove(c)
                    }
                }
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
        try {
            val answer = readString(client)

            if (answer.equals("%server%")) {
                sendString(client, tempInitialMessage)
                clients.add(client)
            }
        } catch (e : SocketException) {
        }
        println("new client has been connected")
    }

    val tempInitialMessage = "init|{\"contender1\":{\"name\": \"Volodya\", \"votes\": 1000, \"rate\": 900, \"mentions\": [" +
            "{\"from\": \"twitter\", \"name\": \"asdzxc\", \"text\": \"asdzxc\", \"timestamp\": \"2017-12-02T02:51:15.952Z\"}," +
            "{\"from\": \"vk\", \"name\": \"123456\", \"text\": \"123456\", \"timestamp\": \"2017-12-02T02:52:15.952Z\"}" +
            "]}," +
            "\"contender2\": {\"name\": \"Ivan\"," +
            "\"votes\": 800, \"rate\": 200, \"mentions\": [" +
            "{\"from\": \"twitter\", \"name\": \"asdzxc\", \"text\": \"asdzxc\", \"timestamp\": \"2017-12-02T02:51:15.952Z\"}," +
            "{\"from\": \"vk\", \"name\": \"123456\", \"text\": \"123456\", \"timestamp\": \"2017-12-02T02:52:15.952Z\"}" +
            "]}" +
            "}"
}