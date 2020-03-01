package components

import SessionEvent
import com.ccfraser.muirwik.components.MColor
import com.ccfraser.muirwik.components.MDividerOrientation
import com.ccfraser.muirwik.components.MDividerVariant
import com.ccfraser.muirwik.components.MGridAlignItems
import com.ccfraser.muirwik.components.MTypographyVariant
import com.ccfraser.muirwik.components.button.MButtonGroupOrientation
import com.ccfraser.muirwik.components.button.MButtonGroupVariant
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.button.mButtonGroup
import com.ccfraser.muirwik.components.mDivider
import com.ccfraser.muirwik.components.mGridContainer
import com.ccfraser.muirwik.components.mIcon
import com.ccfraser.muirwik.components.mTypography
import com.ccfraser.muirwik.components.spacingUnits
import debug
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.css.margin
import penta.BoardState
import penta.ConnectionState
import penta.PentaMove
import penta.PlayerState
import penta.UserInfo
import penta.logic.GameType
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


private fun GameSetupProps.dispatchSessionEvent(sessionEvent: SessionEvent) {
    when (val c = connection) {
        is ConnectionState.ConnectedToGame -> {
            GlobalScope.launch {
                c.sendSessionEvent(sessionEvent)
            }
        }
        else -> {
            dispatchSesionEventLocal(sessionEvent)
        }
    }
}
private fun GameSetupProps.dispatchMove(move: PentaMove) {
    dispatchSessionEvent(SessionEvent.WrappedGameEvent(move.toSerializable()))
}

class GameSetupControls(props: GameSetupProps) : RComponent<GameSetupProps, RState>(props) {
    override fun RBuilder.render() {
        if (props.boardState == undefined) {
            return
        }

        styledDiv {
            //TODO: add reset game button

            if (!props.boardState.gameStarted) {
                GameType.values().forEach { type ->
                    if (type != props.boardState.gameType) {
                        mButton(
                            caption = "mode $type",
                            variant = MButtonVariant.contained,
                            //startIcon = mIcon("add", addAsChild = false),
                            onClick = {
                                props.dispatchMove(
                                    PentaMove.SetGameType(
                                        gameType = type
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
            val conn = props.connection
            if (conn is ConnectionState.ConnectedToGame) {
                if (!props.boardState.gameStarted) {
                    val localSymbols = listOf("triangle", "square", "cross", "circle")
                    localSymbols.forEach { symbol ->
                        props.boardState.gameType.players.forEach { player ->
                            if (props.playingUsers[player] == null) {
                                mButton(
                                    caption = "Join $player as $symbol",
                                    variant = MButtonVariant.outlined,
                                    startIcon = mIcon("add", addAsChild = false),
                                    onClick = {
                                        //                                        val username = if
                                        props.dispatchSessionEvent(
                                            SessionEvent.PlayerJoin(
                                                player = player,
                                                user = UserInfo(conn.userId, symbol)
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
                }
            } else { // local
                // add users
                props.boardState.gameType.players.forEach { player ->
                    if (props.playingUsers[player] == null) {
                        mGridContainer(
                            alignItems = MGridAlignItems.center
                        ) {
                            mIcon(iconName = "add")
                            mTypography(
                                text = "Add $player",
                                variant = MTypographyVariant.button
                            )

                            mButtonGroup(
                                color = MColor.primary,
                                variant = MButtonGroupVariant.outlined,
                                orientation = MButtonGroupOrientation.horizontal
                            ) {
                                val localSymbols = listOf("triangle", "square", "cross", "circle")
                                localSymbols.forEach { symbol ->
                                    mButton(
                                        caption = "as $symbol",
                                        onClick = {
                                            props.dispatchSessionEvent(
                                                SessionEvent.PlayerJoin(
                                                    player = player,
                                                    user = UserInfo("local_$player", symbol)
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                            css {
                                margin(1.spacingUnits)
                            }
                        }
                    }
                }
                // remove users
                props.playingUsers.forEach { (player, user) ->
                    mButton(
                        caption = "Remove $player ${user.figureId}",
                        startIcon = mIcon("clear", addAsChild = false),
                        color = MColor.secondary,
                        onClick = {
                            props.dispatchSessionEvent(
                                SessionEvent.PlayerLeave(
                                    player = player,
                                    user = user
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

            if (!props.boardState.gameStarted) {
                mButton(
                    caption = "Start Game ${props.boardState.gameType}",
                    variant = MButtonVariant.contained,
                    color = MColor.primary,
                    onClick = { props.dispatchMove(PentaMove.InitGame) }
                ) {
                    css {
                        margin(1.spacingUnits)
                    }
                }
//                GameType.values().forEach { gameType ->
//                    mButton(
//                        caption = "Start Game $gameType",
//                        variant = MButtonVariant.contained,
//                        color = MColor.primary,
//                        onClick = { props.dispatchMove(PentaMove.InitGame(gameType)) }
//                    ) {
//                        css {
//                            margin(1.spacingUnits)
//                        }
//                    }
//                }
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
    var state: State
    var boardState: BoardState
    var playingUsers: Map<PlayerState, UserInfo>
    var connection: ConnectionState
}

interface GameSetupDispatchProps : RProps {
    var dispatchMoveLocal: (PentaMove) -> Unit
    var dispatchSesionEventLocal: (SessionEvent) -> Unit
}

val gameSetupControls =
    rConnect<State, Any /*PentaMove*/, WrapperAction, GameSetupStateParameters, GameSetupStateProps, GameSetupDispatchProps, GameSetupProps>(
        { state, configProps ->
            console.debug("TextBoardContainer.state")
            console.debug("state: ", state)
            console.debug("configProps: ", configProps)
            this.state = state
            boardState = state.boardState
            playingUsers = state.playingUsers
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
            dispatchSesionEventLocal = { dispatch(it) }
        }
    )(GameSetupControls::class.js.unsafeCast<RClass<GameSetupProps>>())