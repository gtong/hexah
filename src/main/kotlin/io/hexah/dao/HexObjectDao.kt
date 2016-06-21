package io.hexah.dao

import io.hexah.model.HexObject
import io.hexah.model.HexObjectRarity
import io.hexah.model.HexObjectType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import java.util.*

@Repository
open class HexObjectDao @Autowired constructor(val jdbcTemplate: JdbcTemplate) {

    val mapper = RowMapper { rs, rowNum ->
        HexObject(
                id = rs.getInt("id"),
                created = rs.getTimestamp("created"),
                updated = rs.getTimestamp("updated"),
                guid = rs.getString("guid"),
                setGuid = rs.getString("set_guid"),
                type = HexObjectType.fromDB(rs.getString("type")[0]),
                name = rs.getString("name"),
                rarity = HexObjectRarity.fromDB(rs.getInt("rarity")),
                nameKey = rs.getString("name_key"),
                alternateArt = rs.getBoolean("alternate_art"),
                imagePath = rs.getString("image_path")
        )
    }
    val table = "hex_objects"
    val columns = "id, created, updated, guid, set_guid, type, name, rarity, name_key, alternate_art, image_path"

    open fun add(guid: String, setGuid: String, type: HexObjectType, name: String, rarity: HexObjectRarity, nameKey: String, alternateArt: Boolean, imagePath: String): Int {
        val now = Date()
        val insert = SimpleJdbcInsert(jdbcTemplate).withTableName(table).usingGeneratedKeyColumns("id")

        val id = insert.executeAndReturnKey(mapOf(
                "created" to now,
                "updated" to now,
                "guid" to guid,
                "set_guid" to setGuid,
                "type" to type.db,
                "name" to name,
                "rarity" to rarity.db,
                "name_key" to nameKey,
                "alternate_art" to alternateArt,
                "image_path" to imagePath
        ))

        return id.toInt()
    }

    open fun findAll(): List<HexObject> {
        return jdbcTemplate.query("select $columns from $table", mapper)
    }

}
