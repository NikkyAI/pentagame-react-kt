package components

import containers.pentaViz
import io.data2viz.color.Colors
import io.data2viz.viz.viz
import react.RBuilder
import react.dom.div
import react.dom.h1

fun RBuilder.app() {
    div {
        h1 {
            +"Kotlin React + React-Dom + Redux + React-Redux"
        }
        textBoardState {

        }

//        vizCanvas {
//            attrs.id = "viz"
//            attrs.viz = viz {
//                circle {
//                    x = 5.0
//                    y = 5.0
//                    radius = 5.0
//                    fill = Colors.Web.blue
//                }
//            }
//        }

        pentaViz {

        }

//                    navLink(TODO_LIST_PATH) {
//                        +"Go to todo list"
//                    }
    }
}
