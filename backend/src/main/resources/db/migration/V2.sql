
DROP TABLE players_in_games;

CREATE TABLE playingusers (
	id uuid NOT NULL,
	game uuid NOT NULL,
	"user" uuid NOT NULL,
	player character varying(20) NOT NULL,
	shape character varying(20) NOT NULL
);


CREATE TABLE user_in_game (
	game uuid NOT NULL,
	"playerInGame" uuid NOT NULL
);

ALTER TABLE playingusers
	ADD CONSTRAINT playingusers_pkey PRIMARY KEY (id);

ALTER TABLE user_in_game
	ADD CONSTRAINT pk_user_in_game PRIMARY KEY (game, "playerInGame");

ALTER TABLE playingusers
	ADD CONSTRAINT playingusers_game_player_unique UNIQUE (game, player);

ALTER TABLE playingusers
	ADD CONSTRAINT fk_playingusers_game_id FOREIGN KEY (game) REFERENCES public.games(id) ON UPDATE RESTRICT ON DELETE RESTRICT;

ALTER TABLE playingusers
	ADD CONSTRAINT fk_playingusers_user_id FOREIGN KEY ("user") REFERENCES public.users(id) ON UPDATE RESTRICT ON DELETE RESTRICT;

ALTER TABLE user_in_game
	ADD CONSTRAINT fk_user_in_game_game_id FOREIGN KEY (game) REFERENCES public.games(id) ON UPDATE RESTRICT ON DELETE RESTRICT;

ALTER TABLE user_in_game
	ADD CONSTRAINT fk_user_in_game_playeringame_id FOREIGN KEY ("playerInGame") REFERENCES public.playingusers(id) ON UPDATE RESTRICT ON DELETE RESTRICT;
