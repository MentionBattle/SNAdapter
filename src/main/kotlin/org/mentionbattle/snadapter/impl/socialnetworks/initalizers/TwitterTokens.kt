package org.mentionbattle.snadapter.impl.socialnetworks.initalizers

import org.mentionbattle.snadapter.api.core.SocialNetworkInitializer

@SocialNetworkInitializer("TwitterTokens")
internal class TwitterTokens(map: Map<String, String>) {
    val consumerKey: String by map
    val consumerSecret: String by map
    val accessToken: String by map
    val accessTokenSecret: String by map
}