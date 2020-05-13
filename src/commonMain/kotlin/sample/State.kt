package sample

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

val json = Json(JsonConfiguration.Stable)

@Serializable
data class Action (val cellID:Int) {
    fun toJson() = json.stringify(serializer(), this)
}

fun String.toAction() = json.parse(Action.serializer(), this)


@Serializable
data class State(
    val turnPlayer: Int = 0,
    val board: CharArray = CharArray(9) { '.' }
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as State

        if (turnPlayer != other.turnPlayer) return false
        if (!board.contentEquals(other.board)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = turnPlayer
        result = 31 * result + board.contentHashCode()
        return result
    }

    fun toJson():String = json.stringify(serializer(),this)
}

fun String.toState() = json.parse(State.serializer(), this)