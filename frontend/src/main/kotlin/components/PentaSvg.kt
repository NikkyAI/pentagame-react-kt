package components

import PentaBoard
import actions.Action
import com.ccfraser.muirwik.components.MColor
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import com.github.nwillc.ksvg.RenderMode
import com.github.nwillc.ksvg.elements.SVG
import debug
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.svg.SVGCircleElement
import org.w3c.dom.svg.SVGElement
import penta.BoardState
import penta.ConnectionState
import penta.PentaMove
import penta.PentagameClick
import penta.PlayerState
import penta.UserInfo
import react.RBuilder
import react.RClass
import react.RComponent
import react.RProps
import react.RState
import react.createRef
import react.invoke
import react.redux.rConnect
import reducers.State
import redux.WrapperAction
import styled.styledSvg
import util.drawPentagame
import util.forEach
import kotlin.dom.clear

interface PentaSvgProps : PentaSvgStateProps, PentaSvgDispatchProps

class PentaSvg(props: PentaSvgProps) : RComponent<PentaSvgProps, RState>(props) {
    private val svgRef = createRef<SVGElement>()

    fun dispatchMove(move: PentaMove) {
        when (val connection = props.connection) {
            is ConnectionState.ConnectedToGame -> {
//                when (move) {
//                    // unsupported by old backend
////                    is PentaMove.SelectGrey -> props.dispatchMoveLocal(move)
////                    is PentaMove.SelectPlayerPiece -> props.dispatchMoveLocal(move)
//                    else -> {
                GlobalScope.launch {
                    connection.sendMove(move)
                }
//                    }
//                }
            }
            else -> {
                props.dispatchMoveLocal(move)
            }
        }
    }

    override fun RBuilder.render() {
        styledSvg {
            ref = svgRef
            attrs {
                attributes["preserveAspectRatio"] = "xMidYMid meet"
            }
        }
    }

    override fun componentDidMount() {
        console.log("penta svg mounted")

        redraw(props)
    }

    override fun shouldComponentUpdate(nextProps: PentaSvgProps, nextState: RState): Boolean {
        // TODO update svg content here
        console.log("penta svg updating")
//        console.log("props: ${props.boardState}")
//        console.log("nextProps: ${nextProps.boardState}")

        // access isPlayBack
        when (val conn = nextProps.connection) {
            is ConnectionState.ConnectedToGame -> {
                if (conn.isPlayback) return false
            }
        }
        redraw(nextProps)

        return false
    }

    private fun redraw(svgProps: PentaSvgProps) {
        console.debug("drawing...")

        // does SVG stuff
        svgRef.current?.let { svg ->
            svg.clear()

            val scale = 1000

            val newSVG = SVG.svg {
                viewBox = "0 0 $scale $scale"

                drawPentagame(scale, svgProps.boardState, svgProps.connection, svgProps.playingUsers)
            }
//            val fullSvg = buildString {
//                newSVG.render(this, SVG.RenderMode.INLINE)
//            }
//            console.log("svg: ${fullSvg}")
            val svgInline = buildString {
                newSVG.children.forEach {
                    it.render(this, RenderMode.INLINE)
                }
            }
            svg.innerHTML = svgInline
            svg.setAttribute("viewBox", newSVG.viewBox!!)
        }

        val clickFields = { event: Event ->
            event as MouseEvent
            console.log("event $event ")
            console.log("target ${event.target} ")
            when (val target = event.target) {
                is SVGCircleElement -> {
                    target.classList.forEach { id ->
                        if (PentaBoard.fields.any { it.id == id }) {
                            // TODO: clickfield
                            console.log("clicked field $id")
                            PentagameClick.clickField(
                                PentaBoard.fields.firstOrNull { it.id == id }
                                    ?: throw IllegalStateException("target '${id}' cannot be found"),
                                ::dispatchMove,
                                svgProps.boardState
                            )
                            return@forEach
                        }
                    }
                }
            }
        }
        val clickPieces = { event: Event ->
            event as MouseEvent
            console.log("event $event ")
            console.log("target ${event.target} ")
            when (val target = event.target) {
                is Element -> {
                    target.classList.forEach { id ->
                        console.log("clicked piece ${id}")
                        PentagameClick.clickPiece(
                            props.boardState.figures.firstOrNull { it.id == id }
                                ?: throw IllegalStateException("target '$id' cannot be found"),
                            ::dispatchMove,
                            svgProps.boardState
                        )
                        return@forEach
                    }
                }
            }
            Unit
        }

        PentaBoard.fields.forEach {
            svgRef.current?.getElementsByClassName(it.id)?.item(0)
                ?.addEventListener("click", clickFields, true)
        }
        props.boardState.figures.forEach {
            svgRef.current?.getElementsByClassName(it.id)?.item(0)
                ?.addEventListener("click", clickPieces, true)
        }
    }
}

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
