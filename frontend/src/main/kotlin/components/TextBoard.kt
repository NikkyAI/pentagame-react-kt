package components

import debug
import kotlinx.html.js.onClickFunction
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

interface TextBoardPropsTextBoard : TextBoardStateProps, TextBoardDispatchProps {
//    var boardState: BoardState
//    var addPlayerClick: () -> Unit
//    var startGameClick: () -> Unit
}

class TextBoard(props: TextBoardPropsTextBoard) : RComponent<TextBoardPropsTextBoard, RState>(props) {
    override fun RBuilder.render() {
        if (props.boardState == undefined) {
            return
        }
        div {

            div {
                val localSymbols = listOf("triangle", "square", "cross", "circle")
                localSymbols.forEach { symbol ->
                    button {
                        +"add $symbol"
                        attrs.onClickFunction = {
                            val playerCount = props.boardState.players.size
                            props.addPlayerClick("local$playerCount", symbol)
                        }
                    }
                }
            }
            div {
                button {
                    +"start game"
                    attrs.onClickFunction = { props.startGameClick() }
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
}

interface TextBoardDispatchProps : RProps {
    var addPlayerClick: (playerId: String, figureId: String) -> Unit
    var startGameClick: () -> Unit
    var relay: (PentaMove) -> Unit
}

val textBoardState =
    rConnect<State, PentaMove, WrapperAction, TextBoardsStateParameters, TextBoardStateProps, TextBoardDispatchProps, TextBoardPropsTextBoard>(
        { state, configProps ->
            console.debug("TextBoardContainer.state")
            console.debug("state: ", state)
            console.debug("configProps: ", configProps)
            boardState = state.boardState
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
            startGameClick = { dispatch(PentaMove.InitGame) }
            addPlayerClick = { playerId: String, figureId: String ->
                dispatch(PentaMove.PlayerJoin(PlayerState(playerId, figureId)))
            }
            relay = { dispatch(it) }
        }
    )(TextBoard::class.js.unsafeCast<RClass<TextBoardPropsTextBoard>>())