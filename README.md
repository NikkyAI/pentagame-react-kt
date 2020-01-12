# Pentagame

github: [htps://github.com/NikkyAi/pentagame](htps://github.com/NikkyAi/pentagame)

online: [demo](https://pentagame.herokuapp.com/)

## Building

### Backend

build: `./gradlew :backend:shadowJar`
execute: `heroku local:start`

find runnable jar in `backend/build/libs/`, 
  you may need to set some environment manually

### Frontend

js is bundled in the server automatically

for dev:
`./gradlew :frontend:run -t`
alternative:
`./gradlew :frontend:bundle`
then open `frontend/build/bundle/index.html` in a browser (using idea as webserver)

### Recording a game

The game is currently recorded by the getClient and server, but not serialized or stored

There is currently more data recorded than strictly necessary too

TODO

To record a game some initial setup is necessary, 
knowing the amount of players and which team they are in


# TODO

players have 4 ranked chosen symbols
the highest ranking player gets their preferred symbol in case of conflict of choice

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