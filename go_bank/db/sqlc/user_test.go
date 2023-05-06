package db

import (
	"context"
	"database/sql"
	"testing"
	"time"

	"github.com/kriogenia/my_learnings/go_bank/util"
	"github.com/stretchr/testify/require"
)

func createRandomPassword(t *testing.T) string {
	hashedPassword, err := util.HashPassword(util.RandomString(6))
	require.NoError(t, err)
	require.NotEmpty(t, hashedPassword)
	return hashedPassword

}

func createRandomUser(t *testing.T) User {
	arg := CreateUserParams{
		Username:       util.RandomOwner(),
		HashedPassword: createRandomPassword(t),
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

func TestGetUserByUsername(t *testing.T) {
	created := createRandomUser(t)

	retrieved, err := testQueries.GetUserByUsername(context.Background(), created.Username)
	require.NoError(t, err)
	require.NotEmpty(t, retrieved)

	require.Equal(t, created.ID, retrieved.ID)
	require.Equal(t, created.Username, retrieved.Username)
	require.Equal(t, created.HashedPassword, retrieved.HashedPassword)
	require.Equal(t, created.FullName, retrieved.FullName)
	require.WithinDuration(t, created.CreatedAt, retrieved.CreatedAt, time.Second)
}

func TestUpdateUser(t *testing.T) {
	created := createRandomUser(t)
	arg := UpdateUserParams{
		ID:             created.ID,
		Username:       sql.NullString{String: util.RandomOwner(), Valid: true},
		HashedPassword: sql.NullString{String: createRandomPassword(t), Valid: true},
		FullName:       sql.NullString{String: util.RandomOwner(), Valid: true},
		Email:          sql.NullString{String: util.RandomEmail(), Valid: true},
	}

	updated, err := testQueries.UpdateUser(context.Background(), arg)
	require.NoError(t, err)
	require.NotEmpty(t, updated)

	require.Equal(t, created.ID, updated.ID)
	require.Equal(t, arg.Username.String, updated.Username)
	require.Equal(t, arg.HashedPassword.String, updated.HashedPassword)
	require.Equal(t, arg.FullName.String, updated.FullName)
	require.WithinDuration(t, created.CreatedAt, updated.CreatedAt, time.Second)
}
