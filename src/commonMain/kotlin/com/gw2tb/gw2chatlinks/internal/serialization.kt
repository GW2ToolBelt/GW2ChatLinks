/*
 * Copyright (c) 2021-2024 Leon Linhart
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

@ExperimentalUnsignedTypes
internal fun buildArray(size: Int, block: ArrayBuilder.() -> Unit): UByteArray =
    UByteArray(size).also { ArrayBuilder(it).block() }

@ExperimentalUnsignedTypes
internal fun <T> parseArray(array: UByteArray, block: ArrayParser.() -> T): T =
    ArrayParser(array).block()

@ExperimentalUnsignedTypes
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

}

@ExperimentalUnsignedTypes
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

}