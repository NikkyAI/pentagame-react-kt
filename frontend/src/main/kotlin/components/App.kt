package components

import com.ccfraser.muirwik.components.HRefOptions
import com.ccfraser.muirwik.components.MGridSize
import com.ccfraser.muirwik.components.MGridWrap
import com.ccfraser.muirwik.components.MTabIndicatorColor
import com.ccfraser.muirwik.components.MTabOrientation
import com.ccfraser.muirwik.components.MTabTextColor
import com.ccfraser.muirwik.components.MTabVariant
import com.ccfraser.muirwik.components.MTypographyVariant
import com.ccfraser.muirwik.components.mGridContainer
import com.ccfraser.muirwik.components.mGridItem
import com.ccfraser.muirwik.components.mIcon
import com.ccfraser.muirwik.components.mLink
import com.ccfraser.muirwik.components.mTab
import com.ccfraser.muirwik.components.mTabs
import com.ccfraser.muirwik.components.mTypography
import com.ccfraser.muirwik.components.spacingUnits
import com.ccfraser.muirwik.components.variant
import kotlinx.css.Position
import kotlinx.css.backgroundColor
import kotlinx.css.left
import kotlinx.css.marginTop
import kotlinx.css.padding
import kotlinx.css.position
import kotlinx.css.px
import kotlinx.css.top
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.br
import react.setState
import styled.css
import styled.styledDiv

enum class Tabs {
    help, multiplayer, about, debug_game
}

class App : RComponent<RProps, RState>() {
    var tabValue: Any = Tabs.help

    override fun RBuilder.render() {
        val leftWidthLandscape = "min(100vh, 50vw)"
        val leftWidthPortrait = "min(100vw, 50vh)"

        styledDiv {
            css {
                rule("@media (orientation: landscape)") {
                    put("left", "calc($leftWidthLandscape * 1.05)")
                }
                rule("@media (orientation: portrait)") {
                    put("top", "calc($leftWidthPortrait * 1.05)")
                }
                position = Position.absolute
            }

            mTypography("Pentagame", variant = MTypographyVariant.h2)
            gameSetupControls {}
            mGridContainer(wrap = MGridWrap.noWrap) {
                css {
                    marginTop = 3.spacingUnits
//                        flexGrow = 1.0
                    /*backgroundColor = Color(theme.palette.background.paper)*/
                }

                mGridItem(xs = MGridSize.cellsAuto) {
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
                        // TODO: add conditional tabs (game list)
                        mTab("Rules", Tabs.help, icon = mIcon("help", addAsChild = false))
                        mTab("Multiplayer", Tabs.multiplayer, icon = mIcon("people", addAsChild = false))
                        mTab("About", Tabs.about, icon = mIcon("info", addAsChild = false))
//                            mTab("Item Five", 4, icon = mIcon("shopping_basket", addAsChild = false))
//                            mTab("Item Six", 5, icon = mIcon("thumb_down", addAsChild = false))
//                            mTab("Item Seven", 6, icon = mIcon("thumb_up", addAsChild = false))
                        mTab("Debug Game", Tabs.debug_game, icon = mIcon("developer_mode", addAsChild = false))
                    }
                }

                mGridItem(xs = MGridSize.cellsTrue) {
                    styledDiv {
                        css {
                            padding = "0.5em"
                        }

                        when (tabValue as Tabs) {
                            Tabs.help -> {
                                mTypography("Rules", paragraph = true)
                                mTypography(text = null, paragraph = true) {
                                    mLink(
                                        text = "Illustated Rules (English)",
                                        hRefOptions = HRefOptions(
                                            href = "https://pentagame.org/pdf/Illustrated_Rules.pdf",
                                            targetBlank = true
                                        )
                                    ) {
                                        attrs.variant = MTypographyVariant.button
                                    }
                                }

                                mTypography(text = null, paragraph = true) {
                                    mLink(
                                        text = "Illustated Rules (German)",
                                        hRefOptions = HRefOptions(
                                            href = "https://pentagame.org/pdf/Illustrated_Rules__German_.pdf",
                                            targetBlank = true
                                        )
                                    ) {
                                        attrs.variant = MTypographyVariant.button
                                    }
                                }
                            }
                            Tabs.multiplayer -> {
                                textConnection {}
                            }
                            Tabs.about -> {
                                mTypography("About")
                                mLink(
                                    text = "About Pentagame",
                                    hRefOptions = HRefOptions(
                                        href = "https://pentagame.org/",
                                        targetBlank = true
                                    )
                                ) {
                                    attrs.variant = MTypographyVariant.button
                                }
                                br {}
                                mLink(
                                    text = "Github",
                                    hRefOptions = HRefOptions(
                                        href = "https://github.com/NikkyAI/pentagame",
                                        targetBlank = true
                                    )
                                ) {
                                    attrs.variant = MTypographyVariant.button
                                }
                            }
                            Tabs.debug_game -> {
                                textBoardState {}
                            }
                        }
                    }
                }
            }
        }

        styledDiv {
            css {
                left = 0.px
                top = 0.px
                position = Position.fixed

                rule("@media (orientation: landscape)") {
                    put("height", leftWidthLandscape)
                    put("width", leftWidthLandscape)
                }
                rule("@media (orientation: portrait)") {
                    put("height", leftWidthPortrait)
                    put("width", leftWidthPortrait)
                }

                backgroundColor = kotlinx.css.Color.white
            }
            pentaSvgInteractive {}
        }

/*
        mGridContainer(
            spacing = MGridSpacing.spacing2,
            wrap = MGridWrap.wrap,
            alignItems = MGridAlignItems.stretch
        ) {
            //            css {
//                flexGrow = 1.0
//            }
            mGridItem(xs = MGridSize.cellsAuto) {
                css {
                    position = Position.fixed
                    flexGrow = 1.0
//                    height = 100.pct
//                    minWidth = 50.vh
//                    minHeight = 50.vh
                    maxWidth = 90.vh // TODO: only `width` has any effect
                    maxHeight = 100.vh
                }
                pentaSvgInteractive {}
//                styledDiv {
//                    css {
//                        maxHeight = 100.vh
////                        maxWidth = 100.vh
//                    }
//                }
            }
            mGridItem(xs = MGridSize.cellsAuto) {
                mTypography("Pentagame", variant = MTypographyVariant.h2)
                gameSetupControls {}
//            mTypography("Kotlin React + React-Dom + Redux + React-Redux", variant = MTypographyVariant.h2)
                mGridContainer(wrap = MGridWrap.noWrap) {
                    css {
                        marginTop = 3.spacingUnits
//                        flexGrow = 1.0
                        *//*backgroundColor = Color(theme.palette.background.paper)*//*
                    }

                    mGridItem(xs = MGridSize.cellsAuto) {
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
                            // TODO: add conditional tabs (game list)
                            mTab("Rules", Tabs.help, icon = mIcon("help", addAsChild = false))
                            mTab("Multiplayer", Tabs.multiplayer, icon = mIcon("people", addAsChild = false))
                            mTab("About", Tabs.about, icon = mIcon("info", addAsChild = false))
//                            mTab("Item Five", 4, icon = mIcon("shopping_basket", addAsChild = false))
//                            mTab("Item Six", 5, icon = mIcon("thumb_down", addAsChild = false))
//                            mTab("Item Seven", 6, icon = mIcon("thumb_up", addAsChild = false))
                            mTab("Debug Game", Tabs.debug_game, icon = mIcon("developer_mode", addAsChild = false))
                        }
                    }

                    mGridItem(xs = MGridSize.cellsTrue) {
                        styledDiv {
                            css {
                                padding = "0.5em"
                            }

                            when (tabValue as Tabs) {
                                Tabs.help -> {
                                    mTypography("Rules", paragraph = true)
                                    mTypography(text = null, paragraph = true) {
                                        mLink(
                                            text = "Illustated Rules (English)",
                                            hRefOptions = HRefOptions(
                                                href = "https://pentagame.org/pdf/Illustrated_Rules.pdf",
                                                targetBlank = true
                                            )
                                        ) {
                                            attrs.variant = MTypographyVariant.button
                                        }
                                    }

                                    mTypography(text = null, paragraph = true) {
                                        mLink(
                                            text = "Illustated Rules (German)",
                                            hRefOptions = HRefOptions(
                                                href = "https://pentagame.org/pdf/Illustrated_Rules__German_.pdf",
                                                targetBlank = true
                                            )
                                        ) {
                                            attrs.variant = MTypographyVariant.button
                                        }
                                    }
                                }
                                Tabs.multiplayer -> {
                                    textConnection {}
                                }
                                Tabs.about -> {
                                    mTypography("About")
                                    mLink(
                                        text = "About Pentagame",
                                        hRefOptions = HRefOptions(
                                            href = "https://pentagame.org/",
                                            targetBlank = true
                                        )
                                    ) {
                                        attrs.variant = MTypographyVariant.button
                                    }
                                    br {}
                                    mLink(
                                        text = "Github",
                                        hRefOptions = HRefOptions(
                                            href = "https://github.com/NikkyAI/pentagame",
                                            targetBlank = true
                                        )
                                    ) {
                                        attrs.variant = MTypographyVariant.button
                                    }
                                }
                                Tabs.debug_game -> {
                                    textBoardState {}
                                }
                            }
                        }
                    }
                }
            }
        }
        */
    }
}

fun RBuilder.app() = child(App::class) {}
