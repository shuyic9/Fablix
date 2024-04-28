/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */
const successMessageId = "add-to-cart-success";
const errorMessageId = "add-to-cart-error";

// Helper function to update the URL parameters
function updateUrlParams(page, numResults, sort) {
    const url = new URL(window.location.href);
    url.searchParams.set('page', page);
    url.searchParams.set('numResults', numResults);
    url.searchParams.set('sort', sort);
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
        rowHTML += `<td><button class="btn btn-success add-to-cart" data-id="${resultData[i]['movie_id']}">Add to Cart</button></td>`;
        rowHTML += "</tr>"; // End the table row

        starTableBodyElement.append(rowHTML);
    }
    $(document).on('click', '.add-to-cart', function() {
        const movieId = $(this).data('id');

        // AJAX request to add the movie to the cart
        $.ajax({
            url: 'api/cart',
            method: 'POST',
            data: { movieId: movieId },
            success: function(response) {
                const resultData = JSON.parse(response);
                if(resultData.status === "success"){
                    displayMessage(successMessageId, "Movie added to cart successfully!");
                } else {
                    displayMessage(errorMessageId, "Failed to add movie to cart: " + resultData.message);
                }
            },
            error: function(xhr, status, error) {
                displayMessage(errorMessageId, "Failed to add movie to cart. Error: " + error);
            }
        });
    });
}


function displayMessage(elementId, message) {
    $('#' + elementId).text(message).show().fadeOut(2000);
}



/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
// Makes the HTTP GET request and registers on success callback function handleStarResult
$(document).ready(function () {
    const urlParams = new URLSearchParams(window.location.search);

    function updateMovieList() {
        const numResults = $("#resultsPerPage").val();
        const sort = $("#sortBy").val();
        const title = $('input[name="title"]').val();
        const year = $('input[name="year"]').val();
        const director = $('input[name="director"]').val();
        const star = $('input[name="star"]').val();
        const genre = $('input[name="genre"]').val();

        console.log(`Making AJAX call with: title=${title}, year=${year}, director=${director}, star=${star}, genre=${genre}, page=${currentPage}, sort=${sort}`);

        // Save the current state to session storage
        sessionStorage.setItem('movieListState', JSON.stringify({
            title: title,
            year: year,
            director: director,
            star: star,
            genre: genre,
            page: currentPage,
            numResults: numResults,
            sort: sort
        }));

        jQuery.ajax({
            dataType: "json",
            method: "GET",
            url: "api/movies",
            data: {
                title: title,
                year: year,
                director: director,
                star: star,
                genre: genre,
                page: currentPage,
                numResults: numResults,
                sort: sort
            },
            success: function(resultData) {
                handleMovieListResult(resultData);
                updateUrlParams(currentPage, numResults, sort);
                $("#prevPage").prop('disabled', currentPage <= 1);
                $("#nextPage").prop('disabled', resultData.length < numResults);
                $("#currentPage").text("Page " + currentPage);
            }
        });
    }

    let currentPage = 1;

    const savedState = sessionStorage.getItem('movieListState');
    if (savedState) {
        const state = JSON.parse(savedState);
        currentPage = state.page;
        $("#resultsPerPage").val(state.numResults);
        $("#sortBy").val(state.sort);

        $('input[name="title"]').val(state.title);
        $('input[name="year"]').val(state.year);
        $('input[name="director"]').val(state.director);
        $('input[name="star"]').val(state.star);
        $('input[name="genre"]').val(state.genre);
    } else {
        $('input[name="title"]').val(urlParams.get('title') || '');
        $('input[name="year"]').val(urlParams.get('year') || '');
        $('input[name="director"]').val(urlParams.get('director') || '');
        $('input[name="star"]').val(urlParams.get('star') || '');
        $('input[name="genre"]').val(urlParams.get('genre') || '');
    }

    updateMovieList();

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

    $("#sortBy").change(function () {
        currentPage = 1;
        $("#currentPage").text("Page " + currentPage);
        updateMovieList();
    });
});