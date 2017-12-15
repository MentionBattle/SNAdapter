package org.mentionbattle.snadapter.impl.common.Utils

import java.io.*
import java.net.Socket


private fun InputStream.readInt(): Int {
    val ch1 = this.read()
    val ch2 = this.read()
    val ch3 = this.read()
    val ch4 = this.read()
    if (ch1 or ch2 or ch3 or ch4 < 0)
        throw Exception()
    return (ch1 shl 24) + (ch2 shl 16) + (ch3 shl 8) + (ch4 shl 0)
}

private fun OutputStream.writeInt(v: Int) {
    this.write(v.ushr(24) and 0xFF)
    this.write(v.ushr(16) and 0xFF)
    this.write(v.ushr(8) and 0xFF)
    this.write(v.ushr(0) and 0xFF)
}

fun Socket.readString() : String {

    val it =  this.getInputStream()
    val bytes = mutableListOf<Byte>()
    val buffer = ByteArray(4096)
    var count = it.readInt()
    while (count != 0) {
        val readCount = it.read(buffer, 0, minOf(4096, count))
        if (readCount < 0) {
            throw Exception()
        }
        bytes.addAll(buffer.slice(0..readCount - 1))
        count -= readCount;
    }
    return String(bytes.toByteArray())
}

fun Socket.sendString( msg : String) {
    val bytes = msg.toByteArray()
    val outputStream = this.getOutputStream()
    outputStream.writeInt(bytes.size)
    outputStream.write(bytes)
}