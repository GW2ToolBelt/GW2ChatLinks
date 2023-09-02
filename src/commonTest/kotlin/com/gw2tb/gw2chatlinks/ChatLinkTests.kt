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
@file:OptIn(ExperimentalChatLinks::class)
package com.gw2tb.gw2chatlinks

import kotlin.test.*

@ExperimentalUnsignedTypes
class ChatLinkTests {

    private fun <T> assertDoesNotThrow(block: () -> T): T {
        try {
            return block()
        } catch (t: Throwable) {
            fail(message = "Caught unexpected throwable", cause = t)
        }
    }

    private fun <T> assertDoesNotThrow(result: Result<T>): T =
        assertDoesNotThrow { result.getOrThrow() }

    @Test
    fun testDecodeCoinLink() {
        assertEquals(ChatLink.Coin(amount = 10203u), assertDoesNotThrow(decodeChatLink("[&AdsnAAA=]")))
    }

    @Test
    fun testEncodeCoinLink() {
        assertEquals("[&AdsnAAA=]", assertDoesNotThrow(encodeChatLink(ChatLink.Coin(amount = 10203u))))
    }

    @Test
    fun testDecodeItemLink() {
        val itemID = 46762u
        val skinID = 3709u
        val sigil1ID = 24575u
        val sigil2ID = 24615u

        assertEquals(ChatLink.Item(amount = 1u, itemID = itemID), assertDoesNotThrow(decodeChatLink("[&AgGqtgAA]")))
        assertEquals(ChatLink.Item(amount = 1u, itemID = itemID, firstUpgradeSlot = sigil1ID), assertDoesNotThrow(decodeChatLink("[&AgGqtgBA/18AAA==]")))
        assertEquals(ChatLink.Item(amount = 1u, itemID = itemID, firstUpgradeSlot = sigil1ID, secondUpgradeSlot = sigil2ID), assertDoesNotThrow(decodeChatLink("[&AgGqtgBg/18AACdgAAA=]")))
        assertEquals(ChatLink.Item(amount = 1u, itemID = itemID, skinID = skinID), assertDoesNotThrow(decodeChatLink("[&AgGqtgCAfQ4AAA==]")))
        assertEquals(ChatLink.Item(amount = 1u, itemID = itemID, skinID = skinID, firstUpgradeSlot = sigil1ID), assertDoesNotThrow(decodeChatLink("[&AgGqtgDAfQ4AAP9fAAA=]")))
        assertEquals(ChatLink.Item(amount = 1u, itemID = itemID, skinID = skinID, firstUpgradeSlot = sigil1ID, secondUpgradeSlot = sigil2ID), assertDoesNotThrow(decodeChatLink("[&AgGqtgDgfQ4AAP9fAAAnYAAA]")))
    }

    @Test
    fun testEncodeItemLink() {
        val itemID = 46762u
        val skinID = 3709u
        val sigil1ID = 24575u
        val sigil2ID = 24615u

        assertEquals("[&AgGqtgAA]", assertDoesNotThrow(encodeChatLink(ChatLink.Item(amount = 1u, itemID = itemID))))
        assertEquals("[&AgGqtgBA/18AAA==]", assertDoesNotThrow(encodeChatLink(ChatLink.Item(amount = 1u, itemID = itemID, firstUpgradeSlot = sigil1ID))))
        assertEquals("[&AgGqtgBg/18AACdgAAA=]", assertDoesNotThrow(encodeChatLink(ChatLink.Item(amount = 1u, itemID = itemID, firstUpgradeSlot = sigil1ID, secondUpgradeSlot = sigil2ID))))
        assertEquals("[&AgGqtgCAfQ4AAA==]", assertDoesNotThrow(encodeChatLink(ChatLink.Item(amount = 1u, itemID = itemID, skinID = skinID))))
        assertEquals("[&AgGqtgDAfQ4AAP9fAAA=]", assertDoesNotThrow(encodeChatLink(ChatLink.Item(amount = 1u, itemID = itemID, skinID = skinID, firstUpgradeSlot = sigil1ID))))
        assertEquals("[&AgGqtgDgfQ4AAP9fAAAnYAAA]", assertDoesNotThrow(encodeChatLink(ChatLink.Item(amount = 1u, itemID = itemID, skinID = skinID, firstUpgradeSlot = sigil1ID, secondUpgradeSlot = sigil2ID))))
    }

    @Test
    fun testDecodeNPCTextLink() {
        assertEquals(ChatLink.NPCText(textID = 10007u), assertDoesNotThrow(decodeChatLink("[&AxcnAAA=]")))
        assertEquals(ChatLink.NPCText(textID = 10008u), assertDoesNotThrow(decodeChatLink("[&AxgnAAA=]")))
        assertEquals(ChatLink.NPCText(textID = 10009u), assertDoesNotThrow(decodeChatLink("[&AxknAAA=]")))
        assertEquals(ChatLink.NPCText(textID = 10016u), assertDoesNotThrow(decodeChatLink("[&AyAnAAA=]")))
    }

    @Test
    fun testEncodeNPCTextLink() {
        assertEquals("[&AxcnAAA=]", assertDoesNotThrow(encodeChatLink(ChatLink.NPCText(textID = 10007u))))
        assertEquals("[&AxgnAAA=]", assertDoesNotThrow(encodeChatLink(ChatLink.NPCText(textID = 10008u))))
        assertEquals("[&AxknAAA=]", assertDoesNotThrow(encodeChatLink(ChatLink.NPCText(textID = 10009u))))
        assertEquals("[&AyAnAAA=]", assertDoesNotThrow(encodeChatLink(ChatLink.NPCText(textID = 10016u))))
    }

    @Test
    fun testDecodePoILink() {
        assertEquals(ChatLink.PoI(poiID = 56u), assertDoesNotThrow(decodeChatLink("[&BDgAAAA=]")))
        assertEquals(ChatLink.PoI(poiID = 72u), assertDoesNotThrow(decodeChatLink("[&BEgAAAA=]")))
        assertEquals(ChatLink.PoI(poiID = 825u), assertDoesNotThrow(decodeChatLink("[&BDkDAAA=]")))
    }

    @Test
    fun testEncodePoILink() {
        assertEquals("[&BDgAAAA=]", assertDoesNotThrow(encodeChatLink(ChatLink.PoI(poiID = 56u))))
        assertEquals("[&BEgAAAA=]", assertDoesNotThrow(encodeChatLink(ChatLink.PoI(poiID = 72u))))
        assertEquals("[&BDkDAAA=]", assertDoesNotThrow(encodeChatLink(ChatLink.PoI(poiID = 825u))))
    }

    @Test
    fun testEncodePvPGameLink() {
        assertFailsWith(EncodingUnsupportedException::class) { encodeChatLink(ChatLink.PvPGame).getOrThrow() }
    }

    @Test
    fun testDecodeSkillLink() {
        assertEquals(ChatLink.Skill(skillID = 743u), assertDoesNotThrow(decodeChatLink("[&BucCAAA=]")))
        assertEquals(ChatLink.Skill(skillID = 5491u), assertDoesNotThrow(decodeChatLink("[&BnMVAAA=]")))
        assertEquals(ChatLink.Skill(skillID = 5501u), assertDoesNotThrow(decodeChatLink("[&Bn0VAAA=]")))
    }

    @Test
    fun testEncodeSkillLink() {
        assertEquals("[&BucCAAA=]", assertDoesNotThrow(encodeChatLink(ChatLink.Skill(skillID = 743u))))
        assertEquals("[&BnMVAAA=]", assertDoesNotThrow(encodeChatLink(ChatLink.Skill(skillID = 5491u))))
        assertEquals("[&Bn0VAAA=]", assertDoesNotThrow(encodeChatLink(ChatLink.Skill(skillID = 5501u))))
    }

    @Test
    fun testDecodeTraitLink() {
        assertEquals(ChatLink.Trait(traitID = 646u), assertDoesNotThrow(decodeChatLink("[&B4YCAAA=]")))
    }

    @Test
    fun testEncodeTraitLink() {
        assertEquals("[&B4YCAAA=]", assertDoesNotThrow(encodeChatLink(ChatLink.Trait(traitID = 646u))))
    }

    @Test
    fun testDecodeUserLink() {
        val accountGUID = ubyteArrayOf(0x1u, 0x02u, 0x03u, 0x04u, 0x05u, 0x06u, 0x07u, 0x08u, 0x09u, 0x0Au, 0x0Bu, 0x0Cu, 0x0Du, 0x0Eu, 0x0Fu, 0x10u)
        val characterName = ubyteArrayOf(0x45u, 0x0u, 0x61u, 0x0u, 0x73u, 0x0u, 0x74u, 0x0u, 0x65u, 0x0u, 0x72u, 0x0u)

        assertEquals(ChatLink.User(accountGUID = accountGUID, characterName = characterName), assertDoesNotThrow(decodeChatLink("[&CAECAwQFBgcICQoLDA0ODxBFAGEAcwB0AGUAcgAAAA==]")))
    }

    @Test
    fun testEncodeUserLink() {
        val accountGUID = ubyteArrayOf(0x1u, 0x02u, 0x03u, 0x04u, 0x05u, 0x06u, 0x07u, 0x08u, 0x09u, 0x0Au, 0x0Bu, 0x0Cu, 0x0Du, 0x0Eu, 0x0Fu, 0x10u)
        val characterName = ubyteArrayOf(0x45u, 0x0u, 0x61u, 0x0u, 0x73u, 0x0u, 0x74u, 0x0u, 0x65u, 0x0u, 0x72u, 0x0u)

        assertEquals("[&CAECAwQFBgcICQoLDA0ODxBFAGEAcwB0AGUAcgAAAA==]", assertDoesNotThrow(encodeChatLink(ChatLink.User(accountGUID = accountGUID, characterName = characterName))))
    }

    @Test
    fun testDecodeRecipeLink() {
        assertEquals(ChatLink.Recipe(recipeID = 1u), assertDoesNotThrow(decodeChatLink("[&CQEAAAA=]")))
        assertEquals(ChatLink.Recipe(recipeID = 2u), assertDoesNotThrow(decodeChatLink("[&CQIAAAA=]")))
        assertEquals(ChatLink.Recipe(recipeID = 7u), assertDoesNotThrow(decodeChatLink("[&CQcAAAA=]")))
    }

    @Test
    fun testEncodeRecipeLink() {
        assertEquals("[&CQEAAAA=]", assertDoesNotThrow(encodeChatLink(ChatLink.Recipe(recipeID = 1u))))
        assertEquals("[&CQIAAAA=]", assertDoesNotThrow(encodeChatLink(ChatLink.Recipe(recipeID = 2u))))
        assertEquals("[&CQcAAAA=]", assertDoesNotThrow(encodeChatLink(ChatLink.Recipe(recipeID = 7u))))
    }

    @Test
    fun testDecodeSkinLink() {
        assertEquals(ChatLink.Skin(skinID = 4u), assertDoesNotThrow(decodeChatLink("[&CgQAAAA=]")))
    }

    @Test
    fun testEncodeSkinLink() {
        assertEquals("[&CgQAAAA=]", assertDoesNotThrow(encodeChatLink(ChatLink.Skin(skinID = 4u))))
    }

    @Test
    fun testDecodeOutfitLink() {
        assertEquals(ChatLink.Outfit(outfitID = 4u), assertDoesNotThrow(decodeChatLink("[&CwQAAAA=]")))
    }

    @Test
    fun testEncodeOutfitLink() {
        assertEquals("[&CwQAAAA=]", assertDoesNotThrow(encodeChatLink(ChatLink.Outfit(outfitID = 4u))))
    }

    @Test
    fun testDecodeWvWObjectiveLink() {
        assertEquals(ChatLink.WvWObjective(objectiveID = 6u, mapID = 38u), assertDoesNotThrow(decodeChatLink("[&DAYAAAAmAAAA]")))
    }

    @Test
    fun testEncodeWvWObjectiveLink() {
        assertEquals("[&DAYAAAAmAAAA]", assertDoesNotThrow(encodeChatLink(ChatLink.WvWObjective(objectiveID = 6u, mapID = 38u))))
    }

    @Test
    fun testWvWObjectiveLinkApiID() {
        assertEquals("38-6", ChatLink.WvWObjective(objectiveID = 6u, mapID = 38u).apiID)
    }

    @Test
    fun testDecodeBuildTemplate() {
        val buildTemplate = ChatLink.BuildTemplate(
            professionID = Profession.GUARDIAN.paletteID,
            specializations = listOf(
                ChatLink.BuildTemplate.Specialization(specializationID = 16u, majorTraits = listOf(2u, 2u, 1u)),
                ChatLink.BuildTemplate.Specialization(specializationID = 42u, majorTraits = listOf(1u, 1u, 2u)),
                ChatLink.BuildTemplate.Specialization(specializationID = 27u, majorTraits = listOf(0u, 1u, 2u))
            ),
            skills = listOf(3878u, 4746u, 328u, 254u, 4789u),
            aquaticSkills = listOf(3878u, 310u, 328u, 254u, 4745u),
            professionContext = null
        )
        val rangerBuildTemplate = ChatLink.BuildTemplate(
            professionID = Profession.RANGER.paletteID,
            specializations = listOf(
                ChatLink.BuildTemplate.Specialization(specializationID = 8u, majorTraits = listOf(1u, 1u, 0u)),
                ChatLink.BuildTemplate.Specialization(specializationID = 32u, majorTraits = listOf(2u, 2u, 2u)),
                ChatLink.BuildTemplate.Specialization(specializationID = 55u, majorTraits = listOf(1u, 1u, 1u))
            ),
            skills = listOf(121u, 421u, 181u, 188u, 5678u),
            aquaticSkills = listOf(5934u, 421u, 188u, 428u, 5678u),
            professionContext = ChatLink.BuildTemplate.RangerContext(
                pets = listOf(59u, 46u),
                aquaticPets = listOf(21u, 47u)
            )
        )
        val revenantBuildTemplate = ChatLink.BuildTemplate(
            professionID = Profession.REVENANT.paletteID,
            specializations = listOf(
                ChatLink.BuildTemplate.Specialization(specializationID = 3u, majorTraits = listOf(1u, 0u, 1u)),
                ChatLink.BuildTemplate.Specialization(specializationID = 15u, majorTraits = listOf(1u, 0u, 1u)),
                ChatLink.BuildTemplate.Specialization(specializationID = 63u, majorTraits = listOf(0u, 2u, 2u))
            ),
            skills = listOf(4572u, 4564u, 4651u, 4614u, 4554u),
            aquaticSkills = listOf(4572u, 4564u, 4651u, 4614u, 4554u),
            professionContext = ChatLink.BuildTemplate.RevenantContext(
                legends = listOf(5u, 2u),
                aquaticLegends = listOf(2u, 3u),
                inactiveLegendUtilitySkills = listOf(4564u, 4651u, 4614u),
                inactiveAquaticLegendUtilitySkills = listOf(4614u, 4564u, 4651u)
            )
        )

        assertEquals(buildTemplate, assertDoesNotThrow(decodeChatLink("[&DQEQLyo6GzkmDyYPihI2AUgBSAH+AP4AtRKJEgAAAAAAAAAAAAAAAAAAAAA=]")))
        assertEquals(rangerBuildTemplate, assertDoesNotThrow(decodeChatLink("[&DQQIGiA/Nyp5AC4XpQGlAbUAvAC8AKwBLhYuFjsuFS8AAAAAAAAAAAAAAAA=]")))
        assertEquals(revenantBuildTemplate, assertDoesNotThrow(decodeChatLink("[&DQkDJg8mPz3cEdwR1BHUESsSKxIGEgYSyhHKEQUCAgPUESsSBhIGEtQRKxI=]")))

        assertEquals(buildTemplate, assertDoesNotThrow(decodeChatLink("[&DQEQLyo6GzkmDyYPihI2AUgBSAH+AP4AtRKJEgAAAAAAAAAAAAAAAAAAAAAAAA==]")))
        assertEquals(rangerBuildTemplate, assertDoesNotThrow(decodeChatLink("[&DQQIGiA/Nyp5AC4XpQGlAbUAvAC8AKwBLhYuFjsuFS8AAAAAAAAAAAAAAAAAAA==]")))
        assertEquals(revenantBuildTemplate, assertDoesNotThrow(decodeChatLink("[&DQkDJg8mPz3cEdwR1BHUESsSKxIGEgYSyhHKEQUCAgPUESsSBhIGEtQRKxIAAA==]")))
    }

    @Test
    fun testDecodeBuildTemplate_2023_07() {
        val buildTemplate = ChatLink.BuildTemplate(
            profession = Profession.GUARDIAN,
            specializations = listOf(
                ChatLink.BuildTemplate.Specialization(specializationID = 16u, majorTraits = listOf(2u, 2u, 1u)),
                ChatLink.BuildTemplate.Specialization(specializationID = 42u, majorTraits = listOf(1u, 1u, 2u)),
                ChatLink.BuildTemplate.Specialization(specializationID = 27u, majorTraits = listOf(0u, 1u, 2u))
            ),
            skills = listOf(3878u, 4746u, 328u, 254u, 4789u),
            aquaticSkills = listOf(3878u, 310u, 328u, 254u, 4745u),
            professionContext = null,
            weapons = listOf(0x05u, 0x2Fu),
            weaponSkillOverrides = listOf()
        )
        val rangerBuildTemplate = ChatLink.BuildTemplate(
            profession = Profession.RANGER,
            specializations = listOf(
                ChatLink.BuildTemplate.Specialization(specializationID = 8u, majorTraits = listOf(1u, 1u, 0u)),
                ChatLink.BuildTemplate.Specialization(specializationID = 32u, majorTraits = listOf(2u, 2u, 2u)),
                ChatLink.BuildTemplate.Specialization(specializationID = 55u, majorTraits = listOf(1u, 1u, 1u))
            ),
            skills = listOf(121u, 421u, 181u, 188u, 5678u),
            aquaticSkills = listOf(5934u, 421u, 188u, 428u, 5678u),
            professionContext = ChatLink.BuildTemplate.RangerContext(
                pets = listOf(59u, 46u),
                aquaticPets = listOf(21u, 47u)
            ),
            weapons = listOf(),
            weaponSkillOverrides = listOf(0xF767u)
        )
        val revenantBuildTemplate = ChatLink.BuildTemplate(
            profession = Profession.REVENANT,
            specializations = listOf(
                ChatLink.BuildTemplate.Specialization(specializationID = 3u, majorTraits = listOf(1u, 0u, 1u)),
                ChatLink.BuildTemplate.Specialization(specializationID = 15u, majorTraits = listOf(1u, 0u, 1u)),
                ChatLink.BuildTemplate.Specialization(specializationID = 63u, majorTraits = listOf(0u, 2u, 2u))
            ),
            skills = listOf(4572u, 4564u, 4651u, 4614u, 4554u),
            aquaticSkills = listOf(4572u, 4564u, 4651u, 4614u, 4554u),
            professionContext = ChatLink.BuildTemplate.RevenantContext(
                legends = listOf(5u, 2u),
                aquaticLegends = listOf(2u, 3u),
                inactiveLegendUtilitySkills = listOf(4564u, 4651u, 4614u),
                inactiveAquaticLegendUtilitySkills = listOf(4614u, 4564u, 4651u)
            ),
            weapons = listOf(0x05u, 0x2Fu),
            weaponSkillOverrides = listOf()
        )

        assertEquals(buildTemplate, assertDoesNotThrow(decodeChatLink("[&DQEQLyo6GzkmDyYPihI2AUgBSAH+AP4AtRKJEgAAAAAAAAAAAAAAAAAAAAACBQAvAAA=]")))
        assertEquals(rangerBuildTemplate, assertDoesNotThrow(decodeChatLink("[&DQQIGiA/Nyp5AC4XpQGlAbUAvAC8AKwBLhYuFjsuFS8AAAAAAAAAAAAAAAAAAWf3AAA=]")))
        assertEquals(revenantBuildTemplate, assertDoesNotThrow(decodeChatLink("[&DQkDJg8mPz3cEdwR1BHUESsSKxIGEgYSyhHKEQUCAgPUESsSBhIGEtQRKxICBQAvAAA=]")))
    }

    @Test
    fun testEncodeBuildTemplate() {
        val buildTemplate = ChatLink.BuildTemplate(
            professionID = Profession.GUARDIAN.paletteID,
            specializations = listOf(
                ChatLink.BuildTemplate.Specialization(specializationID = 16u, majorTraits = listOf(2u, 2u, 1u)),
                ChatLink.BuildTemplate.Specialization(specializationID = 42u, majorTraits = listOf(1u, 1u, 2u)),
                ChatLink.BuildTemplate.Specialization(specializationID = 27u, majorTraits = listOf(0u, 1u, 2u))
            ),
            skills = listOf(3878u, 4746u, 328u, 254u, 4789u),
            aquaticSkills = listOf(3878u, 310u, 328u, 254u, 4745u),
            professionContext = null,
            weapons = listOf(0x05u, 0x2Fu),
            weaponSkillOverrides = listOf()
        )
        val rangerBuildTemplate = ChatLink.BuildTemplate(
            professionID = Profession.RANGER.paletteID,
            specializations = listOf(
                ChatLink.BuildTemplate.Specialization(specializationID = 8u, majorTraits = listOf(1u, 1u, 0u)),
                ChatLink.BuildTemplate.Specialization(specializationID = 32u, majorTraits = listOf(2u, 2u, 2u)),
                ChatLink.BuildTemplate.Specialization(specializationID = 55u, majorTraits = listOf(1u, 1u, 1u))
            ),
            skills = listOf(121u, 421u, 181u, 188u, 5678u),
            aquaticSkills = listOf(5934u, 421u, 188u, 428u, 5678u),
            professionContext = ChatLink.BuildTemplate.RangerContext(
                pets = listOf(59u, 46u),
                aquaticPets = listOf(21u, 47u)
            ),
            weapons = listOf(),
            weaponSkillOverrides = listOf(0xF767u)
        )
        val revenantBuildTemplate = ChatLink.BuildTemplate(
            professionID = Profession.REVENANT.paletteID,
            specializations = listOf(
                ChatLink.BuildTemplate.Specialization(specializationID = 3u, majorTraits = listOf(1u, 0u, 1u)),
                ChatLink.BuildTemplate.Specialization(specializationID = 15u, majorTraits = listOf(1u, 0u, 1u)),
                ChatLink.BuildTemplate.Specialization(specializationID = 63u, majorTraits = listOf(0u, 2u, 2u))
            ),
            skills = listOf(4572u, 4564u, 4651u, 4614u, 4554u),
            aquaticSkills = listOf(4572u, 4564u, 4651u, 4614u, 4554u),
            professionContext = ChatLink.BuildTemplate.RevenantContext(
                legends = listOf(5u, 2u),
                aquaticLegends = listOf(2u, 3u),
                inactiveLegendUtilitySkills = listOf(4564u, 4651u, 4614u),
                inactiveAquaticLegendUtilitySkills = listOf(4614u, 4564u, 4651u)
            ),
            weapons = listOf(0x05u, 0x2Fu),
            weaponSkillOverrides = listOf()
        )

        assertEquals("[&DQEQLyo6GzkmDyYPihI2AUgBSAH+AP4AtRKJEgAAAAAAAAAAAAAAAAAAAAACBQAvAAA=]", assertDoesNotThrow(encodeChatLink(buildTemplate)))
        assertEquals("[&DQQIGiA/Nyp5AC4XpQGlAbUAvAC8AKwBLhYuFjsuFS8AAAAAAAAAAAAAAAAAAWf3AAA=]", assertDoesNotThrow(encodeChatLink(rangerBuildTemplate)))
        assertEquals("[&DQkDJg8mPz3cEdwR1BHUESsSKxIGEgYSyhHKEQUCAgPUESsSBhIGEtQRKxICBQAvAAA=]", assertDoesNotThrow(encodeChatLink(revenantBuildTemplate)))
    }

}