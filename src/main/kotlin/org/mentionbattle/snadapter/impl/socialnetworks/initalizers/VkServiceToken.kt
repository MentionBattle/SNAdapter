package org.mentionbattle.snadapter.impl.socialnetworks.initalizers

import org.mentionbattle.snadapter.api.core.Component
import org.mentionbattle.snadapter.api.core.SocialNetworkInitializer

@Component
@SocialNetworkInitializer("VkServiceToken")
internal class VkServiceToken(val map: Map<String, Any>) {
    val accessToken:String by map
}