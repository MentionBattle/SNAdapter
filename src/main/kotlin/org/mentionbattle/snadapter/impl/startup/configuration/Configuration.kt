package org.mentionbattle.snadapter.impl.startup.configuration

class Configuration(port: Int) {
    val socicalNetworks : MutableList<String> = mutableListOf()
    val socialNetworkInitializers : MutableMap<String, MutableMap<String, Any?>> = mutableMapOf()
}