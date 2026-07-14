package net.crewco.mythos.fire

import net.crewco.mythos.addon.AddonContext
import net.crewco.mythos.api.Mythos
import net.crewco.mythos.api.event.EraAdvancedEvent
import net.crewco.mythos.api.event.MythosResetEvent
import net.crewco.mythos.api.role.RoleTier
import net.crewco.mythos.api.story.Beat
import net.crewco.mythos.command.CommandContext.Companion.mm
import net.crewco.mythos.fire.FireContent.CHAINED
import net.crewco.mythos.fire.FireContent.ERA
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import java.util.concurrent.CopyOnWriteArrayList

class FireListener(
    private val mythos: Mythos,
    private val context: AddonContext,
    private val state: FireState,
) : Listener {

    /** Contributed by other addons. Anyone matching one of these can take him off the rock. */
    val liberations = CopyOnWriteArrayList<Liberation>()

    // ---- Zeus takes the fire away --------------------------------------------

    /**
     * The whole punishment of mortals, in nine lines: they cannot light anything.
     *
     * Note it only bites MORTALS. The gods are unaffected, which is precisely the
     * injustice Prometheus is about to do something about.
     */
    @EventHandler(ignoreCancelled = true)
    fun onIgnite(event: BlockIgniteEvent) {
        if (!state.fireWithheld) return
        val player = event.player ?: return
        if (mythos.roles.roleOf(player.uniqueId)?.tier != RoleTier.MORTAL) return

        event.isCancelled = true
        player.sendMessage(mm("<gray>The spark dies before it catches. <dark_gray><i>It has been taken from you."))
    }

    // ---- the theft ------------------------------------------------------------

    /** Prometheus hands the stalk to a mortal. This is the entire point of the era. */
    @EventHandler
    fun onGive(event: PlayerInteractEntityEvent) {
        val giver = event.player
        val receiver = event.rightClicked as? Player ?: return

        // Somebody taking Prometheus off the rock — checked first, because a liberator
        // may well be holding something in their hand.
        if (state.chained && mythos.roles.roleOf(receiver.uniqueId)?.id == "prometheus") {
            val liberation = liberations.firstOrNull { it.requires(giver) }
            if (liberation != null) {
                event.isCancelled = true
                free(receiver, liberation)
                return
            }
        }

        if (!StealFirePower.isStalk(giver.inventory.itemInMainHand, context)) return
        if (mythos.roles.roleOf(receiver.uniqueId)?.tier != RoleTier.MORTAL) {
            giver.sendMessage(mm("<red>They already have fire. <gray>It's the ones with nothing you're doing this for."))
            return
        }
        event.isCancelled = true

        val stalk = giver.inventory.itemInMainHand.clone()
        stalk.amount -= 1
        giver.inventory.setItemInMainHand(if (stalk.amount <= 0) null else stalk)

        state.fireWithheld = false
        state.save()

        context.schedulers.entity(receiver) {
            receiver.inventory.addItem(ItemStack(Material.TORCH, 4), ItemStack(Material.FLINT_AND_STEEL))
            receiver.sendMessage(mm("<gold>A Titan just put fire into your hands."))
            receiver.sendMessage(mm("<dark_gray><i>He knew what it would cost him. He did it anyway."))
        }

        mythos.narrator.tell(
            listOf(
                Beat(20, text = "<dark_gray>» <gold>Prometheus <gray>gives fire to <white>${receiver.name}<gray>.", sound = "minecraft:item.firecharge.use"),
                Beat(55, text = "<gray>Every hearth in the world lights at once, and nobody can put them out again."),
                Beat(55, text = "<dark_gray><i>On Olympus, somebody notices. He does not say anything. He just watches it happen."),
            ),
        )
        mythos.chronicle.record("story", "<gold>Prometheus <gray>gave fire to the mortals, knowing exactly what it would cost him.")
        mythos.eras.complete(ERA, "fire_stolen", "fire came back down in a hollow stalk")
    }

    private fun free(prometheus: Player, liberation: Liberation) {
        state.chained = false
        state.save()
        mythos.profiles.profile(prometheus.uniqueId).setFlag(CHAINED, null)

        context.schedulers.entity(prometheus) {
            prometheus.removePotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS)
            prometheus.sendMessage(mm("<gold>Somebody came."))
        }
        mythos.narrator.tell(
            listOf(
                Beat(20, title = "<gold>The Eagle Falls", subtitle = "<gray>after ${state.livers} of them", sound = "minecraft:entity.parrot.death"),
                Beat(60, text = "<gray>Somebody walks past the rock at the edge of the world — <dark_gray><i>${liberation.freedBy}</i><gray> —"),
                Beat(55, text = "<gray>and asks what he did, and does not like the answer, and shoots the eagle."),
                Beat(60, text = "<dark_gray><i>He was there for thirty generations. He never said a word."),
            ),
        )
        mythos.chronicle.record(
            "story",
            "<gold>Prometheus <gray>was freed after <white>${state.livers}</white> livers — by ${liberation.freedBy}.",
        )
        mythos.eras.complete(ERA, "prometheus_freed", "somebody finally came")
    }

    // ---- the eagle ------------------------------------------------------------

    /**
     * Dawn. Every three minutes of real time, because a myth that punishes you once a day
     * is a myth nobody sees.
     *
     * It cannot kill him: the engine refuses lethal damage to a Titan from nothing at all
     * (see RoleTier.killableBy), so the eagle takes him to the edge and leaves him there,
     * which is the entire horror of it and required no code from this addon.
     */
    fun startEagle() {
        context.schedulers.globalRepeating(20 * 60, 20 * 60 * 3) {
            if (!state.chained) return@globalRepeating
            val prometheus = mythos.roles.holders("prometheus").mapNotNull { Bukkit.getPlayer(it) }.firstOrNull()
                ?: return@globalRepeating

            val eaten = state.ateOne()
            state.save()

            context.schedulers.entity(prometheus) {
                prometheus.world.playSound(prometheus.location, org.bukkit.Sound.ENTITY_PHANTOM_BITE, 1f, 0.6f)
                prometheus.damage(12.0)
                prometheus.sendMessage(mm("<dark_red>The eagle comes. <gray>It takes its time. <dark_gray>(liver #$eaten)"))
                if (eaten % 5 == 0) {
                    prometheus.sendMessage(mm("<dark_gray><i>One sentence and this stops. You know the one. <white>/power endure speak"))
                }
            }
        }
    }

    /** Chained means chained: he doesn't get to walk away from the rock. */
    fun leash() {
        context.schedulers.globalRepeating(100, 40) {
            if (!state.chained) return@globalRepeating
            val rock = state.rock ?: return@globalRepeating
            mythos.roles.holders("prometheus").mapNotNull { Bukkit.getPlayer(it) }.forEach { prometheus ->
                context.schedulers.entity(prometheus) {
                    val here = prometheus.location
                    if (here.world != rock.world || here.distanceSquared(rock) > 25) {
                        prometheus.teleportAsync(rock)
                        prometheus.sendMessage(mm("<dark_gray><i>The chains do not give."))
                    }
                }
            }
        }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        if (mythos.profiles.profile(event.player.uniqueId).hasFlag(CHAINED)) {
            context.schedulers.entityDelayed(event.player, 20, retired = null) {
                event.player.sendMessage(mm("<dark_red>You are still on the rock. <gray>Of course you are."))
            }
        }
    }

    @EventHandler
    fun onEra(event: EraAdvancedEvent) {
        if (event.to.id != ERA) return
        context.schedulers.globalDelayed(80) {
            mythos.roles.holders("prometheus").mapNotNull { Bukkit.getPlayer(it) }.forEach { p ->
                context.schedulers.entity(p) {
                    p.sendMessage(mm("<gold>You can see exactly how this ends for you. <white>/power mecone"))
                }
            }
        }
    }

    @EventHandler
    fun onReset(event: MythosResetEvent) {
        if (event.scope == MythosResetEvent.Scope.PLAYER) return
        state.clear()
        context.logger.info("The fire is back on Olympus, the jar is sealed, and nobody is on the rock.")
    }
}
