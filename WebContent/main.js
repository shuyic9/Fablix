function handleGenreResult(resultData) {
    console.log("handleGenreResult: populating genre list from resultData");
    let genreElement = jQuery("#genre-list");

    // Clear previous results
    genreElement.empty();

    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "<tr>"; // Start the table row
        let genresHTML = "";
        let genresArray = resultData[i]["genreName"].split(", ");

        for (let genre of genresArray) {
            genresHTML += '<a href="movie-list.html?genre=' + genre + '">' + genre + '</a>, ';
        }
        genresHTML = genresHTML.slice(0, -2); // Remove the last comma and space
        rowHTML += "<td>" + genresHTML + "</td>";
        rowHTML += "</tr>"; // End the table row

        genreElement.append(rowHTML);
    }
}

function handleTitleResult() {
    console.log("handleTitleResult: populating title list");
    let titleElement = jQuery("#title-list");
    let numbers = '0123456789*'.split('');
    let characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('');

    titleElement.empty();

    let numbersHTML = "<div class='char-row'>";
    for (let character of numbers) {
        numbersHTML += '<a href="movie-list.html?firstChar=' + character + '">' + character + '</a>';
    }
    numbersHTML += "</div>";

    let charactersHTML = "<div class='char-row'>";
    for (let character of characters) {
        charactersHTML += '<a href="movie-list.html?firstChar=' + character + '">' + character + '</a>';
    }
    charactersHTML += "</div>";

    titleElement.append(numbersHTML);
    titleElement.append(charactersHTML);
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/browsing",
    success: handleGenreResult
});

$(document).ready(function () {
    handleTitleResult();
});