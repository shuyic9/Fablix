// Cache to store autocomplete results
var cacheHash = {};

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

function handleLookup(query, doneCallback) {
    console.log("Autocomplete search initiated for query: " + query);
    if (query.length >= 3) { // Only perform lookup if query length is 3 or more characters
        if (query in cacheHash) {
            console.log("Using cached results for query: " + query);
            handleLookupAjaxSuccess(JSON.stringify(cacheHash[query]), query, doneCallback);
        } else {
            console.log("Sending AJAX request to backend for query: " + query);
            // Send AJAX request to the autocomplete servlet
            jQuery.ajax({
                method: "GET",
                url: "api/autocomplete?query=" + escape(query),
                success: function(data) {
                    handleLookupAjaxSuccess(data, query, doneCallback);
                },
                error: function(errorData) {
                    console.log("Lookup AJAX error");
                    console.log(errorData);
                }
            });
        }
    }
}

function handleLookupAjaxSuccess(data, query, doneCallback) {

    try {
        // If data is already parsed, use it directly
        var jsonData = (typeof data === "string") ? JSON.parse(data) : data;
        console.log("Parsed JSON data: ", jsonData);

        // Cache the results
        cacheHash[query] = jsonData;
        doneCallback({ suggestions: jsonData });
    } catch (e) {
        console.error("Error parsing JSON data: ", e);
        console.error("Received data: ", data);
    }
}


function handleSelectSuggestion(suggestion) {
    console.log("You select " + suggestion["value"] + " with ID " + suggestion["data"]["id"]);
    window.location.href = 'single-movie.html?id=' + suggestion["data"]["id"];
}

function handleNormalSearch(query) {
    console.log("doing normal search with query: " + query);

}

$('#autocomplete').keypress(function(event) {
    if (event.keyCode == 13) { // keyCode 13 is the enter key
        handleNormalSearch($('#autocomplete').val());
    }
});
