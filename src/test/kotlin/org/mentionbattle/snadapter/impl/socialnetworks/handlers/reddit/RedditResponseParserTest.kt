package org.mentionbattle.snadapter.impl.socialnetworks.handlers.reddit

import org.json.JSONException
import org.junit.Test
import java.nio.file.Files.readAllLines
import java.nio.file.Paths

/**
 * @author Novik Dmitry ITMO University
 */
class RedditResponseParserTest {
    private val response = Paths.get("src", "test", "resources", "redditResponse.txt")


    @Test(expected = JSONException::class)
    fun parseEmpty() {
        parseRedditResponse("");
    }

    @Test
    fun parse() {
        parseRedditResponse(readAllLines(response).joinToString(""))
    }

}