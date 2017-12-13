package org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk

import com.vk.api.sdk.client.TransportClient
import com.vk.api.sdk.streaming.clients.VkStreamingApiClient

class ClosableVkStreamingApiClient(transportClient: TransportClient?) : VkStreamingApiClient(transportClient) {
    fun close() {
        asyncHttpClient.close()
    }
}