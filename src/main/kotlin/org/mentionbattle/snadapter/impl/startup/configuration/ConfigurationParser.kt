package org.mentionbattle.snadapter.impl.startup.configuration

import org.json.JSONArray
import org.json.JSONObject
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ConfigurationParser {
    fun parse(path : String) : Configuration {
        if (!Files.exists(Paths.get(path)))  {
            throw ConfigurationParserException("The file does not exist")
        }
        var text = Files.readAllLines(Paths.get(path)).joinToString(separator = "")
        val json = JSONObject(text)

        val port = json["port"] as Int
        val config = Configuration(port)
        val socialNetworks = json["socialNetworks"] as JSONArray
        for (sn in socialNetworks) {
            config.socicalNetworks.add(sn as String)
        }

        val initializers = json["socialNetworkInitializers"] as JSONArray
        for (sn in initializers) {
            if (sn is JSONObject) {
                for (initializerName in sn.keys()) {
                    config.socialNetworkInitializers[initializerName] = parseInitializer(sn[initializerName] as JSONObject)
                }
            } else {
                throw ConfigurationParserException("Json object expected")
            }
        }
        return config
    }

    private fun parseInitializer(initializer : JSONObject): MutableMap<String, Any> {
        val initializerFields = mutableMapOf<String, Any>()
        for (key in initializer.keys()) {
            val item = initializer[key]
            when (item) {
                is Int -> initializerFields[key] = item
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
        return initializerFields
    }
}