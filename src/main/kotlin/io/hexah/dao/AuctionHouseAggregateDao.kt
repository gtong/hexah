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
open class AuctionHouseAggregateDao @Autowired constructor(val jdbcTemplate: JdbcTemplate) {

    val mapper = RowMapper<AuctionHouseAggregate>() { rs, rowNum ->
        AuctionHouseAggregate(
                name = rs.getString("name"),
                rarity = HexObjectRarity.fromDB(rs.getInt("rarity")),
                currency = AuctionHouseCurrency.fromDB(rs.getString("currency")[0]),
                created = rs.getTimestamp("created"),
                updated = rs.getTimestamp("updated"),
                totalTrades = rs.getInt("total_trades"),
                totalTradesPerDay = rs.getDouble("total_trades_per_day"),
                totalMedian = rs.getDouble("total_median"),
                totalAverage = rs.getDouble("total_average"),
                last7Trades = rs.getInt("last7_trades"),
                last7TradesPerDay = rs.getDouble("last7_trades_per_day"),
                last7Median = rs.getDouble("last7_median"),
                last7Average = rs.getDouble("last7_average")
        )
    }
    val table = "auction_house_aggregates"
    val columns = "name, rarity, currency, created, updated, total_trades, total_trades_per_day, total_median, total_average, last7_trades, last7_trades_per_day, last7_median, last7_average"

    open fun save(name: String, rarity: HexObjectRarity, currency: AuctionHouseCurrency, totalTrades: Int, totalTradesPerDay: Double, totalMedian: Double, totalAverage: Double, last7Trades: Int, last7TradesPerDay: Double, last7Median: Double, last7Average: Double) {
        val now = Date()
        val list = jdbcTemplate.query("select $columns from $table where name = ? AND rarity = ? AND currency = ?", mapper, name, rarity.db, currency.db)
        if (list.isEmpty()) {
            jdbcTemplate.update("""insert into auction_house_aggregates
                (name, rarity, currency, created, updated, total_trades, total_trades_per_day, total_median, total_average, last7_trades, last7_trades_per_day, last7_median, last7_average)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                    name, rarity.db, currency.db, now, now, totalTrades, totalTradesPerDay, totalMedian, totalAverage, last7Trades, last7TradesPerDay, last7Median, last7Average
            )
        } else {
            jdbcTemplate.update("""update auction_house_aggregates
                set updated = ?, total_trades = ?, total_trades_per_day = ?, total_median = ?, total_average = ?, last7_trades = ?, last7_trades_per_day = ?, last7_median = ?, last7_average = ?
                where name = ? AND rarity = ? AND currency = ?""",
                    now, totalTrades, totalTradesPerDay, totalMedian, totalAverage, last7Trades, last7TradesPerDay, last7Median, last7Average, name, rarity.db, currency.db
            )
        }
    }

}
