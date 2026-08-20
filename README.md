REST Query storage and execution engine. Queries can be added and stored in a database (PostgreSQL) and later executed for data in another database for analytics. Supports giving results in a batch (traditional approach) or in a stream.

REQUIREMENTS:

JAVA, PostgreSQL, Clickhouse.


How to run

Option 1: Built and run with gradle in terminal (Linux terminal/Powershell/cmd) localy. WARNING: the build and development was done on linux.

1) Make sure that at least JAVA 17 is installed on your machine. If needed export your JAVA HOME and path to JAVA HOME in the terminal (This is a fix for failed compileJava task).

2) Go to the project folder and make sure it contains build.gradle and /src/main/resources/application.properties file in it. In terminal make sure you are on the path of the project (terminal is open in the project directory)

3) Create a file called /src/main/resources/application-local.properties that follows the structure of application.properties and populate it with your values. (Don't include spring.profiles.active line in the local properties file)

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

4) Run the application in docker with:
	docker compose up


ADDITIONALLY:
To replace the storage database for queries, replace the config for primary db source.