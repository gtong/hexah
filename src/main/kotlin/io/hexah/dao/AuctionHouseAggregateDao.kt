package io.hexah.dao

import com.google.gson.Gson
import com.google.gson.JsonParser
import io.hexah.model.AuctionHouseAggregate
import io.hexah.model.AuctionHouseCurrency
import io.hexah.model.HexObjectRarity
import org.postgresql.util.PGobject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.util.*

@Repository
open class AuctionHouseAggregateDao @Autowired constructor(val jdbcTemplate: JdbcTemplate) {

    val parser = JsonParser()
    val gson = Gson()

    val mapper = RowMapper { rs, rowNum ->
        AuctionHouseAggregate(
                name = rs.getString("name"),
                rarity = HexObjectRarity.fromDB(rs.getInt("rarity")),
                nameKey = rs.getString("name_key"),
                currency = AuctionHouseCurrency.fromDB(rs.getString("currency")[0]),
                created = rs.getTimestamp("created"),
                updated = rs.getTimestamp("updated"),
                stats = fromJson(rs.getString("stats"))
        )
    }
    val nameMapper = RowMapper { rs, i ->
        Triple(rs.getString("name"), HexObjectRarity.fromDB(rs.getInt("rarity")), rs.getString("name_key"))
    }
    val table = "auction_house_aggregates"
    val columns = "name, rarity, name_key, currency, created, updated, stats"

    open fun findNames()
            = jdbcTemplate.query("select name, rarity, name_key from $table group by name, rarity, name_key", nameMapper);

    open fun findByNameKey(nameKey: String)
            = jdbcTemplate.query("select $columns from $table where name_key = ?", mapper, nameKey)

    open fun save(name: String, rarity: HexObjectRarity, nameKey: String, currency: AuctionHouseCurrency, stats: Map<String, Number>) {
        val now = Date()
        val list = jdbcTemplate.query("select $columns from $table where name_key = ? AND currency = ?", mapper, nameKey, currency.db)
        val pgStats = PGobject()
        pgStats.type = "JSON"
        pgStats.value = toJson(stats)
        if (list.isEmpty()) {
            jdbcTemplate.update("""insert into auction_house_aggregates
                    (name, rarity, name_key, currency, created, updated, stats) values (?, ?, ?, ?, ?, ?, ?)""",
                    name, rarity.db, nameKey, currency.db, now, now, pgStats
            )
        } else {
            jdbcTemplate.update("""update auction_house_aggregates
                    set updated = ?, stats = ? where name_key = ? AND currency = ?""",
                    now, pgStats, nameKey, currency.db
            )
        }
    }

    private fun toJson(stats: Map<String, Number>) = gson.toJson(stats)

    private fun fromJson(json: String): Map<String, Number> {
        val stats = LinkedHashMap<String, Number>()
        parser.parse(json).asJsonObject.entrySet().forEach { entry ->
            stats.put(entry.key, entry.value.asBigDecimal)
        }
        return stats
    }

}
