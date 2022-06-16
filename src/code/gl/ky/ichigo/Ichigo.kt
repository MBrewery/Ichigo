package gl.ky.ichigo

import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.plugin.java.JavaPlugin

lateinit var pluginInst: Ichigo

class Ichigo : JavaPlugin() {

    override fun onEnable() {
        logger.info("Heyhey~ ichigo coming.")
        pluginInst = this
        Metrics(this, 15468)
        reload()
        server.pluginManager.registerEvents(CommandListener, this)
        getCommand("ichigo_reload")!!.setExecutor(this)
    }

    override fun onDisable() {
    }

    private fun reload() {
        if(!dataFolder.exists()) dataFolder.mkdirs()
        Config.load()
        Parser.load()
        Macro.load()
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if(sender.isOp) {
            reload()
            sender.sendMessage(msg("reloaded"))
        }
        return true
    }
}

fun msg(s: String): String = Config.inst.messages["prefix"] + Config.inst.messages[s]

object CommandListener : Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun onCommand(e: PlayerCommandPreprocessEvent) {
        val r = Macro.transform(e.message.substringAfter("/"), e.player, true) ?: return
        e.message = "/$r"
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onChat(e: AsyncPlayerChatEvent) {
        val r = Macro.transform(e.message, e.player, false) ?: return
        e.isCancelled = true
        Bukkit.getScheduler().callSyncMethod(pluginInst) {
            Bukkit.dispatchCommand(e.player, r)
        }.get()
    }
}
