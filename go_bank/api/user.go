package api

import (
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

type createUserResponse struct {
	ID               int64     `json:"id"`
	Username         string    `json:"username"`
	FullName         string    `json:"full_name"`
	Email            string    `json:"email"`
	PasswordChangeAt time.Time `json:"password_change_at"`
	CreatedAt        time.Time `json:"created_at"`
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

	res := createUserResponse{
		ID:               user.ID,
		Username:         user.Username,
		FullName:         user.FullName,
		Email:            user.Email,
		PasswordChangeAt: user.PasswordChangeAt,
		CreatedAt:        user.CreatedAt,
	}
	ctx.JSON(http.StatusCreated, res)
}
