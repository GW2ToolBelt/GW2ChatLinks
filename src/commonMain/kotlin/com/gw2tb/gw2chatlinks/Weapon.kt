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
package com.gw2tb.gw2chatlinks

import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

/**
 * A utility class mapping from weapon types to palette IDs.
 *
 * @param paletteID the weapon's palette ID
 *
 * @since   0.5.0
 */
public enum class Weapon(
    @get:JvmName("getPaletteID")
    public val paletteID: UShort
) {
    /**
     * Represents any unknown or unexpected value.
     *
     * @since   0.5.0
     */
    UNKNOWN(0x00u),

    /**
     * An axe.
     *
     * @since   0.5.0
     */
    AXE(0x05u),

    /**
     * A longbow.
     *
     * @since   0.5.0
     */
    LONGBOW(0x23u),

    /**
     * A dagger.
     *
     * @since   0.5.0
     */
    DAGGER(0x2Fu),

    /**
     * A focus.
     *
     * @since   0.5.0
     */
    FOCUS(0x31u),

    /**
     * A greatsword.
     *
     * @since   0.5.0
     */
    GREATSWORD(0x32u),

    /**
     * A hammer.
     *
     * @since   0.5.0
     */
    HAMMER(0x33u),

    /**
     * A mace.
     *
     * @since   0.5.0
     */
    MACE(0x35u),

    /**
     * A pistol.
     *
     * @since   0.5.0
     */
    PISTOL(0x36u),

    /**
     * A rifle.
     *
     * @since   0.5.0
     */
    RIFLE(0x55u),

    /**
     * A scepter.
     *
     * @since   0.5.0
     */
    SCEPTER(0x56u),

    /**
     * A shield.
     *
     * @since   0.5.0
     */
    SHIELD(0x57u),

    /**
     * A staff.
     *
     * @since   0.5.0
     */
    STAFF(0x59u),

    /**
     * A sword.
     *
     * @since   0.5.0
     */
    SWORD(0x5Au),

    /**
     * A torch.
     *
     * @since   0.5.0
     */
    TORCH(0x66u),

    /**
     * A warhorn.
     *
     * @since   0.5.0
     */
    WARHORN(0x67u),

    /**
     * A shortbow.
     *
     * @since   0.5.0
     */
    SHORTBOW(0x6Bu);

    public companion object {

        /**
         * Returns a [Weapon] for a given `paletteID`.
         *
         * @param paletteID the palette ID to return the weapon for
         *
         * @return  the weapon for the palette ID, or [Weapon.UNKNOWN]
         *
         * @since   0.5.0
         */
        @JvmName("valueOf")
        @JvmStatic
        public fun valueOf(paletteID: UShort): Weapon = entries.find { paletteID == it.paletteID } ?: UNKNOWN

    }

}