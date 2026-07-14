package net.crewco.mythos.fire

import net.crewco.mythos.addon.AddonContext
import net.crewco.mythos.api.Mythos
import net.crewco.mythos.api.power.Power
import net.crewco.mythos.api.power.PowerContext
import net.crewco.mythos.api.story.Beat
import net.crewco.mythos.command.CommandContext.Companion.mm
import net.crewco.mythos.fire.FireContent.CHAINED
import net.crewco.mythos.fire.FireContent.ERA
import org.bukkit.Bukkit
import org.bukkit.Difficulty
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/** Prometheus offers Zeus a choice, which is the most dangerous thing you can do to a king. */
class MeconePower(private val mythos: Mythos, private val context: AddonContext) : Power {
    override val id = "mecone"
    override val displayName = "The Sacrifice at Mecone"
    override val description = "Divide the offering in two and let Zeus pick. /power mecone"
    override val cooldownSeconds = 0

    override fun use(ctx: PowerContext): Boolean {
        val prometheus = ctx.player
        if (mythos.eras.isComplete(ERA, "the_trick")) {
            prometheus.sendMessage(mm("<red>You only get to do that once. He's not that vain. <dark_gray><i>He is almost that vain."))
            return false
        }
        val zeus = mythos.roles.holders("zeus").mapNotNull { Bukkit.getPlayer(it) }.firstOrNull()
            ?: return false.also { prometheus.sendMessage(mm("<red>Zeus isn't here. The trick needs a mark.")) }

        // The host's menu framework, opened for SOMEONE ELSE — it hops to their region.
        context.menus.open(zeus, MeconeMenu(mythos, prometheus))
        prometheus.sendMessage(mm("<gray>You lay out both portions and step back. <dark_gray><i>Now it's his."))
        return true
    }
}

/** Zeus takes fire away. It is a petty, enormous act. */
class WithholdFirePower(
    private val mythos: Mythos,
    private val context: AddonContext,
    private val state: FireState,
) : Power {
    override val id = "withhold_fire"
    override val displayName = "Take Back the Fire"
    override val description = "Mortals may not light anything. Let them eat it raw. /power withhold_fire"
    override val cooldownSeconds = 0

    override fun use(ctx: PowerContext): Boolean {
        if (state.fireWithheld) {
            ctx.player.sendMessage(mm("<red>They already have nothing. <gray>You cannot take it twice."))
            return false
        }
        state.fireWithheld = true
        state.save()

        // EVERY HEARTH IN THE WORLD GOES OUT. Not a message saying it did — the torches
        // actually go out, around every mortal on the server, on their own region threads.
        Bukkit.getOnlinePlayers().forEach { player ->
            context.schedulers.entity(player) {
                val here = player.location
                for (x in -24..24) for (y in -8..8) for (z in -24..24) {
                    val block = here.clone().add(x.toDouble(), y.toDouble(), z.toDouble()).block
                    when (block.type) {
                        Material.TORCH, Material.WALL_TORCH, Material.FIRE, Material.SOUL_FIRE ->
                            block.type = Material.AIR
                        Material.CAMPFIRE, Material.SOUL_CAMPFIRE -> block.type = Material.AIR
                        else -> Unit
                    }
                }
            }
        }

        mythos.narrator.tell(
            listOf(
                Beat(20, text = "<dark_gray>» <yellow>Zeus <gray>takes the fire back.", sound = "minecraft:block.fire.extinguish"),
                Beat(50, text = "<gray>Every hearth in the world goes out at the same moment. Look around."),
                Beat(50, text = "<dark_gray><i>The mortals cannot light anything now. They will be cold, and they will eat it raw."),
            ),
        )
        mythos.chronicle.record("story", "<yellow>Zeus <gray>took fire away from the mortals.")
        mythos.eras.complete(ERA, "fire_withheld", "the hearths went out")
        return true
    }
}

/** The theft itself: a coal in a hollow stalk, carried down. */
class StealFirePower(
    private val mythos: Mythos,
    private val context: AddonContext,
    private val state: FireState,
) : Power {
    override val id = "steal_fire"
    override val displayName = "The Hollow Stalk"
    override val description = "Take a coal from the forge and carry it down to them. /power steal_fire"
    override val cooldownSeconds = 0

    override fun use(ctx: PowerContext): Boolean {
        val thief = ctx.player
        if (!state.fireWithheld) {
            thief.sendMessage(mm("<red>They still have fire. <gray>There is nothing to steal yet."))
            return false
        }
        if (mythos.eras.isComplete(ERA, "fire_stolen")) {
            thief.sendMessage(mm("<red>It's already down there. Look at the lights."))
            return false
        }
        if (thief.location.blockY < HIGH) {
            thief.sendMessage(mm("<red>You have to go <white>up<red> for it. <gray>The fire is where the gods are. <dark_gray>(y ≥ $HIGH)"))
            return false
        }

        thief.inventory.addItem(stalk(context))
        thief.sendMessage(mm("<gold>A coal, in a hollow fennel stalk. <gray>It will not burn through for a while."))
        thief.sendMessage(mm("<dark_gray><i>Carry it down. Give it to a mortal. Right-click them with it."))
        mythos.chronicle.record("story", "<gold>Prometheus <gray>took a coal from the forge of the gods.")
        return true
    }

    companion object {
        const val HIGH = 150

        fun key(context: AddonContext) = NamespacedKey(context.plugin, "hollow_stalk")

        fun stalk(context: AddonContext): ItemStack = ItemStack(Material.BAMBOO).apply {
            editMeta { meta ->
                meta.displayName(mm("<!i><gold>A Hollow Stalk"))
                meta.lore(
                    listOf(
                        mm("<!i><dark_gray><i>There is a coal inside it."),
                        mm("<!i><dark_gray><i>It is the single most valuable object in the world."),
                        mm(""),
                        mm("<!i><gray>Right-click a mortal to give it to them."),
                    ),
                )
                meta.persistentDataContainer.set(key(context), PersistentDataType.BYTE, 1)
            }
        }

        fun isStalk(item: ItemStack?, context: AddonContext): Boolean =
            item?.itemMeta?.persistentDataContainer?.has(key(context), PersistentDataType.BYTE) == true
    }
}

/** Hephaestus builds a woman out of clay, and every god puts something in her. */
class MakePandoraPower(private val mythos: Mythos) : Power {
    override val id = "make_pandora"
    override val displayName = "Make the Gift"
    override val description = "Build a woman out of clay. Zeus has an idea about a jar. /power make_pandora"
    override val cooldownSeconds = 0

    override fun use(ctx: PowerContext): Boolean {
        val smith = ctx.player
        if (!mythos.eras.isComplete(ERA, "fire_stolen")) {
            smith.sendMessage(mm("<red>Zeus isn't angry yet. <gray>Wait until somebody steals something."))
            return false
        }
        if (!mythos.roles.isSealed("pandora")) {
            smith.sendMessage(mm("<red>She's already been made. <gray>Look what you did."))
            return false
        }

        // Unsealing puts her in front of the spirit queue like any other vacancy — so
        // whoever has been waiting longest gets to be the woman everybody blames.
        mythos.roles.open("pandora", "made from clay, and given to the world")
        mythos.narrator.tell(
            listOf(
                Beat(20, text = "<dark_gray>» <gray>Hephaestus works the clay, and every god puts something in her."),
                Beat(50, text = "<gray>Athena gives her craft. Aphrodite gives her grace. Hermes gives her a voice —"),
                Beat(50, text = "<dark_gray><i>which was Zeus's idea, and was not a kindness."),
                Beat(55, text = "<gray>And a jar. Sealed. With strict instructions not to open it.", sound = "minecraft:block.decorated_pot_place"),
                Beat(50, text = "<dark_gray><i>Think about what kind of person gives you a jar and says that."),
            ),
        )
        mythos.chronicle.record("story", "<light_purple>Pandora <gray>was made out of clay, and given a jar, and told not to open it.")
        mythos.eras.complete(ERA, "pandora_made", "a gift was made, and it was beautiful, and it was a trap")
        return true
    }
}

/** Epimetheus does the one thing his brother told him not to. */
class AcceptPower(private val mythos: Mythos) : Power {
    override val id = "accept"
    override val displayName = "Accept the Gift"
    override val description = "Your brother said not to. You are going to anyway. /power accept"
    override val cooldownSeconds = 0

    override fun use(ctx: PowerContext): Boolean {
        val epimetheus = ctx.player
        if (mythos.roles.holders("pandora").isEmpty()) {
            epimetheus.sendMessage(mm("<red>Nobody has been given to you. <gray>Yet."))
            return false
        }
        epimetheus.sendMessage(mm("<gold>You accept her, of course you do."))
        epimetheus.sendMessage(mm("<dark_gray><i>Your brother is going to hear about this and put his head in his hands."))
        mythos.chronicle.record("story", "<gray>Epimetheus accepted the gift. He had been told, in plain words, not to.")
        return true
    }
}

/** The jar. Not a box — that was a translation error, and it has been ruining her name for centuries. */
class OpenJarPower(private val mythos: Mythos, private val state: FireState) : Power {
    override val id = "open_jar"
    override val displayName = "Open the Jar"
    override val description = "You were told not to. That's not a warning; it's an instruction. /power open_jar"
    override val cooldownSeconds = 0

    override fun use(ctx: PowerContext): Boolean {
        val pandora = ctx.player
        if (state.jarOpened) {
            pandora.sendMessage(mm("<red>It's open. It has been open for a while now."))
            return false
        }
        state.jarOpened = true
        state.save()

        // The world gets worse. Permanently, and for everyone.
        //
        // BUG, fixed: this used to hit EVERY world, including the Void and the Stomach of
        // Kronos — which are explicitly `still` (peaceful, no mobs, no weather) and had their
        // gamerules stomped by a jar opening somewhere else entirely. Evil got into the world.
        // It did not get into the places that aren't one.
        mythos.realms.world("gaia")?.difficulty = Difficulty.HARD

        mythos.narrator.tell(
            listOf(
                Beat(20, title = "<dark_red>The Jar", subtitle = "<gray>it was never a box", sound = "minecraft:entity.wither.spawn"),
                Beat(60, text = "<gray>Everything comes out at once: sickness, and old age, and toil, and the knowledge that you will die."),
                Beat(55, text = "<dark_gray><i>They scatter. They are never going back in."),
                Beat(60, text = "<gray>She gets the lid back on with one thing still inside.", sound = "minecraft:block.decorated_pot_break"),
                Beat(60, text = "<white>Hope.", sound = "minecraft:block.amethyst_block.chime"),
                Beat(55, text = "<dark_gray><i>Nobody has ever agreed on whether that was a mercy or the last cruelty in the jar."),
            ),
        )
        mythos.chronicle.record(
            "story",
            "<light_purple>Pandora <gray>opened the jar. Everything came out except hope, and nobody agrees whether that was kind.",
        )
        mythos.eras.complete(ERA, "the_jar", "everything got out")
        return true
    }
}

/** Zeus nails the thief to a rock. */
class ChainPower(
    private val mythos: Mythos,
    private val context: AddonContext,
    private val state: FireState,
) : Power {
    override val id = "chain"
    override val displayName = "Nail Him to the Rock"
    override val description = "At the edge of the world, where the eagle can find him. /power chain <player>"
    override val cooldownSeconds = 0

    override fun use(ctx: PowerContext): Boolean {
        val zeus = ctx.player
        val target = ctx.args.firstOrNull()?.let { Bukkit.getPlayerExact(it) }
            ?: return false.also { zeus.sendMessage(mm("<red>/power chain <player>")) }

        if (mythos.roles.roleOf(target.uniqueId)?.id != "prometheus") {
            zeus.sendMessage(mm("<red>He's the one who did it. <gray>Punishing anyone else would be beneath even you."))
            return false
        }
        if (!mythos.eras.isComplete(ERA, "fire_stolen")) {
            zeus.sendMessage(mm("<red>He hasn't done anything yet. <dark_gray><i>Yet."))
            return false
        }

        val rock = zeus.location.clone()
        state.rock = rock
        state.save()
        mythos.profiles.profile(target.uniqueId).setFlag(CHAINED, true)

        target.teleportAsync(rock).thenRun {
            context.schedulers.entity(target) {
                target.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, 4, false, false))
                target.sendMessage(mm("<dark_red>Nailed to a rock at the edge of the world."))
                target.sendMessage(mm("<gray>The eagle comes at dawn. It will keep coming."))
                target.sendMessage(mm("<dark_gray><i>You could end this in one sentence. You know which one. /power endure"))
            }
        }
        mythos.narrator.tell(
            listOf(
                Beat(20, text = "<dark_gray>» <yellow>Zeus <gray>nails <gold>Prometheus <gray>to a rock at the edge of the world."),
                Beat(55, text = "<dark_gray><i>The eagle comes at dawn. It eats his liver. The liver grows back."),
                Beat(55, text = "<dark_gray><i>That's the whole punishment. It is supposed to last forever."),
            ),
        )
        mythos.chronicle.record("story", "<yellow>Zeus <gray>chained <gold>Prometheus <gray>to a rock and sent an eagle.")
        mythos.eras.complete(ERA, "prometheus_chained", "the thief was nailed to a rock")
        return true
    }
}

/**
 * **He knows a secret that would end this.**
 *
 * Which goddess will bear a son greater than his father. Zeus would trade the eagle, the
 * rock and an apology for it. Prometheus can say it and walk free, right now, any time.
 *
 * The power exists purely so that not using it is a *choice a player is making*, every
 * day, on purpose. That's the myth. If there were no button, there'd be no defiance.
 */
class EndurePower(private val mythos: Mythos, private val state: FireState) : Power {
    override val id = "endure"
    override val displayName = "Say Nothing"
    override val description = "You know the secret that would free you. /power endure — to speak it, /power endure speak"
    override val cooldownSeconds = 0

    override fun use(ctx: PowerContext): Boolean {
        val prometheus = ctx.player
        if (ctx.args.firstOrNull()?.equals("speak", true) != true) {
            prometheus.sendMessage(mm("<gray>You say nothing. <dark_gray><i>The eagle comes back tomorrow."))
            prometheus.sendMessage(mm("<dark_gray><i>Livers eaten: <white>${state.livers}<dark_gray>. You could stop this whenever you like."))
            return false
        }

        // He talks. It ends immediately, and it costs him everything the story was about.
        state.chained = false
        state.save()
        mythos.profiles.profile(prometheus.uniqueId).setFlag(CHAINED, null)

        mythos.narrator.tell(
            listOf(
                Beat(20, text = "<dark_gray>» <gold>Prometheus <gray>talks."),
                Beat(50, text = "<gray>He names the woman. Zeus goes very pale, and marries her off to a mortal within the hour."),
                Beat(55, text = "<dark_gray><i>The chains come off. The eagle is called back. He is free, and it is the smaller ending."),
            ),
        )
        mythos.chronicle.record("story", "<gold>Prometheus <gray>gave up the secret, and was freed. He did not have to.")
        mythos.eras.complete(ERA, "prometheus_freed", "he talked")
        return true
    }
}
