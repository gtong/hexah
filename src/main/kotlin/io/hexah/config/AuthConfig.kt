package io.hexah.config

import com.auth0.Auth0
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AuthConfig {

    @Value("\${auth.client}")
    lateinit private var authClient: String
    @Value("\${auth.domain}")
    lateinit private var authDomain: String

    @Bean
    open fun authClient() = Auth0(authClient, authDomain).newAuthenticationAPIClient()

}
