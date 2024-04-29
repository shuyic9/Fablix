$(document).ready(function() {
    $.ajax({
        url: 'api/confirmation',
        type: 'GET',
        dataType: 'json',
        success: function(data) {
            let totalPrice = 0;
            const pricePerItem = 7;
            if (data.purchasedItems && data.purchasedItems.length > 0) {
                let tbody = $('#purchased-movies tbody');
                tbody.empty(); // Clear existing rows
                data.purchasedItems.forEach(function(item) {
                    let row = `<tr>
                                  <td>${item.salesId}</td>
                                  <td>${item.movie_title}</td>
                                  <td>${item.quantity}</td>
                               </tr>`;
                    tbody.append(row);
                    totalPrice += item.quantity * pricePerItem;
                });
                $('#total-price').text(`$${totalPrice}`);
            } else {
                let tbody = $('#purchased-movies tbody');
                tbody.html('<tr><td colspan="3">No items to display.</td></tr>');
                $('#total-price').text("$0");
            }
        },
        error: function() {
            console.error("Failed to retrieve purchase details.");
            let tbody = $('#purchased-movies tbody');
            tbody.html('<tr><td colspan="3">Failed to load purchase details.</td></tr>');
            $('#total-price').text("$0");
        }
    });
});
