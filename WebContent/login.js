let login_form = $("#login_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataJson jsonObject (already parsed by jQuery)
 */
function handleLoginResult(resultDataJson) {
    console.log("handle login response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    if (resultDataJson["status"] === "success") {
        window.location.replace("main.html");
    } else {
        // If login fails, display an alert with the error message
        alert(resultDataJson["message"]);
        //grecaptcha.reset();
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    console.log("submit login form");
    formSubmitEvent.preventDefault();

    let email = $("#login_form input[name='username']").val().trim();
    let password = $("#login_form input[name='password']").val().trim();
    //let recaptchaResponse = $("#g-recaptcha-response").val();

    if (email === "") {
        alert("Please fill out the email field.");
        return;
    } else if (password === "") {
        alert("Please fill out the password field.");
        return;
    }
    /*
    else if (recaptchaResponse === "") {
        alert("Please verify that you are not a robot.");
        return;
    }
     */

    $.ajax("api/login", {
        method: "POST",
        data: login_form.serialize(),
        dataType: "json",
        success: handleLoginResult,
        error: function(jqXHR, textStatus, errorThrown) {
            console.error("Failed to process login", textStatus, errorThrown);
            alert("Failed to process login. Please try again later.");
        }
    });
}

login_form.submit(submitLoginForm);
