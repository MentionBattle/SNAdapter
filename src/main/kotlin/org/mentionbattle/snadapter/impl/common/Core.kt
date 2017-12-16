package org.mentionbattle.snadapter.impl.common

import org.json.JSONObject
import org.mentionbattle.snadapter.api.core.Component
import org.mentionbattle.snadapter.api.core.eventsystem.Event
import org.mentionbattle.snadapter.api.core.eventsystem.EventHandler
import org.mentionbattle.snadapter.impl.common.Utils.readString
import org.mentionbattle.snadapter.impl.common.Utils.sendString
import org.mentionbattle.snadapter.impl.eventsystem.ExitEvent
import org.mentionbattle.snadapter.impl.eventsystem.MentionEvent
import org.mentionbattle.snadapter.impl.eventsystem.PrimitiveEventQueue
import org.mentionbattle.snadapter.impl.eventsystem.LogEvent
import org.mentionbattle.snadapter.impl.startup.configuration.Configuration
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

@Component
class Core(private val eventQueue: PrimitiveEventQueue, private val database: Database) : EventHandler {

    private val clients : MutableList<Socket> = mutableListOf<Socket>()
    private lateinit var server : ServerSocket
    private lateinit var firstAnswer : String
    private lateinit var configuration : Configuration

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
                database.addMention(event)
                notifyAllClients(event)
            }
        }
    }

    suspend fun run(configuration: Configuration) {
        this.configuration = configuration
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
                client.sendString(createFirstAnswer(configuration))
                clients.add(client)
            } else {
                client.close()
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
                    c.sendString("mention|" + event.createJson())
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

    private fun createFirstAnswer(configuration: Configuration) : String {
        val contendersJson = mutableListOf<JSONObject>()
        for (c in configuration.contenders) {
            contendersJson.add(createContenderJson(c))
        }
        return "init|" + JSONObject(hashMapOf("contenders" to  contendersJson))
    }

    private fun createContenderJson(contender: Contender) : JSONObject {
        return JSONObject(hashMapOf(
                "name" to contender.name,
                "image" to contender.packImageToBase64(),
                "votes" to queryVotesFromDatabase(contender),
                "rate" to 0,
                "mentions" to arrayListOf<JSONObject>()
                ))
    }
    fun queryVotesFromDatabase(contender : Contender) : Int {
        return database.contenderMentionCount(contender)
    }
}