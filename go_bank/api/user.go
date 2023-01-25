package api

import (
	"database/sql"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
	db "github.com/kriogenia/my_learnings/go_bank/db/sqlc"
	"github.com/kriogenia/my_learnings/go_bank/util"
	"github.com/lib/pq"
)

type createUserRequest struct {
	Username string `json:"username" binding:"required,alphanum"`
	Password string `json:"password" binding:"required,min=6"`
	FullName string `json:"full_name" binding:"required"`
	Email    string `json:"email" binding:"required,email"`
}

type userResponse struct {
	ID               int64     `json:"id"`
	Username         string    `json:"username"`
	FullName         string    `json:"full_name"`
	Email            string    `json:"email"`
	PasswordChangeAt time.Time `json:"password_change_at"`
	CreatedAt        time.Time `json:"created_at"`
}

func newUserResponse(user db.User) userResponse {
	return userResponse{
		ID:               user.ID,
		Username:         user.Username,
		FullName:         user.FullName,
		Email:            user.Email,
		PasswordChangeAt: user.PasswordChangeAt,
		CreatedAt:        user.CreatedAt,
	}
}

func (server *Server) createUser(ctx *gin.Context) {
	var req createUserRequest
	if err := ctx.ShouldBindJSON(&req); handleError(ctx, err, http.StatusBadRequest) {
		return
	}

	hashedPassword, err := util.HashPassword(req.Password)
	if handleError(ctx, err, http.StatusInternalServerError) {
		return
	}

	args := db.CreateUserParams{
		Username:       req.Username,
		HashedPassword: hashedPassword,
		FullName:       req.FullName,
		Email:          req.Email,
	}

	user, err := server.store.CreateUser(ctx, args)
	if err != nil {
		status := http.StatusInternalServerError
		if pqErr, ok := err.(*pq.Error); ok {
			switch pqErr.Code.Name() {
			case "unique_violation":
				status = http.StatusForbidden
			}
		}
		if handleError(ctx, err, status) {
			return
		}
	}

	res := newUserResponse(user)
	ctx.JSON(http.StatusCreated, res)
}

type loginUserRequest struct {
	Username string `json:"username" binding:"required,alphanum"`
	Password string `json:"password" binding:"required,min=6"`
}

type loginUserResponse struct {
	AccessToken string       `json:"access_token"`
	User        userResponse `json:"user"`
}

func (server *Server) loginUser(ctx *gin.Context) {
	var req loginUserRequest
	if err := ctx.ShouldBindJSON(&req); handleError(ctx, err, http.StatusBadRequest) {
		return
	}

	user, err := server.store.GetUserByUsername(ctx, req.Username)
	if err != nil {
		status := http.StatusInternalServerError
		if err == sql.ErrNoRows {
			status = http.StatusNotFound
		}
		if handleError(ctx, err, status) {
			return
		}
	}

	err = util.CheckPassword(req.Password, user.HashedPassword)
	if handleError(ctx, err, http.StatusUnauthorized) {
		return
	}

	accessToken, err := server.tokenMaker.CreateToken(user.ID, server.config.AccessTokenDuration)
	if handleError(ctx, err, http.StatusInternalServerError) {
		return
	}

	res := loginUserResponse{
		AccessToken: accessToken,
		User:        newUserResponse(user),
	}
	ctx.JSON(http.StatusOK, res)
}
