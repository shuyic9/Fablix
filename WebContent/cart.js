$(document).ready(function() {
    // Fetch cart data on page load
    fetchCartData();

    // Function to fetch cart data from the server
    function fetchCartData() {
        $.ajax({
            url: "api/cart",
            method: "GET",
            success: function(response) {
                let resultDataJson = JSON.parse(response);
                handleCartArray(resultDataJson.cartItems);
                updateSessionInfo(resultDataJson);
            }
        });
    }

    // Update the session information on the page
    function updateSessionInfo(data) {
        $("#sessionID").text("Session ID: " + data.sessionID);
        $("#lastAccessTime").text("Last access time: " + new Date(data.lastAccessTime).toLocaleString());
    }

    // Handle the items in the item list
    function handleCartArray(cartItems) {
        let item_list = $("#item_list tbody");
        item_list.empty(); // Clear existing items

        if (cartItems.length === 0) {
            item_list.append("<tr><td colspan='5'>Your cart is empty</td></tr>");
        } else {
            let total = 0;
            cartItems.forEach(function(item) {
                let itemTotal = item.quantity * item.price;
                total += itemTotal;
                item_list.append(
                    `<tr>
                        <td>${item.title}</td>
                        <td>
                            <button class="quantity-modify" data-id="${item.movieId}" data-change="-1">-</button>
                            ${item.quantity}
                            <button class="quantity-modify" data-id="${item.movieId}" data-change="1">+</button>
                        </td>
                        <td>$${item.price.toFixed(2)}</td>
                        <td>$${itemTotal.toFixed(2)}</td>
                        <td><button class="remove-item" data-id="${item.movieId}">Remove</button></td>
                    </tr>`
                );
            });
            item_list.append(`<tr><td colspan='3'>Total</td><td>$${total.toFixed(2)}</td><td></td></tr>`);
        }
    }

    // Event handler for modifying the quantity of an item
    $(document).on('click', '.quantity-modify', function() {
        let movieId = $(this).data('id');
        let change = $(this).data('change');

        $.ajax({
            url: 'api/cart',
            method: 'POST',
            data: {
                movieId: movieId,
                change: change
            },
            success: function(response) {
                let resultDataJson = JSON.parse(response);
                handleCartArray(resultDataJson.cartItems);
            }
        });
    });

    // Event handler for removing an item from the cart
    $(document).on('click', '.remove-item', function() {
        let movieId = $(this).data('id');

        $.ajax({
            url: 'api/cart',
            method: 'POST',
            data: {
                movieId: movieId,
                remove: true
            },
            success: function(response) {
                let resultDataJson = JSON.parse(response);
                handleCartArray(resultDataJson.cartItems);
            }
        });
    });
});
