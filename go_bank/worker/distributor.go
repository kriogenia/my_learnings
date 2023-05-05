package worker

import (
	"context"

	"github.com/hibiken/asynq"
)

type TaskDistributor interface {
	DistributeDispatchVerificationEmailTask(
		ctx context.Context,
		payload *PayloadDispatchVerificationEmail,
		opts ...asynq.Option,
	) error
}
