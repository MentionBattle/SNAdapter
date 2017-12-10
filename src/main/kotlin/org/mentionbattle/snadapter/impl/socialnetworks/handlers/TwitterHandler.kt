package org.mentionbattle.snadapter.impl.socialnetworks.handlers

import com.google.common.collect.Lists
import org.mentionbattle.snadapter.api.core.SocialNetwork
import org.mentionbattle.snadapter.api.core.eventsystem.Event
import org.mentionbattle.snadapter.api.core.eventsystem.EventHandler
import org.mentionbattle.snadapter.api.core.socialnetworks.SocialNetworkHandler
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.Tags
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.TwitterTokens

import jp.nephy.penicillin.Client
import jp.nephy.penicillin.credential.*
import jp.nephy.penicillin.model.Delete
import jp.nephy.penicillin.model.Status
import jp.nephy.penicillin.streaming.AbsStreamingParser
import jp.nephy.penicillin.streaming.IFilterStreamListener
import org.mentionbattle.snadapter.impl.eventsystem.*
import java.util.*

@SocialNetwork("Twitter")
internal class TwitterHandler(token: TwitterTokens, tags: Tags, eventQueue: PrimitiveEventQueue) : SocialNetworkHandler, EventHandler, IFilterStreamListener {

    val eventQueue = eventQueue
    var isWorking = true
    val consumerKey = token.consumerKey as String
    val consumerSecret = token.consumerSecret as String
    val accessToken = token.accessToken as String
    val accessTokenSecret = token.accessTokenSecret as String
    lateinit var listener : AbsStreamingParser<IFilterStreamListener>


    override fun onStatus(status: Status) {
        val url = "https://twitter.com/" + status.user.screenName + "/status/" + status.idStr
        val info = Lists.newArrayList<String>(
                status.text,
                url,
                status.user.profileImageUrlHttps.toString()
        )
        val rnd = Random()
        eventQueue.addEvent(MentionEvent(rnd.nextInt(2) + 1, "twitter",
                url, status.user.name, status.text,
                status.user.profileImageUrlHttps.toString(), Date()))
        println(info)
    }

    override fun onUnknownData(data: String) {
        println("onUnknownData " + data)
    }

    override fun onDelete(delete: Delete) {
        print("onDelete " + delete.toString())
    }

    override fun handleEvent(event: Event) {
        when (event) {
            is ExitEvent -> {
                // stop streaming
                listener.terminate() // Stdout: Closed.
                eventQueue.removeHandler(this)
                isWorking = false
                println("Twitter job cancelled")
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

        val trackList = arrayOf("spaceX")
        val responseStream = client.stream.getFilterStream(track = trackList)
        listener = responseStream.listen(this)
                .onClose { println("Twitter listener stopped") }
                .start()
        // process stream asynchronously (non-blocking)

        eventQueue.addHandler(this)
    }

}