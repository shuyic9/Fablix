$(document).ready(function() {
    $("#login_dashboard_form").submit(function(event) {
        console.log("submit dashboard login form");
        event.preventDefault();

        let email = $("input[name='username']").val().trim();
        let password = $("input[name='password']").val().trim();
        let recaptchaResponse = $("#g-recaptcha-response").val(); // Fetch the reCAPTCHA response

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

        $.ajax("api/_dashboard", {
            method: "POST",
            data: $("#login_dashboard_form").serialize(),
            dataType: "json",
            success: handleLoginDashboardResult,
            error: function(jqXHR, textStatus, errorThrown) {
                console.error("Failed to process dashboard login", textStatus, errorThrown);
                alert("Failed to process login. Please try again later.");
            }
        });
    });
});
