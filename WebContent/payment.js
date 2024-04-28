$(document).ready(function() {
    $('#payment-form').submit(function(event) {
        event.preventDefault(); // Prevent the default form submission

        // Retrieve form data
        const fname = $('#fname').val().trim();
        const lname = $('#lname').val().trim();
        const card = $('#card').val().trim();
        const exp = $('#exp').val().trim();

        // Validation checks for each field
        if (!fname) {
            alert('Please fill in your first name.');
            $('#fname').focus(); // Set focus to the first name input
            return; // Stop the function here
        }
        if (!lname) {
            alert('Please fill in your last name.');
            $('#lname').focus(); // Set focus to the last name input
            return; // Stop the function here
        }
        if (!card) {
            alert('Please enter your credit card number.');
            $('#card').focus(); // Set focus to the credit card input
            return; // Stop the function here
        }
        if (!exp) {
            alert('Please enter the expiration date of your credit card.');
            $('#exp').focus(); // Set focus to the expiration date input
            return; // Stop the function here
        }

        // If all fields are filled, serialize the data and make the AJAX request
        $.ajax("api/payment", {
            method: "POST",
            data: $(this).serialize(),
            success: function(response) {
                if (response.status === 'success') {
                    alert('Payment successful! Order has been placed.');
                    // Optionally, you could redirect the user or clear the form here
                    // window.location.href = 'order-confirmation.html'; // Redirect to another page
                    // $('#payment-form')[0].reset(); // Clear the form
                } else {
                    alert('Payment failed: ' + response.message);
                }
            },
            error: function(jqXHR, textStatus, errorThrown) {
                console.error('Error processing payment:', textStatus, errorThrown);
                alert('Error processing payment. Please try again.');
            }
        });
    });
});
