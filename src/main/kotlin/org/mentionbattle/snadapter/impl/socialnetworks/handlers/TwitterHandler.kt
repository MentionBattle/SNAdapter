package org.mentionbattle.snadapter.impl.socialnetworks.handlers

import com.twitter.hbc.httpclient.auth.OAuth1
import org.mentionbattle.snadapter.api.core.SocialNetwork
import org.mentionbattle.snadapter.api.core.eventsystem.Event
import org.mentionbattle.snadapter.api.core.eventsystem.EventHandler
import org.mentionbattle.snadapter.api.core.socialnetworks.SocialNetworkHandler
import org.mentionbattle.snadapter.impl.eventsystem.ExitEvent
import org.mentionbattle.snadapter.impl.eventsystem.PrimitiveEventQueue
import org.mentionbattle.snadapter.impl.eventsystem.StringEvent
import com.google.common.collect.Lists
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint
import java.util.concurrent.LinkedBlockingQueue
import com.twitter.hbc.core.processor.StringDelimitedProcessor
import com.twitter.hbc.ClientBuilder
import java.util.concurrent.TimeUnit
import com.twitter.hbc.core.Constants
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.Tags
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.TwitterTokens
import org.json.JSONObject

@SocialNetwork("Twitter")
internal class TwitterHandler(token: TwitterTokens, tags: Tags, eventQueue: PrimitiveEventQueue) : SocialNetworkHandler, EventHandler {

    val eventQueue = eventQueue
    var isWorking = true
    val auth = OAuth1(token.consumerKey, token.consumerSecret, token.accessToken, token.accessTokenSecret)

    override fun handleEvent(event: Event) {
        when (event) {
            is ExitEvent -> {
                isWorking = false
                eventQueue.removeHandler(this)
            }
        }
    }

    override fun processData() {
        val queue = LinkedBlockingQueue<String>(10000)
        val endpoint = StatusesFilterEndpoint()
        // add some track terms
        endpoint.trackTerms(Lists.newArrayList("cats"))

        // Create a new BasicClient. By default gzip is enabled.
        val client = ClientBuilder()
                .hosts(Constants.STREAM_HOST)
                .endpoint(endpoint)
                .authentication(auth)
                .processor(StringDelimitedProcessor(queue))
                .build()

        // Establish a connection
        client.connect()

        // Do whatever needs to be done with messages
        for (msgRead in 0..999) {
            val msg = queue.take()
            val json = JSONObject(msg)
            val user = json["user"] as JSONObject
            val name = user["name"] as String
            println(name)
        }

        client.stop()

        // Do whatever needs to be done with messages
        for (msgRead in 0..999) {
            if (client.isDone()) {
                println("Client connection closed unexpectedly: " + client.getExitEvent().getMessage())
                break
            }

            val msg = queue.poll(5, TimeUnit.SECONDS)
            if (msg == null) {
                println("Did not receive a message in 5 seconds")
            } else {
                println(msg)
            }
        }

        client.stop()

        // Print some stats
        System.out.printf("The client read %d messages!\n", client.getStatsTracker().getNumMessages())

        eventQueue.addHandler(this)
        while (isWorking) {
            eventQueue.addEvent(StringEvent("twitter adds event"))
            Thread.sleep(3000)
        }
        println("Twitter job cancelled")
    }

}