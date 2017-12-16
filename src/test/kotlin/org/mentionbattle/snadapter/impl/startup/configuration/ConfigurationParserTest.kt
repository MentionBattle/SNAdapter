package org.mentionbattle.snadapter.impl.startup.configuration

import org.junit.Test

import org.junit.Assert.*
import java.nio.file.Paths

/**
 * @author Novik Dmitry ITMO University
 */
class ConfigurationParserTest {
    private val resourcesFolder = Paths.get("src", "test", "resources")

    @Test(expected = ConfigurationParserException::class)
    fun parseFileNotExist() {
        val parser = ConfigurationParser()
        parser.parse(resourcesFolder.resolve("file_not_exist.config"))
    }

    @Test
    fun parseEmpty() {
        val parser = ConfigurationParser()
        val config = parser.parse(resourcesFolder.resolve("empty.config"))

        assertTrue(config.port == 1020)
        assertTrue(config.socialNetworks.isEmpty())
        assertTrue(config.socialNetworkInitializers.isEmpty())
    }

    @Test
    fun parseNetworks() {
        val parser = ConfigurationParser()
        val config = parser.parse(resourcesFolder.resolve("networks.config"))

        assertTrue(config.port == 1020)
        assertTrue(config.socialNetworks.size == 3)
        assertTrue(config.socialNetworkInitializers.isEmpty())
        assertTrue(config.socialNetworks.stream().allMatch({ s -> s == "Network" }))
    }

    @Test
    fun parseInitializers() {
        val parser = ConfigurationParser()
        val config = parser.parse(resourcesFolder.resolve("initializers.config"))

        assertTrue(config.port == 1020)
        assertTrue(config.socialNetworks.isEmpty())
        assertTrue(config.socialNetworkInitializers.size == 2)

        assertTrue(config.socialNetworkInitializers.containsKey("Tag1"))
        assertTrue(config.socialNetworkInitializers.containsKey("Tag2"))
    }

}