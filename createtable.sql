CREATE DATABASE moviedb;
USE moviedb;

CREATE TABLE movies (
    id VARCHAR(10) NOT NULL DEFAULT '' PRIMARY KEY,
    title VARCHAR(100) NOT NULL DEFAULT '',
    year INT NOT NULL,
    director VARCHAR(100) NOT NULL DEFAULT ''
);

CREATE TABLE stars (
    id VARCHAR(10) NOT NULL DEFAULT '' PRIMARY KEY,
    name VARCHAR(100) NOT NULL DEFAULT '',
    birthYear INT
);

CREATE TABLE stars_in_movies (
    starId VARCHAR(10) NOT NULL DEFAULT '',
    movieId VARCHAR(10) NOT NULL DEFAULT '',
    FOREIGN KEY (starId) REFERENCES stars(id),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);

CREATE TABLE genres (
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(32) NOT NULL DEFAULT ''
);

CREATE TABLE genres_in_movies (
    genreId INT NOT NULL,
    movieId VARCHAR(10) NOT NULL DEFAULT '',
    FOREIGN KEY (genreId) REFERENCES genres(id),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);

CREATE TABLE creditcards (
    id VARCHAR(20) NOT NULL DEFAULT '' PRIMARY KEY,
    firstName VARCHAR(50) NOT NULL DEFAULT '',
    lastName VARCHAR(50) NOT NULL DEFAULT '',
    expiration DATE NOT NULL
);

CREATE TABLE customers (
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    firstName VARCHAR(50) NOT NULL DEFAULT '',
    lastName VARCHAR(50) NOT NULL DEFAULT '',
    ccId VARCHAR(20) NOT NULL DEFAULT '',
    address VARCHAR(200) NOT NULL DEFAULT '',
    email VARCHAR(50) NOT NULL DEFAULT '',
    password VARCHAR(20) NOT NULL DEFAULT '',
    FOREIGN KEY (ccId) REFERENCES creditcards(id)
);

CREATE TABLE sales (
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    customerId INT NOT NULL,
    movieId VARCHAR(10) NOT NULL DEFAULT '',
    FOREIGN KEY (customerId) REFERENCES customers(id),
    FOREIGN KEY (movieId) REFERENCES movies(id),
    saleDate DATE NOT NULL
);

CREATE TABLE ratings (
    movieId VARCHAR(10) NOT NULL DEFAULT '',
    rating FLOAT NOT NULL,
    numVotes INT NOT NULL,
    FOREIGN KEY (movieId) REFERENCES movies(id)
);