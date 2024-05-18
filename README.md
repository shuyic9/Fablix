- # General
    - #### Team#: Spring 2024 Team-XC

    - #### Names: Jun Xia & Shuyi Chen

    - #### Project 4 Video Demo Link:

    - #### Instruction of deployment:
      - On AWS instance, clone project 4:

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
      ```


- # Connection Pooling
    - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.

    - #### Explain how Connection Pooling is utilized in the Fabflix code.

    - #### Explain how Connection Pooling works with two backend SQL.


- # Master/Slave
    - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.

    - #### How read/write requests were routed to Master/Slave SQL?
  
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
- [CartServlet.java](src/CartServlet.java)
- [ConfirmationServlet.java](src/ConfirmationServlet.java)
- [LoginServlet.java](src/LoginServlet.java)
- [MainServlet.java](src/MainServlet.java)
- [MovieListServlet.java](src/MovieListServlet.java)
- [PaymentServlet.java](src/PaymentServlet.java)
- [SingleMovieServlet.java](src/SingleMovieServlet.java)
- [SingleStarServlet.java](src/SingleStarServlet.java)

## Inconsistent Data Log
****
- [errors.log](errors.log)

## Two Parsing Time Optimization Strategies
1. We load the original data from database to check if the new data is already exist, which reduces the number of queries.
2. We used the ```LOAD DATA LOCAL INFILE```  feature to load everything we need to add to the database. After creating the csv files, we only need to use the ```LOAD DATA LOCAL INFILE``` feature to load the data at once, which greatly improves the parsing time.
