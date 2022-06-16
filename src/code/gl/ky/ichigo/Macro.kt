package gl.ky.ichigo

import org.bukkit.command.CommandSender
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor

class Macro {
    var noslash: Boolean = true
    lateinit var permission: String
    lateinit var grammar: String
    lateinit var product: String

    companion object {
        val macros = mutableListOf<Macro>()

        fun load() {
            macros.clear()
            class MacroYaml {
                lateinit var commands: Map<String, Macro>
            }

            val f = pluginInst.dataFolder.resolve("macros.yml")
            if(!f.exists()) pluginInst.saveResource("macros.yml", true)

            f.inputStream().use {
                macros += Yaml(CustomClassLoaderConstructor(pluginInst.javaClass.classLoader))
                    .loadAs(it, MacroYaml::class.java).commands.map { it.value }
            }
        }

        fun transform(text: String, sender: CommandSender, slash: Boolean): String? {
            for(macro in macros) {
                if(slash == macro.noslash) continue
                if(!sender.isOp) if(macro.permission != "" && !sender.hasPermission(macro.permission)) {
                    if(slash) sender.sendMessage(msg("noperm"))
                    continue
                }
                val v = Parser.match(PatternPart.parse(macro.grammar), text) ?: continue
                return Parser.applyVar(macro.product, v)
            }
            return null
        }
    }
}

class PatternPart(val type: Type, val text: String, val key: String = "") {
    enum class Type {
        TEXT,
        VARIABLE,
    }

    companion object {
        fun parse(text: String): List<PatternPart> {
            return text.split(" ").map {
                if(it.startsWith("$")) {
                    val i = it.indexOf(':')
                    PatternPart(Type.VARIABLE, it.substring(1, i), it.substring(i + 1))
                } else {
                    PatternPart(Type.TEXT, it)
                }
            }
        }
    }
}
