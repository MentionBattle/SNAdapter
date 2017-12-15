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
import org.mentionbattle.snadapter.impl.eventsystem.PrimitiveEventQueue
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.RedditAuth
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.Tags


/**
 * @author Novik Dmitry ITMO University
 */
@SocialNetwork("Reddit")
internal class RedditHandler(redditAuth: RedditAuth, tags: Tags, eventQueue: PrimitiveEventQueue) : SocialNetworkHandler, EventHandler {
    private var work: Boolean
    private val redditClient: RedditClient
    private val tags = tags
    private val eventQueue = eventQueue

    init {
        work = true
        val userAgent = UserAgent("bot", redditAuth.url, "v0.1", redditAuth.user)
        val adapter = OkHttpNetworkAdapter(userAgent)
        redditClient = OAuthHelper.automatic(
                adapter,
                Credentials.webapp(
                        redditAuth.clientID,
                        redditAuth.clientSecret,
                        redditAuth.redirectUrl))
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
            val response = redditClient.request { it.url("https://www.reddit.com/r/all/comments/.json?limit=1") }
            println(response.body)

            Thread.sleep(1000)
        }
    }

}