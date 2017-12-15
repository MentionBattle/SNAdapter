package org.mentionbattle.snadapter.impl.common

import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.util.*
import java.util.stream.Collectors

class Contender (val id : Int, val name : String,  path : String) {
    val path : String

    init {
        this.path = path.replace('\\', FileSystems.getDefault().separator[0])
    }

    private fun getImageFormat() : String {
        return path.split(".").last()
    }

    fun packImageToBase64() : String {
        val result = mutableListOf<Byte>()
        BufferedReader(InputStreamReader(FileInputStream(path))).use { br ->
            val result = br.lines().collect(Collectors.joining("\n"))
            return "data:image/${getImageFormat()};base64," + Base64.getEncoder().encodeToString(result.toByteArray());
        }
    }
}