let login_form = $("#login_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataJson jsonObject (already parsed by jQuery)
 */
function handleLoginResult(resultDataJson) {
    console.log("handle login response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If login succeeds, it will redirect the user to main.html
    if (resultDataJson["status"] === "success") {
        window.location.replace("main.html");
    } else {
        // If login fails, display an alert with the error message
        alert(resultDataJson["message"]); // Using alert to show the error message
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    console.log("submit login form");
    formSubmitEvent.preventDefault(); // Prevent the form from submitting through the browser

    // Check if all required fields are filled out
    let email = $("#login_form input[name='username']").val().trim();
    let password = $("#login_form input[name='password']").val().trim();

    if (email === "") {
        alert("Please fill out the email field.");
        return; // Stop the form submission
    } else if (password === "") {
        alert("Please fill out the password field.");
        return; // Stop the form submission
    }

    $.ajax("api/login", {
        method: "POST",
        data: login_form.serialize(),
        dataType: "json", // Ensure jQuery expects and parses the response as JSON
        success: handleLoginResult,
        error: function(jqXHR, textStatus, errorThrown) {
            console.error("Failed to process login", textStatus, errorThrown);
            alert("Failed to process login. Please try again later."); // Show an alert on AJAX failure
        }
    });
}

// Bind the submit action of the form to a handler function
login_form.submit(submitLoginForm);
