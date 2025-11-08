How to run

Option 1: Built and run with gradle in terminal (Powershell/cmd) locally

1) Make sure that at least JAVA 17 is installed on your machine

2) Go to the project folder and make sure it contains build.gradle and application.properties file in it. In terminal make sure you are on the path of the project (terminal is open in the project directory)

3) Create a file called application-local.properties that follows the structure of application.properties and populate it with your configuration.
   Should look similar to the configuration in image but with your values
   <img width="649" height="138" alt="image" src="https://github.com/user-attachments/assets/58d582ff-45b0-4489-a2a7-96a3e085eb1f" />

4) In terminal type in:
	(On Linux) ./gradlew clean build
	(On windows Powershell/cmd) gradlew.bat clean build
5) Run the application with one of the options:
	a) ./gradlew bootRun
	b) java -jar build/libs/QueriesTask-0.0.1-SNAPSHOT.jar

Option 2: Docker (however you will need to edit the docker-compose file suit you and load the data for the containerized database)

1)  Go to the project folder and make sure it contains Dockerfile and docker-compose.yml files in it. In terminal make sure you are on the path of the project (terminal is open in the project directory)

2) Go to  application.properties file and comment out spring.profiles.active=local
   It should not be an issue if there is no application-local.properties file present in the folder, but do it to be sure.

3) In terminal type in:
 	docker compose build


Design process:


1) Three requirements were listed: Ability to add and save a text based SQL query, Ability to review all currently added queries, Receive the result of a query as a 2d array. Out of these the main one is the receiving of the result of SQL query for a database.

2) First to think about is connection to the database. Most of the time I used Hibernate. However as there is no entities involved, because we need to just execute SQL commands and that the application had to be simple., decision was made to use JDBC Template. As it has the ability to execute any SQL command that is passed to it as a String (and because I used it previously in university assignments)

3) JdbcTemplateRepository class was created and annotated with @Repository.
Application properties had to be created to specify the configuration of connection to the database. Only placeholders where added to the properties file and spring profiles local added for spring to get only values from local properties file were used. Mainly done to prevent password leaks to the public repository in case of actual usage. (Not really needed if the database is purely local or temporary)

Query class with Long id and String query was created for future use, annotated with @Component to have Spring discover it. (Component because it is not something I would be saving to a db for this implementation)

In JdbcTemplateRepository method for getting query results in form of a List with map was added to receive query results in easy to parse format. @Transactional(readOnly=true) annotation was added to the method to prevent queries from modifying database data (which is one of the bonus requirements of the assignment)

WARNING: Setting a read only annotation for the access method does not protect the db from SQL injection a 100%, not to mention that there can be a situation where the database will outright ignore the read only setting made by annotation. Recommended to have the ‘user’ of the database have only Read Only access.

4) Added Lombok mainly for the builder pattern it gives out of the box. Reduced the the amount of code in Query as a bonus.

5) Moved to the Service layer and created an interface for the query service to follow. In the interface added methods for the main requirements.

Created a realization of the interface in form of RuntimeQueryService class, which got annotated with @Service annotation. Runtime because all the information it holds will disappear when the application is shutdown or instance of the service is deleted (in this case the service singleton). 

For storing the queries a Collection of Query objects was added.

As the dataset does not change, a Map of String (SQL query) and Object[][] (query result) was added to serve as a cache to not repeat same queries as it can take a long time (which is one of the bonus requirements of the assignment)

Methods of the interface were realized.
Initially method for add query returned a void, but this was a mistake and later was changed to Long to return a the id of the query added. Creates a new Query object with the builder. The id is added from the counter in the class.

Method to get queries just returns the list of queries that were added.

Method to get query results first gets the String of the query, removes it from the list of queries that were initially added. Then look up the Sting in the map of already completed queries. If found, return the already computed results, otherwise pass the query to the repository to get the results. It is either successful or throws an exception which is handled on upper layers. In case of success, values of map in the resulting list is placed into a Object[][] 2d array. Object[][] because I do not know what kind of results I will get.

However, deleting the queries after usage is not good from business logic perspective if it is used for analytics. So in a hotfix, the list to save added queries was swapped out for a set, a modified equals and hashcode that use only String to determine uniqueness. Method add was modified to check in the set for such query first and return the id if it was already added to the set, otherwise add and return the new id.

Also the method for getting results may throw two exceptions in case of required query not found and when it is not possible to execute the query due to it violation the read-only or it is not possible to get the data from the database for various reasons (db empty, no connection)

Also added some getter methods with package-private level of access for testing purposes. 

WARNING: having the service made this way is terrible decision design-wise as it does not scale and information in it disappears when instance is destroyed.
Using a counter in class is bad as it also does not scale. Also it is not static and not atomic. In case of not static, with spring creating the bean as a singleton by default, I did not think it was critical later, at the moment of creation I just forgot about it after creation. 
Not having the counter atomic would become a problem if multi-threading was added to the application. (Thoughts about it later)
If the data changes at some point a restart would be required to clear the ‘cache’. 
Using collections locally is not good as, once again, data loss is a problem in case of outage. I had an idea about using a database table for storing queries (possibly with hash as a determiner for unique queries) and the results of executed queries. This way if the service is down, no progress would be lost and in case of data update on the main dataset, the queries table could just be cleared. This would also solve the problem of an id counter in service. But for this assignment I did not think such solution was necessary and this would require to hold a database table for queries.

6) Moved to presentation layer. Created a controller interface with the required interface and a class realizing the interface.

In the class methods for adding and getting queries just delegate the functionality to the service layer. Method to get the query results also handles the exceptions thrown by the corresponding method on the service layer by returning 404 if query not found or 400 if the query violates something or cannot be executed.


FINAL THOUGHTS

1) Long-running queries.

In this bonus assignment I think it is hinted that I probably should use multi-threading.

Thought 1: @Async in Spring. But then I would need to probably create some task executor and would need to create some way to notify the users that the query was finished. 

Thought 2: Job queue. Have some multi-threaded method running in the background taking queries from the queue when they are placed there and executing them. Also would need a way to notify the users that the query was finished.

Thought 3: Queries have status. Have the queries be accepted by the service and executed in the background. Have the list of queries have the status of running, finished, waiting and have an endpoint to see the result of some query that is finished.

Thought 4: Sub-queries. As SQL queries often consist of sub-queries, we can break down a query for execution and save the results of sub-queries, later we could see is the main query has the sub-queries that were executed before and substitute the results.

Solution for this bonus assignment was not provided as I have little experience when it comes to creating multi-threaded solutions.

2) Integration testing. Not done as I do not have enough experience to make actual integration testing.

3) H2 database. For some reason I could not load the dataset into the database, such that the JDBC template could see the data. I switched to postgres, since I have it on my machine and I am more familiar with it.


5) Run the application in docker with:
	docker compose up
