package sample

import kotlinx.css.*
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.js.*
import org.w3c.dom.HTMLDivElement
import kotlin.browser.document
import kotlin.properties.Delegates

class ChatJs(val send: (String) -> Unit) {
    var msgDiv by Delegates.notNull<HTMLDivElement>()

    fun create() :HTMLDivElement{
        val res = document.create.div { }
        msgDiv = res.append.div {
            css {
                overflow = Overflow.scroll
                height = 200.px
            }
        }
        val input = res.append.input { }
        val button = res.append.button {
            +"button"
            css {
                color = Color.red
            }
            onClickFunction = {
                send(Header.CHAT with input.value)
            }
        }
        return res
    }

    fun receive(msg:String) {
        val isScroll = msgDiv.scrollTop.toInt() == msgDiv.scrollHeight - msgDiv.clientHeight
        msgDiv.append.li {
            +msg
        }
        if (isScroll)
            msgDiv.scroll(0.0, msgDiv.scrollHeight.toDouble())
    }
}