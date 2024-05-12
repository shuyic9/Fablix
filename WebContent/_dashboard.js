// _dashboard.js

// Access the new dashboard login form
let loginDashboardForm = $("#login_dashboard_form");

/**
 * Handle the data returned by LoginDashboardServlet
 * @param resultDataJson jsonObject (already parsed by jQuery)
 */
function handleLoginDashboardResult(resultDataJson) {
    console.log("handle dashboard login response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    if (resultDataJson["status"] === "success") {
        // Redirect to the main dashboard page upon successful login
        window.location.replace("dashboard.html");
    } else {
        // If login fails, display an alert with the error message
        alert(resultDataJson["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginDashboardForm(formSubmitEvent) {
    console.log("submit dashboard login form");
    formSubmitEvent.preventDefault();

    let email = $("#login_dashboard_form input[name='username']").val().trim();
    let password = $("#login_dashboard_form input[name='password']").val().trim();

    // Basic validation of input fields
    if (email === "") {
        alert("Please fill out the email field.");
        return;
    } else if (password === "") {
        alert("Please fill out the password field.");
        return;
    }

    // Send the AJAX request to the new employee dashboard login endpoint
    $.ajax("api/_dashboard", {
        method: "POST",
        data: loginDashboardForm.serialize(),
        dataType: "json",
        success: handleLoginDashboardResult,
        error: function (jqXHR, textStatus, errorThrown) {
            console.error("Failed to process dashboard login", textStatus, errorThrown);
            alert("Failed to process login. Please try again later.");
        }
    });
}

loginDashboardForm.submit(submitLoginDashboardForm);
