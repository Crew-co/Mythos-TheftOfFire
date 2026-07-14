package net.crewco.mythos.fire

import net.crewco.mythos.api.Mythos
import net.crewco.mythos.command.CommandContext.Companion.mm
import net.crewco.mythos.menu.Menu
import net.crewco.mythos.menu.MenuItem
import net.crewco.mythos.api.story.Beat
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * **The trick at Mecone**, and the best argument in this project for the host owning a
 * menu framework.
 *
 * Prometheus divides the sacrifice into two piles and lets Zeus choose. One is a heap of
 * bones wrapped in glistening fat. The other is the actual meat, hidden inside a stomach.
 * Zeus, who is not stupid but *is* vain, takes the shiny one — and from then on mortals
 * burn the bones for the gods and eat the meat themselves.
 *
 * A myth that turns entirely on a player making a choice from two options wants to *be* a
 * two-slot GUI. The whole scene is thirty lines because the host already did the hard part.
 */
class MeconeMenu(
    private val mythos: Mythos,
    private val prometheus: Player,
) : Menu(mm("<dark_gray>Choose, and be bound by it"), rows = 3) {

    override fun build(viewer: Player) {
        item(
            11,
            MenuItem(
                icon(
                    Material.BONE_BLOCK,
                    "<white>The Glistening Portion",
                    listOf(
                        "<gray>Wrapped in rich white fat.",
                        "<gray>It shines. It is beautiful.",
                        "",
                        "<dark_gray><i>You are a god. You choose with your eyes.",
                    ),
                ),
            ) { click -> choose(click.player, fat = true); click.close() },
        )
        item(
            15,
            MenuItem(
                icon(
                    Material.BEEF,
                    "<gray>The Other One",
                    listOf(
                        "<dark_gray>An ox's stomach. Grey. Wet.",
                        "<dark_gray>Frankly unappetising.",
                        "",
                        "<dark_gray><i>Nobody would pick this.",
                    ),
                ),
            ) { click -> choose(click.player, fat = false); click.close() },
        )
        item(13, icon(Material.CAMPFIRE, "<gold>The First Sacrifice", listOf("<gray>Two portions. Pick one, forever.")))
    }

    private fun choose(zeus: Player, fat: Boolean) {
        val beats = if (fat) {
            listOf(
                Beat(20, text = "<dark_gray>» <yellow>Zeus <gray>takes the shining one, because he is a god and it shines."),
                Beat(50, text = "<gray>Under the fat: bones. Nothing but bones."),
                Beat(50, text = "<dark_gray><i>He knew. Some say he knew and took it anyway, so he'd have a reason."),
                Beat(50, text = "<gray>From now on, mortals burn the bones for the gods — and eat the meat themselves.", sound = "minecraft:entity.player.burp"),
            )
        } else {
            listOf(
                Beat(20, text = "<dark_gray>» <yellow>Zeus <gray>looks a long time, and takes the ugly one."),
                Beat(50, text = "<gray>He gets the meat. The mortals get bones and fat, and they will remember."),
                Beat(50, text = "<dark_gray><i>Prometheus says nothing. He is very good at saying nothing."),
            )
        }
        mythos.narrator.tell(beats)
        mythos.profiles.profile(zeus.uniqueId).setFlag("fire.chose-fat", fat)
        mythos.chronicle.record(
            "story",
            if (fat) "<gray>At Mecone, Zeus chose the shining portion, and got bones. Mortals have eaten the meat ever since."
            else "<gray>At Mecone, Zeus was not fooled. The mortals got the bones, and did not forget.",
        )
        mythos.eras.complete(FireContent.ERA, "the_trick", "the sacrifice was divided, and a choice was made")

        prometheus.sendMessage(
            if (fat) mm("<gold>He took it. <dark_gray><i>He is going to work out why in about an hour, and he is going to be furious.")
            else mm("<gold>He didn't take it. <dark_gray><i>You will have to steal the fire the hard way now."),
        )
    }

    private fun icon(material: Material, name: String, lore: List<String>) = ItemStack(material).apply {
        editMeta { meta ->
            meta.displayName(mm("<!i>$name"))
            meta.lore(lore.map { mm("<!i>$it") })
        }
    }
}
