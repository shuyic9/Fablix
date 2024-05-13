document.getElementById('addStarForm').addEventListener('submit', function(event) {
    event.preventDefault();
    const formData = new FormData(this);
    fetch('api/add_star', {
        method: 'POST',
        body: new URLSearchParams(formData)
    })
        .then(response => response.json())
        .then(data => {
            const resultDiv = document.getElementById('result');
            if (data.status === "success") {
                resultDiv.textContent = "Star added successfully with ID: " + data.starId;
                resultDiv.classList.add('text-success');
                document.getElementById('addStarForm').reset();
            } else {
                resultDiv.textContent = "Failed to add star: " + data.message;
                resultDiv.classList.add('text-danger');
            }
        })
        .catch(error => {
            console.error('Error adding star:', error);
            const resultDiv = document.getElementById('result');
            resultDiv.textContent = 'Failed to add star. Please check the console for more information.';
            resultDiv.classList.add('text-danger');
        });
});
