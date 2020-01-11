package containers

import actions.Action
import components.PentaSvg
import components.PentaSvgProps
import kotlinext.js.getOwnPropertyNames
import penta.PentaMove
import penta.redux_rewrite.BoardState
import penta.util.json
import react.RClass
import react.RProps
import react.invoke
import react.redux.rConnect
import redux.WrapperAction

interface PentaSvgParameters : RProps {

}

interface PentaSvgStateProps : RProps {
    var boardState: BoardState
}

interface PentaSvgDispatchProps : RProps {
    var dispatch: (PentaMove) -> Unit
}

val pentaCanvas =
    rConnect<BoardState, Action<PentaMove>, WrapperAction, PentaSvgParameters, PentaSvgStateProps, PentaSvgDispatchProps, PentaSvgProps>(
        { state, configProps ->
            console.log("PentaViz update state")
//            console.log("state: $state ")
            console.log("lastMove: ${state.history.lastOrNull()} ")
            console.log("configProps: ${configProps::class.js} ")
            boardState = state

            // todo: trigger redraw here
        },
        { dispatch, configProps ->
            // any kind of interactivity is linked to dispatching state changes here
            console.log("PentaViz update dispatch")
            console.log("dispatch: $dispatch ")
            console.log("configProps: $configProps ")
//            startGameClick = { dispatch(Action(PentaMove.InitGame)) }
//            addPlayerClick = { dispatch(Action(PentaMove.PlayerJoin(PlayerState("local2", "square")))) }
            this@rConnect.dispatch = { dispatch(Action(it)) }
        }
    )(PentaSvg::class.js.unsafeCast<RClass<PentaSvgProps>>())

