package externals

typealias LoggerPredicate = (getState: () -> Any, action: Any, logEntry: LogEntryObject? /* = null */) -> Boolean

typealias StateToString = (state: Any) -> String

typealias ActionToString = (action: Any) -> String

typealias ErrorToString = (error: Any, prevState: Any) -> String