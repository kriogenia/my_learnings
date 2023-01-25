package db

import (
	"context"
	"testing"
	"time"

	"github.com/kriogenia/my_learnings/go_bank/util"
	"github.com/stretchr/testify/require"
)

func createRandomTransfer(t *testing.T, from Account, to Account) Transfer {
	args := CreateTransferParams{
		FromAccountID: from.ID,
		ToAccountID:   to.ID,
		Amount:        util.RandomMoney(),
	}

	transfer, err := testQueries.CreateTransfer(context.Background(), args)
	require.NoError(t, err)
	require.NotEmpty(t, transfer)

	require.NotZero(t, transfer.ID)
	require.Equal(t, from.ID, transfer.FromAccountID)
	require.Equal(t, to.ID, transfer.ToAccountID)
	require.Equal(t, args.Amount, transfer.Amount)
	require.NotZero(t, transfer.CreatedAt)

	return transfer
}

func TestCreateTransfer(t *testing.T) {
	from, to := createRandomAccount(t, createRandomUser(t)), createRandomAccount(t, createRandomUser(t))
	createRandomTransfer(t, from, to)
}

func TestGetTransfer(t *testing.T) {
	from, to := createRandomAccount(t, createRandomUser(t)), createRandomAccount(t, createRandomUser(t))
	created := createRandomTransfer(t, from, to)

	retrieved, err := testQueries.GetTransfer(context.Background(), created.ID)
	require.NoError(t, err)
	require.NotEmpty(t, retrieved)

	require.Equal(t, created.ID, retrieved.ID)
	require.Equal(t, created.FromAccountID, retrieved.FromAccountID)
	require.Equal(t, created.ToAccountID, retrieved.ToAccountID)
	require.Equal(t, created.Amount, retrieved.Amount)
	require.WithinDuration(t, created.CreatedAt, retrieved.CreatedAt, time.Second)
}

func TestListTransfers(t *testing.T) {
	from, to := createRandomAccount(t, createRandomUser(t)), createRandomAccount(t, createRandomUser(t))
	for i := 0; i < 10; i++ {
		createRandomTransfer(t, from, to)
	}

	args := ListTransferParams{
		FromAccountID: from.ID,
		ToAccountID:   to.ID,
		Limit:         5,
		Offset:        5,
	}

	transfers, err := testQueries.ListTransfer(context.Background(), args)
	require.NoError(t, err)
	require.Len(t, transfers, 5)

	for _, transfer := range transfers {
		require.NotEmpty(t, transfer)
		require.Equal(t, from.ID, transfer.FromAccountID)
		require.Equal(t, to.ID, transfer.ToAccountID)
	}
}
