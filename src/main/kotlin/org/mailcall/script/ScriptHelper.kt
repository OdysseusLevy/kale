package org.mailcall.script

import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.FileReader
import javax.script.ScriptEngineManager

/**
 * @author Odysseus Levy (odysseus@cosmosgame.org)
 */
open class ScriptHelper {

    companion object {
        val engineManager = ScriptEngineManager()

        val logger = LogManager.getLogger(ScriptHelper::class.java.name)
    }

    fun getEngineName(f: File): String {

        return when (f.extension) {
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

        configuration.addObjectsToScope(scope)
        val reader = FileReader(script)
        return engine.eval(reader, scope)
    }
}