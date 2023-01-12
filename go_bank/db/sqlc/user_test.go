package db

import (
	"context"
	"testing"
	"time"

	"github.com/kriogenia/my_learnings/go_bank/util"
	"github.com/stretchr/testify/require"
)

func createRandomUser(t *testing.T) User {
	arg := CreateUserParams{
		Username:       util.RandomOwner(),
		HashedPassword: "pass",
		FullName:       util.RandomOwner(),
		Email:          util.RandomEmail(),
	}

	user, err := testQueries.CreateUser(context.Background(), arg)
	require.NoError(t, err)
	require.NotEmpty(t, user)

	require.NotZero(t, user.ID)
	require.Equal(t, arg.Username, user.Username)
	require.Equal(t, arg.HashedPassword, user.HashedPassword)
	require.Equal(t, arg.FullName, user.FullName)
	require.NotZero(t, user.CreatedAt)
	require.NotZero(t, user.PasswordChangeAt)

	return user
}

func TestCreateUser(t *testing.T) {
	createRandomUser(t)
}

func TestGetUser(t *testing.T) {
	created := createRandomUser(t)

	retrieved, err := testQueries.GetUser(context.Background(), created.ID)
	require.NoError(t, err)
	require.NotEmpty(t, retrieved)

	require.Equal(t, created.ID, retrieved.ID)
	require.Equal(t, created.Username, retrieved.Username)
	require.Equal(t, created.HashedPassword, retrieved.HashedPassword)
	require.Equal(t, created.FullName, retrieved.FullName)
	require.WithinDuration(t, created.PasswordChangeAt, retrieved.PasswordChangeAt, time.Second)
	require.WithinDuration(t, created.CreatedAt, retrieved.CreatedAt, time.Second)
}
