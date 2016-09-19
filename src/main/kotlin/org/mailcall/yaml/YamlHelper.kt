package org.mailcall.yaml

import org.kale.mail.EmailAccountConfig
import org.mailcall.script.SimpleConfiguration
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream

/**
 * @author Odysseus Levy (odysseus.levy@am.sony.com)
 */
class YamlHelper {

    fun loadAccounts(): List<EmailAccountConfig> {

        val yaml = Yaml()
        val folder = File("config")
        val listOfFiles = folder.listFiles{file -> file.extension.equals("imap")}

        return listOfFiles.map { file ->
           yaml.loadAs(FileInputStream(file), EmailAccountConfig::class.java)
        }
    }


}

fun main(args: Array<String>) {

    val helper = YamlHelper()

    // Need placeholder for Tags.
    // Utility for creating an EmailAccount would be good
    // Need notion of "named" objects (using duck typing?

    //val accounts = helper.loadAccounts().map { account -> StoreWrapper(account) }.map { store -> EmailAccount(store, null)}

    val config = SimpleConfiguration()
    //config.putAll(accounts)

}

