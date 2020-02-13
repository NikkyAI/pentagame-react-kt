
CREATE TABLE games (
	id uuid NOT NULL,
	"gameId" character varying(50) NOT NULL,
	history jsonb NOT NULL,
	owner uuid NOT NULL
);


CREATE TABLE players_in_games (
	"user" uuid NOT NULL,
	game uuid NOT NULL
);


CREATE TABLE users (
	id uuid NOT NULL,
	"userId" character varying(50) NOT NULL,
	"passwordHash" character varying(50),
	"displayName" character varying(50),
	"temporaryUser" boolean NOT NULL
);


ALTER TABLE games
	ADD CONSTRAINT games_pkey PRIMARY KEY (id);

ALTER TABLE players_in_games
	ADD CONSTRAINT pk_players_in_games PRIMARY KEY ("user", game);

ALTER TABLE users
	ADD CONSTRAINT users_pkey PRIMARY KEY (id);

ALTER TABLE games
	ADD CONSTRAINT fk_games_owner_id FOREIGN KEY (owner) REFERENCES public.users(id) ON UPDATE RESTRICT ON DELETE RESTRICT;

ALTER TABLE players_in_games
	ADD CONSTRAINT fk_players_in_games_game_id FOREIGN KEY (game) REFERENCES public.games(id) ON UPDATE RESTRICT ON DELETE RESTRICT;

ALTER TABLE players_in_games
	ADD CONSTRAINT fk_players_in_games_user_id FOREIGN KEY ("user") REFERENCES public.users(id) ON UPDATE RESTRICT ON DELETE RESTRICT;

ALTER TABLE users
	ADD CONSTRAINT users_userid_unique UNIQUE ("userId");
