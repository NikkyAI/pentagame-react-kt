# Pentagame

## Building

### JVM

running: `./gradlew run` or `./gradlew runShadow`


package: `./gradlew shadowJar`
find runnable jar in `build/libs/`

### JS

`./gradlew packageJs`
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
