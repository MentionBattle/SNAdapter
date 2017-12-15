package org.mentionbattle.snadapter.impl.startup

import kotlinx.coroutines.experimental.runBlocking
import org.junit.*
import org.mentionbattle.snadapter.impl.startup.components.ComponentSystem
import org.mentionbattle.snadapter.impl.startup.configuration.ConfigurationParser
import java.nio.file.Paths
import kotlin.concurrent.thread


internal class StartUpManagerTest {
    private val resourcesFolder = Paths.get("src", "test", "resources")
    val configuration = ConfigurationParser().parse(resourcesFolder.resolve("testsna.config"))

    @Test
    fun StartUpTest() {
        StartUpManager(configuration, listOf("org.mentionbattle")).use {
            configuration.socialNetworks.forEach { n ->
                if (!(n in it.socialNetworks.keys)) {
                    Assert.fail("Social network with name $n is not started")
                }
            }
        }
    }

    @Test
    fun StartUpCloseTest() {
        var startUpManger: StartUpManager? = null
        try {
            startUpManger = StartUpManager(configuration, listOf("org.mentionbattle"))
            runBlocking {
                startUpManger?.run()
            }
        } finally {
            val closeThread = thread {
                startUpManger?.close()
            }
            Thread.sleep(15000)
            if (closeThread.isAlive) {
                Assert.fail("Shutdown is not completed in 15 seconds")
            }
        }
    }

    @After
    fun cleanComponentSystem() {
        ComponentSystem.destroy()
    }
}
