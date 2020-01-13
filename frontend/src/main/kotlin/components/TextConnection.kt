package components

import debug
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.promise
import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import penta.ConnectionState
import penta.network.GameEvent
import penta.redux_rewrite.BoardState
import react.RBuilder
import react.RClass
import react.RComponent
import react.RProps
import react.RState
import react.createRef
import react.dom.br
import react.dom.button
import react.dom.div
import react.dom.form
import react.dom.input
import react.dom.li
import react.dom.ol
import react.invoke
import react.redux.rConnect
import reducers.State
import redux.WrapperAction

interface TextConnectionProps : TextConnectionStateProps, TextConnectionDispatchProps

class TextConnection(props: TextConnectionProps) : RComponent<TextConnectionProps, RState>(props) {
    val urlRef = createRef<HTMLInputElement>()
    val idRef = createRef<HTMLInputElement>()
    val passwordRef = createRef<HTMLInputElement>()
    override fun RBuilder.render() {
        if (props.connection == undefined) {
            return
        }
        div {
            fun loginForm() {
                form {
                    attrs.onSubmitFunction = { event ->
                        event.preventDefault()
                        GlobalScope.launch {
                            penta.WSClient.login(
                                urlInput = urlRef.current!!.value ?: "",
                                userIdInput = idRef.current?.value ?: "",
                                passwordInput = passwordRef.current?.value ?: "",
                                dispatch = props.dispatchConnection
                            )
                        }
//                            launch({
//
//                            }) { throwable ->
//                                console.error("error; $throwable")
//                            }
                    }
                    input(type = InputType.url) {
                        ref = urlRef
                    }
                    input {
                        ref = idRef
                    }
                    if (props.connection is ConnectionState.RequiresPassword) {
                        input(type = InputType.password) {
                            ref = passwordRef
                        }
                    }
                    button(type = ButtonType.submit) {
                        +"Connect"
                    }
                }
            }
            fun disconnectButton() {
                button {
                    +"Disconnect"
                    attrs.onClickFunction = {
                        // TODO: kill current connections to server

                        props.dispatchConnection(
                            ConnectionState.Disconnected(
                                baseUrl = props.connection.baseUrl,
                                userId = props.connection.userId
                            )
                        )
                    }
                }
            }

            when (val state = props.connection) {
                is ConnectionState.NotLoggedIn -> {
                    loginForm()
                }
                is ConnectionState.RequiresPassword -> {
                    loginForm()
                }
                is ConnectionState.Authenticated -> {
//                    button {
//                        +"Connect to lobby"
//                        attrs.onClickFunction = {
//                            GlobalScope.launch {
//                                penta.WSClient.connectToLobby(
//                                    state = state,
//                                    dispatch = props.dispatchConnection
//                                )
//                            }
//                        }
//                    }
                    disconnectButton()
                    button {
                        +"Create Game"
                        attrs.onClickFunction = {
                            GlobalScope.launch {
                                penta.WSClient.createGameAndConnect(
                                    state,
                                    props.dispatchConnection,
                                    props.dispatchNotation,
                                    props.dispatchNewBoardstate
                                )
                            }
                        }
                    }
                    GlobalScope.promise {
                        penta.WSClient.listGames(
                            state,
                            props.dispatchConnection
                        )
                    }.then { games ->
                        console.info("received games:", games)
                        +"Games:"
                        br{}
                        ol {
                            games.forEachIndexed { index, gameSessionInfo ->
                                li {
                                    div {
                                        +"id: ${gameSessionInfo.id}"
                                        +"owner: ${gameSessionInfo.owner}"
                                        +"players: ${gameSessionInfo.players}"
                                        +"observers: ${gameSessionInfo.observers}"
                                        +"running: ${gameSessionInfo.running}"
                                        button {
                                            +"Join"
                                            attrs.onClickFunction = {
                                                GlobalScope.launch {
                                                    penta.WSClient.connectToGame(
                                                        state,
                                                        gameSessionInfo,
                                                        props.dispatchConnection,
                                                        props.dispatchNotation,
                                                        props.dispatchNewBoardstate
                                                    )
                                                }

                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
//                is ConnectionState.Lobby -> {
//                    // TODO: store list of games in store/section
//                    val games = GlobalScope.promise {
//                         penta.WSClient.listGames(
//                            state,
//                            props.dispatchConnection
//                        )
//                    }
//                    games.then {
//                        ol {
//                            it.forEachIndexed { index, gameSessionInfo ->
//                                li {
//                                    div {
//                                        +"id: ${gameSessionInfo.id}"
//                                        +"owner: ${gameSessionInfo.owner}"
//                                        +"players: ${gameSessionInfo.players}"
//                                        +"observers: ${gameSessionInfo.observers}"
//                                        +"running: ${gameSessionInfo.running}"
//                                        button {
//                                            +"Join"
//                                            attrs.onClickFunction = {
//                                                GlobalScope.launch {
//                                                    penta.WSClient.connectToGame(
//                                                        state,
//                                                        gameSessionInfo,
//                                                        props.dispatchConnection,
//                                                        props.dispatchNotation,
//                                                        props.dispatchNewBoardstate
//                                                    )
//                                                }
//
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//
//                        }
//                    }
//                }
                is ConnectionState.Observing -> {
                    disconnectButton()
                    div {
                        +"connected to ${state.game}"
                    }
                }
                else -> {
                    div {
                        +"TODO: Add Disconnect button"
        //                    button {
        //                        +"Disconnect"
        //                        attrs.onClickFunction = {
        //
        //                        }
        //                    }
                    }
                }
            }
            div {
                +props.connection.toString()
            }
        }
    }

    override fun componentDidMount() {
        console.log("setting values of inputs")
        urlRef.current?.value = props.connection.baseUrl.toString()
        idRef.current?.value = props.connection.userId
//        if (props.connection is ConnectionState.RequiresPassword) {
//            passwordRef.current?.value = props.password
//        }
    }

    override fun shouldComponentUpdate(nextProps: TextConnectionProps, nextState: RState): Boolean {
        console.log("shouldComponentUpdate")
        return true
    }
}

/**
 * parameter on callsite
 */
interface TextConnectionParameters : RProps {
}

//TODO: find a way to compose interface while keeping these private
interface TextConnectionStateProps : TextConnectionParameters {
    var connection: ConnectionState
}

interface TextConnectionDispatchProps : RProps {
    var dispatchConnection: (ConnectionState) -> Unit
    var dispatchNotation: (GameEvent) -> Unit
    var dispatchNewBoardstate: (BoardState) -> Unit
}

val textConnection =
    rConnect<State, Any, WrapperAction, TextConnectionParameters, TextConnectionStateProps, TextConnectionDispatchProps, TextConnectionProps>(
        { state, configProps ->
            console.debug("TextConnectionContainer.state")
            console.debug("state: ", state)
            console.debug("configProps: ", configProps)
            connection = state.connection
        },
        { dispatch, configProps ->
            // any kind of interactivity is linked to dispatching state changes here
            console.debug("TextConnectionContainer.dispatch")
            console.debug("dispatch: ", dispatch)
            console.debug("configProps: ", configProps)
            this.dispatchConnection = { dispatch(it) }
            this.dispatchNotation = { dispatch(it) }
            this.dispatchNewBoardstate = { dispatch(it) }
//            this.dispatch = { dispatch(Action(it)) }
        }
    )(TextConnection::class.js.unsafeCast<RClass<TextConnectionProps>>())