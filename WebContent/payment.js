$(document).ready(function() {
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
                    window.location.href = 'confirmation.html';
                } else {
                    alert('Payment failed: Incorrect Payment Information');
                }
            },
            error: function(jqXHR, textStatus, errorThrown) {
                console.error('Error processing payment:', textStatus, errorThrown);
                alert('Error processing payment. Please try again.');
            }
        });
    });
});
