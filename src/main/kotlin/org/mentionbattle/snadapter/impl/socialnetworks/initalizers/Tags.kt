package org.mentionbattle.snadapter.impl.socialnetworks.initalizers

import org.mentionbattle.snadapter.api.core.SocialNetworkInitializer

@SocialNetworkInitializer("Tags")
public class Tags(map : MutableMap<String, Any>) {
    val contenderA : MutableList<String> by map
    val contenderB : MutableList<String> by map

}