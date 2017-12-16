package org.mentionbattle.snadapter.impl.socialnetworks.handlers

import org.junit.*
import org.junit.rules.ExpectedException
import org.mentionbattle.snadapter.impl.eventsystem.ExitEvent
import org.mentionbattle.snadapter.impl.eventsystem.PrimitiveEventQueue
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.Tags
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.TwitterTokens
import org.mentionbattle.snadapter.impl.startup.configuration.ConfigurationParser
import org.mockito.Mockito
import org.mockito.Mockito.*
import twitter4j.Status
import twitter4j.TwitterObjectFactory
import java.nio.file.Files
import java.nio.file.Paths

internal class TwitterHandlerTest {
    private lateinit var twitter: TwitterHandler
    private val eventQueue = mock(PrimitiveEventQueue::class.java)
    private var statusList = emptyList<Status>()
    private val tweetsFolder = Paths.get("src", "test", "resources", "tweets.txt")

    @Before
    fun setUp() {
        val configuration = ConfigurationParser().parse(Paths.get("sna.config"))
        val TwitterTokens: MutableMap<String, Any> by configuration.socialNetworkInitializers
        val tokenList = TwitterTokens.map { Pair(it.key, it.value.toString()) }
        val tokens = TwitterTokens(tokenList.toMap())
        val Tags: MutableMap<String, Any> by configuration.socialNetworkInitializers
        twitter = TwitterHandler(tokens, Tags(Tags), eventQueue)
        Files.readAllLines(tweetsFolder)
                .asSequence()
                .map { TwitterObjectFactory.createStatus(it) }
                .forEach { statusList = statusList.plus(it) }
    }

    @After
    fun tearDown() {
    }

    private fun <T> anyObject(): T {
        return Mockito.anyObject<T>()
    }

    @get:Rule
    val thrown = ExpectedException.none()

    @Test
    fun processData() {
        var handlerRemoved = false
        `when`(eventQueue.removeHandler(twitter)).then {
            handlerRemoved = true
            println("handler removed from event queue")
        }
        twitter.processData()
        twitter.handleEvent(ExitEvent())
        assert(handlerRemoved)
    }

    @Test
    fun handleEvent() {
        var counter = 0
        `when`((eventQueue).addEvent(anyObject())).then {
            counter++
        }
        twitter.processData()
        for (status in statusList) {
            twitter.listener.onStatus(status)
        }
        println("Mentions count: " + counter.toString())
        println("Real count: " + statusList.size.toString())
        assert(counter == statusList.size)
    }
}