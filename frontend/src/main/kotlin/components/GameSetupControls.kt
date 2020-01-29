package components

import com.ccfraser.muirwik.components.MColor
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.mIcon
import com.ccfraser.muirwik.components.spacingUnits
import debug
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.css.margin
import kotlinx.serialization.list
import penta.BoardState
import penta.ConnectionState
import penta.PentaMove
import penta.PlayerState
import penta.network.GameEvent
import penta.util.json
import react.RBuilder
import react.RClass
import react.RComponent
import react.RProps
import react.RState
import react.invoke
import react.redux.rConnect
import reducers.State
import redux.WrapperAction
import styled.css
import styled.styledDiv

interface GameSetupProps : GameSetupStateProps, GameSetupDispatchProps {
//    var boardState: BoardState
//    var addPlayerClick: () -> Unit
//    var startGameClick: () -> Unit
}

private fun GameSetupProps.dispatchMove(move: PentaMove) {
    when (val c = connection) {
        is ConnectionState.ConnectedToGame -> {
            GlobalScope.launch {
                c.sendMove(move)
            }
        }
        else -> {
            dispatchMoveLocal(move)
        }
    }
}

class GameSetupControls(props: GameSetupProps) : RComponent<GameSetupProps, RState>(props) {

    override fun RBuilder.render() {
        if (props.boardState == undefined) {
            return
        }

        styledDiv {
            if (!props.boardState.gameStarted) {
                val conn = props.connection
                if (conn is ConnectionState.ConnectedToGame) {
                    val localSymbols = listOf("triangle", "square", "cross", "circle")
                    localSymbols.forEach { symbol ->
                        if (props.boardState.players.none { it.figureId == symbol || it.id == conn.userId }) {
                            mButton(
                                caption = "Join as $symbol",
                                variant = MButtonVariant.outlined,
                                startIcon = mIcon("add", addAsChild = false),
                                onClick = {
                                    props.dispatchMove(
                                        PentaMove.PlayerJoin(
                                            PlayerState(conn.userId, symbol)
                                        )
                                    )
                                }
                            ) {
                                css {
                                    margin(1.spacingUnits)
                                }
                            }
                        }
                    }
                } else {
                    val localSymbols = listOf("triangle", "square", "cross", "circle")
                    localSymbols.forEach { symbol ->
                        if (props.boardState.players.none { it.figureId == symbol }) {
                            mButton(
                                caption = "Add $symbol",
                                variant = MButtonVariant.outlined,
                                startIcon = mIcon("add", addAsChild = false),
                                onClick = {
                                    val playerCount = props.boardState.players.size
                                    props.dispatchMove(
                                        PentaMove.PlayerJoin(
                                            PlayerState(
                                                "local$playerCount",
                                                symbol
                                            )
                                        )
                                    )
                                }
                            ) {
                                css {
                                    margin(1.spacingUnits)
                                }
                            }
                        }
                    }
                }
                mButton(
                    caption = "Start Game",
                    variant = MButtonVariant.contained,
                    color = MColor.primary,
                    onClick = { props.dispatchMove(PentaMove.InitGame) }
                ) {
                    css {
                        margin(1.spacingUnits)
                    }
                }
            }
        }
        styledDiv {
            mButton(
                caption = "Export History",
                variant = MButtonVariant.outlined,
                onClick = {
                    val serializable = props.boardState.history.map { it.toSerializable() }
                    val serialized = json.toJson(GameEvent.serializer().list, serializable)
                    console.info("history: ", serialized.toString())
                    serializable.forEach {
                        console.info(it, json.toJson(GameEvent.serializer(), it).toString())
                    }
                }
            ) {
                css {
                    margin(1.spacingUnits)
                }
            }
        }

        children()
    }

    override fun shouldComponentUpdate(nextProps: GameSetupProps, nextState: RState): Boolean {
        console.debug("shouldComponentUpdate")
        return true
    }
}

/**
 * parameter on callsite
 */
interface GameSetupStateParameters : RProps {
//    var size: Int
}

//TODO: find a way to compose interface while keeping these private
interface GameSetupStateProps : RProps {
    var boardState: BoardState
    var connection: ConnectionState
}

interface GameSetupDispatchProps : RProps {
    var dispatchMoveLocal: (PentaMove) -> Unit
}

val gameSetupControls =
    rConnect<State, PentaMove, WrapperAction, GameSetupStateParameters, GameSetupStateProps, GameSetupDispatchProps, GameSetupProps>(
        { state, configProps ->
            console.debug("TextBoardContainer.state")
            console.debug("state: ", state)
            console.debug("configProps: ", configProps)
            boardState = state.boardState
            connection = state.multiplayerState.connectionState
        },
        { dispatch, configProps ->
            // any kind of interactivity is linked to dispatching state changes here
            console.debug("TextBoardContainer.dispatch")
            console.debug("dispatch: ", dispatch)
            console.debug("configProps: ", configProps)
//            startGameClick = { dispatch(Action(PentaMove.InitGame)) }
//            addPlayerClick = { playerId: String, figureId: String ->
//                dispatch(Action(PentaMove.PlayerJoin(PlayerState(playerId, figureId))))
//            }
//            relay = { dispatch(Action(it)) }
            dispatchMoveLocal = { dispatch(it) }
        }
    )(GameSetupControls::class.js.unsafeCast<RClass<GameSetupProps>>())