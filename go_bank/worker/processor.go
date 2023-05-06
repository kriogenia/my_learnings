package worker

import (
	"context"

	"github.com/hibiken/asynq"
)

type TaskProcessor interface {
	Start() error
	ProcessDispatchVerificationEmailTask(ctx context.Context, task *asynq.Task) error
}
