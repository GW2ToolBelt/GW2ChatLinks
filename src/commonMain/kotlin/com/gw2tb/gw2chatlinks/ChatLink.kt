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
package com.gw2tb.gw2chatlinks

import com.gw2tb.gw2chatlinks.internal.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmName
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val UINT_24BIT_MAX_VALUE = 0xFFFFFF16u

/**
 * Attempts to decode a [ChatLink] object from a chat link string.
 *
 * @param source    the chat link source to decode
 *
 * @return  a [Result] wrapping the decoded chat link or an exception if [source] could not be decoded
 *
 * @since   0.1.0
 */
@OptIn(
    ExperimentalChatLinks::class,
    ExperimentalEncodingApi::class,
    ExperimentalUnsignedTypes::class,
    ExperimentalUuidApi::class
)
public fun decodeChatLink(
    source: String
): Result<ChatLink> = runCatching {
    require(source.startsWith("[&")) { "Input does not start with chat link prefix (\"[&\"): $source" }
    require(source.endsWith("]")) { "Input does not end with chat link suffix (\"]\"): $source" }

    parseArray(Base64.decode(source.substring(startIndex = 2, endIndex = source.length - 1)).toUByteArray()) {
        when (val identifier = nextByte().toInt()) {
            ChatLink.Coin.IDENTIFIER -> ChatLink.Coin(amount = nextInt())
            ChatLink.Item.IDENTIFIER -> {
                val amount = nextByte()
                val itemId = next3Bytes()

                val flags = nextByte().toUInt()

                val skinId = if ((flags and ChatLink.Item.SKINNED) != 0u) {
                    nextPaddedIdentifier()
                } else {
                    null
                }

                val firstUpgradeSlot = if ((flags and ChatLink.Item.FIRST_UPGRADE_SLOT_IN_USE) != 0u) {
                    nextPaddedIdentifier()
                } else {
                    null
                }

                val secondUpgradeSlot = if ((flags and ChatLink.Item.SECOND_UPGRADE_SLOT_IN_USE) != 0u) {
                    nextPaddedIdentifier()
                } else {
                    null
                }

                ChatLink.Item(
                    amount = amount,
                    itemId = itemId,
                    skinId = skinId,
                    firstUpgradeSlot = firstUpgradeSlot,
                    secondUpgradeSlot = secondUpgradeSlot
                )
            }
            ChatLink.NpcText.IDENTIFIER -> ChatLink.NpcText(textId = nextPaddedIdentifier())
            ChatLink.Poi.IDENTIFIER -> ChatLink.Poi(poiId = nextPaddedIdentifier())
            ChatLink.PvpGame.IDENTIFIER -> ChatLink.PvpGame
            ChatLink.Skill.IDENTIFIER -> ChatLink.Skill(skillId = nextPaddedIdentifier())
            ChatLink.Trait.IDENTIFIER -> ChatLink.Trait(traitId = nextPaddedIdentifier())
            ChatLink.User.IDENTIFIER -> {
                val accountGuid = nextUuid()
                val characterName = UByteArray(remaining - 2) { nextByte() }

                nextShort().let { check(it == 0u.toUShort()) { "Expected two zero bytes but found: $it" } }

                ChatLink.User(
                    accountGuid = accountGuid,
                    characterName = characterName
                )
            }
            ChatLink.Recipe.IDENTIFIER -> ChatLink.Recipe(recipeId = nextPaddedIdentifier())
            ChatLink.Skin.IDENTIFIER -> ChatLink.Skin(skinId = nextPaddedIdentifier())
            ChatLink.Outfit.IDENTIFIER -> ChatLink.Outfit(outfitId = nextPaddedIdentifier())
            ChatLink.WvwObjective.IDENTIFIER -> ChatLink.WvwObjective(
                objectiveId = nextPaddedIdentifier(),
                mapId = nextPaddedIdentifier()
            )
            ChatLink.BuildTemplate.IDENTIFIER -> {
                val professionId = nextByte()

                val specializations = List(size = 3) {
                    ChatLink.BuildTemplate.Specialization(
                        specializationId = nextByte(),
                        majorTraits = nextByte().let { majorTraits ->
                            List(size = 3) { index -> ((majorTraits shr (index * 2)) and 0x3u).let { if (it == 0u.toUByte()) null else (it - 1u).toUByte() } }
                        }
                    )
                }

                val allSkills = List(size = 10) { nextShort() }

                val profCtxOffset = position
                val context = Profession.valueOf(paletteId = professionId).parseContext(
                    nextByte = ::nextByte,
                    nextShort = ::nextShort,
                    nextInt = ::nextInt
                )

                position = profCtxOffset + ChatLink.BuildTemplate.ProfessionContext.BYTE_SIZE

                val weapons = when {
                    remaining > 0 -> List(nextByte().toInt()) { nextShort() }
                    else -> emptyList()
                }

                val weaponSkillOverrides = when {
                    remaining > 0 -> List(nextByte().toInt()) { nextInt() }
                    else -> emptyList()
                }

                ChatLink.BuildTemplate(
                    professionId = professionId,
                    specializations = specializations,
                    skills = allSkills.filterIndexed { index, _ -> index % 2 == 0 },
                    aquaticSkills = allSkills.filterIndexed { index, _ -> index % 2 != 0 },
                    professionContext = context,
                    weapons = weapons,
                    weaponSkillOverrides = weaponSkillOverrides
                )
            }
            ChatLink.Achievement.IDENTIFIER -> ChatLink.Achievement(achievementId = nextPaddedIdentifier())
            ChatLink.FashionTemplate.IDENTIFIER -> {
                ChatLink.FashionTemplate(
                    aquabreatherSkinId = nextShort(),
                    backpackSkinId = nextShort(),
                    backpackColorIds = List(4) { nextShort() },
                    chestSkinId = nextShort(),
                    chestColorIds = List(4) { nextShort() },
                    bootsSkinId = nextShort(),
                    bootsColorIds = List(4) { nextShort() },
                    glovesSkinId = nextShort(),
                    glovesColorIds = List(4) { nextShort() },
                    helmetSkinId = nextShort(),
                    helmetColorIds = List(4) { nextShort() },
                    leggingsSkinId = nextShort(),
                    leggingsColorIds = List(4) { nextShort() },
                    shouldersSkinId = nextShort(),
                    shouldersColorIds = List(4) { nextShort() },
                    outfitId = nextShort(),
                    outfitColorIds = List(4) { nextShort() },
                    firstAquaticWeaponSkinId = nextShort(),
                    secondAquaticWeaponSkinId = nextShort(),
                    firstMainhandWeaponSkinId = nextShort(),
                    firstOffhandWeaponSkinId = nextShort(),
                    secondMainhandWeaponSkinId = nextShort(),
                    secondOffhandWeaponSkinId = nextShort(),
                    visibility = ChatLink.FashionTemplate.Visibility(nextShort())
                )
            }
            else -> error("Unsupported chat link format: ${identifier.toString(16)}")
        }
    }
}

/**
 * Attempts to encode a [ChatLink] into a chat link string.
 *
 * @param chatLink  the chat link to encode
 *
 * @return  a [Result] wrapping the encoded chat link or an exception if [chatLink] could not be encoded
 *
 * @since   0.1.0
 */
@OptIn(ExperimentalEncodingApi::class, ExperimentalUnsignedTypes::class)
public fun encodeChatLink(
    chatLink: ChatLink
): Result<String> = runCatching {
    buildString {
        append("[&")
        Base64.encodeToAppendable(chatLink.asUByteArray().toByteArray(), destination = this)
        append("]")
    }
}

/**
 * An object representing a Guild Wars 2 chat link.
 *
 * @since   0.1.0
 */
public sealed class ChatLink {

    @ExperimentalUnsignedTypes
    internal abstract fun asUByteArray(): UByteArray

    /**
     * A link to an achievement.
     *
     * Only the shape of the parameters (e.g. list sizes) is validated and not the actual data (e.g. ID validity).
     *
     * @param achievementId the ID of the achievement. _(Must be in unsigned 24bit range.)_
     *
     * @throws IllegalArgumentException if any parameter value does not match its expected shape
     *
     * @since   1.1.0
     */
    public data class Achievement(
        @get:JvmName("getAchievementId")
        val achievementId: UInt
    ) : ChatLink() {

        internal companion object {
            const val IDENTIFIER = 0x0E

            /*
             *    1 identifier
             * +  3 achievementId
             * +  1 reserved byte
             */
            const val BYTE_SIZE = 5
        }

        init {
            require(achievementId < UINT_24BIT_MAX_VALUE) { "Achievement `achievementId` must be in unsigned 24bit range (0..167771215)" }
        }

        @ExperimentalUnsignedTypes
        override fun asUByteArray(): UByteArray = buildArray(BYTE_SIZE) {
            putByte(IDENTIFIER.toUByte())
            put3Bytes(achievementId)
            putByte(0u)
        }

    }

    /**
     * A build template.
     *
     * Skill IDs are palette IDs and should not be confused with the skill IDs in the official Guild Wars 2 API.
     *
     * Only the shape of the parameters (e.g. list sizes) is validated and not the actual data (e.g. ID validity).
     *
     * @param professionId          the ID of the template's profession
     * @param specializations       the template's specializations. _(Must contain three elements.)_
     * @param skills                the IDs of the template's (terrestrial) skills. _(Must contain five elements.)_
     * @param aquaticSkills         the IDs of the template's aquatic skills. _(Must contain five elements.)_
     * @param professionContext     the profession-specific context, or `null` if no profession specific information
     *                              exists for the template's profession
     * @param weapons               the [weapons][Weapon] of the template.
     * @param weaponSkillOverrides  the skill IDs of the template's weapon skill overrides
     *
     * @throws IllegalArgumentException if any parameter value does not match its expected shape
     *
     * @since   0.1.0
     */
    public data class BuildTemplate(
        @get:JvmName("getProfessionId")
        val professionId: UByte,
        val specializations: List<Specialization>,
        val skills: List<UShort>,
        val aquaticSkills: List<UShort>,
        val professionContext: ProfessionContext?,
        val weapons: List<UShort> = emptyList(),
        val weaponSkillOverrides: List<UInt> = emptyList()
    ) : ChatLink() {

        /**
         * @param profession            the template's profession
         * @param specializations       the template's specializations. _(Must contain three elements.)_
         * @param skills                the IDs of the template's (terrestrial) skills. _(Must contain five elements.)_
         * @param aquaticSkills         the IDs of the template's aquatic skills. _(Must contain five elements.)_
         * @param professionContext     the profession-specific context, or `null` if no profession specific information
         *                              exists for the template's profession
         * @param weaponSkillOverrides  the skill IDs of the template's weapon skill overrides
         *
         * @throws IllegalArgumentException if any parameter value does not match its expected shape
         *
         * @since   0.5.0
         */
        public constructor(
            profession: Profession,
            specializations: List<Specialization>,
            skills: List<UShort>,
            aquaticSkills: List<UShort>,
            professionContext: ProfessionContext?,
            weapons: List<UShort> = emptyList(),
            weaponSkillOverrides: List<UInt> = emptyList()
        ): this(profession.paletteId, specializations, skills, aquaticSkills, professionContext, weapons, weaponSkillOverrides)

        internal companion object {
            const val IDENTIFIER = 0x0D

            /*
             *    1 identifier
             * +  1 professionId
             * +  6 specializations (3 * 2)
             * + 20 skills (10 * 2)
             * + 16 professionContext
             * +  1 weaponBytePairCount
             * +  1 relicId
             */
            const val BASE_BYTE_SIZE = 46
        }

        init {
            require(specializations.size == 3) { "BuildTemplate `specializations` must contain exactly three specializations" }
            require(skills.size == 5) { "BuildTemplate `skills` must contain exactly five skill IDs" }
            require(aquaticSkills.size == 5) { "BuildTemplate `aquaticSkills` must contain exactly five skill IDs" }

            when (Profession.valueOf(professionId)) {
                Profession.RANGER -> require(professionContext != null && professionContext is RangerContext) { "Ranger profession requires non-null RangerContext" }
                Profession.REVENANT -> require(professionContext != null && professionContext is RevenantContext) { "Revenant profession requires non-null RevenantContext" }
                else -> require(professionContext == null) { "BuildTemplate `professionContext` should be `null` for any professions other than Ranger and Revenant" }
            }
        }

        @ExperimentalUnsignedTypes
        override fun asUByteArray(): UByteArray = buildArray(BASE_BYTE_SIZE + (weapons.size * UShort.SIZE_BYTES) + (weaponSkillOverrides.size * UInt.SIZE_BYTES)) {
            putByte(IDENTIFIER.toUByte())
            putByte(professionId)
            specializations.forEach { specialization ->
                putByte(specialization.specializationId)

                var majorTraits: UByte = 0u
                specialization.majorTraits.forEachIndexed { index, majorTrait ->
                    majorTraits = majorTraits or (((majorTrait?.plus(1u)?.toUByte() ?: 0u) and 0x3u) shl index * 2)
                }

                putByte(majorTraits)
            }
            skills.zip(aquaticSkills).forEach { (skillId, aquaticSkillId) ->
                putShort(skillId)
                putShort(aquaticSkillId)
            }

            val profCtxOffset = position
            professionContext?.apply { putContext() }

            if (position > profCtxOffset + ProfessionContext.BYTE_SIZE)
                error("Unexpected ProfessionContext size: ${position - profCtxOffset}")

            position = profCtxOffset + ProfessionContext.BYTE_SIZE

            putByte(weapons.size.toUByte())
            weapons.forEach(this::putShort)

            putByte(weaponSkillOverrides.size.toUByte())
            weaponSkillOverrides.forEach(this::putInt)
        }

        /**
         * A build template's specialization.
         *
         * @param specializationId  the ID of the specialization
         * @param majorTraits       the indices of the selected major traits, or `null` if no trait is selected for a
         *                          slot
         *
         * @throws IllegalArgumentException if any parameter value does not match its expected shape
         *
         * @since   0.1.0
         */
        public data class Specialization(
            @get:JvmName("getSpecializationId")
            val specializationId: UByte,
            val majorTraits: List<UByte?>
        ) {

            init {
                require(majorTraits.size == 3) { "Specialization `majorTraits` must contain exactly three elements" }
            }

        }

        /**
         * Profession-specific additional information.
         *
         * @since   0.1.0
         */
        public sealed class ProfessionContext {

            internal companion object {
                const val BYTE_SIZE = 16
            }

            @ExperimentalUnsignedTypes
            internal abstract fun ArrayBuilder.putContext()

        }

        /**
         * Profession-specific information for rangers.
         *
         * Only the shape of the parameters (e.g. list sizes) is validated and not the actual data (e.g. ID validity).
         *
         * @param pets          the ID of the template's (terrestrial) pets. _(Must contain two elements.)_
         * @param aquaticPets   the ID of the template's aquatic pets. _(Must contain two elements.)_
         *
         * @throws IllegalArgumentException if any parameter value does not match its expected shape
         *
         * @since   0.1.0
         */
        public data class RangerContext(
            val pets: List<UByte>,
            val aquaticPets: List<UByte>
        ) : ProfessionContext() {

            init {
                require(pets.size == 2) { "RangerContext `pets` must contain exactly two pet IDs" }
                require(aquaticPets.size == 2) { "RangerContext `aquaticPets` must contain exactly two pet IDs" }
            }

            @ExperimentalUnsignedTypes
            override fun ArrayBuilder.putContext() {
                pets.forEach { petId -> putByte(petId) }
                aquaticPets.forEach { petId -> putByte(petId) }
            }

        }

        /**
         * Profession-specific information for revenants.
         *
         * For revenants, [BuildTemplate.skills] and [BuildTemplate.aquaticSkills] contain the IDs of the template's
         * skills for the **active** terrestrial and aquatic legend respectively.
         *
         * Only the shape of the parameters (e.g. list sizes) is validated and not the actual data (e.g. ID validity).
         *
         * @param legends                               the ID of the template's (terrestrial) legends. _(Must contain
         *                                              two elements.)_
         * @param aquaticLegends                        the ID of the template's aquatic legends. _(Must contain two
         *                                              elements.)_
         * @param inactiveLegendUtilitySkills           the IDs of the template's utility skills for the **inactive**
         *                                              (terrestrial) legend. _(Must contain three elements.)_
         * @param inactiveAquaticLegendUtilitySkills    the IDs of the template's utility  skills for the **inactive**
         *                                              aquatic legend. _(Must contain three elements.)_
         *
         * @throws IllegalArgumentException if any parameter value does not match its expected shape
         *
         * @since   0.1.0
         */
        public data class RevenantContext(
            val legends: List<UByte>,
            val aquaticLegends: List<UByte>,
            val inactiveLegendUtilitySkills: List<UShort>,
            val inactiveAquaticLegendUtilitySkills: List<UShort>
        ) : ProfessionContext() {

            init {
                require(legends.size == 2) { "RevenantContext `legends` must contain exactly two legend IDs" }
                require(aquaticLegends.size == 2) { "RevenantContext `aquaticLegends` must contain exactly two legend IDs" }
                require(inactiveLegendUtilitySkills.size == 3) { "RevenantContext `inactiveLegendUtilitySkills` must contain exactly three skill IDs" }
                require(inactiveAquaticLegendUtilitySkills.size == 3) { "RevenantContext `inactiveAquaticLegendUtilitySkills` must contain exactly three skill IDs" }
            }

            @ExperimentalUnsignedTypes
            override fun ArrayBuilder.putContext() {
                legends.forEach { legendId -> putByte(legendId) }
                aquaticLegends.forEach { legendId -> putByte(legendId) }
                inactiveLegendUtilitySkills.forEach { skillId -> putShort(skillId) }
                inactiveAquaticLegendUtilitySkills.forEach { skillId -> putShort(skillId) }
            }

        }

    }

    /**
     * A link that represents a number of coins.
     *
     * Coin links are - at the time of writing - disabled and cannot be fully tested.
     *
     * @param amount    the amount of coins (in copper)
     *
     * @since   0.1.0
     */
    @ExperimentalChatLinks
    public data class Coin(
        @get:JvmName("getAmount")
        val amount: UInt
    ) : ChatLink() {

        internal companion object {
            const val IDENTIFIER = 0x01

            /*
             *    1 identifier
             * +  4 amount
             */
            const val BYTE_SIZE = 5
        }

        @ExperimentalUnsignedTypes
        override fun asUByteArray(): UByteArray = buildArray(BYTE_SIZE) {
            putByte(IDENTIFIER.toUByte())
            putInt(amount)
        }

    }

    /**
     * A fashion template.
     *
     * Only the shape of the parameters (e.g. list sizes) is validated and not the actual data (e.g. ID validity).
     *
     * @param aquabreatherSkinId            the ID of the aquabreather's skin
     * @param backpackSkinId                the ID of the backpack's skin
     * @param backpackColorIds              the IDs of the backpack's colors
     * @param chestSkinId                   the ID of the chest armor's skin
     * @param chestColorIds                 the IDs of the chest armor's colors
     * @param bootsSkinId                   the ID of the boots' skin
     * @param bootsColorIds                 the IDs of the boots' colors
     * @param glovesSkinId                  the ID of the gloves' skin
     * @param glovesColorIds                the IDs of the gloves' colors
     * @param helmetSkinId                  the ID of the helmet's skin
     * @param helmetColorIds                the IDs of the helmet's colors
     * @param leggingsSkinId                the ID of the leggings' skin
     * @param leggingsColorIds              the IDs of the leggings' colors
     * @param shouldersSkinId               the ID of the shoulders' skin
     * @param shouldersColorIds             the IDs of the shoulders' colors
     * @param outfitId                      the ID of the outfit
     * @param outfitColorIds                the IDs of the outfit's colors
     * @param firstAquaticWeaponSkinId      the ID of the first aquatic weapon's skin
     * @param secondAquaticWeaponSkinId     the ID of the second aquatic weapon's skin
     * @param firstMainhandWeaponSkinId     the ID of the first primary weapon's skin
     * @param firstOffhandWeaponSkinId      the ID of the first secondary weapon's skin
     * @param secondMainhandWeaponSkinId    the ID of the second primary weapon's skin
     * @param secondOffhandWeaponSkinId     the ID of the second secondary weapon's skin
     * @param visibility                    the visibility settings for various equipment pieces
     *
     * @throws IllegalArgumentException if any parameter value does not match its expected shape
     *
     * @since   2.1.0
     */
    public data class FashionTemplate(
        val aquabreatherSkinId: UShort,
        val backpackSkinId: UShort,
        val backpackColorIds: List<UShort>,
        val chestSkinId: UShort,
        val chestColorIds: List<UShort>,
        val bootsSkinId: UShort,
        val bootsColorIds: List<UShort>,
        val glovesSkinId: UShort,
        val glovesColorIds: List<UShort>,
        val helmetSkinId: UShort,
        val helmetColorIds: List<UShort>,
        val leggingsSkinId: UShort,
        val leggingsColorIds: List<UShort>,
        val shouldersSkinId: UShort,
        val shouldersColorIds: List<UShort>,
        val outfitId: UShort,
        val outfitColorIds: List<UShort>,
        val firstAquaticWeaponSkinId: UShort,
        val secondAquaticWeaponSkinId: UShort,
        val firstMainhandWeaponSkinId: UShort,
        val firstOffhandWeaponSkinId: UShort,
        val secondMainhandWeaponSkinId: UShort,
        val secondOffhandWeaponSkinId: UShort,
        val visibility: Visibility
    ) : ChatLink() {

        internal companion object {
            const val IDENTIFIER = 0x0F

            /*
             *    1 identifier
             * +  2 aquabreatherSkinId
             * +  2 backpackSkinId
             * +  8 backpackColorIds
             * +  2 chestSkinId
             * +  8 chestColorIds
             * +  2 bootsSkinId
             * +  8 bootsColorIds
             * +  2 glovesSkinId
             * +  8 glovesColorIds
             * +  2 helmetSkinId
             * +  8 helmetColorIds
             * +  2 leggingsSkinId
             * +  8 leggingsColorIds
             * +  2 shouldersSkinId
             * +  8 shouldersColorIds
             * +  2 outfitId
             * +  8 outfitColorIds
             * +  2 firstAquaticWeaponSkinId
             * +  2 secondAquaticWeaponSkinId
             * +  2 firstMainhandWeaponSkinId
             * +  2 firstOffhandWeaponSkinId
             * +  2 secondMainhandWeaponSkinId
             * +  2 secondOffhandWeaponSkinId
             * +  2 visibility
             */
            const val BYTE_SIZE = 97
        }

        init {
            require(backpackColorIds.size == 4) { "FashionTemplate `backpackColorIds` must contain exactly four color IDs" }
            require(chestColorIds.size == 4) { "FashionTemplate `chestColorIds` must contain exactly four color IDs" }
            require(bootsColorIds.size == 4) { "FashionTemplate `bootsColorIds` must contain exactly four color IDs" }
            require(glovesColorIds.size == 4) { "FashionTemplate `glovesColorIds` must contain exactly four color IDs" }
            require(helmetColorIds.size == 4) { "FashionTemplate `helmetColorIds` must contain exactly four color IDs" }
            require(leggingsColorIds.size == 4) { "FashionTemplate `leggingsColorIds` must contain exactly four color IDs" }
            require(shouldersColorIds.size == 4) { "FashionTemplate `shouldersColorIds` must contain exactly four color IDs" }
            require(outfitColorIds.size == 4) { "FashionTemplate `outfitColorIds` must contain exactly four color IDs" }
        }

        @ExperimentalUnsignedTypes
        override fun asUByteArray(): UByteArray = buildArray(BYTE_SIZE) {
            putByte(IDENTIFIER.toUByte())
            putShort(aquabreatherSkinId)
            putShort(backpackSkinId)
            backpackColorIds.forEach(::putShort)
            putShort(chestSkinId)
            chestColorIds.forEach(::putShort)
            putShort(bootsSkinId)
            bootsColorIds.forEach(::putShort)
            putShort(glovesSkinId)
            glovesColorIds.forEach(::putShort)
            putShort(helmetSkinId)
            helmetColorIds.forEach(::putShort)
            putShort(leggingsSkinId)
            leggingsColorIds.forEach(::putShort)
            putShort(shouldersSkinId)
            shouldersColorIds.forEach(::putShort)
            putShort(outfitId)
            outfitColorIds.forEach(::putShort)
            putShort(firstAquaticWeaponSkinId)
            putShort(secondAquaticWeaponSkinId)
            putShort(firstMainhandWeaponSkinId)
            putShort(firstOffhandWeaponSkinId)
            putShort(secondMainhandWeaponSkinId)
            putShort(secondOffhandWeaponSkinId)
            putShort(visibility.bits)
        }

        /**
         * The visibility flags of a fashion template.
         *
         * @param bits  the raw bits
         *
         * @since   2.1.0
         */
        @JvmInline
        public value class Visibility(public val bits: UShort) {

            private companion object {
                private const val AQUABREATHER_MASK = 0x0001u
                private const val BACKPACK_MASK = 0x0002u
                private const val CHEST_MASK = 0x0004u
                private const val BOOTS_MASK = 0x0008u
                private const val GLOVES_MASK = 0x0010u
                private const val HELMET_MASK = 0x0020u
                private const val LEGGINGS_MASK = 0x0040u
                private const val SHOULDERS_MASK = 0x0080u
                private const val OUTFIT_MASK = 0x0100u
                private const val FIRST_AQUATIC_WEAPON_MASK = 0x0200u
                private const val SECOND_AQUATIC_WEAPON_MASK = 0x0400u
                private const val FIRST_MAINHAND_WEAPON_MASK = 0x0800u
                private const val FIRST_OFFHAND_WEAPON_MASK = 0x1000u
                private const val SECOND_MAINHAND_WEAPON_MASK = 0x2000u
                private const val SECOND_OFFHAND_WEAPON_MASK = 0x4000u
            }

            /**
             * Constructs a new visibility bitset for a fashion template.
             *
             * @param isAquabreatherVisible         whether the aquabreather is visible
             * @param isBackpackVisible             whether the backpack is visible
             * @param isChestVisible                whether the chest armor is visible
             * @param areBootsVisible              whether the boots are visible
             * @param areGlovesVisible              whether the gloves are visible
             * @param isHelmetVisible               whether the helmet is visible
             * @param areLeggingsVisible            whether the leggings are visible
             * @param areShouldersVisible           whether the shoulders are visible
             * @param isOutfitVisible               whether the outfit is visible
             * @param isFirstAquaticWeaponVisible   whether the first aquatic weapon is visible
             * @param isSecondAquaticWeaponVisible  whether the second aquatic weapon is visible
             * @param isFirstMainhandWeaponVisible  whether the first primary weapon is visible
             * @param isFirstOffhandWeaponVisible   whether the first secondary weapon is visible
             * @param isSecondMainhandWeaponVisible whether the second primary weapon is visible
             * @param isSecondOffhandWeaponVisible whether the second secondary weapon is visible
             *
             * @since   2.1.0
             */
            public constructor(
                isAquabreatherVisible: Boolean,
                isBackpackVisible: Boolean,
                isChestVisible: Boolean,
                areBootsVisible: Boolean,
                areGlovesVisible: Boolean,
                isHelmetVisible: Boolean,
                areLeggingsVisible: Boolean,
                areShouldersVisible: Boolean,
                isOutfitVisible: Boolean,
                isFirstAquaticWeaponVisible: Boolean,
                isSecondAquaticWeaponVisible: Boolean,
                isFirstMainhandWeaponVisible: Boolean,
                isFirstOffhandWeaponVisible: Boolean,
                isSecondMainhandWeaponVisible: Boolean,
                isSecondOffhandWeaponVisible: Boolean
            ) : this(Unit.let {
                var bits = 0u

                if (isAquabreatherVisible) bits = bits or AQUABREATHER_MASK
                if (isBackpackVisible) bits = bits or BACKPACK_MASK
                if (isChestVisible) bits = bits or CHEST_MASK
                if (areBootsVisible) bits = bits or BOOTS_MASK
                if (areGlovesVisible) bits = bits or GLOVES_MASK
                if (isHelmetVisible) bits = bits or HELMET_MASK
                if (areLeggingsVisible) bits = bits or LEGGINGS_MASK
                if (areShouldersVisible) bits = bits or SHOULDERS_MASK
                if (isOutfitVisible) bits = bits or OUTFIT_MASK
                if (isFirstAquaticWeaponVisible) bits = bits or FIRST_AQUATIC_WEAPON_MASK
                if (isSecondAquaticWeaponVisible) bits = bits or SECOND_AQUATIC_WEAPON_MASK
                if (isFirstMainhandWeaponVisible) bits = bits or FIRST_MAINHAND_WEAPON_MASK
                if (isFirstOffhandWeaponVisible) bits = bits or FIRST_OFFHAND_WEAPON_MASK
                if (isSecondMainhandWeaponVisible) bits = bits or SECOND_MAINHAND_WEAPON_MASK
                if (isSecondOffhandWeaponVisible) bits = bits or SECOND_OFFHAND_WEAPON_MASK

                bits.toUShort()
            })

            /**
             * Returns whether the aquabreather is visible.
             *
             * @since   2.1.0
             */
            public val isAquabreatherVisible: Boolean get() = (bits.toUInt() and AQUABREATHER_MASK) != 0u

            /**
             * Returns whether the backpack is visible.
             *
             * @since   2.1.0
             */
            public val isBackpackVisible: Boolean get() = (bits.toUInt() and BACKPACK_MASK) != 0u

            /**
             * Returns whether the chest armor is visible.
             *
             * @since   2.1.0
             */
            public val isChestVisible: Boolean get() = (bits.toUInt() and CHEST_MASK) != 0u

            /**
             * Returns whether the boots are visible.
             *
             * @since   2.1.0
             */
            public val areBootsVisible: Boolean get() = (bits.toUInt() and BOOTS_MASK) != 0u

            /**
             * Returns whether the gloves are visible.
             *
             * @since   2.1.0
             */
            public val areGlovesVisible: Boolean get() = (bits.toUInt() and GLOVES_MASK) != 0u

            /**
             * Returns whether the helmet is visible.
             *
             * @since   2.1.0
             */
            public val isHelmetVisible: Boolean get() = (bits.toUInt() and HELMET_MASK) != 0u

            /**
             * Returns whether the leggings are visible.
             *
             * @since   2.1.0
             */
            public val areLeggingsVisible: Boolean get() = (bits.toUInt() and LEGGINGS_MASK) != 0u

            /**
             * Returns whether the shoulder armor is visible.
             *
             * @since   2.1.0
             */
            public val areShouldersVisible: Boolean get() = (bits.toUInt() and SHOULDERS_MASK) != 0u

            /**
             * Returns whether the outfit is visible.
             *
             * @since   2.1.0
             */
            public val isOutfitVisible: Boolean get() = (bits.toUInt() and OUTFIT_MASK) != 0u

            /**
             * Returns whether the first aquatic weapon is visible.
             *
             * @since   2.1.0
             */
            public val isFirstAquaticWeaponVisible: Boolean get() = (bits.toUInt() and FIRST_AQUATIC_WEAPON_MASK) != 0u

            /**
             * Returns whether the second aquatic weapon is visible.
             *
             * @since   2.1.0
             */
            public val isSecondAquaticWeaponVisible: Boolean get() = (bits.toUInt() and SECOND_AQUATIC_WEAPON_MASK) != 0u

            /**
             * Returns whether the first primary weapon is visible.
             *
             * @since   2.1.0
             */
            public val isFirstMainhandWeaponVisible: Boolean get() = (bits.toUInt() and FIRST_MAINHAND_WEAPON_MASK) != 0u

            /**
             * Returns whether the first secondary weapon is visible.
             *
             * @since   2.1.0
             */
            public val isFirstOffhandWeaponVisible: Boolean get() = (bits.toUInt() and FIRST_OFFHAND_WEAPON_MASK) != 0u

            /**
             * Returns whether the second primary weapon is visible.
             *
             * @since   2.1.0
             */
            public val isSecondMainhandWeaponVisible: Boolean get() = (bits.toUInt() and SECOND_MAINHAND_WEAPON_MASK) != 0u

            /**
             * Returns whether the second secondary weapon is visible.
             *
             * @since   2.1.0
             */
            public val isSecondOffhandWeaponVisible: Boolean get() = (bits.toUInt() and SECOND_OFFHAND_WEAPON_MASK) != 0u

            override fun toString(): String = buildString {
                append("Visibility(")
                append("isAquabreatherVisible=$isAquabreatherVisible,")
                append(" isBackpackVisible=$isBackpackVisible,")
                append(" isChestVisible=$isChestVisible,")
                append(" areBootsVisible=$areBootsVisible,")
                append(" areGlovesVisible=$areGlovesVisible,")
                append(" isHelmetVisible=$isHelmetVisible,")
                append(" areLeggingsVisible=$areLeggingsVisible,")
                append(" areShouldersVisible=$areShouldersVisible,")
                append(" isOutfitVisible=$isOutfitVisible,")
                append(" isFirstAquaticWeaponVisible=$isFirstAquaticWeaponVisible,")
                append(" isSecondAquaticWeaponVisible=$isSecondAquaticWeaponVisible,")
                append(" isFirstMainhandWeaponVisible=$isFirstMainhandWeaponVisible,")
                append(" isFirstOffhandWeaponVisible=$isFirstOffhandWeaponVisible,")
                append(" isSecondMainhandWeaponVisible=$isSecondMainhandWeaponVisible,")
                append(" isSecondOffhandWeaponVisible=$isSecondOffhandWeaponVisible")
                append(")")
            }

        }

    }

    /**
     * A link to a stack of items.
     *
     * Only the shape of the parameters (e.g. list sizes) is validated and not the actual data (e.g. ID validity).
     *
     * @param amount            the amount of items
     * @param itemId            the ID of the item. _(Must be in unsigned 24bit range.)_
     * @param skinId            the ID of the item's skin. _(Must be in unsigned 24bit range.)_
     * @param firstUpgradeSlot  the ID of the item's first upgrade slot's content. _(Must be in unsigned 24bit range.)_
     * @param secondUpgradeSlot the ID of the item's second upgrade slot's content. _(Must be in unsigned 24bit range.)_
     *
     * @throws IllegalArgumentException if any parameter value does not match its expected shape
     *
     * @since   0.1.0
     */
    public data class Item(
        @get:JvmName("getAmount")
        val amount: UByte,
        @get:JvmName("getItemId")
        val itemId: UInt,
        @get:JvmName("getSkinId")
        val skinId: UInt? = null,
        @get:JvmName("getFirstUpgradeSlot")
        val firstUpgradeSlot: UInt? = null,
        @get:JvmName("getSecondUpgradeSlot")
        val secondUpgradeSlot: UInt? = null
    ) : ChatLink() {

        internal companion object {
            const val IDENTIFIER = 0x02

            /*
             *    1 identifier
             * +  1 amount
             * +  3 itemId
             * +  1 flags
             */
            const val BASE_SIZE = 6

            const val SKINNED = 0x80u
            const val FIRST_UPGRADE_SLOT_IN_USE = 0x40u
            const val SECOND_UPGRADE_SLOT_IN_USE = 0x20u
        }

        init {
            require(itemId < UINT_24BIT_MAX_VALUE) { "Item `itemId` must be in unsigned 24bit range (0..167771215)" }
            require(skinId == null || skinId < UINT_24BIT_MAX_VALUE) { "Item `skinId` must be in unsigned 24bit range (0..167771215)" }
            require(firstUpgradeSlot == null || firstUpgradeSlot < UINT_24BIT_MAX_VALUE) { "Item `firstUpgradeSlot` must be in unsigned 24bit range (0..167771215)" }
            require(secondUpgradeSlot == null || secondUpgradeSlot < UINT_24BIT_MAX_VALUE) { "Item `secondUpgradeSlot` must be in unsigned 24bit range (0..167771215)" }
        }

        @ExperimentalUnsignedTypes
        override fun asUByteArray(): UByteArray = buildArray(
            size = BASE_SIZE
                + (if (skinId != null) 4 else 0)
                + (if (firstUpgradeSlot != null) 4 else 0)
                + (if (secondUpgradeSlot != null) 4 else 0)
        ) {
            putByte(IDENTIFIER.toUByte())
            putByte(amount)
            put3Bytes(itemId)

            var flags = 0u
            if (skinId != null) flags = flags or SKINNED
            if (firstUpgradeSlot != null) flags = flags or FIRST_UPGRADE_SLOT_IN_USE
            if (secondUpgradeSlot != null) flags = flags or SECOND_UPGRADE_SLOT_IN_USE

            putByte(flags.toUByte())

            skinId?.let {
                put3Bytes(it)
                putByte(0u)
            }
            firstUpgradeSlot?.let {
                put3Bytes(it)
                putByte(0u)
            }
            secondUpgradeSlot?.let {
                put3Bytes(it)
                putByte(0u)
            }
        }

    }

    /**
     * A link to an NPC text.
     *
     * NPC text links are - at the time of writing - disabled and cannot be fully tested.
     *
     * Only the shape of the parameters (e.g. list sizes) is validated and not the actual data (e.g. ID validity).
     *
     * @param textId    the ID of the text. _(Must be in unsigned 24bit range.)_
     *
     * @throws IllegalArgumentException if any parameter value does not match its expected shape
     *
     * @since   0.1.0
     */
    @ExperimentalChatLinks
    public data class NpcText(
        @get:JvmName("getTextId")
        val textId: UInt
    ) : ChatLink() {

        internal companion object {
            const val IDENTIFIER = 0x03

            /*
             *    1 identifier
             * +  3 textId
             * +  1 reserved byte
             */
            const val BYTE_SIZE = 5
        }

        init {
            require(textId < UINT_24BIT_MAX_VALUE) { "NPCText `textId` must be in unsigned 24bit range (0..167771215)" }
        }

        @ExperimentalUnsignedTypes
        override fun asUByteArray(): UByteArray = buildArray(BYTE_SIZE) {
            putByte(IDENTIFIER.toUByte())
            put3Bytes(textId)
            putByte(0u)
        }

    }

    /**
     * A link to an outfit.
     *
     * Only the shape of the parameters (e.g. list sizes) is validated and not the actual data (e.g. ID validity).
     *
     * @param outfitId  the ID of the outfit. _(Must be in unsigned 24bit range.)_
     *
     * @throws IllegalArgumentException if any parameter value does not match its expected shape
     *
     * @since   0.1.0
     */
    public data class Outfit(
        @get:JvmName("getOutfitId")
        val outfitId: UInt
    ) : ChatLink() {

        internal companion object {
            const val IDENTIFIER = 0x0B

            /*
             *    1 identifier
             * +  3 outfitId
             * +  1 reserved byte
             */
            const val BYTE_SIZE = 5
        }

        init {
            require(outfitId < UINT_24BIT_MAX_VALUE) { "Outfit `outfitId` must be in unsigned 24bit range (0..167771215)" }
        }

        @ExperimentalUnsignedTypes
        override fun asUByteArray(): UByteArray = buildArray(BYTE_SIZE) {
            putByte(IDENTIFIER.toUByte())
            put3Bytes(outfitId)
            putByte(0u)
        }

    }

    /**
     * A link to a PoI (i.e. a landmark, a waypoint, or a vista).
     *
     * Only the shape of the parameters (e.g. list sizes) is validated and not the actual data (e.g. ID validity).
     *
     * @param poiId the ID of the PoI. _(Must be in unsigned 24bit range.)_
     *
     * @throws IllegalArgumentException if any parameter value does not match its expected shape
     *
     * @since   0.1.0
     */
    public data class Poi(
        @get:JvmName("getPoiId")
        val poiId: UInt
    ) : ChatLink() {

        internal companion object {
            const val IDENTIFIER = 0x04

            /*
             *    1 identifier
             * +  3 poiId
             * +  1 reserved byte
             */
            const val BYTE_SIZE = 5
        }

        init {
            require(poiId < UINT_24BIT_MAX_VALUE) { "PoI `poiId` must be in unsigned 24bit range (0..167771215)" }
        }

        @ExperimentalUnsignedTypes
        override fun asUByteArray(): UByteArray = buildArray(BYTE_SIZE) {
            putByte(IDENTIFIER.toUByte())
            put3Bytes(poiId)
            putByte(0u)
        }

    }

    /**
     * A link to a PvP game.
     *
     * The format of PvP game links is unknown.
     *
     * @since   0.1.0
     */
    @ExperimentalChatLinks
    public data object PvpGame : ChatLink() {

        internal const val IDENTIFIER = 0x05

        @ExperimentalUnsignedTypes
        override fun asUByteArray(): UByteArray { throw EncodingUnsupportedException("PvPGame") }

    }

    /**
     * A link to a recipe.
     *
     * Only the shape of the parameters (e.g. list sizes) is validated and not the actual data (e.g. ID validity).
     *
     * @param recipeId  the ID of the recipe. _(Must be in unsigned 24bit range.)_
     *
     * @throws IllegalArgumentException if any parameter value does not match its expected shape
     *
     * @since   0.1.0
     */
    public data class Recipe(
        @get:JvmName("getRecipeId")
        val recipeId: UInt
    ) : ChatLink() {

        internal companion object {
            const val IDENTIFIER = 0x09

            /*
             *    1 identifier
             * +  3 recipeId
             * +  1 reserved byte
             */
            const val BYTE_SIZE = 5
        }

        init {
            require(recipeId < UINT_24BIT_MAX_VALUE) { "Recipe `recipeId` must be in unsigned 24bit range (0..167771215)" }
        }

        @ExperimentalUnsignedTypes
        override fun asUByteArray(): UByteArray = buildArray(BYTE_SIZE) {
            putByte(IDENTIFIER.toUByte())
            put3Bytes(recipeId)
            putByte(0u)
        }

    }

    /**
     * A link to a skill.
     *
     * Only the shape of the parameters (e.g. list sizes) is validated and not the actual data (e.g. ID validity).
     *
     * @param skillId   the ID of the skill. _(Must be in unsigned 24bit range.)_
     *
     * @throws IllegalArgumentException if any parameter value does not match its expected shape
     *
     * @since   0.1.0
     */
    public data class Skill(
        @get:JvmName("getSkillId")
        val skillId: UInt
    ) : ChatLink() {

        internal companion object {
            const val IDENTIFIER = 0x06

            /*
             *    1 identifier
             * +  3 skillId
             * +  1 reserved byte
             */
            const val BYTE_SIZE = 5
        }

        init {
            require(skillId < UINT_24BIT_MAX_VALUE) { "Skill `skillId` must be in unsigned 24bit range (0..167771215)" }
        }

        @ExperimentalUnsignedTypes
        override fun asUByteArray(): UByteArray = buildArray(BYTE_SIZE) {
            putByte(IDENTIFIER.toUByte())
            put3Bytes(skillId)
            putByte(0u)
        }

    }

    /**
     * A link to a skin.
     *
     * Only the shape of the parameters (e.g. list sizes) is validated and not the actual data (e.g. ID validity).
     *
     * @param skinId    the ID of the skin. _(Must be in unsigned 24bit range.)_
     *
     * @throws IllegalArgumentException if any parameter value does not match its expected shape
     *
     * @since   0.1.0
     */
    public data class Skin(
        @get:JvmName("getSkinId")
        val skinId: UInt
    ) : ChatLink() {

        internal companion object {
            const val IDENTIFIER = 0x0A

            /*
             *    1 identifier
             * +  3 skinId
             * +  1 reserved byte
             */
            const val BYTE_SIZE = 5
        }

        init {
            require(skinId < UINT_24BIT_MAX_VALUE) { "Skin `skinId` must be in unsigned 24bit range (0..167771215)" }
        }

        @ExperimentalUnsignedTypes
        override fun asUByteArray(): UByteArray = buildArray(BYTE_SIZE) {
            putByte(IDENTIFIER.toUByte())
            put3Bytes(skinId)
            putByte(0u)
        }

    }

    /**
     * A link to a trait.
     *
     * Only the shape of the parameters (e.g. list sizes) is validated and not the actual data (e.g. ID validity).
     *
     * @param traitId   the ID of the trait. _(Must be in unsigned 24bit range.)_
     *
     * @throws IllegalArgumentException if any parameter value does not match its expected shape
     *
     * @since   0.1.0
     */
    public data class Trait(
        @get:JvmName("getTraitId")
        val traitId: UInt
    ) : ChatLink() {

        internal companion object {
            const val IDENTIFIER = 0x07

            /*
             *    1 identifier
             * +  3 traitId
             * +  1 reserved byte
             */
            const val BYTE_SIZE = 5
        }

        init {
            require(traitId < UINT_24BIT_MAX_VALUE) { "Trait `traitId` must be in unsigned 24bit range (0..167771215)" }
        }

        @ExperimentalUnsignedTypes
        override fun asUByteArray(): UByteArray = buildArray(BYTE_SIZE) {
            putByte(IDENTIFIER.toUByte())
            put3Bytes(traitId)
            putByte(0u)
        }

    }

    /**
     * A link to a user.
     *
     * User links are - at the time of writing - disabled and cannot be fully tested.
     *
     * Only the shape of the parameters (e.g. list sizes) is validated and not the actual data (e.g. ID validity).
     *
     * @param accountGuid   the account GUID
     * @param characterName the character name (UTF-16LE encoded)
     *
     * @throws IllegalArgumentException if any parameter value does not match its expected shape
     *
     * @since   0.1.0
     */
    @ExperimentalChatLinks
    public data class User @ExperimentalUnsignedTypes @ExperimentalUuidApi constructor(
        // https://youtrack.jetbrains.com/issue/KT-31880
        @get:JvmName("getAccountGuid")
        val accountGuid: Uuid,
        @get:JvmName("getCharacterName")
        val characterName: UByteArray
    ) : ChatLink() {

        internal companion object {
            const val IDENTIFIER = 0x08
        }

        @ExperimentalUnsignedTypes
        @ExperimentalUuidApi
        override fun asUByteArray(): UByteArray = buildArray(Uuid.SIZE_BYTES + characterName.size + 3) {
            putByte(IDENTIFIER.toUByte())
            putUuid(accountGuid)
            characterName.forEach { putByte(it) }

            putShort(0u)
        }

        @OptIn(ExperimentalUnsignedTypes::class, ExperimentalUuidApi::class)
        override fun equals(other: Any?): Boolean {
            if (this === other) return true

            return other is User
                && accountGuid == other.accountGuid
                && characterName.contentEquals(other.characterName)
        }

        @OptIn(ExperimentalUnsignedTypes::class, ExperimentalUuidApi::class)
        override fun hashCode(): Int {
            val prime = 31
            var result = 1
            result = prime * result + accountGuid.hashCode()
            result = prime * result + characterName.contentHashCode()
            return result
        }

    }

    /**
     * A link to a WvW objective.
     *
     * Only the shape of the parameters (e.g. list sizes) is validated and not the actual data (e.g. ID validity).
     *
     * @param objectiveId   the map-specific ID of the objective _(Must be in unsigned 24bit range.)_
     * @param mapId         the ID of the map of the objective. _(Must be in unsigned 24bit range.)_
     *
     * @throws IllegalArgumentException if any parameter value does not match its expected shape
     *
     * @since   0.1.0
     */
    public data class WvwObjective(
        @get:JvmName("getObjectiveId")
        val objectiveId: UInt,
        @get:JvmName("getMapId")
        val mapId: UInt
    ) : ChatLink() {

        internal companion object {
            const val IDENTIFIER = 0x0C

            /*
             *    1 identifier
             * +  3 objectiveId
             * +  1 reserved byte
             * +  3 mapId
             * +  1 reserved byte
             */
            const val BYTE_SIZE = 9
        }

        init {
            require(objectiveId < UINT_24BIT_MAX_VALUE) { "WvWObjective `objectiveId` must be in unsigned 24bit range (0..167771215)" }
            require(mapId < UINT_24BIT_MAX_VALUE) { "WvWObjective `mapId` must be in unsigned 24bit range (0..167771215)" }
        }

        @ExperimentalUnsignedTypes
        override fun asUByteArray(): UByteArray = buildArray(BYTE_SIZE) {
            putByte(IDENTIFIER.toUByte())
            put3Bytes(objectiveId)
            putByte(0u)
            put3Bytes(mapId)
            putByte(0u)
        }

        /**
         * Returns the ID for this objective as used in the official Guild Wars 2 API.
         *
         * The ID has the format: `"$mapId-$objectiveId"`
         *
         * @since   0.1.0
         */
        public val apiId: String get() = "$mapId-$objectiveId"

    }

}
