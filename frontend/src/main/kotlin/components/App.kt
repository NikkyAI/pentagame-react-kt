package components

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.styles.Breakpoint
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
import react.dom.h1
import react.dom.h2
import styled.css
import styled.styledDiv

//import com.ccfraser.muirwik.components.*

fun RBuilder.app() {

    mGridContainer(spacing = MGridSpacing.spacing2) {
        css {
            flexGrow = 1.0
        }
        mGridItem(xs = MGridSize.cells6) {
            pentaSvg {}
        }
        mGridItem(xs = MGridSize.cells6) {
            h1 {
                +"Pentagame"
            }
            h2 {
                +"Kotlin React + React-Dom + Redux + React-Redux"
            }
            mDivider()
            textConnection {}
            mDivider()
            textBoardState {}
        }
    }
//
//    mContainer {
//
//
//        mContainer {
//
//            mPaper {
//
//            }
//
//        }
//    }



//
//    styledDiv {
//        css {
//            maxWidth = 100.pct
//            display = Display.flex
//            flexDirection = FlexDirection.row
//        }
//        styledDiv {
//            css {
//                maxWidth = 100.pct
//                maxHeight = 100.vh
//                flexGrow = 0.5
//                flexShrink = 0.5
////                alignSelf = Align.stretch
//                width = 100.pct
//                height = 100.pct
//            }
//        }

//        styledDiv {
//            css {
//                maxWidth = 100.pct
//                display = Display.flex
//                flexDirection = FlexDirection.column
////                flexGrow = 0.5
////                flexShrink = 0.5
//
//                // make scrolling possible
//                maxHeight = 100.vh
//                overflow = Overflow.auto
//            }
//            styledDiv {
//                css {
//                    maxWidth = 100.pct
//                    flexGrow = 0.0
//                    flexShrink = 0.0
//                }
//
//
//            }
//            styledDiv {
//                css {
//                    maxWidth = 100.pct
//                    flexGrow = 0.0
//                    flexShrink = 0.0
//                }
//                textBoardState {}
//            }
//        }
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
//    }
}
