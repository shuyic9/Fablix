$(document).ready(function() {

    fetchCartData();

    function fetchCartData() {
        $.ajax({
            url: "api/cart",
            method: "GET",
            success: function(response) {
                let resultDataJson = JSON.parse(response);
                handleCartArray(resultDataJson.cartItems);
            }
        });
    }

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
                        <td>${item.movieTitle}</td>
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

    $(document).on('click', '.quantity-modify', function() {
        let movieId = $(this).data('id');
        let change = $(this).data('change');

        let action = (change === 1) ? 'increase' : 'decrease';

        $.ajax({
            url: 'api/cart',
            method: 'POST',
            data: {
                movieId: movieId,
                action: action
            },
            success: function(response) {
                let resultDataJson = JSON.parse(response);
                handleCartArray(resultDataJson.cartItems);
            }
        });
    });


    $(document).on('click', '.remove-item', function() {
        let movieId = $(this).data('id');

        $.ajax({
            url: 'api/cart',
            method: 'POST',
            data: {
                movieId: movieId,
                action: 'delete'
            },
            success: function(response) {
                let resultDataJson = JSON.parse(response);
                handleCartArray(resultDataJson.cartItems);
            }
        });
    });

});
