package gl.ky.ichigo

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor

object Parser {
    var caseSensitive = false
    var skip = Regex(" \t\n\r")
    var patterns = mutableMapOf<String, Regex>()

    fun load() {
        class ParserYaml {
            var caseSensitive: Boolean = false
            lateinit var skip: String
            lateinit var patterns: Map<String, String>
        }

        val f = pluginInst.dataFolder.resolve("parser.yml")
        if(!f.exists()) pluginInst.saveResource("parser.yml", true)

        f.inputStream().use {
            val yaml = Yaml(CustomClassLoaderConstructor(pluginInst.javaClass.classLoader))
                .loadAs(it, ParserYaml::class.java)
            caseSensitive = yaml.caseSensitive
            skip = yaml.skip.toRegex()
            patterns = yaml.patterns
                .mapValues{ p -> if(yaml.caseSensitive) p.value.toRegex() else p.value.toRegex(RegexOption.IGNORE_CASE) }
                .toMutableMap()
        }
    }

    /**
     * use the pattern to match the string
     * if matched, capture variables from the string
     * @returns map of name -> variables
     */
    fun match(pattern: List<PatternPart>, str: String): Map<String, String>? {
        val ret = mutableMapOf<String, String>()
        var offset = 0
        for(part in pattern) {
            skip.matchAt(str, offset)?.let {
                offset = it.range.last + 1
            }
            when(part.type) {
                PatternPart.Type.TEXT -> {
                    if (!str.startsWith(part.text, offset)) return null
                    offset += part.text.length
                }
                PatternPart.Type.VARIABLE -> {
                    val m = patterns[part.key]?.matchAt(str, offset) ?: return null
                    offset = m.range.last + 1
                    ret[part.text] = m.groups[0]!!.value
                }
            }
        }
        return ret
    }

    fun applyVar(template: String, vars: Map<String, String>): String {
        var ret = template
        for((key, value) in vars) {
            ret = ret.replace("\$$key", value)
        }
        return ret
    }

}
