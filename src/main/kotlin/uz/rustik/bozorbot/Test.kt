package uz.rustik.bozorbot

fun main() {
    println("salom^".split("0").size)
    var list = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9")

    list = list.safeSubList(0, 6)

    list.chunked(2) {
        println(it.joinToString(", "))
    }

}