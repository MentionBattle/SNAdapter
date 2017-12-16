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
    private val tweetURLScheme = "https://twitter.com/%s/status/%s"

    val listener = object : StatusListener {
        override fun onStatus(status: Status) {
            val contenderIds = calculate(status.text, tags)
            for (id in contenderIds) {
                eventQueue.addEvent(MentionEvent(id, "twitter",
                        tweetURLScheme.format(status.user.screenName, status.id.toString()),
                        status.user.name, status.text, status.user.profileImageURLHttps.toString(),
                        status.createdAt))
            }
        }

        override fun onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {}

        override fun onTrackLimitationNotice(numberOfLimitedStatuses: Int) {}

        override fun onScrubGeo(userId: Long, upToStatusId: Long) {}

        override fun onStallWarning(warning: StallWarning) {}

        override fun onException(ex: Exception) {
            eventQueue.addEvent(LogEvent(ex.localizedMessage))
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