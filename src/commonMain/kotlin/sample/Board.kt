package sample

class Game {
    val players = listOf(0, 1)
    val turnPlayer = 0
    val board = CharArray(9) { '.' }

    val lines
        get() = listOf(
            // row
            listOf(board[0], board[1], board[2]),
            listOf(board[3], board[4], board[5]),
            listOf(board[6], board[7], board[8]),

            // col
            listOf(board[0], board[3], board[6]),
            listOf(board[1], board[4], board[7]),
            listOf(board[2], board[5], board[8]),

            // cross
            listOf(board[0], board[4], board[8]),
            listOf(board[2], board[4], board[6])
        )

    fun put(x: Int, y: Int, c: Char) {
        board[3 * y + x] = c
    }

    fun put(action: Action, c:Char) {
        board[action.cellID] = c
    }

    fun winPlayer(): Int? {
        for (line in lines) {
            if (line.allEQ('o')) return 0
            if (line.allEQ('x')) return 1
        }
        return null
    }

    fun getState() = State(turnPlayer, board)
}

fun <T> Iterable<T>.allEQ(element: T) = all { it == element }