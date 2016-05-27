package org.kale.mail

import org.apache.logging.log4j.LogManager
import java.io.InputStream
import javax.mail.Multipart
import javax.mail.Part


data class Attachment(val part: Part) {

    fun getFileName() = part.getFileName()
    fun getInputStream() = part.getInputStream()
}

/**
 * Code involving working with JavaMail's Mime structure
 */
object MimePart {

    val logger = LogManager.getLogger(this)

    fun makeSafe(s: String?): String {
        return if (s == null) "" else s
    }

    // We want just the content type string, remove everything after the ";" (typically the mime border info)
    fun getContentType(part: Part) = makeSafe(part.getContentType()).split(";").getOrNull(0)

    fun dumpStructure(part: Part, prefix: String = ""): Unit {


        if (part.isMimeType("multipart/*") && part is Multipart){
            logger.info("$prefix${getContentType(part)}")

            for(i in  0 .. part.count ) {
                val mp = part.getBodyPart(i)
                dumpStructure(mp, prefix + "\t")
            }
        }else if (part.isMimeType("text/*")) {
            logger.info("$prefix${getContentType(part)}")
        } else {
            logger.info("$prefix${getContentType(part)} file: ${part.getFileName()} disposition: ${part.disposition}")
        }
    }

    fun isAttachment(part: Part): Boolean =  Part.ATTACHMENT.equals(part.disposition, true)

    //TODO

    fun getAttachments(part: Part): List<Attachment> {

        return emptyList()

//        val content = part.content
//        when (content) {
//            is Multipart -> {
//                content.get
//            }
//            is InputStream -> {
//                if (isAttachment(part))
//                    listOf(Attachment(part))
//                else
//                    listOf()
//
//            }
//            else -> listOf<Attachment>()
//        }
//        part.getContent match {
//            case multi: Multipart => {
//
//                var list = List[Attachment]()
//                for(i <- 0 until multi.getCount) {
//                list = list ::: getAttachments(multi.getBodyPart(i))
//            }
//
//                list
//            }
//
//            case is: InputStream if isAttachment(part) => List(Attachment(part))
//            case _ => List()
//
//        }

    }

    fun Multipart.getParts(): List<Part> {
        val list = mutableListOf<Part>()

        for(i in 0 .. this.count -1) {
            list.add(this.getBodyPart(i))
        }

        return list
    }

    fun getMultiPartText(multi: Multipart): String{

        for(i in 0 .. multi.count) {
            val part = multi.getBodyPart(i)

            if (part.isMimeType("text/*"))
                return part.content.toString()

            val result = getBodyText(part)
            if (result != null)
                return result
        }

        return ""
    }

    fun getRelatedText(multi: Multipart): String {

        var text = ""

        multi.getParts().forEach { part -> text += getBodyText(part) }
        return text
    }

    fun isMultiTypeOf(part: Part, type: String): Boolean = (part is Multipart && part.isMimeType(type))

    /**
     * Simplified algorithm for extracting a message's text. When presented with alternatives it will always
     * choose the 'preferred' format (by convention this is html)
     *
     * Note that with complicated formats (such as lots of attachments with text interspersed between them) it will
     * simply return the first text block it finds
     *
     * @return null if no text is found
     */
    fun getBodyText(part: Part): String {

        val content = part.content
        return when (content) {
            is String -> content
            is Multipart -> {
                if (part.isMimeType("multipart/alternative"))
                    getBodyText(content.getBodyPart(content.count - 1))
                else if (part.isMimeType("multipart/related"))
                    getRelatedText(content)
                else
                    getMultiPartText(content)
            }

            is InputStream -> if (isAttachment(part)) "Attachment[${part.fileName}]" else "InputStream"
            else -> ""
        }
    }

}
