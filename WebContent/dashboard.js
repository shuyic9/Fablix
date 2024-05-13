$(document).ready(function() {
    // Fetch and display metadata
    function fetchMetadata() {
        $.ajax({
            url: 'api/dashboard',
            type: 'GET',
            dataType: 'json',
            success: function(data) {
                var tablesContainer = $('#tablesContainer');
                tablesContainer.empty();

                data.forEach(function(table) {
                    var tableHtml = `
                        <div class="table-responsive">
                            <h2>${table.tableName}</h2>
                            <table class="table table-striped">
                                <thead>
                                    <tr>
                                        <th>Attribute Name</th>
                                        <th>Attribute Type</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    ${table.columns.map(col => `
                                        <tr>
                                            <td>${col.Field}</td>
                                            <td>${col.Type}</td>
                                        </tr>`).join('')}
                                </tbody>
                            </table>
                        </div>`;
                    tablesContainer.append(tableHtml);
                });
            },
            error: function(error) {
                console.log('Error fetching metadata:', error);
            }
        });
    }

    fetchMetadata();

    // Button actions
    $('#addMovieBtn').click(function() {
        window.location.href = 'addmovie.html';
    });

    $('#addStarBtn').click(function() {
        window.location.href = 'addstar.html';
    });
});
