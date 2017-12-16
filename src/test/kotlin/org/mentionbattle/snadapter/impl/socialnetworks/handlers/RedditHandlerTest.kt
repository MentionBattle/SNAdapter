package org.mentionbattle.snadapter.impl.socialnetworks.handlers

import org.junit.After
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.mentionbattle.snadapter.impl.eventsystem.ExitEvent
import org.mentionbattle.snadapter.impl.eventsystem.PrimitiveEventQueue
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.RedditAuth
import org.mentionbattle.snadapter.impl.startup.configuration.ConfigurationParser
import org.mockito.Mockito
import twitter4j.Status
import twitter4j.TwitterObjectFactory
import java.nio.file.Files
import java.nio.file.Paths

/**
 * @author Novik Dmitry ITMO University
 */
class RedditHandlerTest {
    private lateinit var reddit: RedditHandler
    private val eventQueue = Mockito.mock(PrimitiveEventQueue::class.java)

    @Before
    fun setUp() {
        val configuration = ConfigurationParser().parse(Paths.get("sna.config"))
        val RedditAuth: MutableMap<String, Any> by configuration.socialNetworkInitializers
        val keyList = RedditAuth.map { Pair(it.key, it.value.toString()) }
        val redditAuth = RedditAuth(keyList.toMap())
        val Tags: MutableMap<String, Any> by configuration.socialNetworkInitializers
        reddit = RedditHandler(
                redditAuth,
                org.mentionbattle.snadapter.impl.socialnetworks.initalizers.Tags(Tags),
                eventQueue)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun handleEvent() {
        var handlerRemoved = false
        Mockito.`when`(eventQueue.removeHandler(reddit)).then {
            handlerRemoved = true
            println("handler removed from event queue")
        }
        reddit.handleEvent(ExitEvent())
        assert(handlerRemoved)
    }

    @Test
    fun processData() {
    }

}