package net.crewco.mythos.fire

import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

/** Fire, a jar, a rock, and a running total of eaten livers. */
class FireState(private val file: File) {

    private val yaml = if (file.exists()) YamlConfiguration.loadConfiguration(file) else YamlConfiguration()

    @Volatile var fireWithheld: Boolean = yaml.getBoolean("fire-withheld", false)
    @Volatile var jarOpened: Boolean = yaml.getBoolean("jar-opened", false)
    @Volatile var chained: Boolean = yaml.getBoolean("chained", false)
    @Volatile var rock: Location? = yaml.getLocation("rock")

    private val eaten = AtomicInteger(yaml.getInt("livers", 0))
    val livers: Int get() = eaten.get()
    fun ateOne(): Int = eaten.incrementAndGet()

    @Synchronized
    fun save() {
        yaml.set("fire-withheld", fireWithheld)
        yaml.set("jar-opened", jarOpened)
        yaml.set("chained", chained)
        yaml.set("rock", rock)
        yaml.set("livers", eaten.get())
        runCatching { yaml.save(file) }
    }

    fun clear() {
        fireWithheld = false
        jarOpened = false
        chained = false
        rock = null
        eaten.set(0)
        save()
    }
}
