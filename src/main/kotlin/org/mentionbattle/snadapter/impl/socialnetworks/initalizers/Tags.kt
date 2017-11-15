package org.mentionbattle.snadapter.impl.socialnetworks.initalizers

import org.mentionbattle.snadapter.api.core.SocialNetworkInitializer

@SocialNetworkInitializer("Tags")
public class Tags(map : MutableMap<String, Any>) {
    val words : MutableList<String> by map

}