package org.mentionbattle.snadapter.impl.eventsystem

import org.json.JSONObject
import org.mentionbattle.snadapter.api.core.eventsystem.Event
import java.util.*
import java.text.SimpleDateFormat
import java.text.DateFormat
import java.util.TimeZone
import kotlin.collections.HashMap


class MentionEvent(contender : Int, from : String, url: String, userName : String,
                   text: String, avatarUrl: String, timeStamp: Date) : Event {

    val contender = contender
    val message : HashMap<String, Any> = hashMapOf()

    init {

        val tz = TimeZone.getTimeZone("UTC")
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'") // Quoted "Z" to indicate UTC, no timezone offset
        df.timeZone = tz

        message["url"] = url;
        message["name"] = userName
        message["from"] = from
        message["text"] = text
        message["avatarUrl"] = avatarUrl
        message["timestamp"] = df.format(timeStamp)
    }

    fun createJson() : JSONObject {
        return JSONObject(
                hashMapOf("contender" to contender, "msg" to JSONObject(message))
        )
    }
}