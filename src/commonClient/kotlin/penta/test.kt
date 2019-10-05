val replayGame = """
[
  {
    "type": "penta.SerialNotation.InitGame",
    "players": [
      "square",
      "triangle"
    ]
  },
  {
    "type": "penta.SerialNotation.SwapOwnPiece",
    "player": "square",
    "piece": "p01",
    "otherPiece": "p00",
    "from": "B",
    "to": "A",
    "setGrey": false
  },
  {
    "type": "penta.SerialNotation.SwapOwnPiece",
    "player": "triangle",
    "piece": "p10",
    "otherPiece": "p11",
    "from": "A",
    "to": "B",
    "setGrey": false
  },
  {
    "type": "penta.SerialNotation.SwapOwnPiece",
    "player": "square",
    "piece": "p04",
    "otherPiece": "p01",
    "from": "E",
    "to": "A",
    "setGrey": false
  },
  {
    "type": "penta.SerialNotation.SwapOwnPiece",
    "player": "triangle",
    "piece": "p14",
    "otherPiece": "p11",
    "from": "E",
    "to": "A",
    "setGrey": false
  },
  {
    "type": "penta.SerialNotation.MovePlayer",
    "player": "square",
    "piece": "p01",
    "from": "E",
    "to": "b",
    "setBlack": true,
    "setGrey": true
  },
  {
    "type": "penta.SerialNotation.SetBlack",
    "id": "b1",
    "from": "b",
    "to": "E-2-c"
  },
  {
    "type": "penta.SerialNotation.SetGrey",
    "id": "g0",
    "from": null,
    "to": "E-3-c"
  },
  {
    "type": "penta.SerialNotation.MovePlayer",
    "player": "triangle",
    "piece": "p11",
    "from": "E",
    "to": "b",
    "setBlack": false,
    "setGrey": true
  },
  {
    "type": "penta.SerialNotation.SetGrey",
    "id": "g1",
    "from": null,
    "to": "E-4-c"
  },
  {
    "type": "penta.SerialNotation.SwapOwnPiece",
    "player": "square",
    "piece": "p04",
    "otherPiece": "p00",
    "from": "A",
    "to": "B",
    "setGrey": false
  },
  {
    "type": "penta.SerialNotation.SwapOwnPiece",
    "player": "triangle",
    "piece": "p14",
    "otherPiece": "p10",
    "from": "A",
    "to": "B",
    "setGrey": false
  },
  {
    "type": "penta.SerialNotation.MovePlayer",
    "player": "square",
    "piece": "p00",
    "from": "A",
    "to": "a",
    "setBlack": true,
    "setGrey": true
  },
  {
    "type": "penta.SerialNotation.SetBlack",
    "id": "b0",
    "from": "a",
    "to": "c-1-d"
  },
  {
    "type": "penta.SerialNotation.SetGrey",
    "id": "g2",
    "from": null,
    "to": "c-2-d"
  },
  {
    "type": "penta.SerialNotation.MovePlayer",
    "player": "triangle",
    "piece": "p10",
    "from": "A",
    "to": "a",
    "setBlack": false,
    "setGrey": true
  },
  {
    "type": "penta.SerialNotation.SetGrey",
    "id": "g3",
    "from": null,
    "to": "c-3-d"
  },
  {
    "type": "penta.SerialNotation.MovePlayer",
    "player": "square",
    "piece": "p04",
    "from": "B",
    "to": "e",
    "setBlack": true,
    "setGrey": true
  },
  {
    "type": "penta.SerialNotation.SetBlack",
    "id": "b4",
    "from": "e",
    "to": "b-3-c"
  },
  {
    "type": "penta.SerialNotation.SetGrey",
    "id": "g4",
    "from": null,
    "to": "b-2-c"
  },
  {
    "type": "penta.SerialNotation.MovePlayer",
    "player": "triangle",
    "piece": "p14",
    "from": "B",
    "to": "e",
    "setBlack": false,
    "setGrey": true
  },
  {
    "type": "penta.SerialNotation.SetGrey",
    "id": "g4",
    "from": "b-2-c",
    "to": "b-1-c"
  },
  {
    "type": "penta.SerialNotation.Win",
    "players": [
      "square",
      "triangle"
    ]
  }
]
""".trimIndent()

val replaySetGrey = """[{"type":"penta.SerialNotation.InitGame","players":["triangle","square","cross"]},{"type":"penta.SerialNotation.SwapOwnPiece","player":"triangle","piece":"p01","otherPiece":"p00","from":"B","to":"A","setGrey":false},{"type":"penta.SerialNotation.SwapOwnPiece","player":"square","piece":"p14","otherPiece":"p13","from":"E","to":"D","setGrey":false},{"type":"penta.SerialNotation.SwapOwnPiece","player":"cross","piece":"p22","otherPiece":"p21","from":"C","to":"B","setGrey":false},{"type":"penta.SerialNotation.SwapOwnPiece","player":"triangle","piece":"p03","otherPiece":"p02","from":"D","to":"C","setGrey":false},{"type":"penta.SerialNotation.SwapOwnPiece","player":"square","piece":"p12","otherPiece":"p11","from":"C","to":"B","setGrey":false},{"type":"penta.SerialNotation.SwapOwnPiece","player":"cross","piece":"p20","otherPiece":"p24","from":"A","to":"E","setGrey":false},{"type":"penta.SerialNotation.SwapOwnPiece","player":"triangle","piece":"p00","otherPiece":"p03","from":"B","to":"C","setGrey":false},{"type":"penta.SerialNotation.SwapOwnPiece","player":"square","piece":"p14","otherPiece":"p11","from":"D","to":"C","setGrey":false},{"type":"penta.SerialNotation.SwapOwnPiece","player":"cross","piece":"p24","otherPiece":"p22","from":"A","to":"B","setGrey":false},{"type":"penta.SerialNotation.MovePlayer","player":"triangle","piece":"p00","from":"C","to":"a","setBlack":true,"setGrey":true},{"type":"penta.SerialNotation.SetBlack","id":"b0","from":"a","to":"e-3-a"},{"type":"penta.SerialNotation.SetGrey","id":"g0","from":null,"to":"e-2-a"},{"type":"penta.SerialNotation.MovePlayer","player":"square","piece":"p11","from":"D","to":"b","setBlack":true,"setGrey":true},{"type":"penta.SerialNotation.SetBlack","id":"b1","from":"b","to":"b-1-c"},{"type":"penta.SerialNotation.SetGrey","id":"g1","from":null,"to":"b-2-c"},{"type":"penta.SerialNotation.MovePlayer","player":"cross","piece":"p22","from":"A","to":"c","setBlack":true,"setGrey":true},{"type":"penta.SerialNotation.SetBlack","id":"b2","from":"c","to":"b-3-c"},{"type":"penta.SerialNotation.SetGrey","id":"g2","from":null,"to":"E-6-c"},{"type":"penta.SerialNotation.MovePlayer","player":"triangle","piece":"p03","from":"B","to":"d","setBlack":true,"setGrey":true},{"type":"penta.SerialNotation.SetBlack","id":"b3","from":"d","to":"A-2-B"},{"type":"penta.SerialNotation.SetGrey","id":"g3","from":null,"to":"A-3-B"},{"type":"penta.SerialNotation.MovePlayer","player":"square","piece":"p14","from":"C","to":"e","setBlack":true,"setGrey":true},{"type":"penta.SerialNotation.SetBlack","id":"b4","from":"e","to":"e-1-a"},{"type":"penta.SerialNotation.SetGrey","id":"g4","from":null,"to":"E-5-c"},{"type":"penta.SerialNotation.MovePlayer","player":"cross","piece":"p20","from":"E","to":"b","setBlack":false,"setGrey":false},{"type":"penta.SerialNotation.SwapOwnPiece","player":"triangle","piece":"p04","otherPiece":"p01","from":"E","to":"A","setGrey":false},{"type":"penta.SerialNotation.SwapOwnPiece","player":"square","piece":"p12","otherPiece":"p10","from":"B","to":"A","setGrey":false},{"type":"penta.SerialNotation.SwapOwnPiece","player":"cross","piece":"p21","otherPiece":"p20","from":"C","to":"b","setGrey":false}]"""