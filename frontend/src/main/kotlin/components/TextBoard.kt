package components

import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.form.mFormControlLabel
import com.ccfraser.muirwik.components.list.mList
import com.ccfraser.muirwik.components.list.mListItem
import com.ccfraser.muirwik.components.mSwitch
import com.ccfraser.muirwik.components.mTypography
import com.ccfraser.muirwik.components.spacingUnits
import com.ccfraser.muirwik.components.table.mTable
import com.ccfraser.muirwik.components.table.mTableBody
import com.ccfraser.muirwik.components.table.mTableCell
import com.ccfraser.muirwik.components.table.mTableHead
import com.ccfraser.muirwik.components.table.mTableRow
import com.ccfraser.muirwik.components.transitions.mCollapse
import debug
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.css.Color
import kotlinx.css.backgroundColor
import kotlinx.css.margin
import kotlinx.serialization.list
import penta.BoardState
import penta.ConnectionState
import penta.PentaMove
import penta.PlayerState
import penta.UserInfo
import penta.network.GameEvent
import penta.util.json
import react.RBuilder
import react.RClass
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.invoke
import react.redux.rConnect
import react.setState
import reducers.State
import redux.WrapperAction
import styled.css
import styled.styledDiv

interface TextBoardProps : TextBoardStateProps, TextBoardDispatchProps {
//    var boardState: BoardState
//    var addPlayerClick: () -> Unit
//    var startGameClick: () -> Unit
}

class TextBoard(props: TextBoardProps) : RComponent<TextBoardProps, RState>(props) {
    var figureSwitch: Boolean = false
    var historySwitch: Boolean = false
    fun TextBoardProps.dispatchMove(move: PentaMove) {
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

    override fun RBuilder.render() {
        if (props.boardState == undefined) {
            return
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
            val lastMove = props.boardState.history.lastOrNull()
            if (lastMove != null) {
                mButton(
                    caption = "Undo ${lastMove.asNotation()}",
                    variant = MButtonVariant.outlined,
                    onClick = {
                        props.dispatchMove(
                            PentaMove.Undo(
                                moves = listOf(lastMove.toSerializable())
                            )
                        )
                    }
                ) {
                    css {
                        margin(1.spacingUnits)
                    }
                }
            } else {
                +"no move to undo"
            }
        }

        div {
            with(props.boardState) {
                mTypography("Players")
                mList {
                    props.playingUsers.forEach { (player, user) ->
                        mListItem(player.toString(), user.id + " " + user.figureId)
                    }
                }
                mTypography("turn: $turn")
                mTypography("currentPlayer: $currentPlayer")
                mTypography("selectedPlayerPiece: $selectedPlayerPiece")
                mTypography("selectedBlackPiece: $selectedBlackPiece")
                mTypography("selectedGrayPiece: $selectedGrayPiece")
                mTypography("selectingGrayPiece: $selectingGrayPiece")
                mTypography("gameStarted: $gameStarted")
//                mTypography("Figures")
                mFormControlLabel(
                    label = "Figures",
                    control = mSwitch(
                        checked = figureSwitch,
                        onChange = { event, state ->
                            setState {
                                figureSwitch = state
                            }
                        },
                        addAsChild = false
                    )
                )

                mCollapse(
                    show = figureSwitch
                ) {
                    mTable {
                        mTableHead {
                            mTableRow {
                                mTableCell { +"id" }
                                mTableCell { +"color" }
                                mTableCell { +"type" }
                                mTableCell { +"position" }
                            }
                        }
                        mTableBody {
                            figures.forEach {
                                mTableRow {
                                    mTableCell { +it.id }
                                    mTableCell {
                                        css {
                                            backgroundColor = Color(it.color.rgbHex)
                                        }
                                        mTypography(it.color.rgbHex)
                                    }
                                    mTableCell { +it::class.simpleName.toString() }
                                    mTableCell { +positions[it.id]?.id.toString() }
                                }
                            }
                        }
                    }
                }
//                mTypography("History")

                mFormControlLabel(
                    label = "History",
                    control = mSwitch(
                        checked = historySwitch,
                        onChange = { event, state ->
                            setState {
                                historySwitch = state
                            }
                        },
                        addAsChild = false
                    )
                )
                mCollapse(show = historySwitch) {
                    mTable {
                        mTableHead {
                            mTableRow {
                                mTableCell { +"noation" }
                                mTableCell { +"move" }
                            }
                        }
                        mTableBody {
                            history.forEach {
                                mTableRow {
                                    mTableCell { +it.asNotation() }
                                    mTableCell { +it.toString() }
                                }
                            }
                        }
                    }
                }
            }
            mTypography(props.boardState.toString())

            children()
        }
    }
}

/**
 * parameter on callsite
 */
interface TextBoardsStateParameters : RProps {
//    var size: Int
}

//TODO: find a way to compose interface while keeping these private
interface TextBoardStateProps : RProps {
    var state: State
    var boardState: BoardState
    var playingUsers: Map<PlayerState, UserInfo>
    var connection: ConnectionState
}

interface TextBoardDispatchProps : RProps {
    var dispatchMoveLocal: (PentaMove) -> Unit
}

val textBoardState =
    rConnect<State, PentaMove, WrapperAction, TextBoardsStateParameters, TextBoardStateProps, TextBoardDispatchProps, TextBoardProps>(
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
        }
    )(TextBoard::class.js.unsafeCast<RClass<TextBoardProps>>())