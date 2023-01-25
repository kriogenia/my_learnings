package api

import (
	"database/sql"
	"errors"
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
	db "github.com/kriogenia/my_learnings/go_bank/db/sqlc"
	"github.com/kriogenia/my_learnings/go_bank/token"
)

type transferRequest struct {
	FromAccountID int64  `json:"from_account_id" binding:"required,min=1"`
	ToAccountID   int64  `json:"to_account_id" binding:"required,min=1"`
	Amount        int64  `json:"amount" binding:"required,gt=0"`
	Currency      string `json:"currency" binding:"required,currency"`
}

func (server *Server) createTransfer(ctx *gin.Context) {
	var req transferRequest
	if err := ctx.ShouldBindJSON(&req); handleError(ctx, err, http.StatusBadRequest) {
		return
	}

	fromAccount, valid := server.validAccount(ctx, req.FromAccountID, req.Currency)
	if !valid {
		return
	}

	authPayload := ctx.MustGet(authorizationPayloadKey).(*token.Payload)
	if fromAccount.Owner != authPayload.UserID {
		err := errors.New("from account doesn't belong to the authenticated user")
		if handleError(ctx, err, http.StatusForbidden) {
			return
		}
	}

	_, valid = server.validAccount(ctx, req.ToAccountID, req.Currency)
	if !valid {
		return
	}

	args := db.TransferTxParams{
		FromAccountID: req.FromAccountID,
		ToAccountID:   req.ToAccountID,
		Amount:        req.Amount,
	}

	result, err := server.store.TransferTx(ctx, args)
	if handleError(ctx, err, http.StatusInternalServerError) {
		return
	}

	ctx.JSON(http.StatusCreated, result)
}

func (server *Server) validAccount(ctx *gin.Context, accountID int64, currency string) (db.Account, bool) {
	account, err := server.store.GetAccount(ctx, accountID)
	if err == sql.ErrNoRows {
		return account, !handleError(ctx, err, http.StatusNotFound)
	}
	if handleError(ctx, err, http.StatusInternalServerError) {
		return account, false
	}

	if account.Currency != currency {
		err := fmt.Errorf("account {%d} currency mistmatch: %s vs %s", accountID, account.Currency, currency)
		return account, !handleError(ctx, err, http.StatusBadRequest)
	}

	return account, true
}
