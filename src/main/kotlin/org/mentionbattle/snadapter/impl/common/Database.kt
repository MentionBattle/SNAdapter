package org.mentionbattle.snadapter.impl.common

import org.mentionbattle.snadapter.api.core.SocialNetworkInitializer
import org.mentionbattle.snadapter.impl.eventsystem.MentionEvent
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet





@SocialNetworkInitializer("Database")
class Database(map : HashMap<String, Any>) {
    private val name : String by map
    init {
        getConnection().use { c ->
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
        try {
           getConnection().use { c ->
                val sql = "INSERT INTO MENTIONS " +
                        "(Contender, Mention, Time) VALUES " +
                        "(\"${mentionEvent.contender}\", \"\", datetime('now'))"
                c.createStatement().use {
                    it.executeUpdate(sql)
                }
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    fun contenderMentionCount(contender: Contender) : Int {
        try {
            getConnection().use { c ->
                c.createStatement().use { stmt ->
                    stmt.executeQuery("SELECT COUNT() as TOTAL FROM MENTIONS " +
                            "WHERE MENTIONS.Contender = \"${contender.id}\"").use {
                        val result = it.getInt("TOTAL")
                        return result
                    }
                }
            }
        } catch (e : Exception) {
            throw RuntimeException(e)
        }
    }

    fun getConnection(): Connection {
        return DriverManager.getConnection("jdbc:sqlite:$name.db");
    }
}