package penta

sealed class Notation {
    abstract val asNotation: String

    class MovePlayer(
        private val player: String,
        private val piece: String,
        private val from: String,
        private val to: String,
        private val setBlack: SetBlack?,
        private val setGrey: SetGrey?
    ) : Notation() {
        override val asNotation: String
            get() = "$player: $piece ($from -> $to) ${setBlack?.asNotation ?: ""} ${setGrey?.asNotation ?: ""}"
    }

    class SwapOwnPiece(
        private val player: String,
        private val piece: String,
        private val otherPiece: String,
        private val from: String,
        private val to: String,
        private val setGrey: SetGrey?
    ) : Notation() {
        override val asNotation: String
            get() = "$player: $piece, $otherPiece ($from <-> $to) ${setGrey?.asNotation ?: ""}"

    }

    class SwapHostilePieces(
        private val player: String,
        private val otherPlayer: String,
        private val piece: String,
        private val otherPiece: String,
        private val from: String,
        private val to: String,
        private val setGrey: SetGrey?
    ) : Notation() {
        // TODO: figure out if `@otherPlayer` is correct
        override val asNotation: String
            get() = "$player: $piece, $otherPiece@$otherPlayer ($from <-/-> $to) ${setGrey?.asNotation ?: ""}"

    }

    class CooperativeSwap(
        private val player: String,
        private val otherPlayer: String,
        private val piece: String,
        private val otherPiece: String,
        private val from: String,
        private val to: String,
        private val setGrey: SetGrey?
    ) : Notation() {
        override val asNotation: String
            get() = "$player: $piece, $otherPiece@$otherPlayer ($from <=> $to) ${setGrey?.asNotation ?: ""}"
    }

    class SetGrey(val from: String?, private val to: String): Notation() {
        // TODO: missing $from `${from?.let {"$it -> "} ?: ""}`
        override val asNotation: String
            get() = "& [$to]"
    }

    class SetBlack(val from: String, private val to: String) : Notation() {
        override val asNotation: String
            get() = "& [$to]"
    }
}
