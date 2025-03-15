# todoist-api

Backend in Java Spring with PostgreSQL.

---
## Environmental Variables:
Create a <b>.env</b> file in the root folder with the following properties:
* **DB_USER** - user to be used in creating the Docker instance & logging on the database
* **DB_PASSWORD** - password of the user to be used in creating the Docker instance & logging in
* **DB_NAME** - name of the DB used in PostgreSQL
* **CONNECTION_STRING_DOCKER** - connection string used by Docker
* **CONNECTION_DEFAULT** - default connection string in case app is started manually (requires _online_ **postgres** container with the specified **POSTGRES_DB**)