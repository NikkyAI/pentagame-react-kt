package components

import com.ccfraser.muirwik.components.MColor
import com.ccfraser.muirwik.components.MGridJustify
import com.ccfraser.muirwik.components.MGridSize
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.button.mIconButton
import com.ccfraser.muirwik.components.form.MFormControlVariant
import com.ccfraser.muirwik.components.form.mFormControl
import com.ccfraser.muirwik.components.list.mList
import com.ccfraser.muirwik.components.list.mListItem
import com.ccfraser.muirwik.components.list.mListItemIcon
import com.ccfraser.muirwik.components.list.mListItemSecondaryAction
import com.ccfraser.muirwik.components.list.mListItemText
import com.ccfraser.muirwik.components.mDivider
import com.ccfraser.muirwik.components.mGridContainer
import com.ccfraser.muirwik.components.mGridItem
import com.ccfraser.muirwik.components.mTextField
import com.ccfraser.muirwik.components.mTypography
import debug
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.promise
import kotlinx.css.flexGrow
import kotlinx.css.pct
import kotlinx.css.width
import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import penta.BoardState
import penta.ConnectionState
import penta.PentaMove
import penta.network.LobbyEvent
import penta.network.GameEvent
import penta.network.GameSessionInfo
import penta.redux.MultiplayerState
import react.RBuilder
import react.RClass
import react.RComponent
import react.RProps
import react.RState
import react.dom.form
import react.invoke
import react.redux.rConnect
import reducers.State
import redux.WrapperAction
import styled.css

interface TextConnectionProps : TextConnectionStateProps, TextConnectionDispatchProps

class TextConnection(props: TextConnectionProps) : RComponent<TextConnectionProps, RState>(props) {
    var urlValue = props.connection.baseUrl.toString()
    var idValue = ""
    var passwordValue = ""

    private fun <T> requestGameList(connection: T)
        where T : ConnectionState, T : ConnectionState.HasSession
    {
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

    private fun RBuilder.urlInput() {
        mTextField(
            type = InputType.url,
            label = "Url",
            variant = MFormControlVariant.filled,
            defaultValue = props.connection.baseUrl.toString(),
            onChange = { event: Event ->
                urlValue = (event.target as HTMLInputElement).value
            }
        ) {
            css {
                width = 100.pct
            }
        }
    }

    private fun RBuilder.userIdInput() {
        mTextField(
            label = "user id",
            variant = MFormControlVariant.filled,
            defaultValue = props.connection.userId,
            onChange = { event ->
                idValue = (event.target as HTMLInputElement).value
            }
        ) {
            css {
                width = 100.pct
            }
        }
    }

    private fun RBuilder.passwordInput() {
        mTextField(
            label = "password",
            type = InputType.password,
            variant = MFormControlVariant.filled,
            onChange = { event ->
                passwordValue = (event.target as HTMLInputElement).value
            }
        ) {
            css {
                width = 100.pct
            }
        }
    }

    private fun RBuilder.loginSubmitButton() {
        mButton(
            caption = "Login",
            variant = MButtonVariant.outlined,
            type = ButtonType.submit,
            color = MColor.primary
        )
    }

    private fun RBuilder.disconnectButton(connection: ConnectionState) {
        mButton(
            caption = "Disconnect",
            variant = MButtonVariant.outlined,
            color = MColor.secondary,
            onClick = { event ->
                // TODO: kill current connection to server
                when (connection) {
                    is ConnectionState.ConnectedToGame -> {
                        GlobalScope.launch {
                            connection.leave()
                        }
                    }
                    is ConnectionState.Lobby -> {
                        GlobalScope.launch {
                            connection.disconnect()
                        }
                    }
                }
                props.dispatchConnection(
                    ConnectionState.Disconnected(
                        baseUrl = props.connection.baseUrl,
                        userId = props.connection.userId
                    )
                )
            }
        )
    }

    private fun loginFunction(event: Event) {
        event.preventDefault()
        GlobalScope.launch {
            val nextState = penta.WSClient.login(
                urlInput = urlValue,
                userIdInput = idValue,
                passwordInput = passwordValue,
                dispatch = props.dispatchConnection
            )
            when (nextState) {
                is ConnectionState.Authenticated -> {
                    penta.WSClient.connectToLobby(
                        state = nextState,
                        dispatch = props.dispatchConnection,
                    dispatchLobbyEvent = props.dispatchLobbyEvent
                    )
//                    requestGameList(connection = nextState)
                }
            }
        }
    }

    override fun RBuilder.render() {
        if (props.connection == undefined) {
            return
        }
        when (val state = props.connection) {
            is ConnectionState.Disconnected -> {
                mFormControl {
                    form {
                        attrs.onSubmitFunction = ::loginFunction
                        mGridContainer(
                            justify = MGridJustify.center
                        ) {
                            css {
                                flexGrow = 1.0
                            }
                            mGridItem(xs = MGridSize.cells12) {
                                urlInput()
                            }
                            mGridItem(xs = MGridSize.cells12) {
                                userIdInput()
                            }
                            mGridItem(xs = MGridSize.cells12) {
                                loginSubmitButton()
                            }
                        }
                    }
                }
            }
            is ConnectionState.RequiresPassword -> {
                mFormControl {
                    form {
                        attrs.onSubmitFunction = ::loginFunction
                        mGridContainer(
                            justify = MGridJustify.center
                        ) {
                            mGridItem(xs = MGridSize.cells12) {
                                urlInput()
                            }
                            mGridItem(xs = MGridSize.cells12) {
                                userIdInput()
                            }
                            mGridItem(xs = MGridSize.cells12) {
                                passwordInput()
                            }
                            mGridItem(xs = MGridSize.cells12) {
                                loginSubmitButton()
                            }
                        }
                    }
                }
            }
            is ConnectionState.Lobby -> {
                mGridContainer {
                    mGridItem(xs = MGridSize.cells4) {
                        disconnectButton(state)
                    }
                    mGridItem(xs = MGridSize.cells4) {
                        mButton(
                            caption = "Create Game",
                            variant = MButtonVariant.contained,
                            color = MColor.primary,
                            onClick = {
                                GlobalScope.launch {
                                    penta.WSClient.createGameAndConnect(
                                        state,
                                        props.dispatchConnection,
                                        props.dispatchNotationLocal,
                                        props.dispatchNewBoardstate
                                    )
                                }
                            }
                        )
                    }
                }

                mList {
                    mListItem("Games")
                    mDivider()
                    props.games.forEach { (id, gameSessionInfo) ->
                        mListItem("id", gameSessionInfo.id) {
                            mListItemText("owner", gameSessionInfo.owner)
                            mListItemText("players", "${gameSessionInfo.playingUsers}")
                            mListItemText("observers", "${gameSessionInfo.observers}")
                            mListItemText("running", "${gameSessionInfo.running}")
                            mListItemSecondaryAction {
                                mIconButton(
                                    iconName = "send",
                                    onClick = { event ->
                                        // join
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
                                )
                            }
                        }
                    }
                    mDivider()
                    mListItem(
                        button = true,
                        onClick = { requestGameList(connection = state) }
                    ) {
                        mListItemIcon(iconName = "refresh")
                        mListItemText("Update Games List")
                    }
                }
            }
            is ConnectionState.ConnectedToGame -> {
                mGridContainer {
//                    mGridItem(xs = MGridSize.cells3) {
//                        disconnectButton(state)
//                    }
                    mGridItem(xs = MGridSize.cells3) {
                        mButton(
                            caption = "Leave",
                            variant = MButtonVariant.outlined,
                            color = MColor.secondary,
                            onClick = {
                                GlobalScope.launch {
                                    state.leave()
                                }
                            }
                        )
                    }
                    mGridItem(xs = MGridSize.cells12) {
                        mTypography("Connected to ...${state.game}")
                    }
                }
            }
            else -> {
                mTypography("TODO: Add Disconnect button")
                mTypography("TODO: Implement view for $state")
            }
        }
        mTypography(props.connection.toString())
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
interface TextConnectionParameters : RProps

//TODO: find a way to compose interface while keeping these private
interface TextConnectionStateProps : TextConnectionParameters {
    var boardState: BoardState
    var connection: ConnectionState
    var games: Map<String, GameSessionInfo>
}

interface TextConnectionDispatchProps : RProps {
    var dispatchConnection: (ConnectionState) -> Unit
    var dispatchMultiplayerAction: (MultiplayerState.Companion.Actions) -> Unit
    var dispatchMoveLocal: (PentaMove) -> Unit
    var dispatchNotationLocal: (GameEvent) -> Unit
    var dispatchNewBoardstate: (BoardState) -> Unit
    var dispatchLobbyEvent: (LobbyEvent) -> Unit
}

val textConnection =
    rConnect<State, Any, WrapperAction, TextConnectionParameters, TextConnectionStateProps, TextConnectionDispatchProps, TextConnectionProps>(
        { state, configProps ->
            console.debug("TextConnectionContainer.state")
            console.debug("state: ", state)
            console.debug("configProps: ", configProps)
            boardState = state.boardState
            connection = state.multiplayerState.connectionState
            games = state.multiplayerState.lobby.games
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
            this.dispatchLobbyEvent = { dispatch(it) }
//            this.dispatch = { dispatch(Action(it)) }
        }
    )(TextConnection::class.js.unsafeCast<RClass<TextConnectionProps>>())
