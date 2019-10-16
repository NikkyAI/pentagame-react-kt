package penta.server

object GameController {
    var idCounter = 0
    val games = mutableListOf<ServerGamestate>()

    fun create(owner: User): ServerGamestate {
        val game = ServerGamestate("game_${idCounter++}", owner)
        games += game
        return game
    }

    fun get(gameId: String): ServerGamestate? {
        return games.find { it.id == gameId }
    }
}