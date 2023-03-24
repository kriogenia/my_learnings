# Go Bank

Little bank REST API made as a first contact with the language Go. The framework supporting this project is Gin and the database used in it is PostgreSQL using SQLc.

The REST API features six different endpoints:
* `POST /users` to create a new user in the system.
* `POST /users/login` to start a new session.
* `POST /accounts` to create a new bank account.
* `GET /accounts` to retrieve the information of the user accounts, features pagination.
* `GET /accounts/:id` to retrieve the information of a single account.
* `POST /transfers` to create a new money transfer between accounts.


The REST API has the following features:
* Authentication using PASETO and JWT.
* Check of authorization in each operation.
* Data validation for the input fields.
* Easy local deployment with the premade commands in the Makefile.

## Deployment

There's a Docker compose file to run the whole environment with ease, it requires Docker:

```sh
docker compose up
```