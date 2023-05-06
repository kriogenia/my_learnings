package gapi

import (
	"fmt"

	db "github.com/kriogenia/my_learnings/go_bank/db/sqlc"
	"github.com/kriogenia/my_learnings/go_bank/pb"
	"github.com/kriogenia/my_learnings/go_bank/token"
	"github.com/kriogenia/my_learnings/go_bank/util"
	"github.com/kriogenia/my_learnings/go_bank/worker"
)

// Server serves gRPC requests for the banking service
type Server struct {
	pb.UnimplementedGoBankServer
	config          util.Config
	store           db.Store
	tokenMaker      token.Maker
	taskDistributor worker.TaskDistributor
}

// NewServer creates a new gRPC server
func NewServer(config util.Config, store db.Store, taskDistributor worker.TaskDistributor) (*Server, error) {
	tokenMaker, err := token.NewPasetoMaker(config.TokenSymmetricKey)
	if err != nil {
		return nil, fmt.Errorf("cannot create token maker: %w", err)
	}

	server := &Server{
		config:          config,
		store:           store,
		tokenMaker:      tokenMaker,
		taskDistributor: taskDistributor,
	}

	return server, nil
}
