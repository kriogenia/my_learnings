DB_URL=postgresql://root:secret@localhost:5432/gobank?sslmode=disable
GO_PATH=~/go/bin

postgres:
	docker run --name gobank-postgres -p 5432:5432 -e POSTGRES_USER=root -e POSTGRES_PASSWORD=secret -d postgres:15-alpine

createdb:
	docker exec -it gobank-postgres createdb --username=root --owner=root gobank

dropdb:
	docker exec -it gobank-postgres dropdb gobank

migrateup:
	$(GO_PATH)/migrate -path db/migration -database "$(DB_URL)" -verbose up

migratedown:
	$(GO_PATH)/migrate -path db/migration -database "$(DB_URL)" -verbose down

sqlc:
	$(GO_PATH)/sqlc generate

test:
	go test -v -cover ./...

.PHONY: postgres createdb dropdb migrateup migratedown
