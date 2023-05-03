package gapi

import (
	"context"
	"database/sql"
	"log"
	"time"

	db "github.com/kriogenia/my_learnings/go_bank/db/sqlc"
	"github.com/kriogenia/my_learnings/go_bank/pb"
	"github.com/kriogenia/my_learnings/go_bank/util"
	"github.com/kriogenia/my_learnings/go_bank/val"
	"github.com/lib/pq"
	"google.golang.org/genproto/googleapis/rpc/errdetails"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

func (server *Server) UpdateUser(ctx context.Context, req *pb.UpdateUserRequest) (*pb.UpdateUserResponse, error) {
	if violations := validateUpdateUserRequest(req); violations != nil {
		return nil, invalidArgumentError(violations)
	}

	args := db.UpdateUserParams{
		ID:       req.GetId(),
		Username: toNullString(req.Username),
		FullName: toNullString(req.FullName),
		Email:    toNullString(req.Email),
	}

	if req.Password != nil {
		hashedPassword, err := util.HashPassword(req.GetPassword())
		if err != nil {
			log.Fatalf("failed to hash password: %s", err)
			return nil, status.Error(codes.Internal, "error creating user")
		}
		args.HashedPassword = toNullString(&hashedPassword)
		args.PasswordChangeAt = sql.NullTime{
			Time:  time.Now(),
			Valid: true,
		}
	}

	user, err := server.store.UpdateUser(ctx, args)
	if err != nil {
		if err == sql.ErrNoRows {
			return nil, status.Error(codes.NotFound, "user not found")
		}
		if pqErr, ok := err.(*pq.Error); ok {
			switch pqErr.Code.Name() {
			case "unique_violation":
				return nil, status.Error(codes.AlreadyExists, "username or email already exists")
			}
		}
		log.Fatalf("failed to update user: %s", err)
		return nil, status.Errorf(codes.Internal, "error updating user")
	}

	res := &pb.UpdateUserResponse{
		User: mapUser(user),
	}
	return res, nil
}

func validateUpdateUserRequest(req *pb.UpdateUserRequest) (violations []*errdetails.BadRequest_FieldViolation) {
	if req.Username != nil {
		if err := val.ValidateUsername(req.GetUsername()); err != nil {
			violations = append(violations, fieldViolation("username", err))
		}
	}

	if req.Password != nil {
		if err := val.ValidatePassword(req.GetPassword()); err != nil {
			violations = append(violations, fieldViolation("password", err))
		}
	}

	if req.FullName != nil {
		if err := val.ValidateFullName(req.GetFullName()); err != nil {
			violations = append(violations, fieldViolation("full_name", err))
		}
	}

	if req.Email != nil {
		if err := val.ValidateEmail(req.GetEmail()); err != nil {
			violations = append(violations, fieldViolation("email", err))
		}
	}

	return
}

func toNullString(input *string) sql.NullString {
	if input != nil {
		return sql.NullString{String: *input, Valid: true}
	}
	return sql.NullString{Valid: false}
}
