DELIMITER $$

CREATE PROCEDURE `add_movie`(
    IN p_title VARCHAR(100),        -- Parameter for movie title
    IN p_year INT,                  -- Parameter for movie year (can be NULL)
    IN p_director VARCHAR(100),     -- Parameter for movie director
    IN p_star_name VARCHAR(100),    -- Parameter for star name
    IN p_star_birth_year INT,       -- Parameter for star birth year (can be NULL)
    IN p_genre_name VARCHAR(32)     -- Parameter for genre name
)
BEGIN
    DECLARE v_movie_id VARCHAR(10);    -- Variable for new movie ID
    DECLARE v_star_id VARCHAR(10);     -- Variable for new star ID
    DECLARE v_genre_id INT;            -- Variable for genre ID
    DECLARE v_movie_exists INT;        -- Variable to check if movie exists

    -- Check if the movie already exists
    SELECT COUNT(*) INTO v_movie_exists
    FROM movies
    WHERE title = p_title
      AND (year = p_year OR (year IS NULL AND p_year IS NULL))
      AND director = p_director;

    IF v_movie_exists = 0 THEN
        -- Generate new movie ID
        SET v_movie_id = (SELECT CONCAT('tt', LPAD(COALESCE(MAX(SUBSTRING(id, 3)) + 1, 1), 8, '0')) FROM movies);
        -- Insert the movie
        INSERT INTO movies (id, title, year, director)
        VALUES (v_movie_id, p_title, p_year, p_director);

        -- Generate new star ID
        SET v_star_id = (SELECT CONCAT('nm', LPAD(COALESCE(MAX(SUBSTRING(id, 3)) + 1, 1), 8, '0')) FROM stars);
        -- Insert new star
        INSERT INTO stars (id, name, birthYear)
        VALUES (v_star_id, p_star_name, p_star_birth_year);

        -- Link star to the movie
        INSERT INTO stars_in_movies (starId, movieId)
        VALUES (v_star_id, v_movie_id);

        -- Handle genre
        SELECT id INTO v_genre_id
        FROM genres
        WHERE name = p_genre_name;

        IF v_genre_id IS NULL THEN
            -- Insert new genre
            INSERT INTO genres (name)
            VALUES (p_genre_name);
            SET v_genre_id = LAST_INSERT_ID();
        END IF;

        -- Link genre to the movie
        INSERT INTO genres_in_movies (genreId, movieId)
        VALUES (v_genre_id, v_movie_id);

        SELECT 'Movie added successfully with IDs - Movie: ', v_movie_id, ' Star: ', v_star_id, ' Genre: ', v_genre_id;
    ELSE
        SELECT 'Movie already exists.';
    END IF;
END$$

DELIMITER ;
