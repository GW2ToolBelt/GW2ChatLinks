/*
 * Copyright (c) 2021-2023 Leon Linhart
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
@file:OptIn(ExperimentalUnsignedTypes::class)
package com.gw2tb.gw2chatlinks

import com.gw2tb.gw2chatlinks.internal.*
import kotlin.test.*

class Base64Tests {

    @Test
    fun testDecodeRFC4648() {
        assertEquals("", decodeBase64("").asByteArray().decodeToString())
        assertEquals("f", decodeBase64("Zg==").asByteArray().decodeToString())
        assertEquals("fo", decodeBase64("Zm8=").asByteArray().decodeToString())
        assertEquals("foo", decodeBase64("Zm9v").asByteArray().decodeToString())
        assertEquals("foob", decodeBase64("Zm9vYg==").asByteArray().decodeToString())
        assertEquals("fooba", decodeBase64("Zm9vYmE=").asByteArray().decodeToString())
        assertEquals("foobar", decodeBase64("Zm9vYmFy").asByteArray().decodeToString())
    }

    @Test
    fun testEncodeRFC4648() {
        assertEquals("", encodeBase64(ubyteArrayOf()))
        assertEquals("Zg==", encodeBase64("f".encodeToByteArray().asUByteArray()))
        assertEquals("Zm8=", encodeBase64("fo".encodeToByteArray().asUByteArray()))
        assertEquals("Zm9v", encodeBase64("foo".encodeToByteArray().asUByteArray()))
        assertEquals("Zm9vYg==", encodeBase64("foob".encodeToByteArray().asUByteArray()))
        assertEquals("Zm9vYmE=", encodeBase64("fooba".encodeToByteArray().asUByteArray()))
        assertEquals("Zm9vYmFy", encodeBase64("foobar".encodeToByteArray().asUByteArray()))
    }

    // Test values are from https://en.wikipedia.org/wiki/Base64

    @Test
    fun testDecodeLen24NoPaddingChar() {
        val decoded = decodeBase64("YW55IGNhcm5hbCBwbGVhc3Vy")
        assertEquals("any carnal pleasur", decoded.asByteArray().decodeToString())
    }

    @Test
    fun testDecodeLen24OnePaddingChar() {
        val decoded = decodeBase64("YW55IGNhcm5hbCBwbGVhc3U=")
        assertEquals("any carnal pleasu", decoded.asByteArray().decodeToString())
    }

    @Test
    fun testDecodeLen24TwoPaddingChars() {
        val decoded = decodeBase64("YW55IGNhcm5hbCBwbGVhcw==")
        assertEquals("any carnal pleas", decoded.asByteArray().decodeToString())
    }

    @Test
    fun testDecodeLen22() {
        val decoded = decodeBase64("YW55IGNhcm5hbCBwbGVhcw")
        assertEquals("any carnal pleas", decoded.asByteArray().decodeToString())
    }

    @Test
    fun testDecodeLen23() {
        val decoded = decodeBase64("YW55IGNhcm5hbCBwbGVhc3U")
        assertEquals("any carnal pleasu", decoded.asByteArray().decodeToString())
    }

    @Test
    fun testEncodeLen24() {
        val encoded = encodeBase64("any carnal pleasur".encodeToByteArray().asUByteArray())
        assertEquals("YW55IGNhcm5hbCBwbGVhc3Vy", encoded)
    }

    @Test
    fun testEncodeLen23() {
        val encoded = encodeBase64("any carnal pleasu".encodeToByteArray().asUByteArray())
        assertEquals("YW55IGNhcm5hbCBwbGVhc3U=", encoded)
    }

    @Test
    fun testEncodeLen22() {
        val encoded = encodeBase64("any carnal pleas".encodeToByteArray().asUByteArray())
        assertEquals("YW55IGNhcm5hbCBwbGVhcw==", encoded)
    }

}