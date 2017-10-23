package org.mentionbattle.snadapter.impl.socialnetworks.handlers

import org.mentionbattle.snadapter.api.core.SocialNetwork
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.ServiceToken


@SocialNetwork("Twitter")
internal class TwitterHandler(token:ServiceToken) : AbstractSNHandler() {

    val token = token

    override fun processData() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}