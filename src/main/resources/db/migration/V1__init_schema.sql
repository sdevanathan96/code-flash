    CREATE TYPE difficulty   AS ENUM ('EASY', 'MEDIUM', 'HARD');
    CREATE TYPE confidence   AS ENUM ('AGAIN', 'HARD', 'GOOD', 'EASY');
    CREATE TYPE list_source  AS ENUM ('BUNDLED', 'LEETCODE_CUSTOM', 'LEETCODE_STUDY_PLAN');


    CREATE TABLE tags (
      id    BIGSERIAL PRIMARY KEY,
      name  VARCHAR(100) NOT NULL UNIQUE
    );


    CREATE TABLE problem_lists (
      id                BIGSERIAL    PRIMARY KEY,
      name              VARCHAR(255) NOT NULL UNIQUE,
      source            list_source  NOT NULL,
      favorite_id_hash  VARCHAR(100),
      source_url        VARCHAR(500)
    );


    CREATE TABLE problems (
      id          BIGSERIAL    PRIMARY KEY,
      slug        VARCHAR(255) NOT NULL UNIQUE,
      title       VARCHAR(255) NOT NULL,
      difficulty  difficulty   NOT NULL,
      url         VARCHAR(500),
      created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
    );


    CREATE TABLE problem_tags (
      problem_id  BIGINT NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
      tag_id      BIGINT NOT NULL REFERENCES tags(id)     ON DELETE CASCADE,
      PRIMARY KEY (problem_id, tag_id)
    );


    CREATE TABLE problem_list_items (
      problem_id       BIGINT NOT NULL REFERENCES problems(id)      ON DELETE CASCADE,
      problem_list_id  BIGINT NOT NULL REFERENCES problem_lists(id)  ON DELETE CASCADE,
      PRIMARY KEY (problem_id, problem_list_id)
    );


    CREATE TABLE solve_records (
        id          BIGSERIAL  PRIMARY KEY,
        problem_id  BIGINT     NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
        solved_at   TIMESTAMP  NOT NULL DEFAULT NOW(),
        confidence  confidence NOT NULL,
        notes       TEXT
    );


    CREATE TABLE srs_states (
      id             BIGSERIAL PRIMARY KEY,
      problem_id     BIGINT    NOT NULL REFERENCES problems(id) ON DELETE CASCADE UNIQUE,
      interval_days  INT       NOT NULL DEFAULT 1,
      ease_factor    FLOAT     NOT NULL DEFAULT 2.5,
      next_due_date  DATE      NOT NULL DEFAULT CURRENT_DATE,
      total_solves   INT       NOT NULL DEFAULT 0
    );

    CREATE INDEX idx_srs_next_due ON srs_states(next_due_date);
    CREATE INDEX idx_solve_records_problem ON solve_records(problem_id);