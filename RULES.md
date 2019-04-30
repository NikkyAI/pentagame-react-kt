# Rules

globalState:
  - board
  - turn
  - players

- start(currentPlayer)
  - canClickOnPiece(targetPiece)
    - piece.player == currentPlayer  
      -> action: selectPlayerTarget(currentPlayerPiece = piece)
    
- selectPlayerTarget(playerPiece: Piece)
  - canClickOnPiece(targetPiece: Piece)
    - hasPath  
      && targetPiece.field != playerPiece  
      && targetPiece.type == player  
      -> action: movePiece(targetPiece, playerPiece.field)  
      -> action: movePlayerPiece(playerPiece, playerPiece.field)
    - hasPath  
      && targetPiece.field != playerPiece  
      && targetPiece.type == black  
      -> action: movePlayerPiece(playerPiece, targetPiece.field)  
      -> action: placeBlack(targetPiece)
    - hasPath  
      && targetPiece.field != playerPiece  
      && targetPiece.type == gray  
      -> action: movePlayerPiece(playerPiece, targetPiece.field)  
      -> action: removePiece(targetPiece)
  - canClickOnField(targetField)
    - hasPath
      && targetField.isEmpty  
      -> action: movePlayerPiece(playerPiece, targetField)
 
- movePlayerPiece(playerPiece: Piece, targetField; Field)
  - targetField.type == joint  
    -> action: removePiece(playerPiece)  
    -> action: pickGray()
  - else  
    -> action: movePiece(playerPiece, targetField)

- placeBlack(blackPiece: Piece)
  - canClickOnField(targetField: Field)
    - targetField.isEmpty  
      -> action: movePiece(blackPiece, targetField)

- pickGray
  - val grayPiece = board.findPiece(field = null, type = gray)  
    -> action: return placeGray(grayPiece)
  - canClickOnPiece(targetPiece: Piece)
    - targetPiece.type == gray  
      // && board.pieces.byType(gray).length  
      -> action: return placeGray(targetPiece)

- placeGray(grayPiece: Piece)
  - canClickOnField(targetField: Field)
    - targetField.isEmpty  
      -> action: movePiece(grayPiece, targetField)

- hasPath(start: Field, end: Field) # builtin?
  - checkConnection(start, end, [start])

- checkConnection(field: Field, target: Field, skip: Field[]): Boolean
  - forEach field.connected 
    - if field in skip
      - `continue@forEach`
    - 