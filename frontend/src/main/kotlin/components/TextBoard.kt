package components

import debug
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.list
import penta.PentaMove
import penta.PlayerState
import penta.redux_rewrite.BoardState
import react.RBuilder
import react.RClass
import react.RComponent
import react.RProps
import react.RState
import react.dom.br
import react.dom.button
import react.dom.div
import react.dom.li
import react.dom.ol
import react.dom.p
import react.invoke
import react.redux.rConnect
import reducers.State
import redux.WrapperAction
import penta.ConnectionState
import penta.SerialNotation
import penta.util.json

interface TextBoardPropsTextBoard : TextBoardStateProps, TextBoardDispatchProps {
//    var boardState: BoardState
//    var addPlayerClick: () -> Unit
//    var startGameClick: () -> Unit
}

class TextBoard(props: TextBoardPropsTextBoard) : RComponent<TextBoardPropsTextBoard, RState>(props) {
    fun TextBoardPropsTextBoard.dispatchMove(move: PentaMove) {
        when(val c = connection) {
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

            if(!props.boardState.gameStarted){
                div {
                    when (val conn = props.connection) {
                        is ConnectionState.Observing -> {
                            val localSymbols = listOf("triangle", "square", "cross", "circle")
                            localSymbols.forEach { symbol ->
                                if(props.boardState.players.none { it.figureId == symbol }) {
                                    button {
                                        +"Join as $symbol"
                                        attrs.onClickFunction = {
                                            val playerCount = props.boardState.players.size
                                            props.dispatchMove(PentaMove.PlayerJoin(PlayerState(conn.userId, symbol)))
                                        }
                                    }
                                }
                            }
                        }
                        else -> {
                            val localSymbols = listOf("triangle", "square", "cross", "circle")
                            localSymbols.forEach { symbol ->
                                if(props.boardState.players.none { it.figureId == symbol }) {
                                    button {
                                        +"add $symbol"
                                        attrs.onClickFunction = {
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
                                    }
                                }
                            }
                        }
                    }
                }
                div {
                    button {
                        +"start game"
                        attrs.onClickFunction = { props.dispatchMove(PentaMove.InitGame) }
                    }
                }
            }

            div {
                button {
                    +"export history"
                    attrs.onClickFunction = {
                        val serializable = props.boardState.history.map { it.toSerializable() }
                        val serialized = json.toJson(SerialNotation.serializer().list, serializable)
                        console.info("history: ", serialized.toString())
                        serializable.forEach {
                            console.info(it, json.toJson(SerialNotation.serializer(), it).toString())
                        }
                    }
                }
            }

            div {
                with(props.boardState) {
                    div {
                        +"players: "
                        ol {
                            players.forEach {
                                li {
                                    +it.toString()
                                }
                            }
                        }
                    }
                    p {
                        +"currentPlayer: $currentPlayer"
                        br {}
                        +"selectedPlayerPiece: $selectedPlayerPiece"
                        br {}
                        +"selectedBlackPiece: $selectedBlackPiece"
                        br {}
                        +"selectedGrayPiece: $selectedGrayPiece"
                        br {}
                        +"selectingGrayPiece: $selectingGrayPiece"
                        br {}
                        +"gameStarted: $gameStarted"
                    }
                    div {
                        +"figures: "
                        ol {
                            figures.forEach {
                                li {
                                    +it.toString()
                                }
                            }
                        }
                    }

                    div {
                        +"history: "
                        ol {
                            history.forEach {
                                li {
                                    +it.asNotation()
                                }
                            }
                        }
                    }
                    div {
                        +"positions: "
                        ol {
                            positions.forEach { (id, field) ->
                                li {
                                    +"$id : ${field?.id}"
                                }
                            }
                        }
                    }
                }
            }
            div {
                +props.boardState.toString()
            }

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