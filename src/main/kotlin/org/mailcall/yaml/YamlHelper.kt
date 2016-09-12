package org.mailcall.yaml

import org.kale.mail.EmailAccountConfig
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream

/**
 * @author Odysseus Levy (odysseus.levy@am.sony.com)
 */
class YamlHelper {

    fun loadAccounts() = {

        val yaml = Yaml()
        val folder = File("config")
        val listOfFiles = folder.listFiles{file -> file.extension.equals("imap")}

        listOfFiles.map { file ->
           yaml.loadAs(FileInputStream(file), EmailAccountConfig::class.java)
        }
    }


}

fun main(args: Array<String>) {

    val helper = YamlHelper()

    val accounts = helper.loadAccounts()
}

