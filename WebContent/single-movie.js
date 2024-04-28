
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
    movieInfoElement.empty(); // Clear previous content

    let genres = movieInfo["genres"] || '';
    let genresHTML = "";
    if (genres) {
        const genresArray = genres.split(', ');
        for (let i = 0; i < genresArray.length; i++) {
            genresHTML +=
                '<a href="movie-list.html?genre=' + genresArray[i] + '">' +
                genresArray[i] + '</a>' + ', ';
        }
        if (genresHTML.length > 0) {
            genresHTML = genresHTML.slice(0, -2);
        }
    }

    let snames = movieInfo["stars"] || '';
    let starsHTML = "";
    if (snames) {
        const id_name_array = snames.split(', ');
        for (let j = 0; j < id_name_array.length; j++) {
            const parts = id_name_array[j].split("-");
            if (parts.length >= 2) {
                const star_id = parts[0];
                const star_name = parts[1];
                starsHTML +=
                    '<a href="single-star.html?id=' + star_id + '">' +
                    star_name + '</a>' + ', ';
            }
        }
        if (starsHTML.length > 0) {
            starsHTML = starsHTML.slice(0, -2);
        }
    }

    movieInfoElement.append(`
        <h1>${movieInfo.movie_title}</h1>
        <h3>Year: <span>${movieInfo.movie_year}</span></h3>
        <h3>Director: <span>${movieInfo.movie_director}</span></h3>
        <h3>Genres: <span>${genresHTML}</span></h3>
        <h3>Stars: ${starsHTML}</h3> 
        <h3>Rating: <span>${movieInfo.rating}</span></h3>
        <button id="add-to-cart-btn" class="btn btn-success">Add to Cart</button> 
    `);

    console.log("handleResult: Populated movie info successfully");
}




$(document).on('submit', '#cart-form', function(event) {
    event.preventDefault();  // Prevent the default form submission behavior

    // Serialize the form data and include movieId
    let formData = $(this).serialize() + "&movieId=" + movieId;

    // Send an AJAX POST request
    $.ajax({
        url: 'api/cart',
        type: 'POST',
        data: formData,
        success: function(response) {
            const resultData = JSON.parse(response);
            if (resultData.status === "success") {
                displayMessage('some-success-element-id', "Movie added to cart successfully!", true);
            } else {
                displayMessage('some-error-element-id', "Failed to add movie to cart: " + resultData.message, false);
            }
        },
        error: function(xhr, status, error) {
            displayMessage('some-error-element-id', "Failed to add movie to cart. Error: " + error, false);
        }
    });
});

function displayMessage(elementId, message, isSuccess) {
    const element = $('#' + elementId);
    element.text(message);
    element.removeClass(isSuccess ? 'alert-danger' : 'alert-success');
    element.addClass(isSuccess ? 'alert-success' : 'alert-danger');
    element.show().fadeOut(3000); // Adjust timing or effects as needed
}



let movieId;
$(document).ready(function() {
    movieId = getParameterByName('id');
    $.ajax({
        dataType: "json",
        method: "GET",
        url: `api/single-movie?id=${movieId}`,
        success: handleResult
    });
});
