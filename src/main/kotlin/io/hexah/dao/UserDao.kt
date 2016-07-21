package io.hexah.dao

import io.hexah.model.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.*

@Repository
open class UserDao @Autowired constructor(private val jdbcTemplate: JdbcTemplate) {

    private val mapper = RowMapper { rs, rowNum ->
        User(
                id = rs.getInt("id"),
                created = rs.getTimestamp("created"),
                updated = rs.getTimestamp("updated"),
                email = rs.getString("email"),
                status = UserStatus.fromDB(rs.getString("status")[0]),
                guid = rs.getString("guid"),
                lastActive = rs.getTimestamp("last_active"),
                hexUser = rs.getString("hex_user"),
                sellCards = rs.getBoolean("sell_cards"),
                sellEquipment = rs.getBoolean("sell_equipment")
        )
    }
    private val table = "users"
    private val columns = "id, created, updated, email, status, guid, last_active, hex_user, sell_cards, sell_equipment"

    open fun findAll() = jdbcTemplate.query("select $columns from $table where deleted is null order by id", mapper)

    open fun findById(id: Int) = jdbcTemplate.queryForObject("select $columns from $table where id = ?", mapper, id)

    open fun findByGuid(guid: String) = jdbcTemplate.queryForObject("select $columns from $table where guid = ?::uuid", mapper, guid)

    open fun findOtherVerifiedHexUser(id: Int, hexUser: String) =
            jdbcTemplate.query(
                    "select $columns from $table where id <> ? and hex_user = ? and status = ? and deleted is null", mapper,
                    id, hexUser, UserStatus.Verified.db).elementAtOrNull(0)

    open fun create(email: String, status: UserStatus, guid: String, sellCards: Boolean, sellEquipment: Boolean): Int {
        val now = Date()
        val insert = SimpleJdbcInsert(jdbcTemplate).withTableName(table).usingGeneratedKeyColumns("id")

        val id = insert.executeAndReturnKey(mapOf(
                "created" to now,
                "updated" to now,
                "email" to email,
                "status" to status.db,
                "guid" to guid,
                "last_active" to now,
                "sell_cards" to sellCards,
                "sell_equipment" to sellEquipment
        ))

        return id.toInt()
    }

    open fun updateLastActive(id: Int, timestamp: Date) {
        jdbcTemplate.update("update $table set last_active = ? where id = ?", timestamp, id)
    }

    open fun updateStatusAndHexuser(id: Int, status: UserStatus, hexUser: String) {
        val now = Date()
        jdbcTemplate.update("update $table set status = ?, hex_user = ?, updated = ? where id = ?", status.db, hexUser, now, id)
    }

}
