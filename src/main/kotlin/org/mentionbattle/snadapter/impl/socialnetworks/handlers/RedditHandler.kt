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
import org.mentionbattle.snadapter.impl.eventsystem.MentionEvent
import org.mentionbattle.snadapter.impl.eventsystem.PrimitiveEventQueue
import org.mentionbattle.snadapter.impl.socialnetworks.handlers.reddit.parseRedditResponse
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.RedditAuth
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.Tags
import java.time.Instant
import java.util.*
import java.util.Date.from


/**
 * @author Novik Dmitry ITMO University
 */
@SocialNetwork("Reddit")
internal class RedditHandler(redditAuth: RedditAuth, tags: Tags, eventQueue: PrimitiveEventQueue) : SocialNetworkHandler, EventHandler {
    private var work: Boolean
    private val redditClient: RedditClient
    private val tags = tags
    private val eventQueue = eventQueue
    private var timespamp = Date()

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
            }
        }

    }

    override fun processData() {
        while (work) {
            val response = redditClient.request { it.url("http://www.reddit.com/r/all/comments/.json?limit=100") }
            val comments = parseRedditResponse(response.body)

            var current = timespamp
            for (comment in comments) {
                if (comment.date.after(timespamp)) {
                    current = comment.date

                    var contenderIds = intArrayOf()
                    for (key in tags.contenderA) {
                        if (comment.text.contains(key, true)) {
                            contenderIds = contenderIds.plus(1)
                            break
                        }
                    }
                    for (key in tags.contenderB) {
                        if (comment.text.contains(key, true)) {
                            contenderIds = contenderIds.plus(2)
                            break
                        }
                    }
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
            timespamp = current

            Thread.sleep(2000)
        }
    }
}