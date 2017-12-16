package org.mentionbattle.snadapter.impl.common.Utils

import kotlinx.coroutines.experimental.launch
import org.junit.Assert
import org.junit.Test
import java.net.ServerSocket
import java.net.Socket

internal class SocketExtensionKtTest {
    @Test
    fun socketExtensionTest() {

        fun generateText(length : Int) : String {
            val sb = StringBuilder()
            for (i in 0..length) {
                sb.append('a')
            }
            return sb.toString()
        }

        val testPort = 6840
        val textLength = 10_000

        ServerSocket(testPort).use { server ->
            launch {
                val socket = server.accept()
                socket.sendString(generateText(textLength))
            }
            val client = Socket("127.0.0.1", testPort)
            Assert.assertSame(client.readString().equals(generateText(textLength)), true)

        }
    }

}