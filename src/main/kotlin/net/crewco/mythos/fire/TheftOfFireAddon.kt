package net.crewco.mythos.fire

import net.crewco.mythos.addon.AddonBase
import net.crewco.mythos.api.Mythos
import net.crewco.mythos.api.ext.consume
import net.crewco.mythos.api.role.ClaimRules
import net.crewco.mythos.api.role.RoleDefinition
import net.crewco.mythos.api.role.RoleTier
import java.io.File

/**
 * Story #4 — and the first one that *cannot finish itself*.
 *
 * Prometheus's punishment ends when Heracles walks past, four chapters from now, in a jar
 * nobody has written. So this addon completes six of its seven objectives and then leaves
 * one open — possibly for years — with a hole in the side of it shaped exactly like the
 * thing that will eventually fill it (see [Liberation]).
 *
 * That isn't a limitation of the architecture. That's the architecture doing the thing the
 * myth does.
 */
class TheftOfFireAddon : AddonBase() {

    override fun onEnable() {
        val mythos = Mythos.from(context)
        val state = FireState(File(context.dataFolder, "fire.yml"))
        val listener = FireListener(mythos, context, state)

        mythos.eras.register(FireContent.ERA_OF_FIRE)
        mythos.roles.register(FireContent.PROMETHEUS)
        mythos.roles.register(FireContent.EPIMETHEUS)
        mythos.roles.register(FireContent.PANDORA) // sealed: she doesn't exist until she's made

        mythos.powers.register(MeconePower(mythos, context))
        mythos.powers.register(StealFirePower(mythos, context, state))
        mythos.powers.register(EndurePower(mythos, state))
        mythos.powers.register(WithholdFirePower(mythos, context, state))
        mythos.powers.register(ChainPower(mythos, context, state))
        mythos.powers.register(MakePandoraPower(mythos))
        mythos.powers.register(AcceptPower(mythos))
        mythos.powers.register(OpenJarPower(mythos, state))

        context.registerListener(listener)
        listener.startEagle()
        listener.leash()

        /*
         * The ending this addon can't write.
         *
         * Nothing here knows what a Heracles is. When some jar eventually posts a
         * Liberation, whoever it names can right-click the man on the rock and take him
         * off it — and because the engine replays contributions, that jar can be installed
         * today, next year, or never. Until then the eagle comes every dawn and `/era`
         * shows one objective outstanding, which is correct.
         */
        mythos.extensions.consume<Liberation>(Liberation.POINT) { liberation ->
            listener.liberations += liberation
            context.logger.info("A way off the rock exists: ${liberation.id} (${liberation.freedBy})")
        }

        // One tick on, every other addon has registered its cast — so we can hand Zeus and
        // Hephaestus the parts they play in OUR story, which theirs had no reason to give them.
        context.schedulers.globalDelayed(1) {
            fallbacks(mythos)
            mythos.roles.extend("zeus") { it.copy(powers = (it.powers + listOf("withhold_fire", "chain")).distinct()) }
            mythos.roles.extend("hephaestus") { it.copy(powers = (it.powers + "make_pandora").distinct()) }
        }

        context.logger.info("Prometheus can see what this will cost him. He is going to do it anyway.")
    }

    /** If nobody upstream made a Zeus or a Hephaestus, this chapter still has to be tellable. */
    private fun fallbacks(mythos: Mythos) {
        if (mythos.roles.definition("zeus") == null) {
            mythos.roles.register(
                RoleDefinition(
                    id = "zeus", displayName = "Zeus", tier = RoleTier.OLYMPIAN, era = FireContent.ERA,
                    domains = listOf("sky", "oath"), color = "<yellow>",
                    lore = listOf("You have everything, and somebody is about to take a very small piece of it."),
                    powers = listOf("withhold_fire", "chain"),
                    claimRules = listOf(ClaimRules.sinceEra(FireContent.ERA)),
                ),
            )
        }
        if (mythos.roles.definition("hephaestus") == null) {
            mythos.roles.register(
                RoleDefinition(
                    id = "hephaestus", displayName = "Hephaestus", tier = RoleTier.OLYMPIAN, era = FireContent.ERA,
                    domains = listOf("the forge"), color = "<yellow>",
                    lore = listOf("They will ask you to make something beautiful, for a bad reason. You will do it beautifully."),
                    powers = listOf("make_pandora"),
                    claimRules = listOf(ClaimRules.sinceEra(FireContent.ERA)),
                ),
            )
        }
    }
}
