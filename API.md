# API

## request channel

init a game

```
creator <- FullSync(gameconfig = SyncGameConfiguration(players: [null, null, null, null], mode = Mode.TWO_PLAYER), spectators=[], creator="creator", events = [])
creator -> ChangeMode(mode = Mode.TWO_V_TWO)
@everyone <- SyncGameConfiguration(players: [null, null, null, null], mode = Mode.TWO_V_TWO)
```


connect to game and join

```
player1 <- FullSync(gameconfig = SyncGameConfiguration(players: [null, null, null, null], mode = Mode.TWO_V_TWO), spectators=[], creator="creator", events = [])
@everyone <- SpectatorConnected("player1")
player1 -> RequestToPlay(user = "player1" ,number = 1, shape = "triangle") // require(user == session.user)
creator <- RequestToPlay(user = "player1" ,number = 1, shape = "triangle")
creator -> SyncGameConfiguration(players: [("player1","triangle",false), null, null, null], mode = Mode.TWO_V_TWO)   // this needs a popup editing screen
@everyone <- SyncGameConfiguration(players: [("player1","triangle",false), null, null, null], mode = Mode.TWO_V_TWO)
```


start a game

```
@everyone <- FullSync(gameconfig = SyncGameConfiguration(players: ["player1","triangle",false), "player2","circle",true)], mode = Mode.TWO_PLAYER), spectators=["creator", "player1", "player2"], creator="creator", events = [])
player2 -> SetReady()
@everyone <- SyncGameConfiguration(players: ["player1","triangle",true), "player2","circle",true)], mode = Mode.TWO_PLAYER)
creator -> StartGame()
@everone <- StartGame(startedBy="creator")
@everone <- SyncBoard(pieces={"pieceId": "fieldId", ...})

```

full turn (with replacing events)

```
player1 -> Move(piece = "player1-red", to = "targetFieldId", swapWith = "player2-blue")
@everone <- ScorePoint(move = Move(piece = "player1-red", to = "targetFieldId", swapWith = "player2-blue"))
player1 -> UpdateLast(Move(piece = "player1-red", to = "E", swapWith = "player2-blue", setGreyTo = "soemOtherFieldId"))
@everyone <- UpdateLast(Move(piece = "player1-red", to = "E", swapWith = "player2-blue", setGreyTo = "soemOtherFieldId"))
```

move

```
player1 -> Move.MovePiece(piece = "player1-red", to = "targetFieldId")
@everone <- TurnFinished(nextPlayer = "player2", move = Move(player = "player1", piece = "player1-red", to = "targetFieldId"))
```

move and set black

```
player1 -> Move.MovePieceToBlocker(piece = "player1-red", to = "targetFieldID", blockerPiece = "black-3")
@everone <- TurnIncomplete(CanMoveBlackBlocker(blockerPiece = "black-3"), move = Move.MovePieceToBlocker(piece = "player1-red", to = "targetFieldID", blockerPiece = "black-3"))
player1 -> SetBlack(piece="black-3", to = "soemOtherFieldId")
@everyone <- TurnFinished(nextPlayer = "player2", move = SetBlack(player="player1", piece="black-3", to = "soemOtherFieldId"))
```

move and set black and grey

```
player1 -> Move.MovePieceToBlocker(piece = "player1-red", to = "targetFieldID", blockerPiece = "black-3")
@everone <- TurnIncomplete(CanMoveBlackBlocker(blockerPiece = "black-3"), CanMoveGreyBlocker(greyPieces = [...]), move = Move.MovePieceToBlocker(piece = "player1-red", to = "targetFieldID", blockerPiece = "black-3"))
player1 -> SetBlack(piece="black-3", to = "soemOtherFieldId")
@everone <- TurnIncomplete(CanMoveGreyBlocker(greyPieces = [...]), move = [Move.MovePieceToBlocker(piece = "player1-red", to = "targetFieldID", blockerPiece = "black-3"), SetBlack(piece="black-3", to = "soemOtherFieldId")])
player1 -> SetGrey(piece="grey-1", to = "soemOtherFieldId")
@everyone <- TurnFinished(nextPlayer = "player2", move = [Move.MovePieceToBlocker(piece = "player1-red", to = "targetFieldID", blockerPiece = "black-3"), SetBlack(piece="black-3", to = "soemOtherFieldId"), SetGrey(piece="grey-1", to = "soemOtherFieldId")])
```

swap and set grey

```
player1 -> Move.Swap(piece = "player1-red", to = "targetFieldId", swapWith = "player2-blue")
@everone <- TurnIncomplete(CanMoveGreyBlocker(greyPieces = [...]), move = Move.Swap(player = "player1", piece = "player1-red", to = "targetFieldId", swapWith = "player2-blue"))
player1 -> SetGrey(piece="grey-1", to = "soemOtherFieldId")
@everyone <- TurnFinished(ScorePoint(), nextPlayer = "player2", move = [Move.Swap(player = "player1", piece = "player1-red", to = "targetFieldId", swapWith = "player2-blue"), SetGrey(player="player1", piece="grey-1", to = "soemOtherFieldId")])
```


TODO: undo move



player forfeits

TODO: figure out how to mark a player as having given up
pieces stay o the board but are inactive

```
player1 -> Forfeit()
@everone <- Forfeit(player="player1")
@everyone <- SyncGameConfiguration(players: ["player1","triangle-dead",true), "player2","circle",true), "player3","circle",true)], mode = Mode.THREE_PLAYER)
```


player is skipped

```
player1 <- Ping("please make a move")
@everyone <- Move.TimeoutSkip(player = "player1", nextPlayer = "player2")
```
