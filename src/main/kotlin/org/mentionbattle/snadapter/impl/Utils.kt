package org.mentionbattle.snadapter.impl

import java.io.*
import java.net.Socket


private fun readIntInner(socket: InputStream): Int {
    val ch1 = socket.read()
    val ch2 = socket.read()
    val ch3 = socket.read()
    val ch4 = socket.read()
    if (ch1 or ch2 or ch3 or ch4 < 0)
        throw Exception()
    return (ch1 shl 24) + (ch2 shl 16) + (ch3 shl 8) + (ch4 shl 0)
}

private fun writeIntInner(v: Int, out : OutputStream) {
    out.write(v.ushr(24) and 0xFF)
    out.write(v.ushr(16) and 0xFF)
    out.write(v.ushr(8) and 0xFF)
    out.write(v.ushr(0) and 0xFF)
}

fun readString(socket : Socket) : String {

    val it =  socket.getInputStream()
    val bytes = mutableListOf<Byte>()
    val buffer = ByteArray(4096)
    var count = readIntInner(it)
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

fun sendString(socket : Socket, msg : String) {
    val bytes = msg.toByteArray()
    val outputStream = socket.getOutputStream()
    writeIntInner(bytes.size, outputStream)
    outputStream.write(bytes)
}