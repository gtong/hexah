package io.hexah.dao

import io.hexah.model.AuctionHouseFeed
import io.hexah.model.AuctionHouseFeedType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.util.*

@Repository
open class AuctionHouseFeedDao @Autowired constructor(val jdbcTemplate: JdbcTemplate) {

    val mapper = RowMapper<AuctionHouseFeed>() { rs, rowNum ->
        AuctionHouseFeed(
                filename = rs.getString("filename"),
                created = rs.getTimestamp("created"),
                updated = rs.getTimestamp("updated"),
                type = AuctionHouseFeedType.valueOf(rs.getString("type")),
                inProgress = rs.getBoolean("in_progress"),
                numLoaded = rs.getInt("num_loaded"),
                completed = rs.getTimestamp("completed")
        )
    }
    val table = "auction_house_feeds"
    val columns = "filename, created, updated, type, in_progress, num_loaded, completed"

    open fun add(filename: String, type: AuctionHouseFeedType) {
        val now = Date()
        jdbcTemplate.update(
                "insert into $table (filename, created, updated, type) values (?, ?, ?, ?)",
                filename, now, now, type.name
        )
    }

    open fun findAll(): List<AuctionHouseFeed> {
        return jdbcTemplate.query("select $columns from $table", mapper)
    }
}
