package db

import (
	"context"
	"database/sql"
	"testing"
	"time"

	"github.com/kriogenia/my_learnings/go_bank/util"
	"github.com/stretchr/testify/require"
)

func createRandomAccount(t *testing.T, user User) Account {
	arg := CreateAccountParams{
		Owner:    user.ID,
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
	createRandomAccount(t, createRandomUser(t))
}

func TestGetAccount(t *testing.T) {
	created := createRandomAccount(t, createRandomUser(t))

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
	created := createRandomAccount(t, createRandomUser(t))

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
	created := createRandomAccount(t, createRandomUser(t))
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
	created := createRandomAccount(t, createRandomUser(t))
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
	created := createRandomAccount(t, createRandomUser(t))

	err := testQueries.DeleteAccount(context.Background(), created.ID)
	require.NoError(t, err)

	retrieved, err := testQueries.GetAccount(context.Background(), created.ID)
	require.Error(t, err)
	require.EqualError(t, err, sql.ErrNoRows.Error())
	require.Empty(t, retrieved)
}

func TestListAccounts(t *testing.T) {
	var lastAccount Account
	for i := 0; i < 10; i++ {
		lastAccount = createRandomAccount(t, createRandomUser(t))
	}

	arg := ListAccountsParams{
		Owner:  lastAccount.Owner,
		Limit:  5,
		Offset: 0,
	}

	accounts, err := testQueries.ListAccounts(context.Background(), arg)
	require.NoError(t, err)
	require.NotEmpty(t, accounts)

	for _, account := range accounts {
		require.NotEmpty(t, account)
		require.Equal(t, lastAccount.Owner, account.Owner)
	}
}
