package io.hexah.config

import io.hexah.controller.handler.AuthHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer
import org.springframework.boot.context.embedded.ErrorPage
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

@Configuration
open class WebConfig : WebMvcConfigurerAdapter() {

    @Autowired
    lateinit private var authHandler: AuthHandler

    @Bean
    open fun notFoundCustomizer() = NotFoundCustomizer()

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authHandler)
    }

    class NotFoundCustomizer : EmbeddedServletContainerCustomizer {
        override fun customize(container: ConfigurableEmbeddedServletContainer) {
            container.addErrorPages(ErrorPage(HttpStatus.NOT_FOUND, "/"))
        }
    }

}
