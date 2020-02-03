
CREATE TABLE games (
	id uuid NOT NULL,
	"gameId" character varying(50) NOT NULL,
	history jsonb NOT NULL
);

ALTER TABLE games OWNER TO postgres;

CREATE TABLE players_in_games (
	"user" uuid NOT NULL,
	game uuid NOT NULL
);

ALTER TABLE players_in_games OWNER TO postgres;

CREATE TABLE users (
	id uuid NOT NULL,
	"userId" character varying(50) NOT NULL,
	"passwordHash" character varying(50) NOT NULL,
	"displayName" character varying(50)
);

ALTER TABLE users OWNER TO postgres;

ALTER TABLE games
	ADD CONSTRAINT games_pkey PRIMARY KEY (id);

ALTER TABLE players_in_games
	ADD CONSTRAINT pk_players_in_games PRIMARY KEY ("user", game);

ALTER TABLE users
	ADD CONSTRAINT users_pkey PRIMARY KEY (id);

ALTER TABLE players_in_games
	ADD CONSTRAINT fk_players_in_games_game_id FOREIGN KEY (game) REFERENCES public.games(id) ON UPDATE RESTRICT ON DELETE RESTRICT;

ALTER TABLE players_in_games
	ADD CONSTRAINT fk_players_in_games_user_id FOREIGN KEY ("user") REFERENCES public.users(id) ON UPDATE RESTRICT ON DELETE RESTRICT;

ALTER TABLE users
	ADD CONSTRAINT users_userid_unique UNIQUE ("userId");
