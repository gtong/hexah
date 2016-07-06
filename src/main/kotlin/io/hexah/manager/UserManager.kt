package io.hexah.manager

import io.hexah.dao.UserDao
import io.hexah.model.UserStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component
open class UserManager @Autowired constructor(private val userDao: UserDao) {

    private var cache: Map<String, Int> = emptyMap()

    fun getOrCreateUserId(email: String): Int {
        if (email !in cache) {
            // Refresh the cache
            cache = userDao.findAll().associate { Pair(it.email, it.id) }
        }
        if (email in cache) {
            return cache[email]!!
        }
        // Create the user if not in cache
        val id = userDao.create(email, UserStatus.Unverified, UUID.randomUUID().toString())

        // Refresh the cache again
        cache = userDao.findAll().associate { Pair(it.email, it.id) }

        return id
    }

}

