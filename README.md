# todoist-api

Backend in Java Spring with PostgreSQL. 

## Environmental Variables:
Create a `.env` file in the root folder with the following properties:
* **DB_USER** - user to be used in creating the Docker instance & logging on the database.
* **DB_PASSWORD** - password of the user to be used in creating the Docker instance & logging in.
* **DB_NAME** - name of the DB used in PostgreSQL.
* **CONNECTION_STRING_DOCKER** - connection string used by Docker. Value is typically `jdbc:postgresql://db:5432/<db_name>`.
* **CONNECTION_DEFAULT** - default connection string in case app is started manually. Requires _online_ **postgres** container with the specified DB variables. Value is typically `jdbc:postgresql://localhost:5432/<db_name>`.
* **SECRET_KEY** - secret used for validating JWT signature

The `.env` file then needs to be added in the Environmental variables field in IntelliJ. 

When using a dev container, the file will be automatically loaded.

## Profiles:
Use VM option `-Dspring.profiles.active=<profile>` in IntelliJ.
1. **debug** - used when app is launched from the IDE, uses SQLite.
2. **dev** - used when app is launched from Docker, uses PostgreSQL container. Must be configured correctly in `.env`.

## Testing:
Just run `mvn test` and all should be good. Try to not implement features where testing will require environmental variables or other specific changes to Maven / IntelliJ configurations.

You can also run the tests in IntelliJ by rightclicking and running tests on package `src/test/java`.

## Running the application:
After configuring the app correctly using the steps above, the application can be ran directly in IntelliJ or use `docker-compose.yaml` to setup and use the dev container.

1. `http://localhost:8080/register` - with a username & password body to add a user to the database.
2. `http://localhost:8080/login` - with a username & password body to receive a JWT.
3. `http://localhost:8080/api/users` - requires a valid JWT, use `Authorization` header with `Bearer <your JWT>`.
<br>For selecting a specific user, add `/{id}` after the initial URL.
