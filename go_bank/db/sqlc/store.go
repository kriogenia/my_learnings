package db

import (
	"context"
	"database/sql"
	"fmt"
)

// Store provides all functions to execute DB queries and transactions
type Store struct {
	*Queries
	db *sql.DB
}

func NewStore(db *sql.DB) *Store {
	return &Store{
		db:      db,
		Queries: New(db),
	}
}

// exectx executes a function within a database transaction
func (store *Store) execTx(ctx context.Context, fn func(*Queries) error) error {
	tx, err := store.db.BeginTx(ctx, nil)
	if err != nil {
		return err
	}

	q := New(tx)
	err = fn(q)
	if err != nil {
		if rbErr := tx.Rollback(); rbErr != nil {
			return fmt.Errorf("tx err: %v, rb err: %v", err, rbErr)
		}
	}

	return tx.Commit()
}

// TransferTxParams contains the input parameters of the transfer transaction
type TransferTxParams struct {
	FromAccountID int64 `json:"from_account_id"`
	ToAccountID   int64 `json:"to_account_id"`
	Amount        int64 `json:"amount"`
}

// TransferTxResult is the result of the transfer transaction
type TransferTxResult struct {
	Transfer    Transfer `json:"transfer"`
	FromAccount Account  `json:"from_account"`
	ToAccount   Account  `json:"to_account"`
	FromEntry   Entry    `json:"from_entry"`
	ToEntry     Entry    `json:"to_entry"`
}

// TransferTx performs a money transfer from one account to the other
// It creates a transfer record, add account entries and update accounts' balance within a single database transaction
func (store *Store) TransferTx(ctx context.Context, arg TransferTxParams) (TransferTxResult, error) {
	var result TransferTxResult

	err := store.execTx(ctx, func(q *Queries) error {
		var err error

		result.Transfer, err = q.CreateTransfer(ctx, CreateTransferParams{
			Amount:        arg.Amount,
			FromAccountID: arg.FromAccountID,
			ToAccountID:   arg.ToAccountID,
		})
		if err != nil {
			return err
		}

		result.FromEntry, err = q.CreateEntry(ctx, CreateEntryParams{
			AccountID: arg.FromAccountID,
			Amount:    -arg.Amount,
		})
		if err != nil {
			return err
		}

		result.ToEntry, err = q.CreateEntry(ctx, CreateEntryParams{
			AccountID: arg.ToAccountID,
			Amount:    arg.Amount,
		})
		if err != nil {
			return err
		}

		if arg.FromAccountID < arg.ToAccountID {
			result.FromAccount, result.ToAccount, err = moveAmount(ctx, q, MoveAmountParams{
				First:          arg.FromAccountID,
				Second:         arg.ToAccountID,
				AmountToSecond: arg.Amount,
			})
		} else {
			result.ToAccount, result.FromAccount, err = moveAmount(ctx, q, MoveAmountParams{
				First:          arg.ToAccountID,
				Second:         arg.FromAccountID,
				AmountToSecond: -arg.Amount,
			})
		}
		return err
	})

	return result, err
}

type MoveAmountParams struct {
	First          int64
	Second         int64
	AmountToSecond int64
}

func moveAmount(ctx context.Context, q *Queries, params MoveAmountParams) (first Account, second Account, err error) {
	first, err = q.AddAccountBalance(ctx, AddAccountBalanceParams{
		ID:     params.First,
		Amount: -params.AmountToSecond,
	})
	if err != nil {
		return
	}
	second, err = q.AddAccountBalance(ctx, AddAccountBalanceParams{
		ID:     params.Second,
		Amount: params.AmountToSecond,
	})
	return
}
