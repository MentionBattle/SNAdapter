package org.mentionbattle.snadapter.impl.socialnetworks.handlers.reddit

import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.util.*

/**
 * @author Novik Dmitry ITMO University
 */
class RedditResponseParser() {
    fun parse(str: String): List<RedditComment> {
        val json = JSONObject(str)
        val data = json["data"] as JSONObject
        val children = data["children"] as JSONArray
        val result = mutableListOf<RedditComment>()
        for (child in children) {
            if (child is JSONObject) {
                val data = child["data"] as JSONObject
                val url = "https://www.reddit.com${data["permalink"]}"
                val text = data["body"] as String
                val date = Date(1000L * data["created_utc"].toString().toDouble().toLong())
                val author = data["author"] as String
                result.add(RedditComment(text, author, date, url))
            } else {
                throw RedditResponseParserException("JSONObject was expected")
            }
        }
        return result
    }
}

fun parseRedditResponse(text: String) : List<RedditComment> = RedditResponseParser().parse(text)