package io.hexah.dao

import io.hexah.model.HexObjectType
import io.hexah.model.UserItemAvailability
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository

@Repository
open class UserItemAvailabilityDao @Autowired constructor(private val jdbcTemplate: JdbcTemplate) {

    private val mapper = RowMapper { rs, rowNum ->
        UserItemAvailability(
                userId = rs.getInt("user_id"),
                userLastActive = rs.getTimestamp("last_active"),
                hexObjectType = HexObjectType.fromDB(rs.getString("type")[0]),
                numberAvailable = rs.getInt("available")
        )
    }
    open fun findAvailabileByNameKey(nameKey: String)
            = jdbcTemplate.query("""
select u.id as user_id, u.last_active, o.type, i.number - i.in_transactions - (case when o.type = 'e' then 1 else 4 end) as available
from user_items i, hex_objects o, users u
where i.item_guid = o.guid and o.name_key = ?
      and i.user_id = u.id and u.status = 'v' and i.sell = true
      and (case when o.type = 'c' then u.sell_cards when o.type = 'e' then u.sell_equipment else false end) = true
      and (i.number - i.in_transactions) > (case when o.type = 'e' then 1 else 4 end)
order by 3 desc""", mapper, nameKey)

}
