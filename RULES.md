# Rules

globalState:
  - board
  - turn
  - players

- start(currentPlayer)
  - canClickOnPiece(targetPiece)
    - piece.player == currentPlayer  
      -> actions: selectPlayerTarget(currentPlayerPiece = piece)
    
- selectPlayerTarget(playerPiece: Piece)
  - canClickOnPiece(targetPiece: Piece)
    - hasPath  
      && targetPiece.field != playerPiece  
      && targetPiece.type == player  
      -> actions: movePiece(targetPiece, playerPiece.field)  
      -> actions: movePlayerPiece(playerPiece, playerPiece.field)
    - hasPath  
      && targetPiece.field != playerPiece  
      && targetPiece.type == black  
      -> actions: movePlayerPiece(playerPiece, targetPiece.field)  
      -> actions: placeBlack(targetPiece)
    - hasPath  
      && targetPiece.field != playerPiece  
      && targetPiece.type == gray  
      -> actions: movePlayerPiece(playerPiece, targetPiece.field)  
      -> actions: removePiece(targetPiece)
  - canClickOnField(targetField)
    - hasPath
      && targetField.isEmpty  
      -> actions: movePlayerPiece(playerPiece, targetField)
 
- movePlayerPiece(playerPiece: Piece, targetField; Field)
  - targetField.type == joint  
    -> actions: removePiece(playerPiece)  
    -> actions: pickGray()
  - else  
    -> actions: movePiece(playerPiece, targetField)

- placeBlack(blackPiece: Piece)
  - canClickOnField(targetField: Field)
    - targetField.isEmpty  
      -> actions: movePiece(blackPiece, targetField)

- pickGray
  - val grayPiece = board.findPiece(field = null, type = gray)  
    -> actions: return placeGray(grayPiece)
  - canClickOnPiece(targetPiece: Piece)
    - targetPiece.type == gray  
      // && board.pieces.byType(gray).length  
      -> actions: return placeGray(targetPiece)

- placeGray(grayPiece: Piece)
  - canClickOnField(targetField: Field)
    - targetField.isEmpty  
      -> actions: movePiece(grayPiece, targetField)

- hasPath(start: Field, end: Field) # builtin?
  - checkConnection(start, end, [start])

- checkConnection(field: Field, target: Field, skip: Field[]): Boolean
  - forEach field.connected 
    - if field in skip
      - `continue@forEach`
    - 