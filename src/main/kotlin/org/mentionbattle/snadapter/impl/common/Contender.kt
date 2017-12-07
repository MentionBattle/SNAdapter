package org.mentionbattle.snadapter.impl.common

import java.io.FileInputStream
import java.util.*

class Contender (name : String, path : String) {
    val name = name;
    val path = path;

    private fun getImageFormat() : String {
        return path.split(".").last()
    }

    fun packImageToBase64() : String {
        val result = mutableListOf<Byte>()
        FileInputStream(path).use {
            var t : ByteArray;
            while (true) {
                t = it.readBytes()
                if (t.isEmpty()) {
                    break;
                }
                result.addAll(t.toList())
            }
        }
        return "data:image/${getImageFormat()};base64," + Base64.getEncoder().encodeToString(result.toByteArray());
    }
}