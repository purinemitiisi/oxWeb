package sample

import kotlinx.css.*
import kotlinx.css.properties.border
import kotlinx.html.dom.create
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLDivElement
import sample.State.Companion.toState
import kotlin.browser.document
import kotlin.dom.clear

class BoardJs(val sendAction: (String) -> Unit) {
    //board -> area -> elements?
    enum class Area { BOARD }

    lateinit var elements: List<HTMLDivElement>
    val boxSize = 50.px
    val boxBorder = 5.px

    fun create(): HTMLDivElement {
        val res = document.create.div {
            css {
                display = Display.grid
                val col = LinearDimension.minContent
                gridTemplateColumns = GridTemplateColumns(col, col, col)
            }
        }
        elements = List(9) { box(boxSize, Color.black, it / 3 + 1, it % 3 + 1, sendAction) }
        elements.forEach {
            res.append(it)
        }
        return res
    }

    //todo:id
    fun set(col: Int, row: Int, color: Color) {
//        elements[col-1+(row-1)*3].click()
        val element = elements[col - 1 + (row - 1) * 3]
        val color =
            if (element.childElementCount == 0)
                element.append(circle(boxSize - boxBorder * 2, color))
            else
                element.clear()
    }

    fun receive(json: String) {
        val state = json.toState().alsoPrintln()
        for ((i, c) in state.board.withIndex()) {
            elements[i].clear()
            when (c) {
                'o' -> elements[i].append(circle(boxSize - boxBorder * 2, Color.red))
                'x' -> elements[i].append(circle(boxSize - boxBorder * 2, Color.blue))
            }
        }
    }
}

fun board(sendAction: (String) -> Unit): HTMLDivElement {
    val res = document.create.div {
        css {
            display = Display.grid
            val col = LinearDimension.minContent
            gridTemplateColumns = GridTemplateColumns(col, col, col)
        }
    }
    repeat(9) {
        res.append(box(50.px, Color.black, it / 3 + 1, it % 3 + 1, sendAction))
    }
    return res
}

fun box(size: LinearDimension, color: Color, col: Int, row: Int, sendAction: (String) -> Unit): HTMLDivElement {
    var res: HTMLDivElement? = null
    res = document.create.div {
        val borderPX = 5.px
        var state: HTMLDivElement? = null
        css {
            height = size
            width = size
            border(borderPX, BorderStyle.solid, color)
            gridRow = GridRow(row.toString())
            gridColumn = GridColumn(col.toString())
        }

        onClickFunction = {
            sendAction(Header.GAME with "$row$col")
        }
    }
    return res
}

fun circle(size: LinearDimension, color: Color): HTMLDivElement {
    return document.create.div {
        css {
            height = size
            width = size
            border(5.px, BorderStyle.solid, color, 50.pct)
        }
    }
}