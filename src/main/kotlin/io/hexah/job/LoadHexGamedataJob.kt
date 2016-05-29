package io.hexah.job

import com.google.gson.JsonParser
import io.hexah.dao.HexObjectDao
import io.hexah.model.HexObjectRarity
import io.hexah.model.HexObjectType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.File
import java.util.zip.GZIPInputStream
import javax.annotation.PostConstruct

@Component
@DependsOn("flywayInitializer")
open class LoadHexGamedataJob @Autowired constructor(
        val hexObjectDao: HexObjectDao,
        @Value("\${jobs.run}") val runJobs: Boolean
) {

    val FILENAME = "data/gamedata.gz"

    val log = LoggerFactory.getLogger(javaClass)
    val parser = JsonParser()

    @PostConstruct
    fun init() {
        val file = File(FILENAME)
        if (!file.exists()) {
            throw RuntimeException("Could not find $FILENAME")
        }
        val existing = hexObjectDao.findAll().map { it.guid }.toSet()
        val stats = load(file, existing)
        log.info("Loaded Hex Game Data: $stats")
    }

    private fun load(file: File, existing: Set<String>): Stats {
        val reader = GZIPInputStream(file.inputStream()).bufferedReader()
        val stats = Stats()
        try {
            read(reader, fun(content: String) {
                stats.objects++
                if (content.contains("Reckoning.Game.CardTemplate")) {
                    addCard(stats, content, existing)
                }
            })
        } finally {
            reader.close()
        }
        return stats
    }

    private fun addCard(stats: Stats, content: String, existing: Set<String>) {
        try {
            stats.cards++
            val json = parser.parse(cleanup(content)).asJsonObject;
            val guid = json.getAsJsonObject("m_Id").get("m_Guid").asString
            val setGuid = json.getAsJsonObject("m_SetId").get("m_Guid").asString
            if (existing.contains(guid)) {
                stats.cardsExisting++
                return
            }
            val name = json.get("m_Name").asString
            val rarity = HexObjectRarity.valueOf(json.get("m_CardRarity").asString)
            val alternateArt = json.get("m_HasAlternateArt").asInt == 1
            hexObjectDao.add(guid, setGuid, name, HexObjectType.Card, rarity, alternateArt)
            stats.cardsAdded++
        } catch (e: Throwable) {
            log.error("Error adding card [$content]", e)
            throw e
        }
    }

    private fun cleanup(content: String): String {
        return content.replace(""""\,\s+\}""".toRegex(), "\"}")
    }

    fun read(reader: BufferedReader, parser: (String) -> Unit) {
        var append = false
        var buffer = StringBuffer()
        reader.forEachLine { line ->
            when (line) {
                "$$$---$$$" -> {
                    append = false
                    parser(cleanup(buffer.toString()))
                    buffer = StringBuffer()
                }
                "$$--$$" -> {
                    append = true
                    parser(cleanup(buffer.toString()))
                    buffer = StringBuffer()
                }
                else -> if (append) {
                    buffer.append(line)
                }
            }
        }
        parser(cleanup(buffer.toString()))
    }

    private data class Stats(
            var objects: Int = 0,
            var cards: Int = 0,
            var cardsExisting: Int = 0,
            var cardsAdded: Int = 0
    )

}
