How to run

Option 1: Built and run with gradle in terminal (Linux terminal/Powershell/cmd) locally. WARNING: the build and development was done on linux.

1) Make sure that at least JAVA 17 is installed on your machine. If needed export your JAVA HOME and path to JAVA HOME in the terminal (This is a fix for failed compileJava task).

2) Go to the project folder and make sure it contains build.gradle and /src/main/resources/application.properties file in it. In terminal make sure you are on the path of the project (terminal is open in the project directory)

3) Create a file called /src/main/resources/application-local.properties that follows the structure of application.properties and populate it with your configuration. (Don't include spring.profiles.active line in the local properties file)
   
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

4) Run the application in docker with:
	docker compose up
