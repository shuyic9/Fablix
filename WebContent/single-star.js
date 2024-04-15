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
    console.log("handleResult: populating star info from resultData");

    if (!resultData || resultData.length === 0) {
        console.error("No data received or data is empty");
        return;
    }

    let starInfoElement = jQuery("#star_info");
    starInfoElement.empty(); // Clear previous content

    let moviesHTML = "<span>Movies: ";
    resultData.forEach((movie, index) => {
        moviesHTML += `<a href='single-movie.html?id=${movie["movie_id"]}'>${movie["movie_title"]}</a>`;
        if (index < resultData.length - 1) { // Check if it's not the last item
            moviesHTML += ', '; // Add a comma after each movie except the last one
        }
    });
    moviesHTML += "</span>";

    // Assuming star name and DOB are the same across all entries
    let starName = resultData[0]["star_name"];
    let starDOB = resultData[0]["star_dob"] ? resultData[0]["star_dob"] : "N/A";

    starInfoElement.append(`
        <h1>${starName}</h1>
        <h3>Date of Birth: ${starDOB}</h3>
        <h3>${moviesHTML}</h3>
    `);

    console.log("handleResult: Populated star info successfully");
}

let starId = getParameterByName('id');
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: `api/single-star?id=${starId}`,
    success: handleResult
});
