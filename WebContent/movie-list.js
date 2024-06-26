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
        rowHTML += `<td> <button class="add-to-cart btn btn-success" data-id="${resultData[i]['movie_id']}">Add to Cart</button></td>`;
        rowHTML += "</tr>"; // End the table row

        starTableBodyElement.append(rowHTML);
    }
}


function displayMessage(message) {
    alert(message);
}


// This .ready function focuses solely on handling the "Add to Cart" functionality.
    $(document).ready(function() {
        $('#movieList_table_body').on('click', '.add-to-cart', function(event) {
            event.preventDefault();
            let movieId = $(this).data('id')
            $.ajax({
                url: 'api/cart',
                type: 'POST',
                data: { movieId: movieId, action: 'add' },
                success: function(response) {
                    const resultData = JSON.parse(response);
                    console.log("Response received:", resultData);
                    if (resultData.status === "success") {
                        displayMessage("Movie added to cart successfully!");
                    } else {
                        displayMessage("Failed to add movie to cart");
                    }
                },
            });
        });
    });


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
// Makes the HTTP GET request and registers on success callback function handleStarResult
$(document).ready(function () {
    const urlParams = new URLSearchParams(window.location.search);

    console.log(urlParams.get('title'));
    console.log(urlParams.get('year'));
    console.log(urlParams.get('director'));
    console.log(urlParams.get('star'));
    console.log(urlParams.get('genre'));
    console.log(urlParams.get('firstChar'));

    function initSearchFields() {
        if (urlParams.toString()) { // If there are URL parameters, use them
            console.log("URL parameters found, initializing search fields");
            $('input[name="title"]').val(urlParams.get('title') || '');
            $('input[name="year"]').val(urlParams.get('year') || '');
            $('input[name="director"]').val(urlParams.get('director') || '');
            $('input[name="star"]').val(urlParams.get('star') || '');
            $('input[name="genre"]').val(urlParams.get('genre') || '');
            $('input[name="firstChar"]').val(urlParams.get('firstChar') || '');
            $("#resultsPerPage").val(urlParams.get('numResults') || '10');
            $("#sortBy").val(urlParams.get('sort') || 'title_asc_rating_desc');
        } else { // No URL parameters, assume new search and clear storage
            console.log("No URL parameters, clearing session storage");
            sessionStorage.clear();
        }
    }

    let currentPage = parseInt(urlParams.get('page')) || 1;

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

    function updateMovieList() {
        const numResults = $("#resultsPerPage").val();
        const sort = $("#sortBy").val();
        const title = $('input[name="title"]').val();
        const year = $('input[name="year"]').val();
        const director = $('input[name="director"]').val();
        const star = $('input[name="star"]').val();
        const genre = urlParams.get('genre');
        const firstChar = urlParams.get('firstChar');

        console.log(`Making AJAX call with: title=${title}, year=${year}, director=${director}, star=${star}, numResults=${numResults}, genre=${genre}, firstChar=${firstChar}, page=${currentPage}, sort=${sort}`);

        // Save the current state to session storage
        sessionStorage.setItem('movieListState', JSON.stringify({
            title: title,
            year: year,
            director: director,
            star: star,
            genre: genre,
            firstChar: firstChar,
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
                firstChar: firstChar,
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

    initSearchFields();
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


});