package org.mentionbattle.snadapter.impl.startup.configuration

class Configuration(port: Int) {
    val port = port
    val socialNetworks: MutableList<String> = mutableListOf()
    val socialNetworkInitializers : MutableMap<String, MutableMap<String, Any>> = mutableMapOf()
}