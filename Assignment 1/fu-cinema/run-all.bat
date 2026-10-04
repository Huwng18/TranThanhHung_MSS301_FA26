@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot
set PATH=%JAVA_HOME%\bin;C:\apache-maven-3.9.12\bin;%PATH%
echo Using Java version:
java -version

echo Building customer-service...
cd customer-service
call mvn clean package -DskipTests
cd ..

echo Building movie-service...
cd movie-service
call mvn clean package -DskipTests
cd ..

echo Building booking-service...
cd booking-service
call mvn clean package -DskipTests
cd ..

echo Building api-gateway...
cd api-gateway
call mvn clean package -DskipTests
cd ..

echo Starting customer-service (Port 8081)...
start "customer-service" cmd /k "set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot && set PATH=%%JAVA_HOME%%\bin;%%PATH%% && cd customer-service\target && java -jar customer-service-0.0.1-SNAPSHOT.jar"

echo Starting movie-service (Port 8082)...
start "movie-service" cmd /k "set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot && set PATH=%%JAVA_HOME%%\bin;%%PATH%% && cd movie-service\target && java -jar movie-service-0.0.1-SNAPSHOT.jar"

echo Starting booking-service (Port 8083)...
start "booking-service" cmd /k "set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot && set PATH=%%JAVA_HOME%%\bin;%%PATH%% && cd booking-service\target && java -jar booking-service-0.0.1-SNAPSHOT.jar"

echo Starting api-gateway (Port 9000)...
start "api-gateway" cmd /k "set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot && set PATH=%%JAVA_HOME%%\bin;%%PATH%% && cd api-gateway\target && java -jar api-gateway-0.0.1-SNAPSHOT.jar"

echo All services are starting up via JARs on Java 17! Please wait 15 seconds before running Postman.
