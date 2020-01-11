package containers

import actions.Action
import components.PentaViz
import components.PentaVizProps
import penta.PentaMove
import penta.redux_rewrite.BoardState
import react.RClass
import react.RProps
import react.invoke
import react.redux.rConnect
import reducers.State
import redux.WrapperAction

interface PentaVizParameters : RProps {

}

interface PentaVizStateProps : RProps {
    var boardState: BoardState
}

interface PentaVizDispatchProps : RProps {
    var dispatch: (PentaMove) -> Unit
}

val pentaViz =
    rConnect<State, Action<PentaMove>, WrapperAction, PentaVizParameters, PentaVizStateProps, PentaVizDispatchProps, PentaVizProps>(
        { state, configProps ->
            console.log("PentaViz update state")
            console.log("state: $state ")
            console.log("configProps: $configProps ")
            console.log("configProps: ${configProps::class.js} ")
            boardState = state.boardState

            // todo: trigger redraw here
        },
        { dispatch, configProps ->
            // any kind of interactivity is linked to dispatching state changes here
            console.log("PentaViz update dispatch")
            console.log("dispatch: $dispatch ")
            console.log("configProps: $configProps ")
//            startGameClick = { dispatch(Action(PentaMove.InitGame)) }
//            addPlayerClick = { dispatch(Action(PentaMove.PlayerJoin(PlayerState("local2", "square")))) }
            this.dispatch = { dispatch(Action(it)) }
        }
    )(PentaViz::class.js.unsafeCast<RClass<PentaVizProps>>())

