DB_URL=postgresql://root:secret@localhost:5432/gobank?sslmode=disable
APP_IMAGE_NAME=gobank
APP_CONTAINER_NAME=gobank
NETWORK_NAME=gobank-network
POSTGRES_CONTAINER_NAME=gobank-postgres

build:
	docker build -t $(APP_IMAGE_NAME):latest .

release:
	docker run --name $(APP_CONTAINER_NAME) --network $(NETWORK_NAME) -p 8080:8080 -e GIN_MODE=release -e DB_SOURCE="postgresql://root:secret@$(POSTGRES_CONTAINER_NAME):5432/gobank?sslmode=disable" $(APP_IMAGE_NAME):latest

postgres:
	docker run --name $(POSTGRES_CONTAINER_NAME) --network $(NETWORK_NAME) -p 5432:5432 -e POSTGRES_USER=root -e POSTGRES_PASSWORD=secret -d postgres:15-alpine

startdb:
	docker start $(POSTGRES_CONTAINER_NAME)

createdb:
	docker exec -it $(POSTGRES_CONTAINER_NAME) createdb --username=root --owner=root gobank

dropdb:
	docker exec -it $(POSTGRES_CONTAINER_NAME) dropdb gobank

migrateup:
	migrate -path db/migration -database "$(DB_URL)" -verbose up

migrateuponce:
	migrate -path db/migration -database "$(DB_URL)" -verbose up 1

migratedown:
	migrate -path db/migration -database "$(DB_URL)" -verbose down

migratedownonce:
	migrate -path db/migration -database "$(DB_URL)" -verbose down 1

sqlc:
	sqlc generate

test:
	go test -v -cover ./...

server:
	go run main.go

mock:
	mockgen --package mockdb --destination db/mock/store.go github.com/kriogenia/my_learnings/go_bank/db/sqlc Store

network:
	docker network create $(NETWORK_NAME)

.PHONY: build release postgres startdb createdb dropdb migrateup migrateuponce migratedown migratedownonce sqlc test server mock network
