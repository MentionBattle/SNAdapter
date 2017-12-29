package org.mentionbattle.snadapter.impl.common

import kotlinx.coroutines.experimental.runBlocking
import org.junit.*
import org.mentionbattle.snadapter.impl.eventsystem.MentionEvent
import org.mentionbattle.snadapter.impl.startup.components.ComponentSystem
import org.mentionbattle.snadapter.impl.startup.components.ReflectionComponent
import org.mentionbattle.snadapter.impl.startup.components.packages
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

internal class DatabaseTest {
    val name = "testDataBase"
    lateinit var database : Database

    fun createEmptyDatabase() {
        val path = Paths.get("$name.db")
        if (Files.exists(path)) {
            Files.delete(path)
        }
        database = Database(hashMapOf("name" to name))
    }

    @Test
    fun accessDatabase() {
        createEmptyDatabase()
        database.getConnection().use {

        }
    }


    @Test
    fun contenderMentionCount() {
        createEmptyDatabase()
        val answers = hashMapOf<Int, Int>()
        val rnd = Random(19)
        for (i in 1..2) {
            answers[i] = rnd.nextInt(20)
        }
        for (i in 1..2) {
            for (j in 0..answers[i] as Int - 1) {
                database.addMention(createMentionEvent(i), answers[1]!!, answers[2]!!)
            }
        }
        for (i in 1..2) {
            val ans = database.contenderMentionCount(Contender(i, "", ""))
            Assert.assertSame(answers[i], ans)
        }
    }

    fun createMentionEvent(contender : Int) : MentionEvent {
        return MentionEvent(contender, "", "", "", "", "", Date())
    }

}