package worker

import (
	"github.com/hibiken/asynq"
	db "github.com/kriogenia/my_learnings/go_bank/db/sqlc"
)

const (
	QUEUE_CRITICAL = "critical"
	QUEUE_DEFAULT  = "default"
)

type RedisTaskProcessor struct {
	server *asynq.Server
	store  db.Store
}

func NewRedisTaskProcessor(redisOpt asynq.RedisClientOpt, store db.Store) TaskProcessor {
	server := asynq.NewServer(redisOpt, asynq.Config{
		Queues: map[string]int{
			QUEUE_CRITICAL: 10,
			QUEUE_DEFAULT:  5,
		},
	})
	return &RedisTaskProcessor{
		server: server,
		store:  store,
	}
}

func (processor *RedisTaskProcessor) Start() error {
	mux := asynq.NewServeMux()
	mux.HandleFunc(TASK_DISPATCH_VERIFICATION_EMAIL, processor.ProcessDispatchVerificationEmailTask)
	return processor.server.Start(mux)
}
