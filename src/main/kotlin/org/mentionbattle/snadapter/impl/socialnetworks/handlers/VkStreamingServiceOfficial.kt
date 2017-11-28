package org.mentionbattle.snadapter.impl.socialnetworks.handlers

import com.vk.api.sdk.httpclient.HttpTransportClient
import com.vk.api.sdk.streaming.clients.StreamingEventHandler
import com.vk.api.sdk.streaming.clients.VkStreamingApiClient
import com.vk.api.sdk.streaming.clients.actors.StreamingActor
import com.vk.api.sdk.streaming.objects.StreamingCallbackMessage
import com.vk.api.sdk.streaming.objects.StreamingRule
import com.vk.api.sdk.streaming.objects.responses.StreamingGetRulesResponse
import com.vk.api.sdk.streaming.objects.responses.StreamingResponse

internal class VkStreamingServiceOfficial(serviceToken: String,
                                          endpoint: String = "streaming.vk.com") : VkStreamingService {
    private val client: VkStreamingApiClient
    private val actor: StreamingActor

    private var tagSequence = generateSequence(1L) { it + 1 }.iterator()
    //  TODO use bimap
    private var tagToValue: MutableMap<Long, String>
    private var valueToTag: MutableMap<String, Long>
    // TODO unremovable "Котики".. WTF?!
    private val strangeTag = "Котики"

    private var handlers: MutableMap<String, VkEventHandler> = HashMap()
    val defaultHandler: (StreamingCallbackMessage) -> Unit


    init {
        client = VkStreamingApiClient(HttpTransportClient())
        actor = StreamingActor(endpoint, serviceToken)

        println("init tags:")
        valueToTag = getAllRules().filter({ it.tag != "Котики" })
                .associateBy({ it.value }, { it.tag.toLong() }).toMutableMap()
        tagToValue = valueToTag.entries.associateBy({ it.value }, { it.key }).toMutableMap()

        defaultHandler = fun(message: StreamingCallbackMessage) {
            val tags = message.event.tags
            if (strangeTag in tags) return

            println("Event on tags: ${tags}")
            tags.forEach {
                println("""
                    |eventUrl: ${message.event.eventUrl}
                    |tag: ${tagToValue.get(it.toLong())}
                    |text: ${message.event.text}
                    |""".trimMargin())
            }
        }
    }

    override fun getAllRules(): List<StreamingRule> {
        val rulesResponse: StreamingGetRulesResponse = client.rules().get(actor).execute()
        rulesResponse.rules.forEach({ println("tag=${it.tag} value=${it.value}") })
        return rulesResponse.rules
    }

    override fun addNewRule(value: String): Boolean {
        val tag = tagSequence.next()
        println("add new rule: tag=$tag value=$value")

        val response: StreamingResponse = client.rules().add(actor, tag.toString(), value).execute()

        if (response.code != 200) {
            println("code: ${response.code}, error: ${response.error}")
            return false
        }

        valueToTag.put(value, tag)
        tagToValue.put(tag, value)
        return true
    }

    override fun deleteRule(tag: String): Boolean {
        val value = tagToValue.get(tag.toLong())
        println("delete rule: tag=$tag value=$value")

        val response: StreamingResponse = client.rules().delete(actor, tag).execute()

        if (response.code != 200) {
            println("code: ${response.code}, error: ${response.error}")
            return false
        }

        valueToTag.remove(value)
        tagToValue.remove(tag.toLong())
        return true
    }

    override fun addHandler(handlerName: String, handler: VkEventHandler) {
        println("add handler: $handlerName")
        handlers.put(handlerName, handler)
    }

    override fun startListenEvents() {
        println("start listen VK events")
        client.stream().get(actor, object : StreamingEventHandler() {
            override fun handle(message: StreamingCallbackMessage) {
                handlers.forEach { it.value.invoke(message) }
            }
        }).execute()
    }

    override fun deleteAllRules() {
        tagToValue.keys.forEach { deleteRule(it.toString()) }
    }
}

