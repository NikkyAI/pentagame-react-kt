package components

import com.ccfraser.muirwik.components.MColor
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.mTextField
import com.ccfraser.muirwik.components.persist
import debug
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.promise
import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import penta.ConnectionState
import penta.PentaMove
import penta.SerialNotation
import penta.network.GameSessionInfo
import penta.redux.MultiplayerState
import penta.BoardState
import react.*
import react.dom.*
import react.redux.rConnect
import reducers.State
import redux.WrapperAction

interface TextConnectionProps : TextConnectionStateProps, TextConnectionDispatchProps

class TextConnection(props: TextConnectionProps) : RComponent<TextConnectionProps, RState>(props) {
    var urlValue =props.connection.baseUrl.toString()
    var idValue = ""
    var passwordValue = ""

    fun requestGameList(connection: ConnectionState.Authenticated) {
        GlobalScope.promise {
            penta.WSClient.listGames(
                connection,
                props.dispatchConnection
            )
        }.then { games ->
            console.info("received games:", games)
            props.dispatchMultiplayerAction(MultiplayerState.Companion.Actions.SetGames(games))
        }
    }

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
                                urlInput = urlValue,
                                userIdInput = idValue,
                                passwordInput = passwordValue,
                                dispatch = props.dispatchConnection
                            )
                        }
                    }
                    mTextField(
                        type = InputType.url,
                        label = "Url",
                        defaultValue = props.connection.baseUrl.toString(),
                        onChange = { event ->
                            urlValue = (event.target as HTMLInputElement).value
                        }
                    )
                    mTextField(
                        label = "user id",
                        defaultValue = props.connection.userId,
                        onChange = { event ->
                            idValue = (event.target as HTMLInputElement).value
                        }
                    )
                    if (props.connection is ConnectionState.RequiresPassword) {
                        mTextField(
                            label = "password",
                            type = InputType.password,
                            onChange = { event ->
                                passwordValue = (event.target as HTMLInputElement).value
                            }
                        )
                    }
                    mButton(
                        caption = "Submit",
                        variant = MButtonVariant.contained,
                        color = MColor.primary,
                        onClick = { event ->
                            GlobalScope.launch {
                                penta.WSClient.login(
                                    urlInput = urlValue,
                                    userIdInput = idValue,
                                    passwordInput = passwordValue,
                                    dispatch = props.dispatchConnection
                                )
                            }
                        }
                    )
                }
            }

            fun disconnectButton() {
                mButton(
                    caption = "Disconnect",
                    variant = MButtonVariant.contained,
                    color = MColor.secondary,
                    onClick = { event ->
                        // TODO: kill current connections to server

                        props.dispatchConnection(
                            ConnectionState.Disconnected(
                                baseUrl = props.connection.baseUrl,
                                userId = props.connection.userId
                            )
                        )
                    }

                )
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
                                    props.dispatchNotationLocal,
                                    props.dispatchNewBoardstate
                                )
                            }
                        }
                    }

                    +"Games:"
                    br {}
                    ol {
                        props.games.forEachIndexed { index, gameSessionInfo ->
                            li {
                                span {
                                    +"id: ${gameSessionInfo.id} "
                                    +"owner: ${gameSessionInfo.owner} "
                                    +"players: ${gameSessionInfo.players} "
                                    +"observers: ${gameSessionInfo.observers} "
                                    +"running: ${gameSessionInfo.running} "
                                    button {
                                        +"Join"
                                        attrs.onClickFunction = {
                                            GlobalScope.launch {
                                                penta.WSClient.connectToGame(
                                                    state,
                                                    gameSessionInfo,
                                                    props.dispatchConnection,
                                                    props.dispatchNotationLocal,
                                                    props.dispatchNewBoardstate
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    br{}
                    button {
                        +"Update List"
                        attrs.onClickFunction = {
                            requestGameList(connection = state)
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
                    button {
                        +"Leave"
                        attrs.onClickFunction = {
                            GlobalScope.launch {
                                state.leave()
                            }
                        }

                    }
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
//        urlRef.current?.value = props.connection.baseUrl.toString()
//        idRef.current?.value = props.connection.userId

        // update game list

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
    var boardState: BoardState
    var connection: ConnectionState
    var games: List<GameSessionInfo>
}

interface TextConnectionDispatchProps : RProps {
    var dispatchConnection: (ConnectionState) -> Unit
    var dispatchMultiplayerAction: (MultiplayerState.Companion.Actions) -> Unit
    var dispatchMoveLocal: (PentaMove) -> Unit
    var dispatchNotationLocal: (SerialNotation) -> Unit
    var dispatchNewBoardstate: (BoardState) -> Unit
}

val textConnection =
    rConnect<State, Any, WrapperAction, TextConnectionParameters, TextConnectionStateProps, TextConnectionDispatchProps, TextConnectionProps>(
        { state, configProps ->
            console.debug("TextConnectionContainer.state")
            console.debug("state: ", state)
            console.debug("configProps: ", configProps)
            boardState = state.boardState
            connection = state.multiplayerState.connectionState
            games = state.multiplayerState.games
        },
        { dispatch, configProps ->
            // any kind of interactivity is linked to dispatching state changes here
            console.debug("TextConnectionContainer.dispatch")
            console.debug("dispatch: ", dispatch)
            console.debug("configProps: ", configProps)
            this.dispatchConnection = { dispatch(it) }
            this.dispatchMultiplayerAction = { dispatch(it) }
            this.dispatchMoveLocal = { dispatch(it) }
            this.dispatchNotationLocal = { dispatch(it) }
            this.dispatchNewBoardstate = { dispatch(it) }
//            this.dispatch = { dispatch(Action(it)) }
        }
    )(TextConnection::class.js.unsafeCast<RClass<TextConnectionProps>>())