package org.mentionbattle.snadapter.impl.socialnetworks.initalizers

import org.mentionbattle.snadapter.api.core.SocialNetworkInitializer

@SocialNetworkInitializer("VkServiceAuth")
internal class VkServiceAuth(val map: Map<String, Any>) {
    val appId: Int by map
    val serviceToken:String by map
}