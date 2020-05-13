package sample

import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.html.respondHtml
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.resource
import io.ktor.http.content.static
import io.ktor.locations.Location
import io.ktor.locations.Locations
import io.ktor.locations.get
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.sessions.*
import io.ktor.websocket.webSocket
import kotlinx.css.CSSBuilder
import kotlinx.css.Display
import kotlinx.css.display
import kotlinx.html.*
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

actual class Sample {
    actual fun checkMe() = 42
}

actual object Platform {
    actual val name: String = "JVM"
}

class Client(
    val session: DefaultWebSocketSession,
    val id: Int = lastId.getAndIncrement()
) {
    companion object {
        var lastId = AtomicInteger(0)
    }
}

val clients = Collections.synchronizedSet(mutableSetOf<Client>())
val game = Game()
var board = CharArray(9) { '.' }
var lastId = AtomicInteger(0)

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(Locations) {
    }

    install(Sessions) {
        cookie<SessionData>("session")
    }

    intercept(ApplicationCallPipeline.Features) {
        if (call.sessions.get<SessionData>() == null) {
            call.sessions.set(SessionData(lastId.getAndIncrement()))
        }
    }

    install(io.ktor.websocket.WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        get("/") {
            call.respondHtml {
                head {
                    title("Hello from Ktor!")
                }
                body {
                    +"${hello()} from Ktor. Check me value: ${Sample().checkMe()}"
                    div {
                        css {
                            display = Display.flex
                        }
                        div {
                            id = "js-board"
                            +"Loading..."
                        }
                        div {
                            id = "js-chat"
                            +"Loading..."
                        }
                    }
                    script(src = "/static/oxWeb.js") {}
                }
            }
        }

        static("/static") {
            resource("oxWeb.js")
        }

        get<MyLocation> {
            call.respondText("Location: name=${it.name}, arg1=${it.arg1}, arg2=${it.arg2}")
        }
        // Register nested routes
        get<Type.Edit> {
            call.respondText("Inside $it")
        }
        get<Type.List> {
            call.respondText("Inside $it")
        }

        webSocket("/myws/echo") {
            val session = call.sessions.get<SessionData>()?.id
            val client = session
                ?.let { Client(this, it) }
                ?: Client(this)
            clients += client
            try {
                send(Frame.Text(Header.CHAT with "Hi from server"))
                send(Frame.Text(Header.GAME with game.getState().toJson()))
                while (true) {
                    val frame = incoming.receive()
                    if (frame is Frame.Text) {
                        val text = frame.readText().alsoPrintln()
                        val head = text[0]
                        val body = text.drop(1)

                        for (cl in clients) {
                            val ws = cl.session
                            if (head == 'c')
                                ws.send(Frame.Text(Header.CHAT with "Client ${client.id}: $body"))
                            else {
                                val act = body.toAction().alsoPrintln()
                                game.put(act, if (client.id % 2 == 0)'o' else 'x')
                                ws.send(Frame.Text(Header.GAME with game.getState().toJson()))
                            }
                        }
                    }
                }
            } finally {
                clients -= client
            }
        }
    }
}

@Location("/location/{name}")
class MyLocation(val name: String, val arg1: Int = 42, val arg2: String = "default")

@Location("/type/{name}")
data class Type(val name: String) {
    @Location("/edit")
    data class Edit(val type: Type)

    @Location("/list/{page}")
    data class List(val type: Type, val page: Int)
}

fun CommonAttributeGroupFacade.css(builder: CSSBuilder.() -> Unit) {
    this.style = CSSBuilder().apply(builder).toString().trim()
}

data class SessionData(val id:Int)