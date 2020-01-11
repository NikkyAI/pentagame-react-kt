@file:JsModule("redux-logger")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION")

package externals

import redux.Middleware
import kotlin.js.*

external interface ColorsObject {
    var title: dynamic /* Boolean | ActionToString */
        get() = definedExternally
        set(value) = definedExternally
    var prevState: dynamic /* Boolean | StateToString */
        get() = definedExternally
        set(value) = definedExternally
    var action: dynamic /* Boolean | ActionToString */
        get() = definedExternally
        set(value) = definedExternally
    var nextState: dynamic /* Boolean | StateToString */
        get() = definedExternally
        set(value) = definedExternally
    var error: dynamic /* Boolean | ErrorToString */
        get() = definedExternally
        set(value) = definedExternally
}

external interface LevelObject {
    var prevState: dynamic /* String | Boolean | StateToString */
        get() = definedExternally
        set(value) = definedExternally
    var action: dynamic /* String | Boolean | ActionToString */
        get() = definedExternally
        set(value) = definedExternally
    var nextState: dynamic /* String | Boolean | StateToString */
        get() = definedExternally
        set(value) = definedExternally
    var error: dynamic /* String | Boolean | ErrorToString */
        get() = definedExternally
        set(value) = definedExternally
}

external interface LogEntryObject {
    var action: dynamic /* String | Boolean | ActionToString */
        get() = definedExternally
        set(value) = definedExternally
    var started: Number?
        get() = definedExternally
        set(value) = definedExternally
    var startedTime: Date?
        get() = definedExternally
        set(value) = definedExternally
    var took: Number?
        get() = definedExternally
        set(value) = definedExternally
    val error: ((error: Any) -> Any)?
        get() = definedExternally
    val nextState: ((state: Any) -> Any)?
        get() = definedExternally
    val prevState: ((state: Any) -> Any)?
        get() = definedExternally
}

external interface ReduxLoggerOptions {
    var level: dynamic /* String | ActionToString | LevelObject */
        get() = definedExternally
        set(value) = definedExternally
    var duration: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var timestamp: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var colors: dynamic /* ColorsObject | false */
        get() = definedExternally
        set(value) = definedExternally
    val titleFormatter: ((formattedAction: Any, formattedTime: String, took: Number) -> String)?
        get() = definedExternally
    var logger: Any?
        get() = definedExternally
        set(value) = definedExternally
    var logErrors: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var collapsed: dynamic /* Boolean | LoggerPredicate */
        get() = definedExternally
        set(value) = definedExternally
    var predicate: LoggerPredicate?
        get() = definedExternally
        set(value) = definedExternally
    var diff: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var diffPredicate: LoggerPredicate?
        get() = definedExternally
        set(value) = definedExternally
    val stateTransformer: ((state: Any) -> Any)?
        get() = definedExternally
    val actionTransformer: ((action: Any) -> Any)?
        get() = definedExternally
    val errorTransformer: ((error: Any) -> Any)?
        get() = definedExternally
}

external fun <S, A1, R1, A2, R2>createLogger(options: ReduxLoggerOptions? = definedExternally /* null */): Middleware<S, A1, R1, A2, R2>
