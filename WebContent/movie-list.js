/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleMovieListResult(resultData) {
    console.log("handleMovieListResult: populating movie table from resultData");
    let starTableBodyElement = jQuery("#movieList_table_body");

    for (let i = 0; i < Math.min(100, resultData.length); i++) {
        let rowHTML = "<tr>"; // Start the table row
        rowHTML += "<td>" +
            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">' +
            resultData[i]["movie_title"] +
            '</a>' + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_year"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_director"] + "</td>";
        // rowHTML += "<td>" + resultData[i]["movie_genres"] + "</td>";
        let genresHTML = "";
        let genresArray = resultData[i]["movie_genres"].split(", ");

        for (let genre of genresArray) {
            genresHTML += '<a href="movie-list.html?genre=' + genre + '">' + genre + '</a>, ';
        }
        genresHTML = genresHTML.slice(0, -2); // Remove the last comma and space
        rowHTML += "<td>" + genresHTML + "</td>";

        let starsHTML = "";
        let starsArray = resultData[i]["movie_stars"].split(", ");
        // console.log(starsArray);
        for (let star of starsArray) {
            let starInfo = star.split("|");
            starsHTML += '<a href="single-star.html?id=' + starInfo[1] + '">' + starInfo[0] + '</a>, ';
        }
        starsHTML = starsHTML.slice(0, -2); // Remove the last comma and space
        rowHTML += "<td>" + starsHTML + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_rating"] + "</td>";
        rowHTML += "</tr>"; // End the table row

        starTableBodyElement.append(rowHTML);
    }
}



/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
$(document).ready(function () {
    const urlParams = new URLSearchParams(window.location.search);

    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/movies", // Setting request url, which is mapped by MovieListServlet in MovieListServlet.java
        data: {
            title: urlParams.get('title'),
            year: urlParams.get('year'),
            director: urlParams.get('director'),
            star: urlParams.get('star'),
            genre: urlParams.get('genre'),
        },
        success: (resultData) => handleMovieListResult(resultData) // Setting callback function to handle data returned successfully by the MovieListServlet
    });
});