package io.hexah.job

import com.google.gson.JsonParser
import io.hexah.dao.HexObjectDao
import io.hexah.model.HexObjectRarity
import io.hexah.model.HexObjectType
import io.hexah.util.getNameKey
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
        @Value("\${jobs.gamedata.run}") val runJob: Boolean
) {

    val FILENAME = "data/gamedata.gz"

    val log = LoggerFactory.getLogger(javaClass)
    val parser = JsonParser()

    @PostConstruct
    fun init() {
        if (!runJob) {
            return
        }
        log.info("Loading Hex Game Data from $FILENAME")
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
                } else if (content.contains("Reckoning.Game.InventoryEquipmentData")) {
                    addEquipment(stats, content, existing)
                } else if (content.contains("Reckoning.Game.InventoryCardPack")) {
                    addPack(stats, content, existing)
                } else if (content.contains("Reckoning.Game.InventoryMercenaryData")) {
                    addMercenary(stats, content, existing)
                }
            })
        } finally {
            reader.close()
        }
        return stats
    }

    private fun addCard(stats: Stats, content: String, existing: Set<String>) {
        addItem(
                stats = stats.cards,
                content = content,
                existing = existing,
                type = HexObjectType.Card,
                rarityKey = "m_CardRarity",
                alternateArtKey = "m_HasAlternateArt"
        )
    }

    private fun addEquipment(stats: Stats, content: String, existing: Set<String>) {
        addItem(
                stats = stats.equipment,
                content = content,
                existing = existing,
                type = HexObjectType.Equipment,
                setKey = "m_EquipmentSet",
                rarityKey = "m_Rarity"
        )
    }

    private fun addPack(stats: Stats, content: String, existing: Set<String>) {
        addItem(
                stats = stats.packs,
                content = content,
                existing = existing,
                type = HexObjectType.Pack
        )
    }

    private fun addMercenary(stats: Stats, content: String, existing: Set<String>) {
        addItem(
                stats = stats.mercenaries,
                content = content,
                existing = existing,
                type = HexObjectType.Mercenary,
                setKey = "m_MercenarySet",
                rarityKey = "m_Rarity"
        )
    }

    private fun addItem(stats: ItemStats, content: String, existing: Set<String>, type: HexObjectType,
                        setKey: String = "m_SetId", rarityKey: String? = null, alternateArtKey: String? = null) {
        try {
            stats.found++
            val json = parser.parse(cleanup(content)).asJsonObject;
            val guid = json.getAsJsonObject("m_Id").get("m_Guid").asString
            if (existing.contains(guid)) {
                stats.existing++
                return
            }
            val setGuid = json.getAsJsonObject(setKey).get("m_Guid").asString
            val name = json.get("m_Name").asString.trim()
            val rarity = if (rarityKey == null) HexObjectRarity.Unknown else HexObjectRarity.valueOf(json.get(rarityKey).asString)
            val nameKey = getNameKey(name, rarity)
            val alternateArt = if (alternateArtKey == null) false else json.get(alternateArtKey).asInt == 1
            hexObjectDao.add(guid, setGuid, type, name, rarity, nameKey, alternateArt)
            stats.added++
        } catch (e: Throwable) {
            log.error("Error adding $type [$content]", e)
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
            val cards: ItemStats = ItemStats(),
            val equipment: ItemStats = ItemStats(),
            val packs: ItemStats = ItemStats(),
            val mercenaries: ItemStats = ItemStats()
    )

    private data class ItemStats(
            var found: Int = 0,
            var existing: Int = 0,
            var added: Int = 0
    )

}
