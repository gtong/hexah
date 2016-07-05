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
                hexUser = rs.getString("hex_user")
        )
    }
    private val table = "users"
    private val columns = "id, created, updated, email, status, guid, last_active, hex_user"

    open fun findAll() = jdbcTemplate.query("select $columns from $table order by id", mapper)

    open fun create(email: String, status: UserStatus, guid: String): Int {
        val now = Date()
        val insert = SimpleJdbcInsert(jdbcTemplate).withTableName(table).usingGeneratedKeyColumns("id")

        val id = insert.executeAndReturnKey(mapOf(
                "created" to now,
                "updated" to now,
                "email" to email,
                "status" to status.db,
                "guid" to guid,
                "last_active" to now
        ))

        return id.toInt()
    }

}
