package components

import react.RBuilder
import react.dom.br
import react.dom.button
import react.dom.div
import react.dom.h1

fun RBuilder.app() {
    div {
        h1 {
            +"Kotlin React + React-Dom + Redux + React-Redux"
        }
        textBoardState {

        }

//                    navLink(TODO_LIST_PATH) {
//                        +"Go to todo list"
//                    }
    }
}
