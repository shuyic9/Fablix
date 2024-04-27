/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */

// Helper function to update the URL parameters
function updateUrlParams(page, numResults) {
    const url = new URL(window.location.href);
    url.searchParams.set('page', page);
    url.searchParams.set('numResults', numResults);
    window.history.pushState({ path: url.href }, '', url.href);
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleMovieListResult(resultData) {
    console.log("handleMovieListResult: populating movie table from resultData");
    let starTableBodyElement = jQuery("#movieList_table_body");

    // Clear previous results
    starTableBodyElement.empty();

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
    let currentPage = parseInt(urlParams.get('page')) || 1;

    const updateMovieList = function() {
        const numResults = $("#resultsPerPage").val();

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
                page: currentPage,
                numResults: numResults
            },
            success: function(resultData) {
                handleMovieListResult(resultData);
                updateUrlParams(currentPage, numResults);
                $("#prevPage").prop('disabled', currentPage <= 1);
                $("#nextPage").prop('disabled', resultData.length < numResults);
            }
        });
    }

    // Pagination listeners
    $("#prevPage").click(function () {
        if (currentPage > 1) {
            currentPage--;
            $("#currentPage").text("Page " + currentPage);
            updateMovieList();
        }
    });

    $("#nextPage").click(function () {
        currentPage++;
        $("#currentPage").text("Page " + currentPage);
        updateMovieList();
    });

    $("#resultsPerPage").change(function () {
        currentPage = 1;
        $("#currentPage").text("Page " + currentPage);
        updateMovieList();
    });

    updateMovieList();
});