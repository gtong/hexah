package io.hexah.manager

import com.google.gson.JsonObject
import io.hexah.dao.UserItemDao
import io.hexah.model.User
import io.hexah.model.UserItemType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component
open class UserItemManager @Autowired constructor(
        private val userManager: UserManager,
        private val userItemDao: UserItemDao
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun sync(user: User, json: JsonObject) {
        userManager.updateLastActive(user.id)
        when (json["Message"].asString) {
            "Collection" -> collection(user, json)
        }
    }

    private fun collection(user: User, json: JsonObject) {
        when (json["Action"].asString) {
            "Overwrite" -> {
                val collection = userItemDao.findByUserIdAndType(user.id, UserItemType.Card).associateBy { it.itemGuid }
                val toRemove = HashMap(collection)
                for(item in json["Complete"].asJsonArray) {
                    try {
                        val obj = item.asJsonObject
                        val count = obj["Count"].asInt
                        val guid = obj["Guid"].asJsonObject["m_Guid"].asString
                        val existing = collection[guid]
                        if (existing == null) {
                            userItemDao.createIfNotCreated(user.id, guid, UserItemType.Card, count, true)
                        } else {
                            toRemove.remove(guid)
                            if (existing.number != count) {
                                userItemDao.updateCountIfNotUpdated(user.id, guid, count, existing.updated)
                            }
                        }
                    } catch (t: Throwable) {
                        log.error("Could not save collection item [$item] for user ${user.id}", t)
                    }
                }
                for((key, item) in toRemove) {
                    userItemDao.updateCountIfNotUpdated(user.id, item.itemGuid, 0, item.updated)
                }
            }
        }
    }

}

