/*
 * Copyright (c) 2021-2025 Leon Linhart
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.gw2tb.gw2chatlinks.internal

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUnsignedTypes
@ExperimentalUuidApi
internal fun buildArray(size: Int, block: ArrayBuilder.() -> Unit): UByteArray =
    UByteArray(size).also { ArrayBuilder(it).block() }

@ExperimentalUnsignedTypes
@ExperimentalUuidApi
internal fun <T> parseArray(array: UByteArray, block: ArrayParser.() -> T): T =
    ArrayParser(array).block()

@ExperimentalUnsignedTypes
@ExperimentalUuidApi
internal class ArrayBuilder(private val array: UByteArray) {

    var position: Int = 0

    fun putByte(data: UByte) {
        array[position++] = data
    }

    fun putShort(data: UShort) {
        array[position++] = (data.toUByte())
        array[position++] = (data shr 8).toUByte()
    }

    fun put3Bytes(data: UInt) {
        array[position++] = (data.toUByte())
        array[position++] = (data shr 8).toUByte()
        array[position++] = (data shr 16).toUByte()
    }

    fun putInt(data: UInt) {
        array[position++] = (data.toUByte())
        array[position++] = (data shr 8).toUByte()
        array[position++] = (data shr 16).toUByte()
        array[position++] = (data shr 24).toUByte()
    }

    fun putUuid(data: Uuid) {
        val bytes = data.toUByteArray()

        for (i in 3 downTo 0) {
            array[position++] = bytes[i]
        }

        for (i in 5 downTo 4) {
            array[position++] = bytes[i]
        }

        for (i in 7 downTo 6) {
            array[position++] = bytes[i]
        }

        for (i in 8..15) {
            array[position++] = bytes[i]
        }
    }

}

@ExperimentalUnsignedTypes
@ExperimentalUuidApi
internal class ArrayParser(private val array: UByteArray) {

    var position: Int = 0

    val remaining: Int get() = (array.size - position)

    fun nextByte() =
        array[position++]

    fun nextShort() = (nextByte().toUInt() or (nextByte().toUInt() shl 8)).toUShort()

    fun next3Bytes() = (nextByte().toUInt()
        or (nextByte().toUInt() shl 8)
        or (nextByte().toUInt() shl 16))

    fun nextInt() = (nextByte().toUInt()
        or (nextByte().toUInt() shl 8)
        or (nextByte().toUInt() shl 16)
        or (nextByte().toUInt() shl 24))

    fun nextPaddedIdentifier() = next3Bytes().also {
        nextByte().let { check(it == 0u.toUByte()) { "Expected zero byte but found: $it" } }
    }

    fun nextUuid(): Uuid {
        /*
         * Anet encodes UUIDs in a slightly odd way:
         * 04030201-0605-0807-090A-0B0C0D0E0F10
         *
         * is encoded as:
         * - 01 02 03 04
         * - 05 06
         * - 07 08
         * - 09 0A
         * - 0B 0C 0D 0E 0F 10
         */

        val bytes = UByteArray(16)

        for (i in 3 downTo 0) {
            bytes[i] = nextByte()
        }

        for (i in 5 downTo 4) {
            bytes[i] = nextByte()
        }

        for (i in 7 downTo 6) {
            bytes[i] = nextByte()
        }

        for (i in 8..15) {
            bytes[i] = nextByte()
        }

        return Uuid.fromUByteArray(bytes)
    }

}