package gl.ky.ichigo

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor

class Config() {
    companion object {
        lateinit var inst: Config
        fun load() {
            val f = pluginInst.dataFolder.resolve("config.yml")
            if(!f.exists()) pluginInst.saveResource("config.yml", true)

            f.inputStream().use {
                inst = Yaml(CustomClassLoaderConstructor(pluginInst.javaClass.classLoader)).loadAs(it, Config::class.java)
            }
        }
    }
    lateinit var messages: Map<String, String>
}