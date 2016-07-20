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
open class UserItemDao @Autowired constructor(private val jdbcTemplate: JdbcTemplate) {

    private val mapper = RowMapper { rs, rowNum ->
        UserItem(
                userId = rs.getInt("user_id"),
                itemGuid = rs.getString("item_guid"),
                type = UserItemType.fromDB(rs.getString("type")[0]),
                updated = rs.getTimestamp("updated"),
                number = rs.getInt("number"),
                sell = rs.getBoolean("sell")
        )
    }
    private val table = "user_items"
    private val columns = "user_id, item_guid, type, updated, number, sell"

    open fun findByUserIdAndType(userId: Int, type: UserItemType)
            = jdbcTemplate.query("select $columns from $table where user_id = ? and type = ?", mapper, userId, type.db)

    open fun createIfNotCreated(userId: Int, itemGuid: String, type: UserItemType, number: Int, sell: Boolean) {
        val now = Date()

        jdbcTemplate.update("""insert into $table
                (user_id, item_guid, type, updated, number, sell)
                values (?, ?::uuid, ?, ?, ?, ?) on conflict do nothing""",
                userId, itemGuid, type.db, now, number, sell
        )
    }

    open fun updateCountIfNotUpdated(userId: Int, itemGuid: String, number: Int, updated: Date) {
        val now = Date()

        jdbcTemplate.update(
                """update $table set number = ?, updated = ? where user_id = ? and item_guid = ?::uuid and updated = ?""",
                number, now, userId, itemGuid, updated
        )
    }

}
