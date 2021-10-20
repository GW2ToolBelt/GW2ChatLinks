/*
 * Copyright (c) 2021 Leon Linhart
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

/**
 * A utility class for mapping from profession IDs to palette IDs.
 *
 * @param professionID  the profession's ID
 * @param paletteID     the profession's palette ID
 *
 * @since   0.1.0
 */
public enum class Profession(
    public val professionID: String,
    public val paletteID: UByte
) {
    /**
     * Represents any unknown or unexpected value.
     *
     * @since   0.1.0
     */
    UNKNOWN("", 0x00u),
    /**
     * The "Guardian" profession.
     *
     * @since   0.1.0
     */
    GUARDIAN("Guardian", 0x01u),
    /**
     * The "Warrior" profession.
     *
     * @since   0.1.0
     */
    WARRIOR("Warrior", 0x02u),
    /**
     * The "Engineer" profession.
     *
     * @since   0.1.0
     */
    ENGINEER("Engineer", 0x03u),
    /**
     * The "Ranger" profession.
     *
     * @since   0.1.0
     */
    RANGER("Ranger", 0x04u) {

        override fun parseContext(
            nextByte: () -> UByte,
            nextShort: () -> UShort,
            nextInt: () -> UInt
        ): ChatLink.BuildTemplate.RangerContext {
            val pets = List(2) { nextByte() }
            val aquaticPets = List(2) { nextByte() }

            return ChatLink.BuildTemplate.RangerContext(
                pets = pets,
                aquaticPets = aquaticPets
            )
        }

    },
    /**
     * The "Thief" profession.
     *
     * @since   0.1.0
     */
    THIEF("Thief", 0x05u),
    /**
     * The "Elementalist" profession.
     *
     * @since   0.1.0
     */
    ELEMENTALIST("Elementalist", 0x06u),
    /**
     * The "Mesmer" profession.
     *
     * @since   0.1.0
     */
    MESMER("Mesmer", 0x07u),
    /**
     * The "Necromancer" profession.
     *
     * @since   0.1.0
     */
    NECROMANCER("Necromancer", 0x08u),
    /**
     * The "Revenant" profession.
     *
     * @since   0.1.0
     */
    REVENANT("Revenant", 0x09u) {

        override fun parseContext(
            nextByte: () -> UByte,
            nextShort: () -> UShort,
            nextInt: () -> UInt
        ): ChatLink.BuildTemplate.RevenantContext {
            val legends = List(2) { nextByte() }
            val aquaticLegends = List(2) { nextByte() }

            val inactiveLegendUtilitySkills = List(3) { nextShort() }
            val inactiveAquaticLegendUtilitySkills = List(3) { nextShort() }

            return ChatLink.BuildTemplate.RevenantContext(
                legends = legends,
                aquaticLegends = aquaticLegends,
                inactiveLegendUtilitySkills = inactiveLegendUtilitySkills,
                inactiveAquaticLegendUtilitySkills = inactiveAquaticLegendUtilitySkills
            )
        }

    };

    internal open fun parseContext(
        nextByte: () -> UByte,
        nextShort: () -> UShort,
        nextInt: () -> UInt
    ): ChatLink.BuildTemplate.ProfessionContext? {
        return null
    }

    public companion object {

        /**
         * Returns a [Profession] for a given [ChatLink.BuildTemplate].
         *
         * @param template  the template for which to return the profession
         *
         * @return  the profession for the template, or [Profession.UNKNOWN]
         *
         * @since   0.1.0
         */
        @Suppress("NOTHING_TO_INLINE")
        public inline fun valueOf(template: ChatLink.BuildTemplate): Profession = valueOf(template.professionID)

        /**
         * Returns a [Profession] for a given `paletteID`.
         *
         * @param paletteID the palette ID to return the profession for
         *
         * @return  the profession for the palette ID, or [Profession.UNKNOWN]
         *
         * @since   0.1.0
         */
        public fun valueOf(paletteID: UByte): Profession = values().let { values ->
            if (paletteID.toUInt() == 0u || paletteID >= values.size.toUByte())
                UNKNOWN
            else
                values[paletteID.toInt()]
        }

    }
}