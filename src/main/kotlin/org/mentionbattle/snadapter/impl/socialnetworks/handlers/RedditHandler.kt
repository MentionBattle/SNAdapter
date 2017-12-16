package org.mentionbattle.snadapter.impl.socialnetworks.handlers

import net.dean.jraw.RedditClient
import net.dean.jraw.http.OkHttpNetworkAdapter
import net.dean.jraw.http.UserAgent
import net.dean.jraw.oauth.Credentials
import net.dean.jraw.oauth.OAuthHelper
import org.mentionbattle.snadapter.api.core.SocialNetwork
import org.mentionbattle.snadapter.api.core.eventsystem.Event
import org.mentionbattle.snadapter.api.core.eventsystem.EventHandler
import org.mentionbattle.snadapter.api.core.socialnetworks.SocialNetworkHandler
import org.mentionbattle.snadapter.impl.eventsystem.ExitEvent
import org.mentionbattle.snadapter.impl.eventsystem.LogEvent
import org.mentionbattle.snadapter.impl.eventsystem.MentionEvent
import org.mentionbattle.snadapter.impl.eventsystem.PrimitiveEventQueue
import org.mentionbattle.snadapter.impl.socialnetworks.handlers.reddit.RedditResponseParserException
import org.mentionbattle.snadapter.impl.socialnetworks.handlers.reddit.parseRedditResponse
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.RedditAuth
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.Tags
import java.util.*


/**
 * @author Novik Dmitry ITMO University
 */
@SocialNetwork("Reddit")
internal class RedditHandler(redditAuth: RedditAuth, tags: Tags, eventQueue: PrimitiveEventQueue) : SocialNetworkHandler, EventHandler {
    private var work: Boolean
    private val redditClient: RedditClient
    private val tags = tags
    private val eventQueue = eventQueue
    private var timestamp = Date()

    init {
        work = true
        val userAgent = UserAgent("MentionBattle", redditAuth.url, "v0.1", redditAuth.user)
        val adapter = OkHttpNetworkAdapter(userAgent)
        val credentials = Credentials.userless(redditAuth.clientID, redditAuth.clientSecret, UUID.randomUUID())
        redditClient = OAuthHelper.automatic(adapter, credentials)
    }

    override fun handleEvent(event: Event) {
        when (event) {
            is ExitEvent -> {
                synchronized(work) {
                    work = false
                }
                eventQueue.removeHandler(this)
            }
        }
    }

    override fun processData() {
        while (work) {
            val response = redditClient.request { it.url("http://www.reddit.com/r/all/comments/.json?limit=100") }
            try {
                val comments = parseRedditResponse(response.body)

                var current = timestamp
                for (comment in comments) {
                    if (comment.date.after(timestamp)) {
                        current = comment.date

                        val contenderIds = calculate(comment.text, tags)
                        for (id in contenderIds) {
                            eventQueue.addEvent(MentionEvent(id, "reddit",
                                    comment.url,
                                    comment.user,
                                    comment.text,
                                    "http://i.imgur.com/sdO8tAw.png",
                                    comment.date))
                        }
                    }
                }
                timestamp = current
            } catch (e: RedditResponseParserException) {
                eventQueue.addEvent(LogEvent(e.toString()))
            }

            Thread.sleep(2000)
        }
    }
}