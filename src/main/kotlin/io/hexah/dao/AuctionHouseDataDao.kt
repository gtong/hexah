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
open class AuctionHouseDataDao @Autowired constructor(val jdbcTemplate: JdbcTemplate) {

    val mapper = RowMapper<AuctionHouseData>() { rs, rowNum ->
        AuctionHouseData(
                date = rs.getDate("date"),
                name = rs.getString("name"),
                rarity = HexObjectRarity.fromDB(rs.getInt("rarity")),
                currency = AuctionHouseCurrency.fromDB(rs.getString("currency")[0]),
                created = rs.getTimestamp("created"),
                updated = rs.getTimestamp("updated"),
                trades = rs.getInt("trades"),
                low = rs.getInt("low"),
                high = rs.getInt("high"),
                median = rs.getInt("median"),
                average = rs.getDouble("average")
        )
    }
    val table = "auction_house_data"
    val columns = "date, name, rarity, currency, created, updated, trades, low, high, median, average"

    open fun add(date: Date, name: String, rarity: HexObjectRarity, currency: AuctionHouseCurrency, trades: Int, low: Int, high: Int, median: Int, average: Double) {
        val now = Date()
        jdbcTemplate.update("""insert into auction_house_data
                (date, name, rarity, currency, created, updated, trades, low, high, median, average)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                date, name, rarity.db, currency.db, now, now, trades, low, high, median, average
        )
    }

}
