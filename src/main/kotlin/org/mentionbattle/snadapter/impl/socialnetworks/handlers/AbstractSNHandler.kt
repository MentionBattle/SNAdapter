package org.mentionbattle.snadapter.impl.socialnetworks.handlers

import org.mentionbattle.snadapter.api.core.socialnetworks.SocialNetworkData
import org.mentionbattle.snadapter.api.core.socialnetworks.SocialNetworkEventType
import org.mentionbattle.snadapter.api.core.socialnetworks.SocialNetworkHandler
import org.mentionbattle.snadapter.api.core.socialnetworks.SocialNetworkListener

internal abstract class AbstractSNHandler : SocialNetworkHandler {
    public var listeners:MutableSet<SocialNetworkListener> = mutableSetOf()


    //TODO: it's possible to remove eventype, because we can use pattern matching. see when {} at "learn kotlin"

    override fun notifyListeners(data: SocialNetworkData, eventType: SocialNetworkEventType) {
        for (l in listeners)
            l.HandleEvent(eventType, data)
    }

    override fun addListener(listener: SocialNetworkListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: SocialNetworkListener) {
        listeners.remove(listener)
    }
}