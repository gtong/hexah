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

    val gmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    val mapper = RowMapper { rs, rowNum ->
        AuctionHouseData(
                date = rs.getDate("date", gmt),
                name = rs.getString("name"),
                rarity = HexObjectRarity.fromDB(rs.getInt("rarity")),
                nameKey = rs.getString("name_key"),
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
    val columns = "date, name, rarity, name_key, currency, created, updated, trades, low, high, median, average"

    open fun add(date: Date, name: String, nameKey: String, rarity: HexObjectRarity, currency: AuctionHouseCurrency, trades: Int, low: Int, high: Int, median: Int, average: Double) {
        val now = Date()
        jdbcTemplate.update("""insert into auction_house_data
                (date, name, rarity, name_key, currency, created, updated, trades, low, high, median, average)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                date, name, rarity.db, nameKey, currency.db, now, now, trades, low, high, median, average
        )
    }

    open fun findByName(name: String) =
        jdbcTemplate.query("select $columns from $table where name = ? order by date", mapper, name)

    open fun findByNameKey(nameKey: String) =
        jdbcTemplate.query("select $columns from $table where name_key = ? order by date", mapper, nameKey)

}
