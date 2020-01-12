package containers

import actions.Action
import components.PentaSvg
import components.PentaSvgProps
import penta.PentaMove
import penta.redux_rewrite.BoardState
import react.RClass
import react.RProps
import react.invoke
import react.redux.rConnect
import reducers.State
import redux.WrapperAction

interface PentaSvgParameters : RProps {

}

interface PentaSvgStateProps : RProps {
    var boardState: BoardState
}

interface PentaSvgDispatchProps : RProps {
    var dispatchBoardstate: (PentaMove) -> Unit
    var dispatchConnection: (Any) -> Unit
}

val pentaSvg =
    rConnect<State, Action<*>, WrapperAction, PentaSvgParameters, PentaSvgStateProps, PentaSvgDispatchProps, PentaSvgProps>(
        { state, configProps ->
            console.log("PentaViz update state")
//            console.log("state: $state ")
            console.log("lastMove: ${state.boardState.history.lastOrNull()} ")
            console.log("configProps: ${configProps::class.js} ")
            boardState = state.boardState

            // todo: trigger redraw here
        },
        { dispatch, configProps ->
            // any kind of interactivity is linked to dispatching state changes here
            console.log("PentaViz update dispatch")
            console.log("dispatch: $dispatch ")
            console.log("configProps: $configProps ")
            this@rConnect.dispatchBoardstate = { dispatch(Action(it)) }
            this@rConnect.dispatchConnection = { dispatch(Action(it)) }
        }
    )(PentaSvg::class.js.unsafeCast<RClass<PentaSvgProps>>())

