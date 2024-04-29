$(document).ready(function() {
    $.ajax({
        url: 'api/cart',
        type: 'GET',
        dataType: 'json',
        success: function(data) {
            if (data.totalPrice) {
                $('#total-price').text("Total Price: $" + data.totalPrice);
            } else {
                $('#total-price').text("Total Price: $0");
                alert('Your cart is empty.');
            }
        },
        error: function() {
            $('#total-price').text("Failed to load total price.");
            console.error("Failed to retrieve cart details.");
        }
    });

    $('#payment-form').submit(function(event) {
        console.log("Attempting to submit form");
        event.preventDefault();

        const fname = $('#fname').val().trim();
        const lname = $('#lname').val().trim();
        const card = $('#card').val().trim();
        const exp = $('#exp').val().trim();

        if (!fname || !lname || !card || !exp) {
            alert('Please fill in all fields.');
            return;
        }

        $.ajax({
            url: "api/payment",
            method: "POST",
            data: {
                fname: fname,
                lname: lname,
                card: card,
                exp: exp
            },
            dataType: "json",
            success: function(response) {
                if (response.status === 'success') {
                    clearCart();  // Clear the cart before redirecting to the confirmation page
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

    // Function to clear the shopping cart
    function clearCart() {
        $.ajax({
            url: "api/cart",
            method: "POST",
            data: { action: "clear" },
            success: function(response) {
                console.log("Cart has been cleared.");
                window.location.href = 'confirmation.html'; // Redirect after clearing the cart
            },
            error: function() {
                console.error("Failed to clear the cart.");
                alert('Failed to clear the cart, but payment was successful. Please contact support.');
            }
        });
    }
});
