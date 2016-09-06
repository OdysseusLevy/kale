package org.mailcall.script

import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.FileReader
import javax.script.ScriptEngineManager

/**
 * @author Odysseus Levy (odysseus@cosmosgame.org)
 */
class ScriptHelper {

    companion object {
        val engineManager = ScriptEngineManager()

        val logger = LogManager.getLogger(ScriptHelper::class.java.name)
        fun getFileExtension(fullName: String): String {
            checkNotNull(fullName)
            val fileName = File(fullName).name
            val dotIndex = fileName.lastIndexOf('.')
            return if (dotIndex == -1) "" else fileName.substring(dotIndex + 1)
        }
    }

    fun getEngineName(f: File): String {

        return when (getFileExtension(f.getName())) {
            "ruby" -> "ruby"
            "rb" ->  "ruby"
            "groovy" -> "groovy"
            else -> "nashorn"
        }
    }

    fun runScript(script: File, configuration: Configuration): Any {
        val engineName = getEngineName(script)

        logger.info("Executing ${script.getName()} with engine ${engineName}")

        val engine = engineManager.getEngineByName(engineName)
        val scope = engine.createBindings()

        configuration.addObjects(scope)
        val reader = FileReader(script)
        return engine.eval(reader, scope)
    }
}