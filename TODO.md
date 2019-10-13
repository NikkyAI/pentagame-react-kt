# TODO

- [ ] BUGS
  - [ ] black cannot be placed on itself when getting abrakadaba
  - [ ] getting passivly moved out will move you out on the next turn
  - [ ] highlighting on pieces is not complete

- [ ] UI
  - [x] Display currently selected piece (player, black, gray)
  - [x] display turn
  - [x] display piece/figure shapes
  - [ ] change figure shapes
  - [ ] track points (0/3) for each user
        render pieces at the user "corner" ?

- [ ] Server
  - [ ] User
    - [ ] add User storage (SQLDelight)
    - [ ] add routes for setting user data (password, displayname)
  - [ ] add game-session listing
  - [ ] add game-session storage in mem
  

- [ ] game state machine
  - [x] variable user count
  - [x] users taking turns
  - [x] parsing move from notation
  - [x] apply move, keep track of affected pieces
    - [ ] undo moves in reverse order

- [x] path checking

- [ ] multiplatform
  - [x] js
  - [x] jvm
  - [ ] android
 
 - [ ] Test JS coroutines support / multithreading
  
- [ ] minification
  - [x] Terser
  - [ ] ProGuard
  
- [ ] look at js serviceworkers ?