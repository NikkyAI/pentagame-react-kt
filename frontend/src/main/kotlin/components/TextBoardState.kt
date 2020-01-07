package components

import actions.Action
import kotlinx.html.js.onClickFunction
import penta.PentaMove
import penta.PlayerState
import penta.redux_rewrite.BoardState
import react.RBuilder
import react.RClass
import react.RComponent
import react.RProps
import react.RState
import react.dom.button
import react.dom.div
import react.dom.li
import react.dom.ol
import react.dom.p
import react.invoke
import react.redux.rConnect
import reducers.State
import redux.WrapperAction

interface TextBoardStateProps : StateProps, DispatchProps {
//    var boardState: BoardState
//    var addPlayerClick: () -> Unit
//    var startGameClick: () -> Unit
}

class TextBoardState(props: TextBoardStateProps) : RComponent<TextBoardStateProps, RState>(props) {
    override fun RBuilder.render() {
        div {
            div {
                with(props.boardState) {
                    p {
                        +"players: $players"
                    }
                    p {
                        +"currentPlayer: $currentPlayer"
                    }
                    p {
                        +"gameStarted: $gameStarted"
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
                }
            }
            div {
                +props.boardState.toString()
            }


            div {
                button {
                    +"addplayer"
                    attrs.onClickFunction = { props.addPlayerClick() }
                }
            }
            div {
                button {
                    +"start game"
                    attrs.onClickFunction = { props.startGameClick() }
                }
            }


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
/*private*/ interface StateProps : RProps {
    var boardState: BoardState
}

/*private*/ interface DispatchProps : RProps {
    var addPlayerClick: () -> Unit
    var startGameClick: () -> Unit
    var relay: (PentaMove) -> Unit
}

val textBoardState
    = rConnect<State, Action<PentaMove>, WrapperAction, TextBoardsStateParameters, StateProps, DispatchProps, TextBoardStateProps>(
        { state, configProps ->
            println("TextBoardContainer.state")
            println("state: $state ")
            println("configProps: $configProps ")
            boardState = state.boardState
        },
        { dispatch, configProps ->
            // any kind of interactivity is linked to dispatching state changes here
            println("TextBoardContainer.dispatch")
            println("dispatch: $dispatch ")
            println("configProps: $configProps ")
            startGameClick = { dispatch(Action(PentaMove.InitGame)) }
            addPlayerClick = { dispatch(Action(PentaMove.PlayerJoin(PlayerState("local2", "square")))) }
            relay = { dispatch(Action(it)) }
        }
    )(TextBoardState::class.js.unsafeCast<RClass<TextBoardStateProps>>())