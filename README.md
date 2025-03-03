# Bank Account Management API

## Cloning the Repository

To get started, clone the repository locally using the following command:

```sh
https://github.com/Yarynych/bank-account-management-api.git
cd your-repository
```

## Technology Stack

Make sure you have the following technologies installed:

- **Java 17** - [Download](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html)
- **Spring Boot 3.3.9** - [Documentation](https://docs.spring.io/spring-boot/docs/3.3.9/reference/html/)
- **Gradle 8.12.1** - [Download](https://gradle.org/releases/)
- **MySQL 8.2.0** - [Download](https://dev.mysql.com/downloads/)

## Configuration Files

Before launching the project, update the necessary configuration files:

- **Application configuration file:** `src/main/resources/application.yaml`
  - Update database connection details
  - Configure additional settings as needed

### Configuration example:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database
    username: root
    password: password
```

## Database Initialization

To create the necessary tables and populate the database with initial data:

1. Find the SQL script: `src/main/resources/db/V1_create_main_tables.sql`
2. Execute it in MySQL database:

```sh
mysql -u root -p your_database < src/main/resources/db/V1_create_main_tables.sql
```

## Running the Application

After setting up the configuration, run the application using Gradle:

```sh
./gradlew bootRun
```

## Generating JaCoCo Report

To generate a code coverage report using **JaCoCo**, run the command:

```sh
./gradlew clean test jacocoTestReport
```

The report will be available at:

```
build/reports/jacoco/test/html/index.html
```

Open this file in a browser to view coverage details.

## Swagger

- **Swagger** - [Documentation](http://localhost:8080/swagger-ui/index.html)

