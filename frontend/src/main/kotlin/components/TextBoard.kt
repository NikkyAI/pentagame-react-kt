package components

import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.button.mButtonGroup
import com.ccfraser.muirwik.components.list.mList
import com.ccfraser.muirwik.components.list.mListItem
import com.ccfraser.muirwik.components.list.mListItemText
import com.ccfraser.muirwik.components.mTypography
import com.ccfraser.muirwik.components.table.mTable
import com.ccfraser.muirwik.components.table.mTableBody
import com.ccfraser.muirwik.components.table.mTableCell
import com.ccfraser.muirwik.components.table.mTableHead
import com.ccfraser.muirwik.components.table.mTableRow
import debug
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.list
import penta.BoardState
import penta.ConnectionState
import penta.PentaMove
import penta.PlayerState
import penta.SerialNotation
import penta.util.json
import react.RBuilder
import react.RClass
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.invoke
import react.redux.rConnect
import reducers.State
import redux.WrapperAction

interface TextBoardPropsTextBoard : TextBoardStateProps, TextBoardDispatchProps {
//    var boardState: BoardState
//    var addPlayerClick: () -> Unit
//    var startGameClick: () -> Unit
}

class TextBoard(props: TextBoardPropsTextBoard) : RComponent<TextBoardPropsTextBoard, RState>(props) {
    fun TextBoardPropsTextBoard.dispatchMove(move: PentaMove) {
        when (val c = connection) {
            is ConnectionState.Observing -> {
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

        div {
            mButtonGroup {
                if (!props.boardState.gameStarted) {
                    when (val conn = props.connection) {
                        is ConnectionState.Observing -> {
                            if (props.boardState.players.none { it.id == conn.userId }) {
                                val localSymbols = listOf("triangle", "square", "cross", "circle")
                                localSymbols.forEach { symbol ->
                                    if (props.boardState.players.none { it.figureId == symbol }) {
                                        mButton(
                                            caption = "Join as $symbol",
                                            variant = MButtonVariant.outlined,
                                            onClick = {
                                                props.dispatchMove(
                                                    PentaMove.PlayerJoin(
                                                        PlayerState(
                                                            conn.userId,
                                                            symbol
                                                        )
                                                    )
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        else -> {
                            val localSymbols = listOf("triangle", "square", "cross", "circle")
                            localSymbols.forEach { symbol ->
                                if (props.boardState.players.none { it.figureId == symbol }) {
                                    mButton(
                                        caption = "Add $symbol",
                                        variant = MButtonVariant.outlined,
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
                                    )
                                }
                            }
                        }
                    }
                    mButton(
                        caption = "Start Game",
                        variant = MButtonVariant.outlined,
                        onClick = { props.dispatchMove(PentaMove.InitGame) }
                    )
                }

                mButton(
                    caption = "Export History",
                    variant = MButtonVariant.outlined,
                    onClick = {
                        val serializable = props.boardState.history.map { it.toSerializable() }
                        val serialized = json.toJson(SerialNotation.serializer().list, serializable)
                        console.info("history: ", serialized.toString())
                        serializable.forEach {
                            console.info(it, json.toJson(SerialNotation.serializer(), it).toString())
                        }
                    }
                )
            }
            with(props.boardState) {
                mTypography("Players")
                mList {
                    players.forEach {
                        mListItem(it.id, it.figureId)
                    }
                }
                mTypography("currentPlayer: $currentPlayer")
                mTypography("selectedPlayerPiece: $selectedPlayerPiece")
                mTypography("selectedBlackPiece: $selectedBlackPiece")
                mTypography("selectedGrayPiece: $selectedGrayPiece")
                mTypography("selectingGrayPiece: $selectingGrayPiece")
                mTypography("gameStarted: $gameStarted")
                mTypography("Figures")
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
                                mTableCell { +it.color.toString() }
                                mTableCell { +it::class.simpleName.toString() }
                                mTableCell { +positions[it.id]?.id.toString() }
                            }
                        }
                    }
                }
                mList {
                    figures.forEach {
                        mListItem("id", it.id) {
                            mListItemText("color", it.color.toString())
                            mListItemText("type", it::class.simpleName)
                            mListItemText("position", positions[it.id]?.id.toString())
                        }
                    }
                }
//                mTypography("Positions")
//                mList {
//                    positions.forEach { (id, field) ->
//                        mListItem(id, field?.id.toString())
//                    }
//                }
            }
            mTypography(props.boardState.toString())

            children()
        }
    }

//    override fun componentWillUpdate(nextProps: TextBoardStateProps, nextState: RState) {
//        console.log("componentWillUpdate")
//    }

    override fun componentDidUpdate(prevProps: TextBoardPropsTextBoard, prevState: RState, snapshot: Any) {
        console.log("componentDidUpdate")
    }

    override fun shouldComponentUpdate(nextProps: TextBoardPropsTextBoard, nextState: RState): Boolean {
        console.log("shouldComponentUpdate")
        return true
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
    var boardState: BoardState
    var connection: ConnectionState
}

interface TextBoardDispatchProps : RProps {
    var dispatchMoveLocal: (PentaMove) -> Unit
}

val textBoardState =
    rConnect<State, PentaMove, WrapperAction, TextBoardsStateParameters, TextBoardStateProps, TextBoardDispatchProps, TextBoardPropsTextBoard>(
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
    )(TextBoard::class.js.unsafeCast<RClass<TextBoardPropsTextBoard>>())