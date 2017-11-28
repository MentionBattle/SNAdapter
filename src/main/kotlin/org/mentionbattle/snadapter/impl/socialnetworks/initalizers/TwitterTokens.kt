package org.mentionbattle.snadapter.impl.socialnetworks.initalizers

import org.mentionbattle.snadapter.api.core.Component
import org.mentionbattle.snadapter.api.core.SocialNetworkInitializer

@Component
@SocialNetworkInitializer("TwitterTokens")
internal class TwitterTokens(val map: Map<String, String>) {
    val consumerKey= map.get("consumerKey")
    val consumerSecret= map.get("consumerSecret")
    val accessToken= map.get("accessToken")
    val accessTokenSecret= map.get("accessTokenSecret")

}