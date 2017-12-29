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

        getConnection().use { c ->
            val sql = "CREATE TABLE IF NOT EXISTS INFO" +
                    "(id             INTEGER PRIMARY KEY," +
                    " ContenderA     INTEGER    NOT NULL, " +
                    " ContenderB     INTEGER    NOT NULL)";
            val stmt = c.createStatement()

            stmt.executeUpdate(sql)
            stmt.close()
        }
    }

    fun addMention(mentionEvent: MentionEvent, scoreA : Int, scoreB : Int) {
        updateInfoIncrementContender(scoreA, scoreB)
        removeUnnessary(mentionEvent.contender)
        synchronized(lockObject) {

            try {
                getConnection().use { c ->
                    val sql = "INSERT INTO MENTIONS " +
                            "(Contender, Mention, Time) VALUES " +
                            "(${mentionEvent.contender}, " +
                            "\'${StringEscapeUtils.escapeSql(mentionEvent.createJson()["msg"].toString())}\'," +
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

    private fun updateInfoIncrementContender(scoreA: Int, scoreB : Int) {
        clearInfoDatabase()
        try {
            getConnection().use { c ->
                val sql = "INSERT INTO INFO (id, ContenderA, ContenderB) VALUES " +
                        "(0, $scoreA, $scoreB)"
                c.createStatement().use {
                    it.executeUpdate(sql)
                }
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun clearInfoDatabase() {
        try {
            getConnection().use { c ->
                val sql = "DELETE FROM INFO";
                c.createStatement().use {
                    it.executeUpdate(sql)
                }
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun removeUnnessary(contender: Int) {
        synchronized(lockObject) {

            try {
                getConnection().use { c ->

                    val sql = "DELETE FROM MENTIONS WHERE MENTIONS.Contender = $contender and MENTIONS.ID not in " +
                            "(SELECT MENTIONS2.ID FROM MENTIONS AS MENTIONS2 WHERE MENTIONS2.Contender = $contender ORDER BY" +
                            " date(MENTIONS2.Time) DESC LIMIT 100)"
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
                val field = if (contender.id == 1) "ContenderA" else "ContenderB"
                getConnection().use { c ->
                    c.createStatement().use { stmt ->
                        stmt.executeQuery("SELECT $field from Info where id = 0").use {
                            if (it.next()) {
                                val result = it.getInt(field)
                                return result
                            }
                            return 0
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
            getConnection().use { c ->
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

    fun getConnection() : Connection {
        return DriverManager.getConnection("jdbc:sqlite:$name.db")
    }
}