package components

import containers.pentaSvg
import react.RBuilder
import react.dom.div
import react.dom.h1
import react.dom.h2

fun RBuilder.app() {
    div {
        h1 {
            +"Pentagaame"
        }
        h2 {
            +"Kotlin React + React-Dom + Redux + React-Redux"
        }
        pentaSvg {}
        textConnection {}
        textBoardState {}

//        vizCanvas {
//            attrs.id = "demo_viz"
//            attrs.viz = viz {
//                circle {
//                    x = 5.0
//                    y = 5.0
//                    radius = 5.0
//                    fill = Colors.Web.blue
//                }
//            }
//        }


//        pentaViz{
//            +"hallo"
//        }

//                    navLink(TODO_LIST_PATH) {
//                        +"Go to todo list"
//                    }
    }
}
