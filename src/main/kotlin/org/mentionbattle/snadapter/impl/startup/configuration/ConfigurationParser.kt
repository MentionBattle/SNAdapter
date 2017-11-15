package org.mentionbattle.snadapter.impl.startup.configuration

import org.json.JSONArray
import org.json.JSONObject
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ConfigurationParser {
    fun Parse(path : String) : Configuration {
        if (!Files.exists(Paths.get(path)))  {
            throw ConfigurationParserException("The file does not exist")
        }
        var text = Files.readAllLines(Paths.get(path)).joinToString(separator = "")
        val json = JSONObject(text)

        val port = json["port"] as Integer
        val config = Configuration(port as Int)
        val socialNetworks = json["socialNetworks"] as JSONArray
        for (sn in socialNetworks) {
            config.socicalNetworks.add(sn as String)
        }

        val initializers = json["socialNetworkInitializers"] as JSONArray
        for (sn in initializers) {
            if (sn is JSONObject) {
                for (initializerName in sn.keys()) {
                    val initializer = sn[initializerName] as JSONObject
                    val initializerFields = mutableMapOf<String, Any>()
                    for (key in initializer.keys()) {
                        val item = initializer[key]
                        when (item) {
                            is Integer -> initializerFields[key] = item as Int
                            is String -> initializerFields[key] = item
                            is JSONArray -> {
                                val items = mutableListOf<String>()
                                for (s in item) {
                                    if (s is String) {
                                        items.add(s)
                                    } else {
                                        throw ConfigurationParserException("Only strings array supports in SNI")
                                    }
                                }
                                initializerFields[key] = items
                            }
                        }
                    }
                    config.socialNetworkInitializers[initializerName] = initializerFields
                }
            } else {
                throw ConfigurationParserException("Json object expected")
            }
        }
        return config
    }
}