# structure of tables

## user

- id (uuid)
- login
- hashed_password
- displayName ?
- preferredShape: Shape
- apiToken (token, expiration) // jwt

## player (for game)

may be inlined into game

- id: UUID // game.id + user.id
- fk user: User
- fk game: Game
- index: Int
- shape: Shape (enum) // could be a reference to custom SVG shapes in the future


## game

TODO: request to join game, kicking spectators, removing and kicking players (before game begins), reordering players

- id: UUID
- players: List<Player> // flatten these into player, player2 .. 4 ?
- fk player1: User?
- fk player2: User?
- fk player3: User?
- fk player4: User?
- playerShape1: Shape?
- playerShape2: Shape?
- playerShape3: Shape?
- playerShape4: Shape?
- player1Ready: Boolean // readiness is set to false on any change in configuration
- player2Ready: Boolean
- player3Ready: Boolean?
- player4Ready: Boolean?
- password; String? = null // for hidden games
- configuration: 2, 3, 4, 2v2 // more configuration combinations possible in the future
- history: List<Move> // could be a json, order is integral
- createdAt: Timestamp
- startedAt: Timestamp?
- completedAt: Timestamp?
- createdBy: User // creator can edit game and accept players / may be a player too 

## Move

- pk(game, index)
- fk game: Game
- index: Int // must be unique, orderBy index
- piece: Piece
- from: Field
- to: Field
- swapWith: Piece? // when doing a swap, defines which piece is swapped with
- setBlack: Field?
- setGrey: Field?
- takeGrey: Field? // where the grey piece came from (most of the time off-board)



