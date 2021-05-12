## Development stack
* [Javalin](https://javalin.io) web server
* [Exposed](https://github.com/JetBrains/Exposed) database ORM
* [kotlinx.html](https://github.com/Kotlin/kotlinx.html) HTML DSL

## Dependencies

* [PostgreSQL](https://www.postgresql.org)

## Setting up the project using IntelliJ

* Open the project
* Configure debugging (main class is in src/main/kotlin/app/Application.kt)
* Set environment variables
* Run application

## Environment variables

```
DATABASE_URL="jdbc:postgresql://localhost:5432/database-name"
DATABASE_USER="user"
DATABASE_PASSWORD="password"
```

## Administration

* Create an account by clicking "register"
* Run a query manually and set role "1" in "users" table