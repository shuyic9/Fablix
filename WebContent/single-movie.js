function getParameterByName(target) {
    let url = window.location.href;
    target = target.replace(/[\[\]]/g, "\\$&");
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function handleResult(resultData) {
    console.log("handleResult: Received data", resultData);

    if (!resultData || resultData.length === 0) {
        console.error("No data received or data is empty");
        return;
    }

    // Assuming data is correctly received
    let movieInfo = resultData[0]; // Adjust according to actual data structure

    // Check if the necessary data is available
    if (!movieInfo || !movieInfo.movie_title) {
        console.error("Movie data is incomplete or undefined");
        return;
    }

    let movieInfoElement = jQuery("#movie_info");
    movieInfoElement.empty(); // Clear previous content

    let starsHTML = "";
    if (movieInfo.stars) {
        movieInfo.stars.forEach(star => {
            starsHTML += `<a href="single-star.html?id=${star.id}">${star.name}</a>, `;
        });
        starsHTML = starsHTML.slice(0, -2); // Remove trailing comma and space
    }

    movieInfoElement.append(`
        <h1>${movieInfo.movie_title}</h1>
        <h3>Year: <span>${movieInfo.movie_year}</span></h3>
        <h3>Director: <span>${movieInfo.movie_director}</span></h3>
        <h3>Genres: <span>${movieInfo.genres.join(", ")}</span></h3>
        <h3>Stars: ${starsHTML}</h3>
        <h3>Rating: <span>${movieInfo.rating}</span></h3>
    `);

    console.log("handleResult: Populated movie info successfully");
}

let movieId = getParameterByName('id');
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: `api/single-movie?id=${movieId}`,
    success: handleResult
});
