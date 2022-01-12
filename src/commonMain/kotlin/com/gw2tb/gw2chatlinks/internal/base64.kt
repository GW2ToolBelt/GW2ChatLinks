/*
 * Copyright (c) 2021-2022 Leon Linhart
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

private const val CODE_TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

@ExperimentalUnsignedTypes
internal fun decodeBase64(source: String): UByteArray {
    val output = UByteArray((source.length * 3 + 4) / 4)

    var decodedBytes = 0

    var padding = 0
    var mode = 0

    for (i in source.indices) {
        if (padding == 2) error("Found third padding char at index $i in source: $source")

        when (val char = source[i]) {
            '=' -> padding++
            else -> {
                if (padding != 0) error("Found data after padding char at index $i in source: $source")

                val code = (CODE_TABLE.indexOf(char)).toUByte()
                val outputIndex = (i * 6) / 8

                when (mode) {
                    0 -> {
                        output[outputIndex] = code shl 2
                        decodedBytes++
                    }
                    1 -> {
                        output[outputIndex] = output[outputIndex] or (code shr 4)
                        output[outputIndex + 1] = code shl 4
                        decodedBytes++
                    }
                    2 -> {
                        output[outputIndex] = output[outputIndex] or (code shr 2)
                        output[outputIndex + 1] = code shl 6
                        decodedBytes++
                    }
                    3 -> {
                        output[outputIndex] = output[outputIndex] or code
                    }
                    else -> error("Reached unexpected mode: $mode")
                }

                mode = (mode + 1) % 4
            }
        }
    }

    return output.copyOf(newSize = decodedBytes - (if (mode != 0) 1 else padding))
}

@ExperimentalUnsignedTypes
internal fun encodeBase64(data: UByteArray): String = buildString {
    var mode = 0

    for (i in data.indices) {
        when (mode) {
            0 -> {
                val c = (data[i] shr 2).toInt()
                append(CODE_TABLE[c])
            }
            1 -> {
                val c = (((data[i - 1] shl 4) and 0x3Fu) or (data[i] shr 4)).toInt()
                append(CODE_TABLE[c])
            }
            2 -> {
                val c3 = (((data[i - 1] shl 2) and 0x3Fu) or (data[i] shr 6)).toInt()
                val c4 = (data[i] and 0x3Fu).toInt()

                append(CODE_TABLE[c3])
                append(CODE_TABLE[c4])
            }
            else -> error("Reached unexpected mode: $mode")
        }

        mode = (mode + 1) % 3
    }

    when (mode) {
        1 -> {
            val c = ((data.last() shl 4) and 0x3Fu).toInt()
            append(CODE_TABLE[c])
        }
        2 -> {
            val c = ((data.last() shl 2) and 0x3Fu).toInt()
            append(CODE_TABLE[c])
        }
    }

    for (i in 1 downTo (mode + 2) % 3) append("=")
}