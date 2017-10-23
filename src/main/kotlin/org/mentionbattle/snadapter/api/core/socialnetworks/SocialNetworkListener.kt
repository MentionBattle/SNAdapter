package org.mentionbattle.snadapter.api.core.socialnetworks

internal interface SocialNetworkListener {
    fun HandleEvent(eventType : SocialNetworkEventType, data : SocialNetworkData)
}