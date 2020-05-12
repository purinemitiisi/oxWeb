package sample

public fun <T> T.alsoPrintln(): T = also { println(this) }

public fun <T> T.alsoPrintln(preString: String): T = also { println(preString + this) }

public fun <T> T.alsoPrintln(t: (T) -> String): T = also { println(t(this)) }
