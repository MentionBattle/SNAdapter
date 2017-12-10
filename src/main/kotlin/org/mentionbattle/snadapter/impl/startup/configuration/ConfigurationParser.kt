package org.mentionbattle.snadapter.impl.startup.configuration

import org.json.JSONArray
import org.json.JSONObject
import org.mentionbattle.snadapter.impl.common.Contender
import java.nio.file.Files
import java.nio.file.Paths

class ConfigurationParser {
    fun parse(path : String) : Configuration {
        if (!Files.exists(Paths.get(path)))  {
            throw ConfigurationParserException("The file does not exist")
        }
        val text = Files.readAllLines(Paths.get(path)).joinToString(separator = "")
        val json = JSONObject(text)

        val port = json["port"] as Int
        val config = Configuration(port)
        val socialNetworks = json["socialNetworks"] as JSONArray
        for (sn in socialNetworks) {
            config.socialNetworks.add(sn as String)
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
        config.contenders.add(parseContender(json["contenderA"] as JSONObject))
        config.contenders.add(parseContender(json["contenderB"] as JSONObject))
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

    private fun parseContender(contenderJson : JSONObject) : Contender {
        val name =  contenderJson["name"] as String
        val imagePath = contenderJson["image"] as String
        return Contender(name, imagePath)
    }
}