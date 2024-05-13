document.getElementById('addStarForm').addEventListener('submit', function(event) {
    event.preventDefault();
    const formData = new FormData(this);
    fetch('api/add_star', {
        method: 'POST',
        body: new URLSearchParams(formData)
    })
        .then(response => response.json())
        .then(data => {
            if (data.status === "success") {
                alert("Star added successfully.");
            } else {
                alert("Failed to add star: " + data.message);
            }
        })
        .catch(error => {
            console.error('Error adding star:', error);
            alert('Failed to add star. Please check the console for more information.');
        });
});
