package api

import (
	"database/sql"
	"fmt"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
	db "github.com/kriogenia/my_learnings/go_bank/db/sqlc"
	"github.com/kriogenia/my_learnings/go_bank/token"
)

type renewAccessTokenRequest struct {
	RefreshToken string `json:"refresh_token" binding:"required"`
}

type renewAccessTokenResponse struct {
	AccessToken          string    `json:"access_token"`
	AccessTokenExpiresAt time.Time `json:"access_token_expires_at"`
}

func (server *Server) renewAccessToken(ctx *gin.Context) {
	var req renewAccessTokenRequest
	if err := ctx.ShouldBindJSON(&req); handleError(ctx, err, http.StatusBadRequest) {
		return
	}

	refreshPayload, err := server.tokenMaker.VerifyToken(req.RefreshToken)
	if handleError(ctx, err, http.StatusUnauthorized) {
		return
	}

	session, err := server.store.GetSession(ctx, refreshPayload.ID)
	if err != nil {
		status := http.StatusInternalServerError
		if err == sql.ErrNoRows {
			status = http.StatusNotFound
		}
		if handleError(ctx, err, status) {
			return
		}
	}

	if !server.isSessionValid(ctx, session, refreshPayload) {
		return
	}

	if session.RefreshToken != req.RefreshToken && handleError(ctx, fmt.Errorf("mismatched session token"), http.StatusUnauthorized) {
		return
	}

	accessToken, accessPayload, err := server.tokenMaker.CreateToken(refreshPayload.UserID, server.config.AccessTokenDuration)
	if handleError(ctx, err, http.StatusInternalServerError) {
		return
	}

	res := renewAccessTokenResponse{
		AccessToken:          accessToken,
		AccessTokenExpiresAt: accessPayload.ExpiredAt,
	}
	ctx.JSON(http.StatusOK, res)
}

func (server *Server) isSessionValid(ctx *gin.Context, session db.Session, refreshPayload *token.Payload) bool {
	if session.IsBlocked && handleError(ctx, fmt.Errorf("blocked session"), http.StatusUnauthorized) {
		return false
	}

	if session.UserID != refreshPayload.UserID && handleError(ctx, fmt.Errorf("incorrect session user"), http.StatusUnauthorized) {
		return false
	}

	if time.Now().After(session.ExpiresAt) && handleError(ctx, fmt.Errorf("mismatched session token"), http.StatusUnauthorized) {
		return false
	}
	return true
}
