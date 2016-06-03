package io.hexah.dao

import com.google.gson.Gson
import com.google.gson.JsonParser
import io.hexah.model.*
import org.postgresql.util.PGobject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.*

@Repository
open class AuctionHouseAggregateDao @Autowired constructor(val jdbcTemplate: JdbcTemplate) {

    val parser = JsonParser()
    val gson = Gson()

    val mapper = RowMapper<AuctionHouseAggregate>() { rs, rowNum ->
        AuctionHouseAggregate(
                name = rs.getString("name"),
                rarity = HexObjectRarity.fromDB(rs.getInt("rarity")),
                currency = AuctionHouseCurrency.fromDB(rs.getString("currency")[0]),
                created = rs.getTimestamp("created"),
                updated = rs.getTimestamp("updated"),
                stats = fromJson(rs.getString("stats"))
        )
    }
    val table = "auction_house_aggregates"
    val columns = "name, rarity, currency, created, updated, stats"

    open fun save(name: String, rarity: HexObjectRarity, currency: AuctionHouseCurrency, stats: Map<String, Number>) {
        val now = Date()
        val list = jdbcTemplate.query("select $columns from $table where name = ? AND rarity = ? AND currency = ?", mapper, name, rarity.db, currency.db)
        val pgStats = PGobject()
        pgStats.type = "JSON"
        pgStats.value = toJson(stats)
        if (list.isEmpty()) {
            jdbcTemplate.update("""insert into auction_house_aggregates
                    (name, rarity, currency, created, updated, stats) values (?, ?, ?, ?, ?, ?)""",
                    name, rarity.db, currency.db, now, now, pgStats
            )
        } else {
            jdbcTemplate.update("""update auction_house_aggregates
                    set updated = ?, stats = ? where name = ? AND rarity = ? AND currency = ?""",
                    now, pgStats, name, rarity.db, currency.db
            )
        }
    }

    private fun toJson(stats: Map<String, Number>): String {
        return gson.toJson(stats)
    }

    private fun fromJson(json: String): Map<String, Number> {
        val stats = LinkedHashMap<String, Number>()
        parser.parse(json).asJsonObject.entrySet().forEach { entry ->
            stats.put(entry.key, entry.value.asBigDecimal)
        }
        return stats
     }

}
