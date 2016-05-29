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

    val mapper = RowMapper<HexObject>() { rs, rowNum ->
        HexObject(
                id = rs.getInt("id"),
                created = rs.getTimestamp("created"),
                updated = rs.getTimestamp("updated"),
                guid = rs.getString("guid"),
                setGuid = rs.getString("set_guid"),
                name = rs.getString("name"),
                type = HexObjectType.fromDB(rs.getString("type")[0]),
                rarity = HexObjectRarity.fromDB(rs.getInt("rarity")),
                alternateArt = rs.getBoolean("alternate_art")
        )
    }
    val table = "hex_objects"
    val columns = "id, created, updated, guid, set_guid, name, type, rarity, alternate_art"

    open fun add(guid: String, setGuid: String, name: String, type: HexObjectType, rarity: HexObjectRarity, alternateArt: Boolean): Int {
        val now = Date()
        val insert = SimpleJdbcInsert(jdbcTemplate).withTableName(table).usingGeneratedKeyColumns("id")

        val id = insert.executeAndReturnKey(mapOf(
                "CREATED" to now,
                "UPDATED" to now,
                "GUID" to guid,
                "SET_GUID" to setGuid,
                "NAME" to name,
                "TYPE" to type.db,
                "RARITY" to rarity.db,
                "ALTERNATE_ART" to alternateArt
        ))

        return id.toInt()
    }

    open fun findAll(): List<HexObject> {
        return jdbcTemplate.query("select $columns from $table", mapper)
    }

}
