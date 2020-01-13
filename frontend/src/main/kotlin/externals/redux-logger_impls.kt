package externals

data class ReduxLoggerOptionsImpl(
    override var level: String /* String | ActionToString | LevelObject */,
    override var duration: Boolean? = null,
    override var timestamp: Boolean? = null,
    override var colors: ColorsObject = ColorsObjectImpl(),  /* ColorsObject | false */
    override val titleFormatter: ((formattedAction: Any, formattedTime: String, took: Number) -> String)? = null,
    override var logger: Any? = null,
    override var logErrors: Boolean? = null,
    override var collapsed: Boolean = true, /* Boolean | LoggerPredicate */
    override var predicate: LoggerPredicate? = null,
    override var diff: Boolean? = null,
    override var diffPredicate: LoggerPredicate? = null,
    override val stateTransformer: ((state: Any) -> Any)? = null,
    override val actionTransformer: ((action: Any) -> Any)? = null,
    override val errorTransformer: ((error: Any) -> Any)? = null
): ReduxLoggerOptions


data class ColorsObjectImpl (
    override var title: Boolean = true, /* Boolean | ActionToString */
    override var prevState: Boolean = true, /* Boolean | StateToString */
    override var action: Boolean = true, /* Boolean | ActionToString */
    override var nextState: Boolean = true, /* Boolean | StateToString */
    override var error: Boolean = true /* Boolean | ErrorToString */
): ColorsObject