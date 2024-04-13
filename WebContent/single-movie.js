
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

    // Handle stars
    let snames = movieInfo["stars"] || '';  // Default to an empty string if star_name is undefined
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
            starsHTML = starsHTML.slice(0, -2); // Remove trailing comma and space
        }
    }

    movieInfoElement.append(`
        <h1>${movieInfo.movie_title}</h1>
        <h3>Year: <span>${movieInfo.movie_year}</span></h3>
        <h3>Director: <span>${movieInfo.movie_director}</span></h3>
        <h3>Genres: <span>${movieInfo.genres}</span></h3>
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
