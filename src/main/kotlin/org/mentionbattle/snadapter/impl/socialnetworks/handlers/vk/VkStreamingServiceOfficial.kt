package org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk

import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.ServiceActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import com.vk.api.sdk.objects.streaming.responses.GetServerUrlResponse
import com.vk.api.sdk.streaming.clients.StreamingEventHandler
import com.vk.api.sdk.streaming.clients.actors.StreamingActor
import com.vk.api.sdk.streaming.objects.StreamingCallbackMessage
import com.vk.api.sdk.streaming.objects.StreamingRule
import com.vk.api.sdk.streaming.objects.responses.StreamingGetRulesResponse
import com.vk.api.sdk.streaming.objects.responses.StreamingResponse
import org.asynchttpclient.ws.WebSocket
import org.mentionbattle.snadapter.impl.eventsystem.PrimitiveEventQueue
import org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.eventhandlers.HashedTagToContentendIdWithTag
import org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.eventhandlers.VkEventHandler
import org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.objects.RuleInfo

import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.Tags
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.VkServiceAuth
import java.io.Closeable
import java.util.*
import kotlin.collections.ArrayList

internal class VkStreamingServiceOfficial(auth: VkServiceAuth, tags: Tags, eventQueue: PrimitiveEventQueue)
    : VkStreamingService, Closeable {
    private val hashedTags: HashedTagToContentendIdWithTag = HashMap()

    private val auth = auth
    private var streamingClient: ClosableVkStreamingApiClient? = null
    private var apiClient: VkApiClient? = null
    private var actor: StreamingActor? = null

    private var websocket: WebSocket? = null
    private var eventQueue = eventQueue
    private var handlers: MutableList<VkEventHandler> = ArrayList()

    init {

        val transportClient = HttpTransportClient()
        streamingClient = ClosableVkStreamingApiClient(transportClient)
        apiClient = VkApiClient(transportClient)

        val serviceActor = ServiceActor(auth.appId, auth.serviceToken)
        val getServerUrlResponse: GetServerUrlResponse = apiClient!!.streaming().getServerUrl(serviceActor).execute()
        actor = StreamingActor(getServerUrlResponse.getEndpoint(), getServerUrlResponse.getKey())

        deleteAllRules()
        initConteder(tags.contenderA, 1)
        initConteder(tags.contenderB, 2)
    }

    private fun initConteder(synonyms: List<String>, id: Int) {
        synonyms.map { RuleInfo(it.hashCode().toString(), it) }.forEach {
            try {
                addNewRule(it)
            } catch (e: Exception) {
                // ignore: tag already exist
            }
            hashedTags.put(it.tag, Pair(id, it.value))
        }
    }

    override fun getAllRules(): List<StreamingRule> {
        val rulesResponse: StreamingGetRulesResponse = streamingClient!!.rules().get(actor).execute()
        return rulesResponse.rules
    }

    override fun addNewRule(ruleInfo: RuleInfo): Boolean {
        println("add new rule: tag=${ruleInfo.tag} value=${ruleInfo.value}")

        val response: StreamingResponse = streamingClient!!.rules().add(actor, ruleInfo.tag, ruleInfo.value).execute()

        if (response.code != 200) {
            println("code: ${response.code}, error: ${response.error}")
            return false
        }

        return true
    }

    override fun deleteRule(tag: String): Boolean {
        println("delete rule: tag=$tag")

        val response: StreamingResponse = streamingClient!!.rules().delete(actor, tag).execute()

        if (response.code != 200) {
            println("code: ${response.code}, error: ${response.error}")
            return false
        }
        return true
    }

    override fun deleteAllRules() {
        getAllRules().forEach { deleteRule(it.tag) }
    }

    override fun addHandler(handler: VkEventHandler) {
        println("add handler: ${handler.javaClass}")
        handlers.add(handler)
    }

    override fun startListenEvents() {
        println("start listen VK events")
        websocket = streamingClient!!.stream().get(actor, object : StreamingEventHandler() {
            override fun handle(message: StreamingCallbackMessage) {
                println("listen vk event")
                handlers.forEach { it.handle(message, hashedTags) }
            }
        }).execute()
    }

    override fun close() {
        websocket?.close()
        streamingClient?.close()
    }
}

