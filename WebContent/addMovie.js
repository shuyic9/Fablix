document.getElementById('addMovieForm').addEventListener('submit', function(event) {
    event.preventDefault();
    const formData = new FormData(this);

    // Debugging form data before sending
    for (let pair of formData.entries()) {
        console.log(pair[0] + ', ' + pair[1]);
    }

    fetch('api/add_movie', {
        method: 'POST',
        body: new URLSearchParams(formData)
    })
        .then(response => response.json())
        .then(data => {
            const messagesContainer = document.getElementById('messages');
            const newMessage = document.createElement('div');

            if (data.status === 'success') {
                newMessage.textContent = data.message;
                newMessage.classList.add('text-success', 'message');
                document.getElementById('addMovieForm').reset();
            } else {
                newMessage.textContent = 'Error: ' + data.message || 'An error occurred.';
                newMessage.classList.add('text-danger', 'message');
            }

            // Append the new message and ensure only the last three messages are displayed
            messagesContainer.insertBefore(newMessage, messagesContainer.firstChild);
            let messages = document.querySelectorAll('#messages .message');
            if (messages.length > 3) {
                messages[messages.length - 1].remove();
            }
        })
        .catch(error => {
            console.error('Error adding movie:', error);
            const messagesContainer = document.getElementById('messages');
            const errorMessage = document.createElement('div');
            errorMessage.textContent = 'Failed to add movie. Please check the console for more information.';
            errorMessage.classList.add('text-danger', 'message');
            messagesContainer.insertBefore(errorMessage, messagesContainer.firstChild);

            // Ensure only the last three messages are displayed
            let messages = document.querySelectorAll('#messages .message');
            if (messages.length > 3) {
                messages[messages.length - 1].remove();
            }
        });
});
