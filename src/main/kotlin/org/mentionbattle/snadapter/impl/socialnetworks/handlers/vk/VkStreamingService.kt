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
import org.asynchttpclient.ws.WebSocketListener
import org.mentionbattle.snadapter.impl.eventsystem.PrimitiveEventQueue
import org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.eventhandlers.HashedTagToContentendIdWithTag
import org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.eventhandlers.VkMsgHandler
import org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.objects.RuleInfo

import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.Tags
import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.VkServiceAuth
import java.io.Closeable
import java.util.*
import kotlin.collections.ArrayList

internal class VkStreamingService(auth: VkServiceAuth, tags: Tags, eventQueue: PrimitiveEventQueue)
    : Closeable {
    private val auth = auth
    private val hashedTags: HashedTagToContentendIdWithTag = HashMap()

    private var eventQueue = eventQueue
    private var isConnected = false

    private lateinit var streamingClient: ClosableVkStreamingApiClient
    private lateinit var apiClient: VkApiClient
    private lateinit var streamingActor: StreamingActor
    private lateinit var serviceActor: ServiceActor
    private lateinit var websocket: WebSocket

    private var msgHandlers: MutableList<VkMsgHandler> = ArrayList()

    init {
        initConteder(tags.contenderA, 1)
        initConteder(tags.contenderB, 2)
    }

    private fun initConteder(synonyms: List<String>, id: Int) {
        synonyms.map { RuleInfo(it.hashCode().toString(), it) }.forEach {
            hashedTags.put(it.tag, Pair(id, it.value))
        }
    }

    fun ensureRules() {
        if (!isConnected) initApiConnection()

        val oldTags = getAllRules().map { it.tag }
        val common = hashedTags.keys.intersect(oldTags)
        val shouldDelete = oldTags.subtract(common)
        val shouldAdd = hashedTags.keys.subtract(common)

        shouldDelete.forEach { deleteRule(it) }
        shouldAdd.forEach {
            val value: String = hashedTags[it]?.second ?: ""
            if (!value.isEmpty()) addRule(RuleInfo(it, value))
        }
    }

    private fun initApiConnection() {
        val transportClient = HttpTransportClient()
        streamingClient = ClosableVkStreamingApiClient(transportClient)
        apiClient = VkApiClient(transportClient)

        serviceActor = ServiceActor(auth.appId, auth.serviceToken)
        val getServerUrlResponse: GetServerUrlResponse = apiClient.streaming().getServerUrl(serviceActor).execute()
        streamingActor = StreamingActor(getServerUrlResponse.getEndpoint(), getServerUrlResponse.getKey())

        isConnected = true
    }


    private fun getAllRules(): List<StreamingRule> {
        val rulesResponse: StreamingGetRulesResponse = streamingClient.rules().get(streamingActor).execute()
        return rulesResponse.rules
    }

    private fun addRule(ruleInfo: RuleInfo) {
        if (!hashedTags.containsKey(ruleInfo.tag)) return

        println("add new rule: tag=${ruleInfo.tag} value=${ruleInfo.value}")
        val response: StreamingResponse = streamingClient.rules()
                .add(streamingActor, ruleInfo.tag, ruleInfo.value).execute()
    }

    private fun deleteRule(tag: String) {
        if (!hashedTags.containsKey(tag)) return

        println("delete rule: tag=$tag value=${hashedTags[tag]?.second ?: ""}\"")
        val response: StreamingResponse = streamingClient.rules().delete(streamingActor, tag).execute()
    }

    fun addMsgHandler(handler: VkMsgHandler) {
        println("add handler: ${handler.javaClass.canonicalName}")
        msgHandlers.add(handler)
    }

    fun startListenMsgStream() {
        println("start listen VK events")

        getAllRules().forEach { println(it) }

        val listener = object : WebSocketListener {
            override fun onOpen(w: WebSocket?) {}

            override fun onClose(w: WebSocket?) {}

            override fun onError(t: Throwable?) {
                websocket = initWebSocket()
                websocket.addWebSocketListener(this)
            }
        }

        websocket = initWebSocket()
        websocket.addWebSocketListener(listener)
    }

    private fun initWebSocket(): WebSocket {
        return streamingClient.stream().get(streamingActor, object : StreamingEventHandler() {
            override fun handle(message: StreamingCallbackMessage) {
                println("listen vk event")
                msgHandlers.forEach { it.handle(message, hashedTags) }
            }
        }).execute()
    }

    override fun close() {
        if (isConnected) {
            websocket.close()
            streamingClient.close()
        }
    }
}

