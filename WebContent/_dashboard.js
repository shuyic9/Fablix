
$(document).ready(function() {
    let loginDashboardForm = $("#login_dashboard_form");

    // Handle the data returned by LoginDashboardServlet
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
            grecaptcha.reset();
        }
    }

    // Submit the form content with POST method
    loginDashboardForm.submit(function(event) {
        console.log("submit dashboard login form");
        event.preventDefault();

        let email = loginDashboardForm.find("input[name='username']").val().trim();
        let password = loginDashboardForm.find("input[name='password']").val().trim();
        let recaptchaResponse = $("#g-recaptcha-response").val(); // Fetch the reCAPTCHA response

        // Basic validation of input fields
        if (email === "") {
            alert("Please fill out the email field.");
            return;
        } else if (password === "") {
            alert("Please fill out the password field.");
            return;
        } else if (recaptchaResponse === "") {
            alert("Please verify that you are not a robot.");
            return;
        }

        // Send the AJAX request to the new employee dashboard login endpoint
        $.ajax({
            url: "api/_dashboard",
            method: "POST",
            data: loginDashboardForm.serialize(),
            dataType: "json",
            success: handleLoginDashboardResult,
            error: function(jqXHR, textStatus, errorThrown) {
                console.error("Failed to process dashboard login", textStatus, errorThrown);
                alert("Failed to process login. Please try again later.");
            }
        });
    });
});
