package gapi

import (
	"context"
	"database/sql"
	"log"

	db "github.com/kriogenia/my_learnings/go_bank/db/sqlc"
	"github.com/kriogenia/my_learnings/go_bank/pb"
	"github.com/kriogenia/my_learnings/go_bank/util"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
	"google.golang.org/protobuf/types/known/timestamppb"
)

const ERROR_CREATING_USER_SESSION = "error creating user session"

func (server *Server) LoginUser(ctx context.Context, req *pb.LoginUserRequest) (*pb.LoginUserResponse, error) {
	user, err := server.store.GetUserByUsername(ctx, req.GetUsername())
	if err != nil {
		if err == sql.ErrNoRows {
			return nil, status.Error(codes.NotFound, "user not found")
		}
		log.Fatalf("failed to retrieve user: %s", err)
		status.Error(codes.Internal, "failed to retrieve user")
	}

	err = util.CheckPassword(req.Password, user.HashedPassword)
	if err != nil {
		return nil, status.Error(codes.NotFound, "password is not correct")
	}

	accessToken, accessPayload, err := server.tokenMaker.CreateToken(user.ID, server.config.AccessTokenDuration)
	if err != nil {
		log.Fatalf("failed to create access token: %s", err)
		return nil, status.Error(codes.Internal, ERROR_CREATING_USER_SESSION)
	}

	refreshToken, refresPayload, err := server.tokenMaker.CreateToken(user.ID, server.config.RefreshTokenDuration)
	if err != nil {
		log.Fatalf("failed to create refresh token: %s", err)
		return nil, status.Error(codes.Internal, ERROR_CREATING_USER_SESSION)
	}

	session, err := server.store.CreateSession(ctx, db.CreateSessionParams{
		ID:           refresPayload.ID,
		UserID:       user.ID,
		RefreshToken: refreshToken,
		UserAgent:    "",
		ClientIp:     "",
		IsBlocked:    false,
		ExpiresAt:    refresPayload.ExpiredAt,
	})
	if err != nil {
		log.Fatalf("failed to create session: %s", err)
		return nil, status.Error(codes.Internal, ERROR_CREATING_USER_SESSION)
	}

	res := &pb.LoginUserResponse{
		SessionID:             session.ID.String(),
		AccessToken:           accessToken,
		AccessTokenExpiresAt:  timestamppb.New(accessPayload.ExpiredAt),
		RefreshToken:          refreshToken,
		RefreshTokenExpiresAt: timestamppb.New(refresPayload.ExpiredAt),
		User:                  mapUser(user),
	}
	return res, nil
}
