/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");

    let starInfoElement = jQuery("#star_info");
    let starDOB = resultData[0]["star_dob"] ? resultData[0]["star_dob"] : "N/A";


    starInfoElement.append("<p>Star Name: " + resultData[0]["star_name"] + "</p>" +
        "<p>Date Of Birth: " + starDOB + "</p>");

    console.log("handleResult: populating movie table from resultData");
    let movieTableBodyElement = jQuery("#movie_table_body");

    let rowHTML = "";
    rowHTML += "<tr>";
    rowHTML += "<th><a href='single-movie.html?id=" + resultData[0]["movie_id"] + "'>" + resultData[0]["movie_title"] + "</a></th>";
    rowHTML += "<th>" + resultData[0]["movie_year"] + "</th>";
    rowHTML += "<th>" + resultData[0]["movie_director"] + "</th>";
    rowHTML += "</tr>";
    movieTableBodyElement.append(rowHTML);
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

let starId = getParameterByName('id');

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/single-star?id=" + starId,
    success: (resultData) => handleResult(resultData)
});