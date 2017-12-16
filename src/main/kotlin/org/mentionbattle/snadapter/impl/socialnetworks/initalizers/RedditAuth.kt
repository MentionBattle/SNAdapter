package org.mentionbattle.snadapter.impl.socialnetworks.initalizers

import org.mentionbattle.snadapter.api.core.SocialNetworkInitializer

/**
 * @author Novik Dmitry ITMO University
 */
@SocialNetworkInitializer("RedditAuth")
internal class RedditAuth(map: Map<String, String>) {
    val user : String by map
    val url : String by map
    val clientID : String by map
    val clientSecret : String by map
    val redirectUrl : String by map
}