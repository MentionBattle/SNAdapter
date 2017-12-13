package org.mentionbattle.snadapter.impl.socialnetworks.handlers

import org.mentionbattle.snadapter.api.core.SocialNetwork
import org.mentionbattle.snadapter.api.core.eventsystem.Event
import org.mentionbattle.snadapter.api.core.eventsystem.EventHandler
import org.mentionbattle.snadapter.api.core.socialnetworks.SocialNetworkHandler
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.Tags
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.TwitterTokens

import org.mentionbattle.snadapter.impl.eventsystem.*
import twitter4j.Status;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.StallWarning
import twitter4j.StatusDeletionNotice
import twitter4j.StatusListener
import twitter4j.FilterQuery
import twitter4j.TwitterStreamFactory
import twitter4j.TwitterStream

@SocialNetwork("Twitter")
internal class TwitterHandler(token: TwitterTokens, tags: Tags, eventQueue: PrimitiveEventQueue) : SocialNetworkHandler, EventHandler {
    private val eventQueue = eventQueue
    private val tags = tags
    private val tokens = token
    private lateinit var twitterStream: TwitterStream

    private val listener = object : StatusListener {
        override fun onStatus(status: Status) {
            val url = "https://twitter.com/" + status.user.screenName + "/status/" + status.id.toString()
            var contenderIds = intArrayOf()
            for (key in tags.contenderA) {
                if (status.text.contains(key, true)) {
                    contenderIds = contenderIds.plus(1)
                    break
                }
            }
            for (key in tags.contenderB) {
                if (status.text.contains(key, true)) {
                    contenderIds = contenderIds.plus(2)
                    break
                }
            }
            for (id in contenderIds) {
                eventQueue.addEvent(MentionEvent(id, "twitter",
                        url, status.user.name, status.text,
                        status.user.profileImageURLHttps.toString(), status.createdAt))
            }
            println("${status.text} $url")
        }

        override fun onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {}

        override fun onTrackLimitationNotice(numberOfLimitedStatuses: Int) {
            println("Got track limitation notice:" + numberOfLimitedStatuses)
        }

        override fun onScrubGeo(userId: Long, upToStatusId: Long) {}

        override fun onStallWarning(warning: StallWarning) {
            println("Got stall warning:" + warning)
        }

        override fun onException(ex: Exception) {
            ex.printStackTrace()
        }
    }


    override fun handleEvent(event: Event) {
        when (event) {
            is ExitEvent -> {
                twitterStream.cleanUp(); // shutdown internal stream consuming thread
                twitterStream.shutdown(); // Shuts down internal dispatcher thread shared by all TwitterStream instan
                eventQueue.removeHandler(this)
                println("Twitter job cancelled")
            }
        }
    }

    override fun processData() {
        val cb = ConfigurationBuilder()
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(tokens.consumerKey)
                .setOAuthConsumerSecret(tokens.consumerSecret)
                .setOAuthAccessToken(tokens.accessToken)
                .setOAuthAccessTokenSecret(tokens.accessTokenSecret)
        twitterStream = TwitterStreamFactory(cb.build()).instance
        Twitter4jFixer.addListener(twitterStream, listener)
        val track = emptyArray<String>().plus(tags.contenderA).plus(tags.contenderB)
        val trackList = track.joinToString(",")
        twitterStream.filter(FilterQuery(trackList))
        eventQueue.addHandler(this)
    }

}