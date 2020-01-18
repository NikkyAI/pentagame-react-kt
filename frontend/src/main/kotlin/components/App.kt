package components

import com.ccfraser.muirwik.components.MGridSize
import com.ccfraser.muirwik.components.MGridSpacing
import com.ccfraser.muirwik.components.MTabIndicatorColor
import com.ccfraser.muirwik.components.MTabOrientation
import com.ccfraser.muirwik.components.MTabTextColor
import com.ccfraser.muirwik.components.MTabVariant
import com.ccfraser.muirwik.components.MTypographyVariant
import com.ccfraser.muirwik.components.mGridContainer
import com.ccfraser.muirwik.components.mGridItem
import com.ccfraser.muirwik.components.mIcon
import com.ccfraser.muirwik.components.mTab
import com.ccfraser.muirwik.components.mTabs
import com.ccfraser.muirwik.components.mTypography
import com.ccfraser.muirwik.components.spacingUnits
import containers.pentaSvg
import kotlinx.css.flexGrow
import kotlinx.css.marginTop
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.setState
import styled.css

enum class Tabs {
    game, help, multiplayer, about
}

class App : RComponent<RProps, RState>() {
    var tabValue: Any = Tabs.game

    override fun RBuilder.render() {
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
                mGridContainer {
                    css {
                        marginTop = 3.spacingUnits
                        flexGrow = 1.0
                        /*backgroundColor = Color(theme.palette.background.paper)*/
                    }

                    mGridItem(xs = MGridSize.cells9) {
                        when (tabValue as Tabs) {
                            Tabs.help -> {
                                mTypography("Rules")
                            }
                            Tabs.multiplayer -> {
                                textConnection {}
                            }
                            Tabs.about ->  {
                                mTypography("About")
                            }
                            Tabs.game -> {
                                textBoardState {}
                            }
                        }
                    }
                    mGridItem(xs = MGridSize.cells3) {
                        mTabs(
                            tabValue,
                            variant = MTabVariant.scrollable,
                            textColor = MTabTextColor.primary,
                            indicatorColor = MTabIndicatorColor.primary,
                            orientation = MTabOrientation.vertical,
                            onChange = { _, value ->
                                setState {
                                    tabValue = value
                                }
                            }
                        ) {
                            mTab("Rules", Tabs.help, icon = mIcon("help", addAsChild = false))
                            mTab("Multiplayer", Tabs.multiplayer, icon = mIcon("people", addAsChild = false))
                            mTab("About", Tabs.about, icon = mIcon("info", addAsChild = false))
//                            mTab("Item Five", 4, icon = mIcon("shopping_basket", addAsChild = false))
//                            mTab("Item Six", 5, icon = mIcon("thumb_down", addAsChild = false))
//                            mTab("Item Seven", 6, icon = mIcon("thumb_up", addAsChild = false))
                            mTab("Game", Tabs.game, icon = mIcon("developer_mode", addAsChild = false))
                        }
                    }
                }
            }
        }
    }
}


fun RBuilder.app() = child(App::class) {}
