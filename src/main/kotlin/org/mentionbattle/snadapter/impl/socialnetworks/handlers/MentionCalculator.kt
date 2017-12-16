package org.mentionbattle.snadapter.impl.socialnetworks.handlers

import org.mentionbattle.snadapter.impl.socialnetworks.initalizers.Tags

/**
 * @author Novik Dmitry ITMO University
 */

fun calculate(text: String, tags: Tags): List<Int> {
    val result = mutableListOf<Int>()
    for (key in tags.contenderA) {
        if (text.contains(key, true)) {
            result.add(1)
            break
        }
    }
    for (key in tags.contenderB) {
        if (text.contains(key, true)) {
            result.add(2)
            break
        }
    }
    return result
}