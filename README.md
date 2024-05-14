# CS122B-Spring24-Team-XC
This is a UCI CS122B Project 3 made by **Spring 2024 Team XC** (Jun Xia & Shuyi Chen)

---
## Video Demo Link
****
https://youtu.be/0p8sNosDOos

## Application URL
****
https://jackyxia.blog:8443/cs122b-spring24-team-xc-project3/

## Substring Matching Design
****
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
****
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
****
1. We load the original data from database to check if the new data is already exist, which reduces the number of queries.
2. We used the ```LOAD DATA LOCAL INFILE```  feature to load everything we need to add to the database. After creating the csv files, we only need to use the ```LOAD DATA LOCAL INFILE``` feature to load the data at once, which greatly improves the parsing time.

## Contribution
****
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
```