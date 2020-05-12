package sample

expect class Sample() {
    fun checkMe(): Int
}

expect object Platform {
    val name: String
}

fun hello(): String = "Hello from ${Platform.name}"

enum class Header(val c: Char) {
    GAME('g'),
    CHAT('c');

    infix fun with(string: String) = c+string
}