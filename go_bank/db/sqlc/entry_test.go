package db

import (
	"context"
	"testing"
	"time"

	"github.com/kriogenia/my_learnings/go_bank/util"
	"github.com/stretchr/testify/require"
)

func createRandomEntry(t *testing.T, account Account) Entry {
	args := CreateEntryParams{
		AccountID: account.ID,
		Amount:    util.RandomMoney(),
	}

	entry, err := testQueries.CreateEntry(context.Background(), args)
	require.NoError(t, err)
	require.NotEmpty(t, entry)

	require.NotZero(t, entry.ID)
	require.Equal(t, account.ID, entry.AccountID)
	require.Equal(t, args.Amount, entry.Amount)
	require.NotZero(t, entry.CreatedAt)

	return entry
}

func TestCreateEntry(t *testing.T) {
	account := createRandomAccount(t, createRandomUser(t))
	createRandomEntry(t, account)
}

func TestGetEntry(t *testing.T) {
	account := createRandomAccount(t, createRandomUser(t))
	created := createRandomEntry(t, account)

	retrieved, err := testQueries.GetEntry(context.Background(), created.ID)
	require.NoError(t, err)
	require.NotEmpty(t, retrieved)

	require.Equal(t, created.ID, retrieved.ID)
	require.Equal(t, created.Amount, retrieved.Amount)
	require.Equal(t, created.AccountID, retrieved.AccountID)
	require.WithinDuration(t, created.CreatedAt, retrieved.CreatedAt, time.Second)
}

func TestListEntries(t *testing.T) {
	account := createRandomAccount(t, createRandomUser(t))
	for i := 0; i < 10; i++ {
		createRandomEntry(t, account)
	}

	args := ListEntriesParams{
		AccountID: account.ID,
		Limit:     5,
		Offset:    5,
	}

	entries, err := testQueries.ListEntries(context.Background(), args)
	require.NoError(t, err)
	require.Len(t, entries, 5)

	for _, entry := range entries {
		require.NotEmpty(t, entry)
		require.Equal(t, account.ID, entry.AccountID)
	}
}
