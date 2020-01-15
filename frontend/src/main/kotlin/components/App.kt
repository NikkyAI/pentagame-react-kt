package components

import containers.pentaSvg
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.Overflow
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.flexGrow
import kotlinx.css.flexShrink
import kotlinx.css.height
import kotlinx.css.maxHeight
import kotlinx.css.maxWidth
import kotlinx.css.overflow
import kotlinx.css.pct
import kotlinx.css.vh
import kotlinx.css.width
import react.RBuilder
import styled.css
import styled.styledDiv
//import com.ccfraser.muirwik.components.*

fun RBuilder.app() {
    styledDiv {
        css {
            maxWidth = 100.pct
            display = Display.flex
            flexDirection = FlexDirection.row
        }
        styledDiv {
            css {
                maxWidth = 100.pct
                maxHeight = 100.vh
                flexGrow = 0.5
                flexShrink = 0.5
//                alignSelf = Align.stretch
                width = 100.pct
                height = 100.pct
            }
            pentaSvg {}
        }
//        h1 {
//            +"Pentagame"
//        }
//        h2 {
//            +"Kotlin React + React-Dom + Redux + React-Redux"
//        }
//        mTabs {
//            mTab {
//                +"content1"
//            }
//            mTab {
//                +"content2"
//            }
//        }

        styledDiv {
            css {
                maxWidth = 100.pct
                display = Display.flex
                flexDirection = FlexDirection.column
//                flexGrow = 0.5
//                flexShrink = 0.5

                // make scrolling possible
                maxHeight = 100.vh
                overflow = Overflow.auto
            }
            styledDiv {
                css {
                    maxWidth = 100.pct
                    flexGrow = 0.0
                    flexShrink = 0.0
                }
                textConnection {}
            }
            styledDiv {
                css {
                    maxWidth = 100.pct
                    flexGrow = 0.0
                    flexShrink = 0.0
                }
                textBoardState {}
            }
        }
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
