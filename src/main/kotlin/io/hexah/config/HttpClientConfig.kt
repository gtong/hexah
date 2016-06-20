package io.hexah.config

import com.google.api.client.http.javanet.NetHttpTransport
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class HttpClientConfig {

    @Bean
    open fun httpRequestFactory() = NetHttpTransport().createRequestFactory()

}
