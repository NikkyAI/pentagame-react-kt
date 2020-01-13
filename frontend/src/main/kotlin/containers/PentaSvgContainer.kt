package containers

import actions.Action
import components.PentaSvg
import components.PentaSvgProps
import debug
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
            console.debug("PentaViz update state")
            console.debug("state:", state)
            console.debug("configProps: ", configProps)
            boardState = state.boardState

            // todo: trigger redraw here
        },
        { dispatch, configProps ->
            // any kind of interactivity is linked to dispatching state changes here
            console.debug("PentaSvg update dispatch")
            console.debug("dispatch: ", dispatch)
            console.debug("configProps: ", configProps)
            this@rConnect.dispatchBoardstate = { dispatch(Action(it)) }
            this@rConnect.dispatchConnection = { dispatch(Action(it)) }
        }
    )(PentaSvg::class.js.unsafeCast<RClass<PentaSvgProps>>())

