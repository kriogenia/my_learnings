DB_URL=postgresql://root:secret@localhost:5432/gobank?sslmode=disable
CONTAINER_NAME=gobank-postgres

postgres:
	docker run --name $(CONTAINER_NAME) -p 5432:5432 -e POSTGRES_USER=root -e POSTGRES_PASSWORD=secret -d postgres:15-alpine

startdb:
	docker start $(CONTAINER_NAME)

createdb:
	docker exec -it $(CONTAINER_NAME) createdb --username=root --owner=root gobank

dropdb:
	docker exec -it $(CONTAINER_NAME) dropdb gobank

migrateup:
	migrate -path db/migration -database "$(DB_URL)" -verbose up

migratedown:
	migrate -path db/migration -database "$(DB_URL)" -verbose down

sqlc:
	sqlc generate

test:
	go test -v -cover ./...

server:
	go run main.go

mock:
	mockgen --package mockdb --destination db/mock/store.go github.com/kriogenia/my_learnings/go_bank/db/sqlc Store

.PHONY: postgres startdb createdb dropdb migrateup migratedown sqlc test server mock
