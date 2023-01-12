package api

import (
	"database/sql"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	db "github.com/kriogenia/my_learnings/go_bank/db/sqlc"
	"github.com/lib/pq"
)

type createAccountRequest struct {
	Owner    int64  `json:"owner" binding:"required"`
	Currency string `json:"currency" binding:"required,currency"`
}

func (server *Server) createAccount(ctx *gin.Context) {
	var req createAccountRequest
	if err := ctx.ShouldBindJSON(&req); handleError(ctx, err, http.StatusBadRequest) {
		return
	}

	args := db.CreateAccountParams{
		Owner:    req.Owner,
		Balance:  0,
		Currency: req.Currency,
	}

	account, err := server.store.CreateAccount(ctx, args)
	if err != nil {
		var status = http.StatusInternalServerError
		if pqErr, ok := err.(*pq.Error); ok {
			switch pqErr.Code.Name() {
			case "foreign_key_violation", "unique_violation":
				status = http.StatusForbidden
			}
		}
		if handleError(ctx, err, status) {
			return
		}
	}

	ctx.JSON(http.StatusCreated, account)
}

type getAccountByIdRequest struct {
	ID int64 `uri:"id" binding:"required,min=1"`
}

func (server *Server) getAccountById(ctx *gin.Context) {
	var req getAccountByIdRequest
	if err := ctx.ShouldBindUri(&req); handleError(ctx, err, http.StatusBadRequest) {
		return
	}

	account, err := server.store.GetAccount(ctx, req.ID)
	if err == sql.ErrNoRows || handleError(ctx, err, http.StatusInternalServerError) {
		handleError(ctx, err, http.StatusNotFound)
		return
	}

	ctx.JSON(http.StatusOK, account)
}

func (server *Server) getAccounts(ctx *gin.Context) {
	limit, err := strconv.Atoi(ctx.DefaultQuery("limit", "10"))
	if handleError(ctx, err, http.StatusBadRequest) {
		return
	}

	offset, err := strconv.Atoi(ctx.DefaultQuery("offset", "0"))
	if handleError(ctx, err, http.StatusBadRequest) {
		return
	}

	args := db.ListAccountsParams{
		Limit:  int32(limit),
		Offset: int32(offset),
	}

	accounts, err := server.store.ListAccounts(ctx, args)
	if handleError(ctx, err, http.StatusInternalServerError) {
		return
	}

	ctx.JSON(http.StatusOK, accounts)
}
