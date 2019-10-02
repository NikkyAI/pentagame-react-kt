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