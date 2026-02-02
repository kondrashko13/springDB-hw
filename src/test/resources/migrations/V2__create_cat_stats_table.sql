CREATE TABLE cat_stats (
    total_cats INT NULL,
    average_age DOUBLE PRECISION NULL
);

INSERT INTO cat_stats (total_cats, average_age)
SELECT COUNT(*), AVG(age) FROM cats;