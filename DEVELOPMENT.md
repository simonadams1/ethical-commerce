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

It's best to setup environment variables at the operating system level.

The ones that are set in intellij for example, can't be read from a gradle build script(or at least I couldn't do it).

In ubuntu for example, you'd put them in `/etc/environment`. Log out is required for changes to take effect.

```
DB_URL="postgresql://localhost:5432/database-name"
DATABASE_USER="user"
DATABASE_PASSWORD="password"
HOST="http://localhost:8080"
```

## Administration

* Create an account by clicking "register"
* Run a query manually and set role "1" in "users" table