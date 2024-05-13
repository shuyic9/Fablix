DELIMITER $$
DROP PROCEDURE IF EXISTS add_movie;
CREATE PROCEDURE `add_movie`(
    IN p_title VARCHAR(100),
    IN p_year INT,
    IN p_director VARCHAR(100),
    IN p_star_name VARCHAR(100),
    IN p_star_birth_year INT,
    IN p_genre_name VARCHAR(32)
)
BEGIN
    DECLARE v_movie_id VARCHAR(9);
    DECLARE v_star_id VARCHAR(9);
    DECLARE v_genre_id INT;
    DECLARE v_movie_exists INT;

    -- Check if the movie with the same title, year, and director already exists
    SELECT COUNT(*) INTO v_movie_exists
    FROM movies
    WHERE title = p_title AND year = p_year AND director = p_director;

    IF v_movie_exists = 0 THEN
        -- Generate new movie ID, ensuring it's unique
        SET v_movie_id = (SELECT CONCAT('tt', LPAD(COALESCE(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) + 1, 1), 7, '0')) FROM movies);

        -- Insert the movie
        INSERT INTO movies (id, title, year, director) VALUES (v_movie_id, p_title, p_year, p_director);
        INSERT INTO ratings (movieId, rating, numVotes) VALUES (v_movie_id, 0.0, 0);

        -- Check if the star exists
        SELECT id INTO v_star_id FROM stars WHERE name = p_star_name LIMIT 1;

        IF v_star_id IS NULL THEN
            -- Generate new star ID
            SET v_star_id = (SELECT CONCAT('nm', LPAD(COALESCE(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) + 1, 1), 7, '0')) FROM stars);
            -- Insert new star if not exists
            IF p_star_birth_year IS NULL THEN
                INSERT INTO stars (id, name) VALUES (v_star_id, p_star_name);
            ELSE
                INSERT INTO stars (id, name, birthYear) VALUES (v_star_id, p_star_name, p_star_birth_year);
            END IF;
        END IF;

        -- Insert star-movie relation
        INSERT INTO stars_in_movies (starId, movieId) VALUES (v_star_id, v_movie_id);

        -- Check if the genre exists
        SELECT id INTO v_genre_id FROM genres WHERE name = p_genre_name LIMIT 1;

        IF v_genre_id IS NULL THEN
            -- Insert new genre if not exists
            INSERT INTO genres (name) VALUES (p_genre_name);
            SELECT LAST_INSERT_ID() INTO v_genre_id;
        END IF;

        -- Insert genre-movie relation
        INSERT INTO genres_in_movies (genreId, movieId) VALUES (v_genre_id, v_movie_id);

        SELECT CONCAT('Movie added successfully with IDs - Movie: ', v_movie_id, ', Star: ', v_star_id, ', Genre: ', v_genre_id) AS message;
    ELSE
        SELECT 'Movie already exists.' AS message;
    END IF;
END$$

DELIMITER ;
