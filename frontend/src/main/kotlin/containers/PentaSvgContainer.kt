package containers

import actions.Action
import components.PentaSvg
import components.PentaSvgProps
import debug
import penta.PentaMove
import penta.BoardState
import penta.ConnectionState
import penta.PlayerState
import penta.UserInfo
import react.RClass
import react.RProps
import react.invoke
import react.redux.rConnect
import reducers.State
import redux.WrapperAction

interface PentaSvgParameters : RProps {

}

interface PentaSvgStateProps : RProps {
    var state: State
    var boardState: BoardState
    var playingUsers: Map<PlayerState, UserInfo>
    var connection: ConnectionState
}

interface PentaSvgDispatchProps : RProps {
    var dispatchMoveLocal: (PentaMove) -> Unit
    var dispatchConnection: (penta.ConnectionState) -> Unit
}

val pentaSvgInteractive =
    rConnect<State, Action<*>, WrapperAction, PentaSvgParameters, PentaSvgStateProps, PentaSvgDispatchProps, PentaSvgProps>(
        { state, configProps ->
            console.debug("PentaViz update state")
            console.debug("state:", state)
            console.debug("configProps: ", configProps)
            this.state = state
            boardState = state.boardState
            playingUsers = state.playingUsers
            connection = state.multiplayerState.connectionState
            // todo: trigger redraw here
        },
        { dispatch, configProps ->
            // any kind of interactivity is linked to dispatching state changes here
            console.debug("PentaSvg update dispatch")
            console.debug("dispatch: ", dispatch)
            console.debug("configProps: ", configProps)
            this@rConnect.dispatchMoveLocal = { dispatch(Action(it)) }
            this@rConnect.dispatchConnection = { dispatch(Action(it)) }
        }
    )(PentaSvg::class.js.unsafeCast<RClass<PentaSvgProps>>())

