package org.mentionbattle.snadapter.impl.common

import org.apache.commons.lang.StringEscapeUtils
import org.json.JSONObject
import org.mentionbattle.snadapter.api.core.SocialNetworkInitializer
import org.mentionbattle.snadapter.impl.eventsystem.MentionEvent
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet





@SocialNetworkInitializer("Database")
class Database(map : HashMap<String, Any>) {
    private val name : String by map
    private val lockObject : Any = Any()
    init {
        DriverManager.getConnection("jdbc:sqlite:$name.db").use { c ->
            val sql = "CREATE TABLE IF NOT EXISTS MENTIONS" +
                    "(ID             INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    " Contender      INTEGER    NOT NULL, " +
                    " Mention        TEXT    NOT NULL," +
                    " Time           TEXT    NOT NULL)"
            val stmt = c.createStatement()

            stmt.executeUpdate(sql)
            stmt.close()
        }
    }

    fun addMention(mentionEvent: MentionEvent) {
        synchronized(lockObject) {

            try {
                DriverManager.getConnection("jdbc:sqlite:$name.db").use { c ->
                    val sql = "INSERT INTO MENTIONS " +
                            "(Contender, Mention, Time) VALUES " +
                            "(${mentionEvent.contender}, " +
                            "\'${StringEscapeUtils.escapeSql(mentionEvent.createJson().toString())}\'," +
                            " datetime('now'))"
                    c.createStatement().use {
                        it.executeUpdate(sql)
                    }
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }

    fun contenderMentionCount(contender: Contender) : Int {
        synchronized(lockObject) {
            try {
                DriverManager.getConnection("jdbc:sqlite:$name.db").use { c ->
                    c.createStatement().use { stmt ->
                        stmt.executeQuery("SELECT COUNT() as TOTAL FROM MENTIONS " +
                                "WHERE MENTIONS.Contender = ${contender.id}").use {
                            val result = it.getInt("TOTAL")
                            return result
                        }
                    }
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }

    fun getLastContendersMentions(count : Int, contender: Contender) : Array<JSONObject>{
        synchronized(lockObject) {
            DriverManager.getConnection("jdbc:sqlite:$name.db").use { c ->
                c.prepareStatement("SELECT MENTION FROM MENTIONS WHERE MENTIONS.Contender = ${contender.id} " +
                        "ORDER BY date(TIME) DESC LIMIT 100").use { stmt ->
                    stmt.executeQuery().use { result ->
                        val list = mutableListOf<JSONObject>()
                        while (result.next()) {
                            val ans = result.getString("MENTION")
                            list.add(JSONObject(ans))
                            result.next()
                        }
                        return list.toTypedArray();
                    }
                }
            }
        }
    }
}