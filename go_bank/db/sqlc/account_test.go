package db

import (
	"context"
	"database/sql"
	"testing"
	"time"

	"github.com/kriogenia/my_learnings/go_bank/util"
	"github.com/stretchr/testify/require"
)

func createRandomAccount(t *testing.T) Account {
	arg := CreateAccountParams{
		Owner:    util.RandomOwner(),
		Balance:  util.RandomMoney(),
		Currency: util.RandomCurrency(),
	}

	account, err := testQueries.CreateAccount(context.Background(), arg)
	require.NoError(t, err)
	require.NotEmpty(t, account)

	require.NotZero(t, account.ID)
	require.Equal(t, arg.Owner, account.Owner)
	require.Equal(t, arg.Balance, account.Balance)
	require.Equal(t, arg.Currency, account.Currency)
	require.NotZero(t, account.CreatedAt)

	return account
}

func TestCreateAccount(t *testing.T) {
	createRandomAccount(t)
}

func TestGetAccount(t *testing.T) {
	created := createRandomAccount(t)

	retrieved, err := testQueries.GetAccount(context.Background(), created.ID)
	require.NoError(t, err)
	require.NotEmpty(t, retrieved)

	require.Equal(t, created.ID, retrieved.ID)
	require.Equal(t, created.Owner, retrieved.Owner)
	require.Equal(t, created.Balance, retrieved.Balance)
	require.Equal(t, created.Currency, retrieved.Currency)
	require.WithinDuration(t, created.CreatedAt, retrieved.CreatedAt, time.Second)
}

func TestGetAccountForUpdate(t *testing.T) {
	created := createRandomAccount(t)

	retrieved, err := testQueries.GetAccountForUpdate(context.Background(), created.ID)
	require.NoError(t, err)
	require.NotEmpty(t, retrieved)

	require.Equal(t, created.ID, retrieved.ID)
	require.Equal(t, created.Owner, retrieved.Owner)
	require.Equal(t, created.Balance, retrieved.Balance)
	require.Equal(t, created.Currency, retrieved.Currency)
	require.WithinDuration(t, created.CreatedAt, retrieved.CreatedAt, time.Second)
}

func TestUpdateAccount(t *testing.T) {
	created := createRandomAccount(t)
	newBalance := util.RandomMoney()

	arg := UpdateAccountParams{
		Balance: newBalance,
		ID:      created.ID,
	}

	retrieved, err := testQueries.UpdateAccount(context.Background(), arg)
	require.NoError(t, err)
	require.NotEmpty(t, retrieved)

	require.Equal(t, created.ID, retrieved.ID)
	require.Equal(t, created.Owner, retrieved.Owner)
	require.Equal(t, newBalance, retrieved.Balance)
	require.Equal(t, created.Currency, retrieved.Currency)
	require.WithinDuration(t, created.CreatedAt, retrieved.CreatedAt, time.Second)
}

func TestAddAccountBalance(t *testing.T) {
	created := createRandomAccount(t)
	amount := util.RandomMoney()

	arg := AddAccountBalanceParams{
		ID:     created.ID,
		Amount: amount,
	}

	retrieved, err := testQueries.AddAccountBalance(context.Background(), arg)
	require.NoError(t, err)
	require.NotEmpty(t, retrieved)

	require.Equal(t, created.ID, retrieved.ID)
	require.Equal(t, created.Owner, retrieved.Owner)
	require.Equal(t, created.Balance+amount, retrieved.Balance)
	require.Equal(t, created.Currency, retrieved.Currency)
	require.WithinDuration(t, created.CreatedAt, retrieved.CreatedAt, time.Second)

}

func TestDeleteAccount(t *testing.T) {
	created := createRandomAccount(t)

	err := testQueries.DeleteAccount(context.Background(), created.ID)
	require.NoError(t, err)

	retrieved, err := testQueries.GetAccount(context.Background(), created.ID)
	require.Error(t, err)
	require.EqualError(t, err, sql.ErrNoRows.Error())
	require.Empty(t, retrieved)
}

func TestListAccounts(t *testing.T) {
	for i := 0; i < 10; i++ {
		createRandomAccount(t)
	}

	arg := ListAccountsParams{
		Limit:  5,
		Offset: 5,
	}

	accounts, err := testQueries.ListAccounts(context.Background(), arg)
	require.NoError(t, err)
	require.Len(t, accounts, 5)

	for _, account := range accounts {
		require.NotEmpty(t, account)
	}
}
