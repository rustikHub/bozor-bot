package uz.rustik.bozorbot

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.data.domain.AuditorAware
import java.util.*

@Configuration
class Config {
    @Bean
    fun messageResourceBundleMessageSource(): ResourceBundleMessageSource? {
        val messageSource = ResourceBundleMessageSource()
        messageSource.setBasename("messages")
        messageSource.setCacheSeconds(3600)
        messageSource.setDefaultLocale(Locale.ENGLISH)
        messageSource.setDefaultEncoding("UTF-8")
        return messageSource
    }
}