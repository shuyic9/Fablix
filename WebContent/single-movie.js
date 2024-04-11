// Adjusted single-movie.js script to include genres, stars, and rating in the movie detail display

/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    let url = window.location.href;
    target = target.replace(/[\[\]]/g, "\\$&");

    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleResult(resultData) {
    console.log("handleResult: populating movie info from resultData");

    // Assuming resultData is structured to include genres and stars as strings, separated by commas
    // and the rating as a separate field. Adjust based on your actual API response structure.

    let movieInfoElement = jQuery("#movie_info");
    movieInfoElement.append(
        `<p>Movie Title: ${resultData["movie_title"]}</p>` +
        `<p>Year Released: ${resultData["movie_year"]}</p>` +
        `<p>Director: ${resultData["movie_director"]}</p>` +
        `<p>Genres: ${resultData["movie_genres"]}</p>` + // Assuming genres are concatenated in a single string
        `<p>Rating: ${resultData["movie_rating"]}</p>`
    );

    let stars = resultData["movie_stars"].split(", "); // Assuming stars are given in "name|id" format, separated by commas
    let starsHTML = stars.map(starInfo => {
        let [name, id] = starInfo.split("|");
        return `<a href="single-star.html?id=${id}">${name}</a>`;
    }).join(", ");

    jQuery("#star_list").html(`<p>Stars: ${starsHTML}</p>`);
}

// Get movie id from URL
let movieId = getParameterByName('id');

// Make the HTTP GET request and register on success callback function handleResult
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: `api/movie?id=${movieId}`,
    success: handleResult
});
