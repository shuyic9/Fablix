DELIMITER $$

CREATE PROCEDURE `add_movie`(
    IN p_title VARCHAR(100),
    IN p_year INT,
    IN p_director VARCHAR(100),
    IN p_star_name VARCHAR(100),
    IN p_star_birth_year INT,
    IN p_genre_name VARCHAR(32)
)
BEGIN
    DECLARE v_movie_id VARCHAR(10);
    DECLARE v_star_id VARCHAR(10);
    DECLARE v_genre_id INT;
    DECLARE v_movie_exists INT;


    -- Check if the movie already exists
SELECT COUNT(*) INTO v_movie_exists FROM movies
WHERE title = p_title AND year = p_year AND director = p_director;

IF v_movie_exists = 0 THEN
        -- Insert the movie
        SET v_movie_id = (SELECT CONCAT('tt', LPAD(COALESCE(MAX(SUBSTRING(id, 3)) + 1, 1), 8, '0')) FROM movies);
INSERT INTO movies (id, title, year, director) VALUES (v_movie_id, p_title, p_year, p_director);

-- Handle star
SELECT id INTO v_star_id FROM stars WHERE name = p_star_name AND (birthYear = p_star_birth_year OR birthYear IS NULL);
IF v_star_id IS NULL THEN
            -- Insert new star
            SET v_star_id = (SELECT CONCAT('nm', LPAD(COALESCE(MAX(SUBSTRING(id, 3)) + 1, 1), 8, '0')) FROM stars);
INSERT INTO stars (id, name, birthYear) VALUES (v_star_id, p_star_name, p_star_birth_year);
END IF;

        -- Link star to the movie
INSERT INTO stars_in_movies (starId, movieId) VALUES (v_star_id, v_movie_id);

-- Handle genre
SELECT id INTO v_genre_id FROM genres WHERE name = p_genre_name;
IF v_genre_id IS NULL THEN
            -- Insert new genre
            INSERT INTO genres (name) VALUES (p_genre_name);
            SET v_genre_id = LAST_INSERT_ID();
END IF;

        -- Link genre to the movie
INSERT INTO genres_in_movies (genreId, movieId) VALUES (v_genre_id, v_movie_id);

SELECT 'Movie added successfully with IDs - Movie: ';
ELSE
SELECT 'Movie already exists.' ;
END IF;
END$$

DELIMITER ;
