package org.mentionbattle.snadapter.impl.startup.configuration

import org.mentionbattle.snadapter.impl.common.Contender

class Configuration(port: Int) {
    val port = port
    val socialNetworks: MutableList<String> = mutableListOf()
    val socialNetworkInitializers : MutableMap<String, MutableMap<String, Any>> = mutableMapOf()
    val contenders = mutableListOf<Contender>()
}