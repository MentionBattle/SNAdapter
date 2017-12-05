package org.mentionbattle.snadapter.impl.eventsystem

import org.mentionbattle.snadapter.api.core.eventsystem.Event
import java.util.*
import java.text.SimpleDateFormat
import java.text.DateFormat
import java.util.TimeZone





class MentionEvent(contender : Int, from : String, url: String, userName : String,
                   text: String, avatarUrl: String, timeStamp: Date) : Event {

    private val contender = contender
    private val url = url
    private val userName = userName
    private val from = from
    private val avatarUrl = avatarUrl
    private val text = text
    private val timeStamp = timeStamp

    fun packToJson() : String {
        val tz = TimeZone.getTimeZone("UTC")
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'") // Quoted "Z" to indicate UTC, no timezone offset
        df.timeZone = tz
        val sb = StringBuilder()
        sb.append("{")
                .append("\"contender\" : ").append(1).append(",")
                .append("\"msg\" : {")

                    .append("\"url\" : ").append("\"").append(url).append("\"").append(",")
                    .append("\"name\" : ").append("\"").append(userName).append("\"").append(",")
                    .append("\"from\" : ").append("\"").append(from).append("\"").append(",")
                    .append("\"text\" : ").append("\"").append(text).append("\"").append(",")
                    .append("\"avatarUrl\" : ").append("\"").append(avatarUrl).append("\"").append(",")
                    .append("\"timestamp\" : ").append("\"").append(df.format(timeStamp)).append("\"")
                .append("}")
          .append("}")
        return sb.toString()
    }
}