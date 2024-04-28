function getParameterByName(target) {
    let url = window.location.href;
    target = target.replace(/[\[\]]/g, "\\$&");
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    return results && results[2] ? decodeURIComponent(results[2].replace(/\+/g, " ")) : null;
}

function handleResult(resultData) {
    console.log("handleResult: populating movie info from resultData");

    if (!resultData || resultData.length === 0) {
        console.error("No data received or data is empty");
        return;
    }

    let movieInfo = resultData[0];
    if (!movieInfo) {
        console.error("Movie data is incomplete or undefined");
        return;
    }

    let movieInfoElement = jQuery("#movie_info");
    movieInfoElement.empty();

    let genresHTML = movieInfo["genres"] ? movieInfo["genres"].split(', ').map(genre =>
        `<a href="movie-list.html?genre=${genre}">${genre}</a>`).join(', ') : '';
    let starsHTML = movieInfo["stars"] ? movieInfo["stars"].split(', ').map(star => {
        let [id, name] = star.split("-");
        return `<a href="single-star.html?id=${id}">${name}</a>`;
    }).join(', ') : '';

    movieInfoElement.append(`
        <h1>${movieInfo.movie_title}</h1>
        <h3>Year: <span>${movieInfo.movie_year}</span></h3>
        <h3>Director: <span>${movieInfo.movie_director}</span></h3>
        <h3>Genres: <span>${genresHTML}</span></h3>
        <h3>Stars: <span>${starsHTML}</span></h3>
        <h3>Rating: <span>${movieInfo.rating}</span></h3>
        <button id="add-to-cart-btn" class="btn btn-success">Add to Cart</button>
    `);
}

function displayMessage(message) {
    alert(message);
}



$(document).ready(function() {
    let movieId = getParameterByName('id');

    if (movieId) {
        $.ajax({
            dataType: "json",
            method: "GET",
            url: `api/single-movie?id=${movieId}`,
            success: handleResult
        });

        $('#movie_info').on('click', '#add-to-cart-btn', function(event) {
            event.preventDefault();
            let data = { movieId: movieId, action: 'add' };
            $.ajax({
                url: 'api/cart',
                type: 'POST',
                data: data,
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
    } else {
        console.error("No movie ID found in the URL");
    }
});
