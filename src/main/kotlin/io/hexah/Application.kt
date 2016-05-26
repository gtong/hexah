package io.hexah

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.net.URI

@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
open class Application {

}

fun main(args: Array<String>) {
    val databaseURL = System.getenv("DATABASE_URL")
    if (databaseURL != null) {
        val dbURI = URI(databaseURL)
        val (username, password) = dbURI.userInfo.split(":", limit = 2)
        val dbURL = "jdbc:postgresql://${dbURI.host}:${dbURI.port}${dbURI.path}"
        System.setProperty("db.url", dbURL)
        System.setProperty("db.username", username)
        System.setProperty("db.password", password)
    }

    SpringApplication.run(Application::class.java, *args)
}
