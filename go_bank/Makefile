DB_URL=postgresql://root:secret@localhost:5432/gobank?sslmode=disable
CONTAINER_NAME=gobank-postgres
GO_PATH=~/go/bin

postgres:
	docker run --name $(CONTAINER_NAME) -p 5432:5432 -e POSTGRES_USER=root -e POSTGRES_PASSWORD=secret -d postgres:15-alpine

startdb:
	docker start $(CONTAINER_NAME)

createdb:
	docker exec -it $(CONTAINER_NAME) createdb --username=root --owner=root gobank

dropdb:
	docker exec -it $(CONTAINER_NAME) dropdb gobank

migrateup:
	$(GO_PATH)/migrate -path db/migration -database "$(DB_URL)" -verbose up

migratedown:
	$(GO_PATH)/migrate -path db/migration -database "$(DB_URL)" -verbose down

sqlc:
	$(GO_PATH)/sqlc generate

test:
	go test -v -cover ./...

.PHONY: postgres startdb createdb dropdb migrateup migratedown
