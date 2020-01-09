package containers

import actions.Action
import components.PentaViz
import components.PentaVizProps
import io.data2viz.viz.Viz
import kotlinext.js.getOwnPropertyNames
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
    var relay: (PentaMove) -> Unit
}

val pentaViz =
    rConnect<State, Action<PentaMove>, WrapperAction, PentaVizParameters, PentaVizStateProps, PentaVizDispatchProps, PentaVizProps>(
        { state, configProps ->
            println("PentaViz update state")
            println("state: $state ")
            println("configProps: ${configProps::class.js.getOwnPropertyNames()} ")
            boardState = state.boardState
        },
        { dispatch, configProps ->
            // any kind of interactivity is linked to dispatching state changes here
            println("PentaViz update dispatch")
            println("dispatch: $dispatch ")
            println("configProps: $configProps ")
//            startGameClick = { dispatch(Action(PentaMove.InitGame)) }
//            addPlayerClick = { dispatch(Action(PentaMove.PlayerJoin(PlayerState("local2", "square")))) }
            relay = { dispatch(Action(it)) }
        }
    )(PentaViz::class.js.unsafeCast<RClass<PentaVizProps>>())

