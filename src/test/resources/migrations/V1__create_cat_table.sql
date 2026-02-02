CREATE TABLE cats (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    age INT NOT NULL
);


INSERT INTO cats (name, age) VALUES ('Whiskers', 2);
INSERT INTO cats (name, age) VALUES ('Tommy', 3);
INSERT INTO cats (name, age) VALUES ('Garfield', 5);