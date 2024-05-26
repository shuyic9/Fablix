// Cache to store autocomplete results
var cacheHash = {};

function handleLookup(query, doneCallback) {
    console.log("Autocomplete search initiated for query: " + query);
    if (query.length >= 3) { // Only perform lookup if query length is 3 or more characters
        if (query in cacheHash) {
            console.log("Using cached results for query: " + query);
            doneCallback({ suggestions: cacheHash[query] });
            console.log("Cached suggestions: ", cacheHash[query]);
        } else {
            console.log("Sending AJAX request to backend for query: " + query);
            // Send AJAX request to the autocomplete servlet
            jQuery.ajax({
                method: "GET",
                url: "api/autocomplete?query=" + encodeURIComponent(query),
                success: function(data) {
                    // Parse the string into JSON
                    var jsonData = JSON.parse(data);
                    console.log("Server suggestions: ", jsonData);
                    cacheHash[query] = jsonData; // Cache the results
                    doneCallback({ suggestions: jsonData });
                },
                error: function(errorData) {
                    console.log("Lookup AJAX error");
                    console.log(errorData);
                }
            });
        }
    }
}

/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 *
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
    console.log("You select " + suggestion["value"] + " with ID " + suggestion["data"]["ID"]);
    window.location.href = 'single-movie.html?id=' + suggestion["data"]["ID"];
}

/*
 * This statement binds the autocomplete library with the input box element and
 *   sets necessary parameters of the library.
 */
$('#autocomplete').autocomplete({
    lookup: function(query, doneCallback) {
        handleLookup(query, doneCallback);
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion);
    },
    deferRequestBy: 300,
    minChars: 3
});

// bind pressing enter key to a handler function
$('#autocomplete').keypress(function(event) {
    if (event.keyCode == 13) { // keyCode 13 is the enter key
        handleNormalSearch($('#autocomplete').val());
    }
});
