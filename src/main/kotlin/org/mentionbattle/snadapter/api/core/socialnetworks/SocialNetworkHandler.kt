package org.mentionbattle.snadapter.api.core.socialnetworks

internal interface SocialNetworkHandler {
    fun addListener(listener: SocialNetworkListener);
    fun removeListener(listener: SocialNetworkListener);

    fun processData();
}