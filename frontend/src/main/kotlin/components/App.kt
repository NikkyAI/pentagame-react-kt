package components

import com.ccfraser.muirwik.components.*
import containers.pentaSvg
import kotlinx.css.flexGrow
import react.RBuilder
import react.dom.h1
import react.dom.h2
import styled.css
import styled.styledDiv

fun RBuilder.app() {
    mGridContainer(spacing = MGridSpacing.spacing2) {
        css {
            flexGrow = 1.0
        }
        mGridItem(xs = MGridSize.cells6) {
            pentaSvg {}
        }
        mGridItem(xs = MGridSize.cells6) {
            mTypography("Pentagame", variant = MTypographyVariant.h1)
//            mTypography("Kotlin React + React-Dom + Redux + React-Redux", variant = MTypographyVariant.h2)
            mDivider()
            textConnection {}
            mDivider()
            textBoardState {}
        }
    }
}
