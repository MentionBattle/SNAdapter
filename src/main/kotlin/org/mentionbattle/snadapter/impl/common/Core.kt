package org.mentionbattle.snadapter.impl.common

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
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

    private val logger = LogManager.getLogger()
    private val clients : MutableList<Socket> = mutableListOf<Socket>()
    private lateinit var server : ServerSocket
    private lateinit var firstAnswer : String
    private lateinit var configuration : Configuration
    private var rates = hashMapOf<Int, Int>()

    override fun handleEvent(event : Event) {
        when (event) {
            is ExitEvent -> {
                logger.log(Level.INFO, "shutdown start")
                for (c in clients)
                    c.close()
                server.close()
            }
            is LogEvent -> {
                logger.log(Level.INFO, event.text)
            }
            is MentionEvent -> {
                rates[event.contender]?.plus(1)
                database.addMention(event)
                notifyAllClients(event)
            }
        }
    }

    suspend fun run(configuration: Configuration) {
        this.configuration = configuration
        for (c in configuration.contenders)
            rates[c.id] = queryVotesFromDatabase(c)

        eventQueue.addHandler(this)
        server = ServerSocket(configuration.port)
        try {
            while (true) {
                val result = server.accept();
                onAccept(result)
            }
        } catch (e : SocketException) {
            logger.log(Level.INFO, "shutdown end")
        }
        eventQueue.removeHandler(this)
        logger.log(Level.INFO, "server job cancelled")
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
        logger.log(Level.WARN, "smbd connected to server")
    }

    private fun notifyAllClients(event : MentionEvent) {
        synchronized(clients) {
            val toRemove = mutableListOf<Socket>()
            for (c in clients) {
                try {
                    c.sendString(createMentionAnswer(event))
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

    private fun createMentionAnswer(event : MentionEvent) : String{
        return JSONObject(
                hashMapOf(
                        "event" to event.createJson(),
                        "rateA" to rates[1],
                        "rateB" to rates[2]
        )).toString()
    }

    private fun createFirstAnswer(configuration: Configuration) : String {
        val contendersJson = mutableListOf<JSONObject>()
        for (c in configuration.contenders) {
            contendersJson.add(createContenderJson(c))
        }
        return JSONObject(hashMapOf("contenders" to  contendersJson)).toString()
    }

    private fun createContenderJson(contender: Contender) : JSONObject {
        return JSONObject(hashMapOf(
                "name" to contender.name,
                "image" to contender.packImageToBase64(),
                "votes" to rates[contender.id],
                "rate" to 0,
                "mentions" to arrayListOf<JSONObject>()
                ))
    }
    private fun queryVotesFromDatabase(contender : Contender) : Int {
        return database.contenderMentionCount(contender)
    }
}