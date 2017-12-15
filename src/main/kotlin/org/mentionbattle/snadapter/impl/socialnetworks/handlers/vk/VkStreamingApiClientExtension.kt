package org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk

import com.vk.api.sdk.streaming.clients.VkStreamingApiClient

fun VkStreamingApiClient.close() = asyncHttpClient.close()