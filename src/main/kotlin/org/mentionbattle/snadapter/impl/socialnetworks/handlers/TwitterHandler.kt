package org.mentionbattle.snadapter.impl.socialnetworks.handlers

import com.google.common.collect.Lists
import org.mentionbattle.snadapter.api.core.SocialNetwork
import org.mentionbattle.snadapter.api.core.eventsystem.Event
import org.mentionbattle.snadapter.api.core.eventsystem.EventHandler
import org.mentionbattle.snadapter.api.core.socialnetworks.SocialNetworkHandler
import org.mentionbattle.snadapter.impl.eventsystem.ExitEvent
import org.mentionbattle.snadapter.impl.eventsystem.PrimitiveEventQueue
import org.mentionbattle.snadapter.impl.eventsystem.StringEvent
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.Tags
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.TwitterTokens

import jp.nephy.penicillin.Client
import jp.nephy.penicillin.credential.*
import jp.nephy.penicillin.model.Delete
import jp.nephy.penicillin.model.Status
import jp.nephy.penicillin.streaming.IFilterStreamListener

@SocialNetwork("Twitter")
internal class TwitterHandler(token: TwitterTokens, tags: Tags, eventQueue: PrimitiveEventQueue) : SocialNetworkHandler, EventHandler, IFilterStreamListener {


    override fun onStatus(status: Status) {
        val url = "https://twitter.com/" + status.user.screenName + "/status/" + status.idStr
        val info = Lists.newArrayList<String>(
                status.text,
                url,
                status.user.profileImageUrlHttps.toString()
        )
        eventQueue.addEvent(StringEvent(info.toString()))
        println(info)
    }

    override fun onUnknownData(data: String) {
        println("onUnknownData " + data)
    }

    override fun onDelete(delete: Delete) {
        print("onDelete " + delete.toString())
    }


    val eventQueue = eventQueue
    var isWorking = true
    val consumerKey = token.consumerKey as String
    val consumerSecret = token.consumerSecret as String
    val accessToken = token.accessToken as String
    val accessTokenSecret = token.accessTokenSecret as String

    override fun handleEvent(event: Event) {
        when (event) {
            is ExitEvent -> {
                isWorking = false
                eventQueue.removeHandler(this)
            }
        }
    }

    override fun processData() {
        val client = Client.builder()
                .authenticate(
                        ConsumerKey(consumerKey), ConsumerSecret(consumerSecret),
                        AccessToken(accessToken), AccessTokenSecret(accessTokenSecret)
                )
                .connectTimeout(20)  // optional: timeouts in sec
                .readTimeout(40)
                .writeTimeout(20)
                .build()  // return Client instance

        val trackList = arrayOf("putin")
        val responseStream = client.stream.getFilterStream(track = trackList)
        val listener = responseStream.listen(this)
                .onClose { println("Twitter listener stopped") }
                .start()
        // process stream asynchronously (non-blocking)


        eventQueue.addHandler(this)

        // stop streaming after 30 seconds
        Thread.sleep(30000)
        listener.terminate() // Stdout: Closed.
        isWorking = false
        println("Twitter job cancelled")
    }

}