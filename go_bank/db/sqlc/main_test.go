package db

import (
	"database/sql"
	"log"
	"os"
	"testing"

	_ "github.com/lib/pq"
)

const (
	dbDriver = "postgres"
	dbSource = "postgresql://root:secret@localhost:5432/gobank?sslmode=disable"
)

var testDB *sql.DB
var testQueries *Queries

func TestMain(m *testing.M) {
	var err error

	testDB, err = sql.Open(dbDriver, dbSource)
	if err != nil {
		log.Fatal("unable to connect to db: ", err)
	}

	testQueries = New(testDB)

	os.Exit(m.Run())
}
