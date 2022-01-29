package uz.rustik.bozorbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import uz.rustik.testlib.anotation.EnableBotConfig

@SpringBootApplication
@EnableBotConfig
class BozorBotApplication

fun main(args: Array<String>) {
    runApplication<BozorBotApplication>(*args)
}
