package org.mentionbattle.snadapter.impl.common

import org.mentionbattle.snadapter.api.core.Component
import org.mentionbattle.snadapter.api.core.eventsystem.Event
import org.mentionbattle.snadapter.api.core.eventsystem.EventHandler
import org.mentionbattle.snadapter.impl.eventsystem.ExitEvent
import org.mentionbattle.snadapter.impl.eventsystem.MentionEvent
import org.mentionbattle.snadapter.impl.eventsystem.PrimitiveEventQueue
import org.mentionbattle.snadapter.impl.eventsystem.LogEvent
import org.mentionbattle.snadapter.impl.startup.configuration.Configuration
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

@Component
class Core(eventQueue: PrimitiveEventQueue) : EventHandler {

    private val eventQueue = eventQueue
    private val clients : MutableList<Socket> = mutableListOf<Socket>()
    private lateinit var server : ServerSocket
    private lateinit var firstAnswer : String

    override fun handleEvent(event : Event) {
        when (event) {
            is ExitEvent -> {
                System.err.println("LOG :: shutdown start")
                for (c in clients)
                    c.close()
                server.close()
            }
            is LogEvent -> {
                System.err.println("LOG :: " + event.text)
            }
            is MentionEvent -> {
                notifyAllClients(event)
            }
        }
    }

    suspend fun run(configuration: Configuration) {
        firstAnswer = createFirstAnswer(configuration)
        eventQueue.addHandler(this)
        server = ServerSocket(configuration.port)
        try {
            while (true) {
                val result = server.accept();
                onAccept(result)
            }
        } catch (e : SocketException) {
            System.err.println("LOG :: shutdown end")
            println("server shutdown end")
        }
        eventQueue.removeHandler(this)
        System.err.println("LOG :: server job cancelled")
    }

    private fun onAccept(client : Socket) {
        try {
            val answer = client.readString()

            if (answer.equals("%server%")) {
                client.sendString(firstAnswer)
                clients.add(client)
            }
        } catch (e : SocketException) {
        }
        println("new client has been connected")
    }

    private fun notifyAllClients(event : MentionEvent) {
        synchronized(clients) {
            val toRemove = mutableListOf<Socket>()
            for (c in clients) {
                try {
                    c.sendString("mention|" + event.packToJson())
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

    fun createFirstAnswer(configuration: Configuration) : String {
        val sb = StringBuilder()
        val first = configuration.contenders[0]
        val second = configuration.contenders[1]
        sb.append("init|{").
            append("\"contender1\":{").
                append("\"name\": \"").append(first.name).append("\"").append(",").
                append("\"image\": \"").append(first.packImageToBase64()).append("\"").append(",").
                append("\"votes\": ").append(queryVotesFromDatabase(first.name)).append(",").
                append("\"rate\": ").append(0).append(",").
                append("\"mentions\": []").
            append("}").append(",").
            append("\"contender2\":{").
                append("\"name\": \"").append(second.name).append("\"").append(",").
                append("\"image\": \"").append(second.packImageToBase64()).append("\"").append(",").
                append("\"votes\": ").append(queryVotesFromDatabase(second.name)).append(",").
                append("\"rate\": ").append(0).append(",").
                append("\"mentions\": []").
            append("}").
        append("}")
        return sb.toString()
    }

    fun queryVotesFromDatabase(contender : String) : Int {
        return 0
    }
}