# todoist-api

Backend in Java Spring with PostgreSQL. 

## Environmental Variables:
Create a `.env` file in the root folder with the following properties:
* **DB_USER** - user to be used in creating the Docker instance & logging on the database.
* **DB_PASSWORD** - password of the user to be used in creating the Docker instance & logging in.
* **DB_NAME** - name of the DB used in PostgreSQL.
* **CONNECTION_STRING_DOCKER** - connection string used by Docker. Value is typically `jdbc:postgresql://db:5432/${DB_NAME}`.
* **CONNECTION_DEFAULT** - default connection string in case app is started manually. Requires _online_ **postgres** container with the specified DB variables. Value is typically `jdbc:postgresql://localhost:5432/${DB_NAME}`.
* **SECRET_KEY** - secret used for validating JWT signature

The `.env` file then needs to be added as Environmental variables field in IntelliJ. The file will be automatically loaded when used in the dev container.

## Profiles:
Use VM option `--Dspring.profiles.active=<profile>`.
1. **debug** - used when app is launched from the IDE, uses SQLite.
2. **dev** - used when app is launched from Docker, uses PostgreSQL container. Must be configured correctly in `.env`.