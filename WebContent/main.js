function handleGenreResult(resultData) {
    console.log("handleGenreResult: populating genre list from resultData");
    let genreElement = jQuery("#genre_table_body");

    // Clear previous results
    genreElement.empty();

    let rowHTML = "";
    for (let i = 0; i < resultData.length; i++) {
        // Check if we need to start a new row
        if (i % 4 === 0) {
            // If we are not at the first genre, we close the previous row
            if (i !== 0) {
                rowHTML += "</tr>";
                genreElement.append(rowHTML);
            }
            // Start a new row
            rowHTML = "<tr>";
        }

        let genre = resultData[i]["genreName"];
        rowHTML += '<td><a href="movie-list.html?genre=' + genre + '">' + genre + '</a></td>';

        // If we've reached the end, close the row
        if (i === resultData.length - 1) {
            rowHTML += "</tr>";
            genreElement.append(rowHTML);
        }
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

