# Pentagame

github: [htps://github.com/NikkyAi/pentagame](htps://github.com/NikkyAi/pentagame)

online: [demo](https://pentagame.herokuapp.com/)

## Building

### JFX-Client

running: `./gradlew run` or `./gradlew runShadow`

package: `./gradlew shadowJar`
find runnable jar in `build/libs/`

### JVM-Server

build: `./gradlew shadowJarServer`
execute: `heroku local:start`

find runnable jar in `build/libs/`, 
  you may need to set some environment manually

### JS

js is bundled in the server

`./gradlew client-jsTerseJs`
upload `build/html`

## Notation

### Fields

- corner nodes
`c0`-`c4`

- target nodes (inner)  
`j0`-`j4` or `1,3,5,7,9`

- connections
start, end, steps
`c1j4/2`

### Moves

TODO

### Recording a game

TODO

To record a game some initial setup is necessary, 
knowing the amount of players and which team they are in


# TODO

player has 4 ranked chosen symbols
ranking depends on which symbol is taken 
when conflicts arises in multiplayer

# UI

vertical or curved on corner:
- player names/faces
    highlight this turn
    digital clock

tabs:
  - help & rules
  - multiplayer/login:
    - server / connection info
    - all players info
    - session chat
    - rooms / games
    - global chat ?
    - account info
  - notation
  - debug export


# ELO

new players have `null` ranking
when a new player beats a ranked player
  they get awarded the same rank points
  then points get redistributed
total rank points increase with total ranked player count
separated rankings for 2, 3, 4, 2v2 players