- # General
    - #### Team#: Spring 2024 Team-XC

    - #### Names: Jun Xia & Shuyi Chen

    - #### Project 5 Video Demo Link: https://youtu.be/qTPZpi9BtSk

  - #### Throughput:
    - For 1 Control Plane + 3 Worker nodes + 1 master MySQL pod + 1 slave MySQL pod + 2 Fabflix pods: ```11,690.666/minute```
    - For 1 Control Plane + 4 Worker nodes + 1 master MySQL pod + 1 slave MySQL pod + 3 Fabflix pods: ```10,664.315/minute```

  - #### Instruction of deployment:
    - On AWS instance, clone project 5: https://youtu.be/qTPZpi9BtSk

        ```git clone https://github.com/UCI-Chenli-teaching/cs122b-s24-team-xc.git```
    - Navigate to the project 4 directory:
      
        ```cd cs122b-s24-team-xc```
    - Build the war file:
      
        ```mvn package```
    - Copy war file to Tomcat for deployment:
      
        ```sudo cp ./target/*.war /var/lib/tomcat10/webapps/```

  - #### Collaborations and Work Distribution:
  - JackyZzZz (Jun Xia)
    ```
    - Setting up AWS instance
    - createtable.sql
    - Create movie-list page
    - README.md
    - Beautify movie-list page
    - Implement search functionality
    - Implement browse functionality
    - Beautify the main page
    - Extend movie List Page
    - Extend single pages
    - Jump functionality
    - Use PreparedStatement
    - Use Encrypted Password
    - Importing large XML data files into the Fabflix database
    - Domain Registration
    - Full-text Search
    - Connection Pooling
    - Master-Slave Replication
    - Load balancing
    - Fuzzy Search
    - Setting up Docker
    - Setting up Kubernetes
    - Setting up JMeter
    ```

  - shuyic9 (Shuyi Chen)
    ```
    - Create single-movie page
    - Create single-star page
    - Beautify single-movie, single-star page
    - Jump functionality
    - Create demo
    - Implement login page
    - Beautify login page
    - Implement shopping cart
    - Beautify shopping cart
    - Implement payment page
    - Beautify payment page
    - Adding reCAPTCHA
    - Adding HTTPS
    - Implementing a Dashboard using Stored Procedure
    - AutoComplete Search
    - Setting up Docker
    - Setting up Kubernetes
    - Setting up JMeter
    ```


- # Connection Pooling
    - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
      - [AddMovieServlet.java](src/AddMovieServlet.java)
      - [AddStarServlet.java](src/AddStarServlet.java)
      - [CartServlet.java](src/CartServlet.java)
      - [ConfirmationServlet.java](src/ConfirmationServlet.java)
      - [DashboardServlet.java](src/DashboardServlet.java)
      - [LoginDashboardServlet.java](src/LoginDashboardServlet.java)
      - [LoginServlet.java](src/LoginServlet.java)
      - [MainServlet.java](src/MainServlet.java)
      - [MovieListServlet.java](src/MovieListServlet.java)
      - [PaymentServlet.java](src/PaymentServlet.java)
      - [SingleMovieServlet.java](src/SingleMovieServlet.java)
      - [SingleStarServlet.java](src/SingleStarServlet.java)
      - [context.xml](WebContent/META-INF/context.xml)
      - [web.xml](WebContent/WEB-INF/web.xml)

    - #### Explain how Connection Pooling is utilized in the Fablix code.
      - In the [context.xml](WebContent/META-INF/context.xml) file, you can define the configuration of the connection
        pool, specifically through this line:
        ```
          maxTotal="100" maxIdle="30" maxWaitMillis="10000"
        ```
      - In the [web.xml](WebContent/WEB-INF/web.xml) file, you can define the resource reference, specifically through these lines:
        ```
        <resource-ref>
            <description>MySQL DataSource Project1</description>
            <res-ref-name>jdbc/moviedb</res-ref-name>
            <res-type>javax.sql.DataSource</res-type>
            <res-auth>Container</res-auth>
        </resource-ref>
        ```
      - On top of every servlet that needs to connect to the database, define the datasource:
        ```
          @Override
          public void init(ServletConfig config) {
            try {
              dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            } catch (NamingException e) {
              e.printStackTrace();
            }
          }
        ```
      - Then, Use the datasource to get a connection:
        ```
          Connection connection = dataSource.getConnection();
        ```
      - This way, the connection will be reused and not closed after each query, which is more efficient than creating a new connection for each query.

    - #### Explain how Connection Pooling works with two backend SQL.
      - The ```load balancer``` will establish a connection with either the master or the slave database.
      - Since we are using ```connection pooling```, the connection will be reused and not closed after each query to make the connection more efficient.


- # Master/Slave
    - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
      - [AddMovieServlet.java](src/AddMovieServlet.java)
      - [AddStarServlet.java](src/AddStarServlet.java)
      - [CartServlet.java](src/CartServlet.java)
      - [ConfirmationServlet.java](src/ConfirmationServlet.java)
      - [DashboardServlet.java](src/DashboardServlet.java)
      - [LoginDashboardServlet.java](src/LoginDashboardServlet.java)
      - [LoginServlet.java](src/LoginServlet.java)
      - [MainServlet.java](src/MainServlet.java)
      - [MovieListServlet.java](src/MovieListServlet.java)
      - [PaymentServlet.java](src/PaymentServlet.java)
      - [SingleMovieServlet.java](src/SingleMovieServlet.java)
      - [SingleStarServlet.java](src/SingleStarServlet.java)
      - [context.xml](WebContent/META-INF/context.xml)
      - [web.xml](WebContent/WEB-INF/web.xml)

    - #### How read/write requests were routed to Master/Slave SQL?
      - [AddMovieServlet.java](src/AddMovieServlet.java), [AddStarServlet.java](src/AddStarServlet.java), [PaymentServlet.java](src/PaymentServlet.java) are the servlets that write to the database, so they must be routed to the master database.
      - For other servlets, they are read-only, so they are routed to either the master or the slave database.

## Fuzzy Search Design
We leveraged the power of ```User Defined Function(UDF)``` in SQL to implement the fuzzy search, specifically through a library called [Flamingo](https://flamingo.ics.uci.edu/toolkit/).

Flamingo is a toolkit that powered by LEDA algorithm, which is implemented in C and C++. It can be accessed using the interface ```edth```. Here is how it works:
```
edth : boolean edth(string s1, string s1, integer th)
```

```edth``` returns true if the edit distance between s1 and s2 is smaller or equal than th. The function assumes that the strings have the same case (i.e., both strings are lower case or both are upper case).
  
We implemented ```edth``` in our core searching logic in [MovieListServlet.java](src/MovieListServlet.java) and [AutocompleteServlet.java](src/AutocompleteServlet.java). In order to maintain high consistency for the search results, we used the following function to tune the threshold:
```
private int distanceThreshold(String query) {
    if (query.length() <= 4) {
        return 1;
    } else if (query.length() <= 8) {
        return 2;
    } else {
        return 3;
    }
}
```

## Substring Matching Design
In order to search title, director, or stars:
```
Pattern: LIKE %AN% (AN is the keyword you want to search)
```
Any movie that contains the pattern anywhere for the searched condition.

If title is "term", director is "jack", and star is "tom":
```
WHERE title LIKE %term% AND director LIKE %jack% AND star LIKE %tom%
```

## Prepared Statement
We use prepared statement to prevent SQL injection. The prepared statement is used in:
- [AddMovieServlet.java](src/AddMovieServlet.java)
- [AddStarServlet.java](src/AddStarServlet.java)
- [CartServlet.java](src/CartServlet.java)
- [ConfirmationServlet.java](src/ConfirmationServlet.java)
- [DashboardServlet.java](src/DashboardServlet.java)
- [LoginDashboardServlet.java](src/LoginDashboardServlet.java)
- [LoginServlet.java](src/LoginServlet.java)
- [MainServlet.java](src/MainServlet.java)
- [MovieListServlet.java](src/MovieListServlet.java)
- [PaymentServlet.java](src/PaymentServlet.java)
- [SingleMovieServlet.java](src/SingleMovieServlet.java)
- [SingleStarServlet.java](src/SingleStarServlet.java)

## Inconsistent Data Log
- [errors.log](errors.log)

## Two Parsing Time Optimization Strategies
1. We load the original data from database to check if the new data is already exist, which reduces the number of queries.
2. We used the ```LOAD DATA LOCAL INFILE```  feature to load everything we need to add to the database. After creating the csv files, we only need to use the ```LOAD DATA LOCAL INFILE``` feature to load the data at once, which greatly improves the parsing time.
