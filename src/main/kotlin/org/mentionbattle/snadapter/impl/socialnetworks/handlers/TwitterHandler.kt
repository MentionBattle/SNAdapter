package org.mentionbattle.snadapter.impl.socialnetworks.handlers

import com.twitter.hbc.httpclient.auth.OAuth1
import org.mentionbattle.snadapter.api.core.SocialNetwork
import org.mentionbattle.snadapter.api.core.eventsystem.Event
import org.mentionbattle.snadapter.api.core.eventsystem.EventHandler
import org.mentionbattle.snadapter.api.core.socialnetworks.SocialNetworkHandler
import org.mentionbattle.snadapter.impl.eventsystem.ExitEvent
import org.mentionbattle.snadapter.impl.eventsystem.PrimitiveEventQueue
import org.mentionbattle.snadapter.impl.eventsystem.StringEvent
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.Tags
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.TwitterTokens


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
        eventQueue.addHandler(this)
        while (isWorking) {
            eventQueue.addEvent(StringEvent("twitter adds event"))
            Thread.sleep(3000)
        }
        println("Twitter job cancelled")
    }

}