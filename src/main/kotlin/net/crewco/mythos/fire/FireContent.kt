package net.crewco.mythos.fire

import net.crewco.mythos.api.era.EraDefinition
import net.crewco.mythos.api.era.Objective
import net.crewco.mythos.api.role.ClaimRules
import net.crewco.mythos.api.role.Endurance
import net.crewco.mythos.api.role.RoleDefinition
import net.crewco.mythos.api.role.RoleTier
import net.crewco.mythos.api.story.beats
import net.crewco.mythos.api.story.line
import net.crewco.mythos.api.story.pause
import net.crewco.mythos.api.story.title

/**
 * Story #4 — the first time anyone in this mythology does something *for* the mortals,
 * and the first time the gods find out they can be defied by someone who isn't stronger
 * than them, only cleverer.
 *
 * It is also the first story with an ending this addon cannot write. Prometheus's
 * punishment ends when Heracles comes — and Heracles is four chapters and one unwritten
 * jar away. So this addon opens a hole (see [Liberation]) and leaves it open. Possibly
 * for years. That is, structurally, exactly what the myth does.
 */
object FireContent {

    const val ERA = "theft-of-fire"

    /** Set on Prometheus when he's chained. The eagle reads it. */
    const val CHAINED = "fire.chained"
    const val LIVERS = "fire.livers-eaten"

    val ERA_OF_FIRE = EraDefinition(
        id = ERA,
        displayName = "The Theft of Fire",
        order = 3,
        next = "ages-of-man",
        subtitle = "somebody is going to pay for this",
        lore = listOf(
            "The gods have everything. The mortals have nothing, and are cold, and are food.",
            "One Titan — who fought on Zeus's side, who Zeus trusts — notices.",
        ),
        prologue = beats {
            pause(20)
            title("<gold>The Theft of Fire", "<gray>somebody is going to pay for this", sound = "minecraft:item.firecharge.use")
            pause(50)
            line("<gray>The gods have everything. The mortals have nothing.", delayTicks = 50)
            line("<dark_gray><i>They are cold, they are afraid of the dark, and they are food.", delayTicks = 55)
            pause(40)
            line("<gray>One Titan notices. His name means <white>forethought<gray>, which is the joke,", delayTicks = 55)
            line("<gray>because he can see exactly what this will cost him and he does it anyway.", delayTicks = 60)
            pause(40)
            line("<white>/claim prometheus <dark_gray>· <white>/claim epimetheus <dark_gray>· <gray>the mortals are watching.", delayTicks = 30)
        },
        epilogue = beats {
            pause(30)
            title("<dark_red>The Eagle", "<gray>every day, forever, until someone comes", delayTicks = 20, sound = "minecraft:entity.ender_dragon.growl")
            pause(60)
            line("<gray>He is nailed to a rock at the edge of the world, and the eagle comes at dawn.", delayTicks = 55)
            line("<dark_gray><i>It eats his liver. The liver grows back. That is the entire punishment.", delayTicks = 60)
            pause(50)
            line("<gray>He could end it in a sentence. He knows a secret Zeus would trade the world for:", delayTicks = 55)
            line("<dark_gray><i>which woman will bear a son greater than his father.", delayTicks = 50)
            line("<gray>He does not say it. He is going to lie there for thirty generations,", delayTicks = 55)
            line("<gray>until a man in a lion's skin walks past and asks what he did.", delayTicks = 60)
            pause(60)
        },
        objectives = listOf(
            Objective("the_trick", "The sacrifice at Mecone is divided, and Zeus chooses badly"),
            Objective("fire_withheld", "Zeus takes fire away from the mortals"),
            Objective("fire_stolen", "Fire is carried back down in a hollow stalk"),
            Objective("pandora_made", "A gift is made, and it is beautiful, and it is a trap"),
            Objective("the_jar", "Pandora opens it", hidden = true),
            Objective("prometheus_chained", "The thief is nailed to a rock"),
            Objective("prometheus_freed", "Someone, eventually, comes for him", optional = true),
        ),
    )

    /**
     * Prometheus is a Titan who was on the *winning* side — so if EraOfCreation ran, his
     * father Iapetus is in Tartarus and he is not. He isn't in that addon's twelve, so we
     * register him ourselves. He's claimable, and he's the best role in the game, and
     * taking him is a decision you will regret.
     */
    val PROMETHEUS = RoleDefinition(
        id = "prometheus",
        displayName = "Prometheus",
        tier = RoleTier.TITAN,
        era = ERA,
        domains = listOf("forethought", "fire", "the mortals"),
        color = "<gold>",
        lore = listOf(
            "Your name means you can see what's coming.",
            "You are going to do it anyway. That's not stupidity — that's the only kind of courage there is.",
        ),
        powers = listOf("mecone", "steal_fire", "endure"),
        claimRules = listOf(ClaimRules.sinceEra(ERA)),
        // He is still on that rock in the Heroic Age. He does not retire.
        endurance = Endurance.ETERNAL,
    )

    val EPIMETHEUS = RoleDefinition(
        id = "epimetheus",
        displayName = "Epimetheus",
        tier = RoleTier.TITAN,
        era = ERA,
        domains = listOf("afterthought"),
        color = "<gold>",
        lore = listOf(
            "Your name means you work things out afterwards.",
            "Your brother told you not to accept any gifts from Zeus. You are going to accept a gift from Zeus.",
        ),
        powers = listOf("accept"),
        claimRules = listOf(ClaimRules.sinceEra(ERA)),
        endurance = Endurance.ERA,
    )

    /**
     * Pandora is *made*, not born and not claimed — Hephaestus builds her out of clay and
     * every god puts something in. So her role is sealed until `/power make_pandora` runs,
     * and then it's offered to the spirit queue like any other vacancy.
     *
     * She is not the villain of this story. She was handed a sealed jar and told not to
     * open it by the people who gave it to her, which is not a warning, it's an instruction.
     */
    val PANDORA = RoleDefinition(
        id = "pandora",
        displayName = "Pandora",
        tier = RoleTier.MORTAL,
        era = ERA,
        domains = listOf("the gift", "the jar"),
        color = "<light_purple>",
        lore = listOf(
            "Made by Hephaestus. Dressed by Athena. Given a voice by Hermes, which was Zeus's idea of a joke.",
            "They gave you a jar and told you not to open it. Think about what kind of person does that.",
        ),
        powers = listOf("open_jar"),
        claimRules = listOf(ClaimRules.sinceEra(ERA)),
        endurance = Endurance.ETERNAL,
        startsSealed = true, // she does not exist until she is made
    )
}
