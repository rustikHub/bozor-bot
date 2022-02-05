package uz.rustik.bozorbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import uz.ugnis.tgbotlib.EnableTelegramBotAutoConfiguration

@EnableJpaAuditing
@SpringBootApplication
@EnableTelegramBotAutoConfiguration
@EnableJpaRepositories(repositoryBaseClass = BaseRepositoryImpl::class)
class BozorBotApplication

fun main(args: Array<String>) {
    runApplication<BozorBotApplication>(*args)
}
