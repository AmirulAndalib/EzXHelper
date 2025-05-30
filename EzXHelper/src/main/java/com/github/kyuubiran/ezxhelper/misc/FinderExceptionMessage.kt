@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.github.kyuubiran.ezxhelper.misc

import com.github.kyuubiran.ezxhelper.interfaces.INamed

class FinderExceptionMessage {

    private val messages = mutableListOf<String>()

    val msg: String get() = buildString { messages.forEach { append(it) } }

    fun <NamedFinder : INamed> ctor(finder: NamedFinder, msg: String) {
        messages.add(0, buildString {
            // fix r8 cause name lost
            append("[${finder.name}] ")
            append(msg)
            append("\nConditions:\n")
        })
    }

    fun append(msg: String, newLine: Boolean = true) {
        messages.add(if (newLine) "$msg\n" else msg)
    }

    fun condition(msg: String) {
        messages.add("\t$msg\n")
    }

    fun clone(): FinderExceptionMessage = FinderExceptionMessage().also {
        it.messages.addAll(this.messages)
    }
}