package uz.rustik.bozorbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BozorBotApplication

fun main(args: Array<String>) {
    runApplication<BozorBotApplication>(*args)
}
