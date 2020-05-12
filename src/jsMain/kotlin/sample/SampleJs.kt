package sample

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.features.cookies.AcceptAllCookiesStorage
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.features.cookies.cookies
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.webSocketSession
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.readText
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.css.CSSBuilder
import kotlinx.html.CommonAttributeGroupFacade
import kotlinx.html.style
import kotlin.browser.document

actual class Sample {
    actual fun checkMe() = 12
}

actual object Platform {
    actual val name: String = "JS"
}

val wsReceiveActions = mutableMapOf<Char, (String)->Unit>()

suspend fun sessionAction(session: WebSocketSession, board: BoardJs) {
    while (true) {
        val text = (session.incoming.receive() as Frame.Text).readText()
        println(text)
        wsReceiveActions[text[0]]?.invoke(text.drop(1))
    }
}

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun main() {
    val client = HttpClient(Js).config {
        install(WebSockets)
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
    }
    client.cookies("/")
    val ws =
        GlobalScope.async {
            client.webSocketSession(
                HttpMethod.Get,
//                "127.0.0.1",
                "192.168.11.5",
                8080,
                "/myws/echo"
            )
        }
    fun wsSend(msg:String) {
        GlobalScope.launch {
            ws.await().send(Frame.Text(msg))
        }.start()
    }

    val board = BoardJs(::wsSend)
    val chat = ChatJs(::wsSend)

    wsReceiveActions['g'] = board::receive
    wsReceiveActions['c'] = chat::receive

    document.addEventListener("DOMContentLoaded", {
        document.getElementById("js-board")?.append(board.create())
        document.getElementById("js-chat")?.append(chat.create())
    })
    sessionAction(ws.await(),board)
}

fun CommonAttributeGroupFacade.css(builder: CSSBuilder.() -> Unit) {
    this.style = CSSBuilder().apply(builder).toString().trim()
}
