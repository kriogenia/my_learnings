package db

import (
	"context"
	"testing"

	"github.com/stretchr/testify/require"
)

func TestTransferTx(t *testing.T) {
	store := NewStore(testDB)

	from := createRandomAccount(t)
	to := createRandomAccount(t)

	// run n concurrent transfer transactions
	n := 5
	amount := int64(10)

	errs := make(chan error)
	results := make(chan TransferTxResult)

	for i := 0; i < n; i++ {
		go func() {
			args := TransferTxParams{
				Amount:        amount,
				FromAccountID: from.ID,
				ToAccountID:   to.ID,
			}
			result, err := store.TransferTx(context.Background(), args)

			errs <- err
			results <- result
		}()
	}

	for i := 0; i < n; i++ {
		err := <-errs
		require.NoError(t, err)

		result := <-results
		require.NotEmpty(t, result)

		transfer := result.Transfer
		require.NotEmpty(t, transfer)
		require.NotZero(t, transfer.ID)
		require.Equal(t, transfer.Amount, amount)
		require.Equal(t, transfer.FromAccountID, from.ID)
		require.Equal(t, transfer.ToAccountID, to.ID)
		require.NotZero(t, transfer.CreatedAt)

		_, err = store.GetTransfer(context.Background(), transfer.ID)
		require.NoError(t, err)

		fromEntry := result.FromEntry
		require.NotEmpty(t, fromEntry)
		require.NotZero(t, fromEntry.ID)
		require.Equal(t, fromEntry.AccountID, from.ID)
		require.Equal(t, fromEntry.Amount, -amount)
		require.NotZero(t, fromEntry.CreatedAt)

		_, err = store.GetEntry(context.Background(), fromEntry.ID)
		require.NoError(t, err)

		toEntry := result.ToEntry
		require.NotEmpty(t, toEntry)
		require.NotZero(t, toEntry.ID)
		require.Equal(t, toEntry.AccountID, to.ID)
		require.Equal(t, toEntry.Amount, amount)
		require.NotZero(t, toEntry.CreatedAt)

		_, err = store.GetEntry(context.Background(), toEntry.ID)
		require.NoError(t, err)

		fromAccount := result.FromAccount
		require.NotEmpty(t, result.FromAccount)
		require.Equal(t, fromAccount.ID, from.ID)

		toAccount := result.ToAccount
		require.NotEmpty(t, result.ToAccount)
		require.Equal(t, toAccount.ID, to.ID)

		fromDiff := from.Balance - fromAccount.Balance
		toDiff := to.Balance - toAccount.Balance
		require.Equal(t, fromDiff, -toDiff)
		require.Positive(t, fromDiff)
		require.True(t, fromDiff%amount == 0)

		k := int(fromDiff / amount)
		require.True(t, k >= 1 && k <= n)
	}

	updatedFromAccount, err := store.GetAccount(context.Background(), from.ID)
	require.NoError(t, err)

	updateToAccount, err := store.GetAccount(context.Background(), to.ID)
	require.NoError(t, err)

	require.Equal(t, from.Balance-int64(n)*amount, updatedFromAccount.Balance)
	require.Equal(t, to.Balance+int64(n)*amount, updateToAccount.Balance)

}
