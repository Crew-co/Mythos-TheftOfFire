package net.crewco.mythos.fire

/**
 * **The hole this addon cannot fill itself.**
 *
 * Prometheus's punishment ends when Heracles walks past, shoots the eagle, and asks what
 * he did. Heracles is four chapters away and lives in a jar nobody has written. So this
 * addon does not resolve its own final beat — it opens a point and waits.
 *
 * Possibly for years. Which is, structurally, exactly what the myth does.
 *
 * ```kotlin
 * // In LaboursOfHeracles, whenever that gets written:
 * mythos.extensions.contribute(
 *     Liberation.POINT,
 *     Liberation(
 *         id = "heracles",
 *         freedBy = "a man in a lion's skin, on his way to somewhere else",
 *         requires = { player -> mythos.roles.roleOf(player.uniqueId)?.id == "heracles" },
 *     ),
 * )
 * ```
 *
 * Anyone holding a registered Liberation can `/power unchain` at the rock. Until then the
 * eagle comes every dawn, and `/era` shows one objective outstanding, forever, and that
 * is the correct behaviour.
 */
data class Liberation(
    val id: String,
    /** Written into the Chronicle when it happens. */
    val freedBy: String,
    /** Who is allowed to do it. */
    val requires: (org.bukkit.entity.Player) -> Boolean,
) {
    companion object {
        const val POINT = "prometheus:liberation"
    }
}
