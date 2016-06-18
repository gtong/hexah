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

    val mapper = RowMapper { rs, rowNum ->
        AuctionHouseFeed(
                filename = rs.getString("filename"),
                created = rs.getTimestamp("created"),
                updated = rs.getTimestamp("updated"),
                type = AuctionHouseFeedType.fromDB(rs.getString("type")[0]),
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
                filename, now, now, type.db
        )
    }

    open fun update(feed: AuctionHouseFeed) {
        val now = Date()
        jdbcTemplate.update(
                "update $table set in_progress = ?, num_loaded = ?, completed = ?, updated = ? where filename = ?",
                feed.inProgress, feed.numLoaded, feed.completed, now, feed.filename
        )
    }

    open fun findAll(): List<AuctionHouseFeed> {
        return jdbcTemplate.query("select $columns from $table", mapper)
    }

    open fun findOneToProcessByType(type: AuctionHouseFeedType): AuctionHouseFeed? {
        return jdbcTemplate.query(
                "select $columns from $table where type = ? and in_progress = false and completed is null order by created limit 1 for update",
                mapper, type.db).elementAtOrNull(0);
    }
}
