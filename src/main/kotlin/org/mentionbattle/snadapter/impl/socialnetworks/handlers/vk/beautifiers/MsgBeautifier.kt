package org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.beautifiers

import com.vk.api.sdk.client.Lang
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.ServiceActor
import com.vk.api.sdk.queries.users.UserField
import org.apache.logging.log4j.LogManager
import org.mentionbattle.snadapter.impl.socialnetworks.handlers.vk.objects.VkAccount

class MsgBeautifier(val apiClient: VkApiClient, val serviceActor: ServiceActor) {
    private val logger = LogManager.getLogger()

    fun findNameAndAvatarUrl(id: Int): VkAccount {
        val withoutAvatar = "https://pbs.twimg.com/profile_images/681477631104135168/0F1tsp_D.jpg"

        val user = apiClient.users().get(serviceActor)
                .userIds(id.toString())
                .fields(UserField.NICKNAME,
                        UserField.HAS_PHOTO,
                        UserField.PHOTO_100)
                .lang(Lang.RU)
                .execute()
                .getOrNull(0)

        if (user == null) return VkAccount(id, "unknown", withoutAvatar)

        return VkAccount(id, "${user.firstName} ${user.lastName}",
                if (user.hasPhoto()) user.photo100 else withoutAvatar)
    }

    fun cleanUpNamesInText(text: String): String {
        val name = """[^\\]+"""
        val replace = text.replace(Regex("""\[id[0-9]+\|$name\]"""),
                { it.value.split("|")[1].substringBefore("]") })
        return replace
    }

    fun shieldExclamationPoint(text: String): String {
        return text.replace(Regex("""&#33"""), "!")
    }
}