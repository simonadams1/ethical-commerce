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

## Database Migration

Flywheel is used for database migrations.

Back up your development database before working on migration scripts.

To add a new migration script, add a new file in `resources/db.migration` according to instructions specified in `V1__initialize-migrations.sql` file.

To test if an upgrade script works well, duplicate your backed up database, set environment variables to point to it, and launch the application in debug mode to let scripts run using the same process as it would run in production.
